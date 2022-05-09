package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.management;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseTableManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.ExtractedParameter;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.ParameterSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.StockSelection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VisualizationDataManager {
    @Autowired protected DataSource dataSource;
    @Autowired private CourseColumnRepository courseColumnRepository;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public XYChart.Series<Number, Number> getHistoricPricesForIsin(String isin, LocalDate startDate, LocalDate endDate) {
        var foundCourseColumn = false;
        for (var courseColumn : courseColumnRepository.findAll()) {
               if(courseColumn.getName().equals("kurs")) {
                   foundCourseColumn = true;
                   break;
               }
        }

        if(!foundCourseColumn) return null;

        ObservableList<XYChart.Data<Number, Number>> allRows = FXCollections.observableArrayList();

        var dateSubQueryStringBuilder = new StringBuilder();

        if(startDate != null) {
            dateSubQueryStringBuilder.append(" AND datum >= '").append(startDate.format(dateTimeFormatter)).append("'");
        }

        if(endDate != null) {
            dateSubQueryStringBuilder.append(" AND datum <= '").append(endDate.format(dateTimeFormatter)).append("'");
        }

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT datum, kurs FROM "+CourseTableManager.TABLE_NAME+" WHERE isin = '" + isin + "'" + dateSubQueryStringBuilder);

            // for each db row create new custom row
            while (results.next()) {
                var date = results.getDate("datum");
                var course = results.getDouble("kurs");

                allRows.add(new XYChart.Data<>(date.getTime(), course));
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }

        return new XYChart.Series<>(allRows);
    }

    public ObservableList<StockSelection> getStocksWithCourseData() {
        ObservableList<StockSelection> allRows = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT DISTINCT wp.name, wp.isin, wp.wkn FROM " + CourseTableManager.TABLE_NAME + " wk LEFT JOIN wertpapier wp on wp.isin = wk.isin");

            // for each db row create new custom row
            while (results.next()) {
                var wkn = results.getString("wp.wkn");
                var isin = results.getString("wp.isin");
                var name = results.getString("wp.name");

                allRows.add(new StockSelection(wkn, isin, name, false));
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }

        return allRows;
    }

    public XYChart.Series<Number, Number> normalizeData(XYChart.Series<Number, Number> data, StockSelection firstSelectedStock) {
        double minCourse = 0;
        double maxCourse = 0;

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT MIN(kurs), MAX(kurs) FROM " + CourseTableManager.TABLE_NAME + " WHERE isin = '" + firstSelectedStock.getIsin() + "'");

            while (results.next()) {
                minCourse = results.getDouble("MIN(kurs)");
                maxCourse = results.getDouble("MAX(kurs)");
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }

        data.getData().sort(Comparator.comparingDouble(o -> o.YValueProperty().get().doubleValue()));

        var minCourseOfCurrentStock = data.getData().get(0).YValueProperty().get().doubleValue();
        var maxCourseOfCurrentStock = data.getData().get(data.getData().size() - 1).YValueProperty().get().doubleValue();

        var normalizedDataSet = new XYChart.Series<Number, Number>();
        for (var courseData : data.getData()) {
            var currentCourse = courseData.YValueProperty().get().doubleValue();

            var normalizedCourse = ((currentCourse-minCourseOfCurrentStock)/(maxCourseOfCurrentStock-minCourseOfCurrentStock)*(maxCourse-minCourse)) + minCourse;

            normalizedDataSet.setName(data.getName());
            normalizedDataSet.getData().add(new XYChart.Data<>(courseData.getXValue(), normalizedCourse));
        }

        return normalizedDataSet;
    }

    public ObservableList<ParameterSelection> getParameters() {
        ObservableList<ParameterSelection> allRows = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT name, col_type, column_datatype FROM datenbank_spalte WHERE col_type = 'S' OR col_type = 'W'");

            // for each db row create new custom row
            while (results.next()) {
                var parameter = results.getString("name");
                if(parameter.equals("isin") || parameter.equals("datum")) continue;

                var colType = results.getString("col_type");

                var dataType = ColumnDatatype.valueOf(results.getString("column_datatype"));
                if(dataType == ColumnDatatype.TEXT || dataType == ColumnDatatype.DATE || dataType == ColumnDatatype.DATETIME) continue;

                allRows.add(new ParameterSelection(parameter, colType, dataType, false));
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }

        return allRows;
    }

    public ObservableList<ExtractedParameter> getParameterDataForIsin(String isin, String name, ParameterSelection parameter, LocalDate startDate, LocalDate endDate) {
        ObservableList<ExtractedParameter> allRows = FXCollections.observableArrayList();

        var dateSubQueryStringBuilder = new StringBuilder();

        if(startDate != null) {
            dateSubQueryStringBuilder.append(" AND datum >= '").append(startDate.format(dateTimeFormatter)).append("'");
        }

        if(endDate != null) {
            dateSubQueryStringBuilder.append(" AND datum <= '").append(endDate.format(dateTimeFormatter)).append("'");
        }

        String extractionTable;
        if(parameter.getColType().equals("W")) {
            extractionTable = "watch_list";
        } else if(parameter.getColType().equals("S")) {
            extractionTable = "wertpapier_stammdaten";
        } else {
            return null;
        }

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT " + parameter.getParameter() + ", datum FROM "+extractionTable+" WHERE isin = '" + isin + "'" + dateSubQueryStringBuilder + " ORDER BY datum ASC");

            // for each db row create new custom row
            while (results.next()) {
                var date = results.getDate("datum");

                if(parameter.getDataType() == ColumnDatatype.DOUBLE) {
                    var data = results.getDouble(parameter.getParameter());
                    allRows.add(new ExtractedParameter(isin, name, date, data, parameter.getParameter()));
                } else if(parameter.getDataType() == ColumnDatatype.INTEGER) {
                    var data = results.getInt(parameter.getParameter());
                    allRows.add(new ExtractedParameter(isin, name, date, data, parameter.getParameter()));
                }
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }

        return allRows;
    }

    public XYChart.Series<Number, Number> getLineChartParameterData(ObservableList<ExtractedParameter> allRows) {
        if(allRows.size() == 0) return null;

        var chartData = new XYChart.Series<Number, Number>();
        chartData.setName(allRows.get(0).getParameterName());

        for(var row : allRows) {
            chartData.getData().add(new XYChart.Data<>(row.getDate().getTime(), row.getParameter()));
        }

        return chartData;
    }

    public XYChart.Series<String, Number> getBarChartParameterData(List<ObservableList<ExtractedParameter>> allRows) {
        if(allRows.size() == 0 || allRows.get(0).size() == 0) return null;

        var chartData = new XYChart.Series<String, Number>();
        chartData.setName(allRows.get(0).get(0).getName());

        for(var row : allRows) {
            var data = row.get(row.size() - 1);
            chartData.getData().add(new XYChart.Data<>(data.getParameterName(), data.getParameter()));
        }

        return chartData;
    }


    public XYChart.Series<String, Number> getBarChartDepotParameterData(
            Map<String, List<ObservableList<ExtractedParameter>>> allStocks,
            List<StockSelection> selectedTransactions,
            List<StockSelection> selectedWatchList) {

        if (allStocks.size() == 0) return null;

        Map<String, Double> parameterMap = new HashMap<>();
        double depotSum = 0;

        var chartData = new XYChart.Series<String, Number>();
        chartData.setName("Alle Wertpapiere gewichtet");

        for (var stock : allStocks.keySet()) {
            var includeStockTransactions = selectedTransactions.stream().anyMatch(s -> s.getIsin().equals(stock));
            var includeStockWatchList = selectedWatchList.stream().anyMatch(s -> s.getIsin().equals(stock));

            if (includeStockTransactions) {
                depotSum += searchTransactionForStockSum(stock);
            }

            if (includeStockWatchList) {
                depotSum += searchWatchListForStockSum(stock);
            }
        }

        for (var stock : allStocks.keySet()) {
            var includeStockTransactions = selectedTransactions.stream().anyMatch(s -> s.getIsin().equals(stock));
            var includeStockWatchList = selectedWatchList.stream().anyMatch(s -> s.getIsin().equals(stock));

            for(var parameters : allStocks.get(stock)) {
                var latestParameter = parameters.get(parameters.size() - 1);

                double stockSum = 0;

                if (includeStockTransactions) {
                    stockSum += searchTransactionForStockSum(latestParameter.getIsin());
                }

                if (includeStockWatchList) {
                    stockSum += searchWatchListForStockSum(latestParameter.getIsin());
                }


                if(!parameterMap.containsKey(latestParameter.getParameterName())) {
                    parameterMap.put(latestParameter.getParameterName(), latestParameter.getParameter().doubleValue() * stockSum / depotSum);
                } else {
                    var oldValue = parameterMap.get(latestParameter.getParameterName());

                    parameterMap.put(latestParameter.getParameterName(), oldValue + latestParameter.getParameter().doubleValue() * stockSum / depotSum);
                }
            }
        }

        for(var parameter : parameterMap.keySet()) {
            chartData.getData().add(new XYChart.Data<>(parameter, parameterMap.get(parameter)));
        }

        return chartData;
    }

    private double searchTransactionForStockSum(String isin) {
        var stockAmountTransactions = 0;
        var currentStockValue = getLatestStockValue(isin);

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/user.properties"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        try {
            var propertiesTransactionsAmountColumnName = properties.getProperty("TransaktionAnzahlSpaltenName", null);

            if(propertiesTransactionsAmountColumnName == null) {
                return 0;
            }

            try (Connection connection = dataSource.getConnection()) {
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT " + propertiesTransactionsAmountColumnName + ", transaktionstyp FROM depot_transaktion WHERE wertpapier_isin = '" +isin+ "'");

                while (results.next()) {
                    var transactionType = results.getString("transaktionstyp");
                    var amount = results.getInt(propertiesTransactionsAmountColumnName);

                    if(transactionType.equals("kauf")) {
                        stockAmountTransactions += amount;
                    } else {
                        stockAmountTransactions -= amount;
                    }
                }

                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                AbandonedConnectionCleanupThread.checkedShutdown();
            }

        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }

        return stockAmountTransactions * currentStockValue;
    }

    private double searchWatchListForStockSum(String isin) {
        var stockAmountWatchList = 0;
        var currentStockValue = getLatestStockValue(isin);

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("src/main/resources/user.properties"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        try {
            var propertiesWatchListAmountColumnName = properties.getProperty("WatchListeAnzahlSpaltenName", null);

            if(propertiesWatchListAmountColumnName == null) {
                return 0;
            }

            try (Connection connection = dataSource.getConnection()) {
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT " + propertiesWatchListAmountColumnName + " FROM watch_list WHERE isin = '" +isin+ "'");

                while (results.next()) {
                    stockAmountWatchList += results.getInt(propertiesWatchListAmountColumnName);
                }

                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                AbandonedConnectionCleanupThread.checkedShutdown();
            }

        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }

        return stockAmountWatchList * currentStockValue;
    }

    private double getLatestStockValue(String isin) {
        double stockValue = 0;

        var foundCourseColumn = false;
        for (var courseColumn : courseColumnRepository.findAll()) {
            if(courseColumn.getName().equals("kurs")) {
                foundCourseColumn = true;
                break;
            }
        }

        if(!foundCourseColumn) return stockValue;

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT kurs, datum FROM wertpapier_kursdaten WHERE isin = '" +isin+ "' ORDER BY datum DESC LIMIT 1");

            // for each db row create new custom row
            while (results.next()) {
                stockValue = results.getDouble("kurs");
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }

        return stockValue;
    }

    public ObservableList<StockSelection> getStocksWithParameterData() {
        ObservableList<StockSelection> allRows = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT DISTINCT name, isin, wkn FROM wertpapier");

            // for each db row create new custom row
            while (results.next()) {
                var wkn = results.getString("wkn");
                var isin = results.getString("isin");
                var name = results.getString("name");

                allRows.add(new StockSelection(wkn, isin, name, false));
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }

        return allRows;
    }
}
