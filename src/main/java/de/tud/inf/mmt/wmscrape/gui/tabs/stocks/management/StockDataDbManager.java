package de.tud.inf.mmt.wmscrape.gui.tabs.stocks.management;

import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.stocks.data.StockColumnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

@Service
public class StockDataDbManager {

    @Autowired
    StockColumnRepository stockColumnRepository;
    @Autowired
    DataSource dataSource;

    private ArrayList<String> getColumns() {
        ArrayList<String> columns = new ArrayList<>();

        try {
            Statement statement = dataSource.getConnection().createStatement();
            ResultSet results = statement.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_name = 'Stammdaten';");

            while (results.next()) {
                columns.add(results.getString(1));
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return columns;
    }

    private boolean addColumn(String columnName, ColumnDatatype columnDatatype) {
        try {
            if (columnExists(columnName)) {
                return false;
            }

            Statement statement = dataSource.getConnection().createStatement();
            statement.execute("ALTER TABLE Stammdaten ADD " + columnName + " " + columnDatatype.name()+ ";");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean removeColumn(String columnName) {
        try {
            if (!columnExists(columnName)) {
                return false;
            }

            Statement statement = dataSource.getConnection().createStatement();
            statement.execute("ALTER TABLE Stammdaten DROP COLUMN " + columnName + ";");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean columnExists(String columnName) throws SQLException {
        Statement statement = dataSource.getConnection().createStatement();
        ResultSet results = statement.executeQuery("SELECT * FROM information_schema.columns WHERE table_name = 'Stammdaten';");

        while (results.next()) {
            if (results.getString(1).contentEquals(columnName)) {
                return true;
            }
        }
        statement.close();
        return false;
    }

    private int getColumnDataType(String columnName){
        //https://www.tutorialspoint.com/java-resultsetmetadata-getcolumntype-method-with-example

        try {
            if (!columnExists(columnName)) {
                return -1;
            }
            Statement statement = dataSource.getConnection().createStatement();
            ResultSet results = statement.executeQuery("SELECT " + columnName + " FROM Stammdaten");
            int type = results.getMetaData().getColumnType(1);

//            ResultSet results = statement.executeQuery(
//                    "SELECT data_type FROM information_schema.columns WHERE table_name = 'Stammdaten' " +
//                            "AND column_name = '" + columnName + "';");

            statement.close();
            return type;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -2;
    }

    private boolean tableExists() {
        try {
            Statement statement = dataSource.getConnection().createStatement();
            ResultSet results = statement.executeQuery("SHOW TABLES;");

            while (results.next()) {
                if (results.getString(1).contentEquals("Stammdaten")) {
                    return true;
                }
            }
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private boolean initializeTable() {
        try {
            Statement statement = dataSource.getConnection().createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS Stammdaten ( isin VARCHAR(50), datum DATE, PRIMARY KEY (isin, datum));");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
