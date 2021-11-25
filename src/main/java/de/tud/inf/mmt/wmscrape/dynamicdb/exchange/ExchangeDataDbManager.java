package de.tud.inf.mmt.wmscrape.dynamicdb.exchange;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.DynamicDbManger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class ExchangeDataDbManager extends DynamicDbManger{

    public static final String TABLE_NAME = "wechselkurse";
    @Autowired
    ExchangeDataColumnRepository exchangeDataColumnRepository;

    @PostConstruct
    private void initExchangeData() {
        // the exchange data table is not managed by spring
        // and has to be initialized by myself

        if (tableDoesNotExist(TABLE_NAME)) {
            initializeTable("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" (datum DATE PRIMARY KEY);");
        }

        ArrayList<String> columnNames = new ArrayList<>();
        for(ExchangeDataDbTableColumn column : exchangeDataColumnRepository.findAll()) {
            columnNames.add(column.getName());
        }

        for(String colName : getColumns(TABLE_NAME)) {
            if(!columnNames.contains(colName)) {
                ColumnDatatype datatype = getColumnDataType(colName, TABLE_NAME);
                exchangeDataColumnRepository.save(new ExchangeDataDbTableColumn(colName, datatype));
            }
        }

        initColumn("eur");
        initColumn("usd");
        initColumn("gbp");
        initColumn("jpy");
        initColumn("cad");
        initColumn("cny");
    }

    private void initColumn(String name) {
        addColumnIfNotExists(TABLE_NAME, exchangeDataColumnRepository, new ExchangeDataDbTableColumn(name, ColumnDatatype.DOUBLE));
    }


    public void removeColumn(String columnName) {
        Optional<ExchangeDataDbTableColumn> column = exchangeDataColumnRepository.findByName(columnName);
        if(column.isPresent()) {
            column.get().setElementSelections(null);
            removeAbstractColumn(column.get().getName(), TABLE_NAME, exchangeDataColumnRepository);
        }
    }

}
