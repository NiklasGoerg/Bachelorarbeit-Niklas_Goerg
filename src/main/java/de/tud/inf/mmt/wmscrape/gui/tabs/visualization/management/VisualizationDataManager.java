package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.management;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseTableManager;
import de.tud.inf.mmt.wmscrape.dynamicdb.watchlist.WatchListTableManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.controller.VisualizeStockColumnRelationController;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.ExtractedParameter;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.ParameterSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.StockSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.WatchListSelection;
import de.tud.inf.mmt.wmscrape.helper.PropertiesHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VisualizationDataManager {
    @Autowired protected DataSource dataSource;
    @Autowired private CourseColumnRepository courseColumnRepository;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * loads the price history for specific stock
     *
     * @param isin of the stock
     * @param startDate lower bound of time span
     * @param endDate upper bound of time span
     * @return course data which can be passed to and visualized by JavaFX
     */
    public XYChart.Series<Number, Number> getHistoricPricesForIsin(String isin, LocalDate startDate, LocalDate endDate) {
        var courseColumn = PropertiesHelper.getProperty(VisualizeStockColumnRelationController.stockCourseTableCourseColumn);

        if(courseColumn == null) return null;

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
            ResultSet results = statement.executeQuery("SELECT datum, " + courseColumn + " FROM "+CourseTableManager.TABLE_NAME+" WHERE isin = '" + isin + "'" + dateSubQueryStringBuilder);

            // for each db row create new custom row
            while (results.next()) {
                var date = results.getDate("datum");
                var course = results.getDouble(courseColumn);

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

    /**
     *
     * @return list of all stocks for which course data is stored in the database
     */
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

    /**
     *
     * @param data course data for all selected stocks
     * @param firstSelectedStock stock which was selected by the user first, this is the reference for all other stocks
     * @return normalized data
     */
    public XYChart.Series<Number, Number> normalizeData(XYChart.Series<Number, Number> data, StockSelection firstSelectedStock) {
        double minCourse = 0;
        double maxCourse = 0;

        var courseColumn = PropertiesHelper.getProperty(VisualizeStockColumnRelationController.stockCourseTableCourseColumn);

        if(courseColumn == null) return null;

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT MIN(" + courseColumn + "), MAX(" + courseColumn + ") FROM " + CourseTableManager.TABLE_NAME + " WHERE isin = '" + firstSelectedStock.getIsin() + "'");

            while (results.next()) {
                minCourse = results.getDouble("MIN(" + courseColumn + ")");
                maxCourse = results.getDouble("MAX(" + courseColumn + ")");
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

    /**
     *
     * @return all columns of database table "wertpapier_stammdaten" which can be visualized
     */
    public ObservableList<ParameterSelection> getParameters() {
        ObservableList<ParameterSelection> allRows = FXCollections.observableArrayList();

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT name, col_type, column_datatype FROM datenbank_spalte WHERE col_type = 'S'");

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

    /**
     *
     * @param isin of the stock
     * @param name of the stock
     * @param parameter parameters which should be extracted by the program
     * @param startDate lower bound of time span
     * @param endDate upper bound of time span
     * @return list of all parameters read from the database for current stock
     */
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

    /**
     *
     * @param allRows data previously read from the database
     * @return allRows converted into a format which can be displayed by a line chart
     */
    public XYChart.Series<Number, Number> getLineChartParameterData(ObservableList<ExtractedParameter> allRows) {
        if(allRows.size() == 0) return null;

        var chartData = new XYChart.Series<Number, Number>();
        chartData.setName(allRows.get(0).getParameterName());

        for(var row : allRows) {
            chartData.getData().add(new XYChart.Data<>(row.getDate().getTime(), row.getParameter()));
        }

        return chartData;
    }

    /**
     *
     * @param allRows data previously read from the database
     * @return allRows converted into a format which can be displayed by a bar chart
     */
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

    /**
     *
     * @param allStocks all user selected stocks
     * @param selectedTransactions stocks which transaction data should be taken into account for weighted calculation
     * @param selectedWatchList stocks which watch list data should be taken into account for weighted calculation
     * @param watchListSelection specific watch list entries which should be taken into account for weighted calculation
     * @return weighted parameters in a format which can be displayed by JavaFX
     */
    public XYChart.Series<String, Number> getBarChartDepotParameterData(
            Map<String, List<ObservableList<ExtractedParameter>>> allStocks,
            List<StockSelection> selectedTransactions,
            List<StockSelection> selectedWatchList,
            Map<String, List<WatchListSelection>> watchListSelection) {

        if (allStocks.size() == 0) return null;

        Map<String, Double> parameterMap = new HashMap<>();
        Map<String, Double> stockCurrentValues = new HashMap<>();
        Map<String, Double> stockWatchListValues = new HashMap<>();

        double depotSum = 0;

        var chartData = new XYChart.Series<String, Number>();
        chartData.setName("Alle Wertpapiere gewichtet");

        for (var stock : allStocks.keySet()) {
            var includeStockTransactions = selectedTransactions.stream().anyMatch(s -> s.getIsin().equals(stock));
            var includeStockWatchList = selectedWatchList.stream().anyMatch(s -> s.getIsin().equals(stock));

            if (includeStockTransactions) {
                depotSum += searchTransactionForStockSum(stock, stockCurrentValues);
            }

            if (includeStockWatchList) {
                depotSum += searchWatchListForStockSum(stock, stockWatchListValues, watchListSelection.get(stock));
            }

            if(depotSum == 0) {
                depotSum = 1;
            }
        }

        for (var stock : allStocks.keySet()) {
            var includeStockTransactions = selectedTransactions.stream().anyMatch(s -> s.getIsin().equals(stock));
            var includeStockWatchList = selectedWatchList.stream().anyMatch(s -> s.getIsin().equals(stock));

            for(var parameters : allStocks.get(stock)) {
                var latestParameter = parameters.get(parameters.size() - 1);

                double stockSum = 0;

                if (includeStockTransactions) {
                    stockSum += searchTransactionForStockSum(stock, stockCurrentValues);
                }

                if (includeStockWatchList) {
                    stockSum += searchWatchListForStockSum(stock, stockWatchListValues, watchListSelection.get(stock));
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

    /**
     *
     * @param stockSelection stocks which watch list data should be read from the database
     * @return list of watch list data entries
     */
    public List<WatchListSelection> getWatchListData(StockSelection stockSelection) {
        var isin = stockSelection.getIsin();

        List<WatchListSelection> entries = new ArrayList<>();

        var amountColumn = PropertiesHelper.getProperty(VisualizeStockColumnRelationController.watchListTableAmountColumn);
        var buyPriceColumn = PropertiesHelper.getProperty(VisualizeStockColumnRelationController.watchListTableBuyCourseColumn);
        var sellPriceColumn = PropertiesHelper.getProperty(VisualizeStockColumnRelationController.watchListTableSellCourseColumn);

        if(amountColumn == null || buyPriceColumn == null || sellPriceColumn == null) return null;

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT isin, datum, " + amountColumn + ", " + buyPriceColumn + ", " + sellPriceColumn + " FROM " + WatchListTableManager.TABLE_NAME + "  WHERE isin = '" +isin+ "' ORDER BY datum DESC");

            while (results.next()) {
                var watchListEntry = new WatchListSelection();

                watchListEntry.setWkn(stockSelection.getWkn());
                watchListEntry.setIsin(results.getString("isin"));
                watchListEntry.setDate(results.getDate("datum"));
                watchListEntry.setName(stockSelection.getName());
                watchListEntry.setAmount(results.getInt(amountColumn));

                if(watchListEntry.getAmount() < 0) {
                    watchListEntry.setPrice(results.getDouble(sellPriceColumn));
                } else {
                    watchListEntry.setPrice(results.getDouble(buyPriceColumn));
                }

                entries.add(watchListEntry);
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }

        return entries;
    }

    /**
     *
     * @param isin of the stock
     * @param stockValues cached transaction sum
     * @return sum of all transaction entries for stock with specified isin
     */
    private double searchTransactionForStockSum(String isin, Map<String, Double> stockValues) {
        if(stockValues.containsKey(isin)) return stockValues.get(isin);

        var stockAmountTransactions = 0;
        var currentStockValue = getLatestStockValue(isin);

        try {
            var propertiesTransactionsAmountColumnName = PropertiesHelper.getProperty(VisualizeStockColumnRelationController.transactionTableAmountColumn);

            if(propertiesTransactionsAmountColumnName == null) {
                return 0;
            }

            try (Connection connection = dataSource.getConnection()) {
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT " + propertiesTransactionsAmountColumnName + " FROM depot_transaktion WHERE wertpapier_isin = '" +isin+ "'");

                while (results.next()) {
                    stockAmountTransactions += results.getInt(propertiesTransactionsAmountColumnName);
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

        stockValues.put(isin, stockAmountTransactions * currentStockValue);
        return stockAmountTransactions * currentStockValue;
    }

    /**
     *
     * @param isin of the stock
     * @param stockValues cached watch list sum
     * @param watchListSelection watch list entries which should be included in the calculation
     * @return sum of specified watch list entries for specified isin
     */
    private double searchWatchListForStockSum(String isin, Map<String, Double> stockValues, List<WatchListSelection> watchListSelection) {
        if(stockValues.containsKey(isin)) return stockValues.get(isin);

        var stockAmountWatchList = 0;
        var stockBuyValue = 0d;
        var stockSellValue = 0d;

        var watchListBuyCourseColumnName = PropertiesHelper.getProperty(VisualizeStockColumnRelationController.watchListTableBuyCourseColumn);
        var watchListSellCourseColumnName = PropertiesHelper.getProperty(VisualizeStockColumnRelationController.watchListTableSellCourseColumn);

        if(watchListBuyCourseColumnName == null || watchListSellCourseColumnName == null) return 0;

        try {
            var propertiesWatchListAmountColumnName = PropertiesHelper.getProperty(VisualizeStockColumnRelationController.watchListTableAmountColumn);

            if(propertiesWatchListAmountColumnName == null) {
                return 0;
            }

            try (Connection connection = dataSource.getConnection()) {
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT " + propertiesWatchListAmountColumnName + ", " + watchListBuyCourseColumnName + ", " + watchListSellCourseColumnName +  ", datum FROM watch_list WHERE isin = '" +isin+ "'");

                while (results.next()) {
                    var date = results.getDate("datum");

                    if(watchListSelection != null && watchListSelection.stream().noneMatch(s -> s.getDate().equals(date))) continue;

                    stockAmountWatchList += results.getInt(propertiesWatchListAmountColumnName);
                    stockBuyValue = results.getDouble(watchListBuyCourseColumnName);
                    stockSellValue = results.getDouble(watchListSellCourseColumnName);
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

        final double value = stockAmountWatchList > 0 ? stockAmountWatchList * stockBuyValue : stockAmountWatchList * stockSellValue;

        stockValues.put(isin, value);
        return value;
    }

    /**
     *
     * @param isin of the stock
     * @return latest course of stock with specific isin
     */
    private double getLatestStockValue(String isin) {
        double stockValue = 0;

        var courseColumn = PropertiesHelper.getProperty(VisualizeStockColumnRelationController.stockCourseTableCourseColumn);

        if(courseColumn == null) return stockValue;

        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT " + courseColumn + ", datum FROM wertpapier_kursdaten WHERE isin = '" +isin+ "' ORDER BY datum DESC LIMIT 1");

            // for each db row create new custom row
            while (results.next()) {
                stockValue = results.getDouble(courseColumn);
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }

        return stockValue;
    }

    /**
     *
     * @return all stocks including wkn, isin, name
     */
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
