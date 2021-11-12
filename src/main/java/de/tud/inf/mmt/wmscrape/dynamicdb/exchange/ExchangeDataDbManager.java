package de.tud.inf.mmt.wmscrape.dynamicdb.exchange;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataDbTableColumn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class ExchangeDataDbManager extends DynamicDbManger{

    @Autowired
    ExchangeDataColumnRepository exchangeDataColumnRepository;

    @PostConstruct
    private void initExchangeData() {
        // the exchange data table is not managed by spring
        // and has to be initialized by myself

        if (!tableExists("wechselkurse")) {
            initializeTable("CREATE TABLE IF NOT EXISTS wechselkurse (datum DATE PRIMARY KEY);");
        }

        ArrayList<String> columnNames = new ArrayList<>();
        for(ExchangeDataDbTableColumn column : exchangeDataColumnRepository.findAll()) {
            columnNames.add(column.getName());
        }

        for(String colName : getColumns("wechselkurse")) {
            if(!columnNames.contains(colName)) {
                ColumnDatatype datatype = getColumnDataType(colName, "wechselkurse");
                exchangeDataColumnRepository.save(new ExchangeDataDbTableColumn(colName, datatype));
            }
        }

        addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataDbTableColumn("eur", ColumnDatatype.DOUBLE));
        addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataDbTableColumn("usd", ColumnDatatype.DOUBLE));
        addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataDbTableColumn("gbp", ColumnDatatype.DOUBLE));
        addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataDbTableColumn("jpy", ColumnDatatype.DOUBLE));
        addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataDbTableColumn("cad", ColumnDatatype.DOUBLE));
        addColumnIfNotExists("wechselkurse", exchangeDataColumnRepository,new CourseDataDbTableColumn("cny", ColumnDatatype.DOUBLE));
    }


    public void removeColumn(String columnName) {
        Optional<ExchangeDataDbTableColumn> column = exchangeDataColumnRepository.findByName(columnName);
        if(column.isPresent()) {
            column.get().setElementSelections(new ArrayList<>());
            super.removeColumn(column.get().getName(),"wechselkurse", exchangeDataColumnRepository);
        }
    }

}
