package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.CustomCell;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.CustomRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public abstract class StockAndCourseManager extends DataManager {

    @Override
    protected PreparedStatement prepareUpdateStatements(String colName, Connection connection) throws SQLException{
        String sql = "INSERT INTO `"+ dbTableManger.getTableName()+"` (`"+colName+
                "`, datum, isin) VALUES(?,?,?) ON DUPLICATE KEY UPDATE `"+colName+"`=VALUES("+colName+");";
        return connection.prepareStatement(sql);
    }

    @Override
    protected void fillDeleteAllStatement(CustomRow row, PreparedStatement statement) throws SQLException {
        var cells = row.getCells();
        if(cells == null || cells.get("isin") == null) return;

        String isin = cells.get("isin").getTextData();
        fillByDataType(statement, isin, ColumnDatatype.TEXT, 1);
    }


    @Override
    protected void fillDeleteSelectionStatement(CustomRow row, PreparedStatement statement) throws SQLException {
        var cells = row.getCells();
        if(cells == null || cells.get("isin") == null || cells.get("datum") == null) return;

        String isin = cells.get("isin").getTextData();
        String datum = cells.get("datum").getTextData();

        fillByDataType(statement, isin, ColumnDatatype.TEXT, 1);
        fillByDataType(statement, datum, ColumnDatatype.DATE, 2);
    }

    @Override
    protected PreparedStatement prepareDeleteSelectionStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("DELETE FROM `"+ dbTableManger.getTableName()+"` WHERE isin=? and datum=?");
    }

    @Override
    protected PreparedStatement prepareDeleteAllStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("DELETE FROM `"+ dbTableManger.getTableName()+"` WHERE isin=?");
    }


    @Override
    protected Map<String, String> getKeyInformation(CustomRow row) {
        Map<String, String> keys = new HashMap<>();
        String isin = row.getCells().getOrDefault("isin", null).getTextData();
        String date = row.getCells().getOrDefault("datum", null).getTextData();
        if(isin == null || date == null) return null;
        keys.put("isin", isin);
        keys.put("datum", date);
        return keys;
    }

    @Override
    protected void setStatementKeys(CustomCell cell, PreparedStatement stmt,
                                    Map<String, String> keys) throws SQLException{

        // 1 = data
        fillByDataType(stmt, keys.get("datum"), ColumnDatatype.DATE, 2);
        fillByDataType(stmt, keys.get("isin"), ColumnDatatype.TEXT, 3);
    }
}
