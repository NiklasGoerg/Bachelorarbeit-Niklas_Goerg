package de.tud.inf.mmt.wmscrape.gui.tabs.visualization.management;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseTableManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.visualization.data.StockSelection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

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
            ResultSet results = statement.executeQuery("SELECT DISTINCT wp.name, wp.isin FROM " + CourseTableManager.TABLE_NAME + " wk LEFT JOIN wertpapier wp on wp.isin = wk.isin");

            // for each db row create new custom row
            while (results.next()) {
                var isin = results.getString("wp.isin");
                var name = results.getString("wp.name");

                allRows.add(new StockSelection(isin, name, false));
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
}
