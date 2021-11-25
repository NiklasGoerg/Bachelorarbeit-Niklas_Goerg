package de.tud.inf.mmt.wmscrape.dynamicdb.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CourseDataDbManager extends DynamicDbManger{

    public static final String TABLE_NAME = "kursdaten";
    public static final Map<String, ColumnDatatype> colNameToDataType = new HashMap<>();

    @Autowired
    CourseDataColumnRepository courseDataColumnRepository;

    @PostConstruct
    private void initCourseData() {
        // the course data table is not managed by spring
        // and has to be initialized by myself

        if (tableDoesNotExist(TABLE_NAME)) {
            initializeTable("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" (isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");
        }

        ArrayList<String> columnNames = new ArrayList<>();
        for(CourseDataDbTableColumn column : courseDataColumnRepository.findAll()) {
            columnNames.add(column.getName());
            colNameToDataType.put(column.getName(), column.getColumnDatatype());
        }

        for(String colName : getColumns(TABLE_NAME)) {
            if(!columnNames.contains(colName)) {
                ColumnDatatype datatype = getColumnDataType(colName, TABLE_NAME);
                colNameToDataType.put(colName, datatype);
                courseDataColumnRepository.save(new CourseDataDbTableColumn(colName, datatype));
            }
        }

        initColumn("kurs_in_eur", ColumnDatatype.DOUBLE);
        initColumn("volumen", ColumnDatatype.DOUBLE);
        initColumn("tages_hoch", ColumnDatatype.DOUBLE);
        initColumn("tages_tief", ColumnDatatype.DOUBLE);
        initColumn("datum_interessant", ColumnDatatype.DATE);
    }

    private void initColumn(String name, ColumnDatatype columnDatatype) {
        addColumnIfNotExists(TABLE_NAME, courseDataColumnRepository, new CourseDataDbTableColumn(name, columnDatatype));
    }

    public void removeColumn(String columnName) {
        Optional<CourseDataDbTableColumn> column = courseDataColumnRepository.findByName(columnName);
        if(column.isPresent()) {
            column.get().setElementIdentCorrelations(null);
            super.removeAbstractColumn(column.get().getName(), TABLE_NAME, courseDataColumnRepository);
        }
    }
}
