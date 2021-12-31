package de.tud.inf.mmt.wmscrape.dynamicdb.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

@Service
public class CourseTableManager extends DbTableManger {

    public static final String TABLE_NAME = "wertpapier_kursdaten";
    public static final List<String> KEY_COLUMNS = List.of("datum", "isin");
    public static final List<String> RESERVED_COLUMNS = List.of("datum", "isin", "wkn", "name");
    public static final List<String> COLUMN_ORDER = List.of("datum", "isin");

    @Autowired
    CourseColumnRepository courseColumnRepository;

    @PostConstruct
    private void initCourseData() {
        // the course data table is not managed by spring
        // and has to be initialized by myself

        if (tableDoesNotExist(TABLE_NAME)) {
            executeStatement("CREATE TABLE IF NOT EXISTS `"+TABLE_NAME+"` (isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");

            // set the foreign keys to the newly created table
            // do not constrain non hibernate tables. the creation oder is not known for them (if not set -> @Order)
            executeStatement("ALTER TABLE "+TABLE_NAME+
                    // constraint name doesn't matter. wertpapier_stammdaten.isin
                    " ADD CONSTRAINT fk_wertpapier_isin_kursdaten FOREIGN KEY (isin)"+
                    // wertpapier = table name of stock entity
                    " REFERENCES wertpapier (isin)"+" ON DELETE CASCADE"+" ON UPDATE CASCADE");
        }

        initTableColumns(courseColumnRepository, TABLE_NAME);

        //addColumn("kurs_in_eur", ColumnDatatype.DOUBLE);
    }

    @Override
    public boolean removeColumn(String columnName) {
        return removeAbstractColumn(columnName, TABLE_NAME, courseColumnRepository);
    }

    @Override
    public void addColumn(String colName, ColumnDatatype datatype) {
        addColumnIfNotExists(TABLE_NAME, courseColumnRepository, new CourseColumn(colName, datatype));
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public List<String> getKeyColumns() {
        return KEY_COLUMNS;
    }

    @Override
    public List<String> getReservedColumns() {
        return RESERVED_COLUMNS;
    }

    @Override
    public List<String> getColumnOrder() {
        return COLUMN_ORDER;
    }

    @Override
    protected void saveNewInRepository(String colName, ColumnDatatype datatype) {
        courseColumnRepository.saveAndFlush(new CourseColumn(colName, datatype));
    }

    @Override
    public PreparedStatement getPreparedDataStatement(String colName, Connection connection) {
        throw new UnsupportedOperationException();
    }
}
