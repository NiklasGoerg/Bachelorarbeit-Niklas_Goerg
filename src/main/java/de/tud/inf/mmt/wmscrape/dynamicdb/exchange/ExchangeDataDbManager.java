package de.tud.inf.mmt.wmscrape.dynamicdb.exchange;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataDbTableColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.util.ArrayList;

@Service
public class ExchangeDataDbManager {

    @Autowired
    ExchangeDataColumnRepository exchangeDataColumnRepository;
    @Autowired
    DynamicDbManger dynamicDbManger;

    @PostConstruct
    private void initExchangeData() {
        // the exchange data table is not managed by spring
        // and has to be initialized by myself

        if (!dynamicDbManger.tableExists("wechselkurse")) {
            dynamicDbManger.initializeTable("CREATE TABLE IF NOT EXISTS wechselkurse (datum DATE PRIMARY KEY);");
        }

        ArrayList<String> columnNames = new ArrayList<>();
        for(ExchangeDataDbTableColumn column : exchangeDataColumnRepository.findAll()) {
            columnNames.add(column.getName());
        }

        for(String colName : dynamicDbManger.getColumns("wechselkurse")) {
            if(!columnNames.contains(colName)) {
                ColumnDatatype datatype = dynamicDbManger.getColumnDataType(colName, "wechselkurse");
                exchangeDataColumnRepository.save(new ExchangeDataDbTableColumn(colName, datatype));
            }
        }

        dynamicDbManger.addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataDbTableColumn("eur", ColumnDatatype.DOUBLE));
        dynamicDbManger.addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataDbTableColumn("usd", ColumnDatatype.DOUBLE));
        dynamicDbManger.addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataDbTableColumn("gbp", ColumnDatatype.DOUBLE));
        dynamicDbManger.addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataDbTableColumn("jpy", ColumnDatatype.DOUBLE));
        dynamicDbManger.addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataDbTableColumn("cad", ColumnDatatype.DOUBLE));
        dynamicDbManger.addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataDbTableColumn("cny", ColumnDatatype.DOUBLE));
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
