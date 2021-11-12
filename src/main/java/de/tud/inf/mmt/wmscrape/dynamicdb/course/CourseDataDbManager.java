package de.tud.inf.mmt.wmscrape.dynamicdb.course;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;

@Service
public class CourseDataDbManager extends DynamicDbManger{

    @Autowired
    CourseDataColumnRepository courseDataColumnRepository;

    @PostConstruct
    private void initCourseData() {
        // the course data table is not managed by spring
        // and has to be initialized by myself

        if (!tableExists("kursdaten")) {
            initializeTable("CREATE TABLE IF NOT EXISTS kursdaten (isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");
        }

        ArrayList<String> columnNames = new ArrayList<>();
        for(CourseDataDbTableColumn column : courseDataColumnRepository.findAll()) {
            columnNames.add(column.getName());
        }

        for(String colName : getColumns("kursdaten")) {
            if(!columnNames.contains(colName)) {
                ColumnDatatype datatype = getColumnDataType(colName, "kursdaten");
                courseDataColumnRepository.save(new CourseDataDbTableColumn(colName, datatype));
            }
        }

        addColumnIfNotExists("kursdaten", courseDataColumnRepository,new CourseDataDbTableColumn("kurs_in_eur", ColumnDatatype.TEXT));
        addColumnIfNotExists("kursdaten", courseDataColumnRepository,new CourseDataDbTableColumn("volumen", ColumnDatatype.DOUBLE));
        addColumnIfNotExists("kursdaten", courseDataColumnRepository,new CourseDataDbTableColumn("tages_hoch", ColumnDatatype.DOUBLE));
        addColumnIfNotExists("kursdaten", courseDataColumnRepository,new CourseDataDbTableColumn("tages_tief", ColumnDatatype.DOUBLE));
    }


//    public void removeColumn(String columnName) {
//        Optional<StockDataDbTableColumn> column = stockDataColumnRepository.findByName(columnName);
//        if(column.isPresent()) {
//            column.get().setExcelCorrelations(new ArrayList<>());
//            super.removeColumn(column.get().getName(),"stammdaten", stockDataColumnRepository);
//        }
//    }
}
