package de.tud.inf.mmt.wmscrape.dynamicdb.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableManger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class CourseTableManager extends DbTableManger {

    public static final String TABLE_NAME = "kursdaten";
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
            initializeTable("CREATE TABLE IF NOT EXISTS `"+TABLE_NAME+"` (isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");
        }

        initTableColumns(courseColumnRepository, TABLE_NAME);

        addColumn("kurs_in_eur", ColumnDatatype.DOUBLE);
        addColumn("volumen", ColumnDatatype.DOUBLE);
        addColumn("tages_hoch", ColumnDatatype.DOUBLE);
        addColumn("tages_tief", ColumnDatatype.DOUBLE);
        addColumn("datum_interessant", ColumnDatatype.DATE);
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
}
