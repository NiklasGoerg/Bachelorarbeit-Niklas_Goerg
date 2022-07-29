package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.CustomCell;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.CustomRow;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.Stock;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public abstract class StockAndCourseManager extends DataManager {

    @Override
    protected PreparedStatement prepareUpdateStatements(String colName, Connection connection) throws SQLException{
        String sql = "INSERT INTO `"+ dbTableManger.getTableName()+"` (`"+colName+
                "`, datum, isin) VALUES(?,?,?) ON DUPLICATE KEY UPDATE `"+colName+"`=VALUES(`"+colName+"`);";
        return connection.prepareStatement(sql);
    }

    @Override
    protected void fillDeleteAllStatement(CustomRow row, PreparedStatement statement) throws SQLException {
        var cells = row.getCells();
        if(cells == null || cells.get("isin") == null) return;

        String isin = cells.get("isin").getDbData();
        fillByDataType(statement, isin, ColumnDatatype.TEXT, 1);
    }


    @Override
    protected void fillDeleteSelectionStatement(CustomRow row, PreparedStatement statement) throws SQLException {
        var cells = row.getCells();
        if(cells == null || cells.get("isin") == null || cells.get("datum") == null) return;

        String isin = cells.get("isin").getDbData();
        String datum = cells.get("datum").getDbData();

        fillByDataType(statement, isin, ColumnDatatype.TEXT, 1);
        fillByDataType(statement, datum, ColumnDatatype.DATE, 2);
    }

    @Override
    protected PreparedStatement prepareDeleteSelectionStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("DELETE FROM `"+ dbTableManger.getTableName()+"` WHERE isin=? AND datum=?");
    }

    @Override
    protected PreparedStatement prepareDeleteAllStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("DELETE FROM `"+ dbTableManger.getTableName()+"` WHERE isin=?");
    }


    @Override
    protected Map<String, String> getKeyInformation(CustomRow row) {
        Map<String, String> keys = new HashMap<>();
        String isin = row.getCells().getOrDefault("isin", new CustomCell(null, null)).getDbData();
        String date = row.getCells().getOrDefault("datum", new CustomCell(null, null)).getDbData();
        if(isin == null || date == null) return null;
        keys.put("isin", isin);
        keys.put("datum", date);
        return keys;
    }

    @Override
    protected void setStatementKeys(PreparedStatement stmt,
                                    Map<String, String> keys) throws SQLException{

        // 1 = data
        fillByDataType(stmt, keys.get("datum"), ColumnDatatype.DATE, 2);
        fillByDataType(stmt, keys.get("isin"), ColumnDatatype.TEXT, 3);
    }

    @Override
    public boolean addRowForSelection(Object selection) {
        if(!(selection instanceof Stock)) return false;

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("INSERT INTO `"+dbTableManger.getTableName()+"` " +
                    "(datum, isin) VALUES(?,?) ON DUPLICATE KEY UPDATE datum = datum, isin = isin");
            stmt.setDate(1, new Date(System.currentTimeMillis())); // today
            stmt.setString(2, ((Stock) selection).getIsin());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            AbandonedConnectionCleanupThread.checkedShutdown();
        }
        return true;
    }

    @Override
    protected void setDataTableInitialSort(TableView<CustomRow> dataTable) {
        dataTable.getSortOrder().clear();
        dataTable.getColumns().forEach(c ->  {
            if(c.getText().equals("datum") ) {
                c.setSortType(TableColumn.SortType.DESCENDING);
                dataTable.getSortOrder().add(0, c);
            } else if (c.getText().equals("r_par")) dataTable.getSortOrder().add(c);
        });
    }
}
