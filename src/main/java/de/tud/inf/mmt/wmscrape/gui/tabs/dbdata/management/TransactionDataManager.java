package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.transaction.TransactionTableManager;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.CustomCell;
import de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.data.CustomRow;
import javafx.scene.control.TableView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionDataManager extends DataManager {

    @Autowired
    TransactionTableManager transactionTableManager;
    @Autowired
    TransactionColumnRepository transactionColumnRepository;

    @Override
    protected PreparedStatement prepareUpdateStatements(String colName, Connection connection) throws SQLException{
        String sql = "INSERT INTO `"+ dbTableManger.getTableName()+"` (`"+colName+
                "`, transaktions_datum, depot_name, wertpapier_isin) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE `"
                +colName+"`=VALUES(`"+colName+"`);";
        return connection.prepareStatement(sql);
    }

    @Override
    protected void fillDeleteAllStatement(CustomRow row, PreparedStatement statement) throws SQLException {
        var cells = row.getCells();
        if(cells == null || cells.get("depot_name") == null) return;

        String depot = cells.get("depot_name").getDbData();
        fillByDataType(statement, depot, ColumnDatatype.TEXT, 1);
    }


    @Override
    protected void fillDeleteSelectionStatement(CustomRow row, PreparedStatement statement) throws SQLException {
        var cells = row.getCells();
        if(cells == null || cells.get("transaktions_datum") == null || cells.get("depot_name") == null ||
                cells.get("wertpapier_isin") == null) return;

        String date = cells.get("transaktions_datum").getDbData();
        String depot = cells.get("depot_name").getDbData();
        String isin = cells.get("wertpapier_isin").getDbData();
        fillByDataType(statement, date, ColumnDatatype.DATE, 1);
        fillByDataType(statement, depot, ColumnDatatype.TEXT, 2);
        fillByDataType(statement, isin, ColumnDatatype.TEXT, 3);
    }

    @Override
    protected PreparedStatement prepareDeleteSelectionStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("DELETE FROM `"+ dbTableManger.getTableName()+"` " +
                "WHERE transaktions_datum=? AND depot_name=? AND wertpapier_isin=?");
    }

    @Override
    protected PreparedStatement prepareDeleteAllStatement(Connection connection) throws SQLException {
        return connection.prepareStatement("DELETE FROM `"+ dbTableManger.getTableName()+"` WHERE depot_name=?");
    }

    @Override
    protected Map<String, String> getKeyInformation(CustomRow row) {
        Map<String, String> keys = new HashMap<>();
        String date = row.getCells().getOrDefault("transaktions_datum",
                new CustomCell(null, null)).getDbData();
        String depot = row.getCells().getOrDefault("depot_name",
                new CustomCell(null, null)).getDbData();
        String isin = row.getCells().getOrDefault("wertpapier_isin",
                new CustomCell(null, null)).getDbData();

        if(date == null || depot == null || isin == null) return null;
        keys.put("transaktions_datum", date);
        keys.put("depot_name", depot);
        keys.put("wertpapier_isin", isin);
        return keys;
    }

    @Override
    protected void setStatementKeys(PreparedStatement stmt,
                                    Map<String, String> keys) throws SQLException{

        // 1 = data
        fillByDataType(stmt, keys.get("transaktions_datum"), ColumnDatatype.DATE, 2);
        fillByDataType(stmt, keys.get("depot_name"), ColumnDatatype.TEXT, 3);
        fillByDataType(stmt, keys.get("wertpapier_isin"), ColumnDatatype.TEXT, 4);
    }

    @Override
    protected void setColumnRepositoryAndManager() {
        dbTableColumnRepository = transactionColumnRepository;
        dbTableManger = transactionTableManager;
    }

    @Override
    public boolean addRowForSelection(Object selection) {
        throw new UnsupportedOperationException("Adding rows to the transaction table is not implemented");
    }

    @Override
    protected <T extends DbTableColumn> List<? extends DbTableColumn> getTableColumns(DbTableColumnRepository<T, Integer> repository) {
        return transactionColumnRepository.findAll();
    }

    @Override
    protected String getSelectionStatement() {
        return "SELECT * FROM "+ TransactionTableManager.TABLE_NAME;
    }

    @Override
    protected void setDataTableInitialSort(TableView<CustomRow> dataTable) {
        // nothing to sort
    }
}
