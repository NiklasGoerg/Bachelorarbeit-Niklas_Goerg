package de.tud.inf.mmt.wmscrape.dynamicdb.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Service
public class CourseDataDbManager extends DynamicDbManger{

    public static final String TABLE_NAME = "kursdaten";

    @Autowired
    CourseDataColumnRepository courseDataColumnRepository;

    @PostConstruct
    private void initCourseData() {
        // the course data table is not managed by spring
        // and has to be initialized by myself

        if (tableDoesNotExist(TABLE_NAME)) {
            initializeTable("CREATE TABLE IF NOT EXISTS `"+TABLE_NAME+"` (isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");
        }

        // the column names where a representation in db_table_column_exists
        ArrayList<String> representedColumns = new ArrayList<>();
        for(CourseDataDbTableColumn column : courseDataColumnRepository.findAll()) {
            representedColumns.add(column.getName());
        }


        for(String colName : getColumns(TABLE_NAME)) {
            // add new representation
            if(!representedColumns.contains(colName)) {
                ColumnDatatype datatype = getColumnDataType(colName, TABLE_NAME);
                if(datatype == null) continue;
                courseDataColumnRepository.saveAndFlush(new CourseDataDbTableColumn(colName, datatype));
            } else {
                // representation exists
                representedColumns.remove(colName);
            }
        }

        // removing references that do not exist anymore
        removeRepresentation(representedColumns, courseDataColumnRepository);


        addColumn("kurs_in_eur", ColumnDatatype.DOUBLE);
        addColumn("volumen", ColumnDatatype.DOUBLE);
        addColumn("tages_hoch", ColumnDatatype.DOUBLE);
        addColumn("tages_tief", ColumnDatatype.DOUBLE);
        addColumn("datum_interessant", ColumnDatatype.DATE);
    }

    @Override
    public boolean removeColumn(String columnName) {
        return removeAbstractColumn(columnName, TABLE_NAME, courseDataColumnRepository);
    }

    @Override
    protected void addColumn(String colName, ColumnDatatype datatype) {
        addColumnIfNotExists(TABLE_NAME, courseDataColumnRepository, new CourseDataDbTableColumn(colName, datatype));
    }
}
