package de.tud.inf.mmt.wmscrape.gui.tabs.datatab.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.course.CourseDataColumnRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.course.CourseDataTableColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.util.ArrayList;

@Service
public class CourseDataDbManager {

    @Autowired
    CourseDataColumnRepository courseDataColumnRepository;
    @Autowired
    DynamicDbManger dynamicDbManger;

    @PostConstruct
    private void initCourseData() {
        // the course data table is not managed by spring
        // and has to be initialized by myself

        if (!dynamicDbManger.tableExists("kursdaten")) {
            dynamicDbManger.initializeTable("CREATE TABLE IF NOT EXISTS kursdaten (datum DATE PRIMARY KEY);");
        }

        ArrayList<String> columnNames = new ArrayList<>();
        for(CourseDataTableColumn column : courseDataColumnRepository.findAll()) {
            columnNames.add(column.getName());
        }

        for(String colName : dynamicDbManger.getColumns("kursdaten")) {
            if(!columnNames.contains(colName)) {
                ColumnDatatype datatype = dynamicDbManger.getColumnDataType(colName, "kursdaten");
                courseDataColumnRepository.save(new CourseDataTableColumn(colName, datatype));
            }
        }

        dynamicDbManger.addColumnIfNotExists("kursdaten", courseDataColumnRepository,new CourseDataTableColumn("wkn", ColumnDatatype.TEXT));
        dynamicDbManger.addColumnIfNotExists("kursdaten", courseDataColumnRepository,new CourseDataTableColumn("volumen", ColumnDatatype.DOUBLE));
        dynamicDbManger.addColumnIfNotExists("kursdaten", courseDataColumnRepository,new CourseDataTableColumn("tages_hoch", ColumnDatatype.DOUBLE));
        dynamicDbManger.addColumnIfNotExists("kursdaten", courseDataColumnRepository,new CourseDataTableColumn("tages_tief", ColumnDatatype.DOUBLE));
    }


//    public void removeColumn(String columnName) {
//        Optional<CourseDataTableColumn> column = courseDataColumnRepository.findByName(columnName);
//        if(column.isPresent()) {
//            column.get().setExcelCorrelations(new ArrayList<>());
//            dynamicDbManger.removeColumn(column.get().getName(),"stammdaten", courseDataColumnRepository);
//        }
//    }

    public Connection getConnection() {
        return dynamicDbManger.getConnection();
    }
}