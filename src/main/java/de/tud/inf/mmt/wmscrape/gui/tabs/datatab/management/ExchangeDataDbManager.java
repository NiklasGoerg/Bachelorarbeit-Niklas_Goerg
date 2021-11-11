package de.tud.inf.mmt.wmscrape.gui.tabs.datatab.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.course.CourseDataTableColumn;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.exchange.ExchangeDataColumnRepository;
import de.tud.inf.mmt.wmscrape.gui.tabs.datatab.data.exchange.ExchangeDataTableColumn;
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
        for(ExchangeDataTableColumn column : exchangeDataColumnRepository.findAll()) {
            columnNames.add(column.getName());
        }

        for(String colName : dynamicDbManger.getColumns("wechselkurse")) {
            if(!columnNames.contains(colName)) {
                ColumnDatatype datatype = dynamicDbManger.getColumnDataType(colName, "wechselkurse");
                exchangeDataColumnRepository.save(new ExchangeDataTableColumn(colName, datatype));
            }
        }

        dynamicDbManger.addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataTableColumn("eur", ColumnDatatype.DOUBLE));
        dynamicDbManger.addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataTableColumn("usd", ColumnDatatype.DOUBLE));
        dynamicDbManger.addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataTableColumn("gbp", ColumnDatatype.DOUBLE));
        dynamicDbManger.addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataTableColumn("jpy", ColumnDatatype.DOUBLE));
        dynamicDbManger.addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataTableColumn("cad", ColumnDatatype.DOUBLE));
        dynamicDbManger.addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataTableColumn("cny", ColumnDatatype.DOUBLE));
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
