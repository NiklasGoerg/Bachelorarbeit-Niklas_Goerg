package de.tud.inf.mmt.wmscrape.dynamicdb.historic;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger;
import de.tud.inf.mmt.wmscrape.dynamicdb.VisualDatatype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@Service
public class HistoricTableManager extends DbTableManger {

    public static final String TABLE_NAME = "wertpapier_historische_daten";
    public static final List<String> NOT_EDITABLE_COLUMNS = List.of("isin", "datum", "kurs");
    public static final List<String> RESERVED_COLUMNS = List.of("isin", "datum", "kurs");
    public static final List<String> COLUMN_ORDER = List.of("isin", "datum", "kurs");

    @Autowired
    HistoricColumnRepository historicColumnRepository;

    /**
     * <li> creates the table in the database because it is not managed by hibernate</li>
     * <li> column-entities are managed based on the columns in the database</li>
     * <li> optional: predefined columns can be added to the db table with {@link de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger#addColumn(String, VisualDatatype)}</li>
     */
    @PostConstruct
    private void initExchangeData() {
        // the exchange data table is not managed by spring
        // and has to be initialized by myself

        if (tableDoesNotExist(TABLE_NAME)) {
            executeStatement("CREATE TABLE IF NOT EXISTS `"+TABLE_NAME+"` (datum DATE, isin varchar(50), kurs double, PRIMARY KEY (datum, isin));");
        }

        initTableColumns(historicColumnRepository, TABLE_NAME);
    }


    @Override
    public boolean removeColumn(String columnName) {
        return removeAbstractColumn(columnName, TABLE_NAME, historicColumnRepository);
    }

    @Override
    public void addColumn(String colName, VisualDatatype visualDatatype) {
        addColumnIfNotExists(TABLE_NAME, historicColumnRepository, new HistoricColumn(colName, visualDatatype));
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public List<String> getNotEditableColumns() {
        return NOT_EDITABLE_COLUMNS;
    }

    @Override
    public List<String> getReservedColumns() {
        return RESERVED_COLUMNS;
    }

    @Override
    public List<String> getDefaultColumnOrder() { return COLUMN_ORDER; }

    @Override
    protected void saveNewInRepository(String colName, ColumnDatatype datatype) {
        historicColumnRepository.saveAndFlush(new HistoricColumn(colName, datatype));
    }

    @Override
    public PreparedStatement getPreparedDataStatement(String colName, Connection connection) {
        throw new UnsupportedOperationException();
    }
}
