package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SingleExtraction implements ElementExtraction {

    private Map<String , PreparedStatement> preparedStatements;

    @Override
    public void extract(WebsiteElement element) {
        PreparedCorrelation preparedCorrelation;
        preparedStatements = new HashMap<>();
        Connection connection = getConnection();
        PreparedStatement statement;
        String dbColName;

        // it's a list but due to ui restraints containing only one selection
        for (var selection : element.getElementSelections()) {
            for(var ident : element.getElementIdentCorrelations()) {

                preparedCorrelation = prepareCorrelation(ident, selection);

                String data = findData(preparedCorrelation);
                data = sanitize(data, preparedCorrelation.getDatatype());

                if(isValid(data, ident.getColumnDatatype())) {
                    dbColName = preparedCorrelation.getDbColName();

                    if(preparedStatements.containsKey(dbColName)) {
                        statement = preparedStatements.get(dbColName);
                    } else {
                        statement = prepareStatement(connection, preparedCorrelation);
                        if(statement != null) {
                            preparedStatements.put(dbColName, statement);
                        }
                    }

                    fillStatement(statement, data, ident.getColumnDatatype());
                }
            }
            break;
        }
        storeInDb();

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected abstract PreparedStatement prepareStatement(Connection connection, PreparedCorrelation correlation);
    protected abstract PreparedCorrelation prepareCorrelation(ElementIdentCorrelation correlation, ElementSelection selection);
    protected abstract Connection getConnection();
    protected abstract String findData(PreparedCorrelation correlation);
    protected abstract void log(String line);

    private void fillStatement(PreparedStatement statement, String data, ColumnDatatype datatype) {
        try {
            if (data == null) fillNullByDataType(datatype, statement);
            else fillByDataType(datatype, statement, data);
            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            log("FEHLER: SQL Statement:"+e.getMessage()+" <-> "+e.getCause());
        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
           log("FEHLER: Bei dem Parsen des Wertes '"+data+"' in das Format"+datatype.name()+
                   ". "+e.getMessage()+" <-> "+e.getCause());
        }
    }

    private void storeInDb() {
        for(PreparedStatement statement : preparedStatements.values()) {
            try {
                statement.executeBatch();
                statement.close();
            } catch (SQLException e) {
                log("FEHLER: SQL Statements konnten nicht ausgeführt werden. "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String sanitize(String data, ColumnDatatype datatype) {
        if(data == null) return "";

        String sanitized = removeExceptFirst("\\S+", data.trim());

        switch (datatype) {
            case INT, DOUBLE -> sanitized = removeExceptFirst("(\\-|\\+)?[0-9]+((\\.|,)?[0-9]+)?", sanitized).replace(",",".");
            case DATE -> sanitized = getRegularDate(sanitized);
            case TEXT -> {
                return sanitized;
            }
            default -> {
                return "";
            }
        }
        return sanitized;
    }

    private String removeExceptFirst(String regex, String text) {

        StringBuilder builder = new StringBuilder(text);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        // delete everything except the first match
        if (matcher.find()) {
            builder.delete(0, matcher.start()-1);
            builder.delete(matcher.end(), text.length()-1);
        }
        return builder.toString();
    }

    private boolean isValid(String data, ColumnDatatype datatype) {
        if(datatype == null) return false;

        // TODO log
        switch (datatype) {
            case INT, DOUBLE -> {
                return data.matches("^[\\-+]?[0-9]+([.,]?[0-9]+)?$");
            }
            case DATE -> {
                return data.matches("^(\\d{1,2}|\\d{4})-\\d{1,2}-(\\d{1,2}|\\d{4})$");
            }
            default -> {
                return true;
            }
        }
    }

    private String getRegularDate(String text) {

        // matches every date format
        if (!text.matches("^[^0-9]*(\\d{4}|\\d{1,2}|\\d)[^0-9]+\\d{1,2}[^0-9]+(\\d{4}|\\d{1,2}|\\d)[^0-9]*$")) {
            return "";
        }

        String[] sub = getDateSubstrings(text);
        // building new date from parts
        if (sub[0].length()==2 && sub[1].length()==1 && sub[2].length()==1) {
            // yy-m-d -> dd-MM-yyyy
            // TODO year >2099
            return "-0"+sub[2]+"-0"+sub[1]+"-20"+sub[0];
        }
        if (sub[0].length()==1 && sub[1].length()==1 && sub[2].length()==2) {
            // ?-?-yy -> dd-MM-yyyy
            // TODO year >2099
            return "0"+sub[0]+"-0"+sub[1]+"-20"+sub[2];
        }



        var firstPadding = "0".repeat(2 - sub[0].length());
        var centerPadding = "0".repeat(2 - sub[1].length());
        var lastPadding = "0".repeat(2 - sub[2].length());

        if(text.matches("^[^0-9]*\\d{4}[^0-9]+\\d{1,2}[^0-9]+\\d{1,2}[^0-9]*$")) {
            // matches yyyy-?-? -> ?-?-yyyy assuming yyyy-MM-dd
            assumeDMOrder(sub, 2, 1);
            return lastPadding+sub[2] +"-"+ centerPadding+sub[1] +"-"+ sub[0];

        } else if(text.matches("^[^0-9]*\\d{1,2}[^0-9]+\\d{1,2}[^0-9]+\\d{4}[^0-9]*$")) {
            // matches ?-?-yyyy
            assumeDMOrder(sub, 0, 1);
            return firstPadding+sub[0] +"-"+ centerPadding+sub[1] +"-"+ sub[2];

        } else if(text.matches("^[^0-9]*(\\d{1,2}[^0-9]+\\d{1,2}[^0-9]+\\d{1,2})[^0-9]*$")) {
            // matches ?-?-? -> ?-?-yyyy
            // TODO year >2099
            assumeDMOrder(sub, 0, 1);
            return firstPadding+sub[0] +"-"+ centerPadding+sub[1] +"-20"+ lastPadding+sub[2];

        } else return "";
    }

    private void assumeDMOrder(String[] order, int x, int y) {
        // Assume Date Month order
        String tmp;

        int a = Integer.parseInt(order[x]);
        int b = Integer.parseInt(order[y]);

        if(a <= 12 && b > 12) {
            // assuming 'b' is day an 'a' is month
            tmp = order[x];
            order[x] = order[y];
            order[y] = tmp;
        }
    }

    private String[] getDateSubstrings(String text) {
        // pattern to extract the substrings
        Pattern pattern = Pattern.compile("\\d{1,4}");
        String[] sub = new String[3];
        int i = 0;

        StringBuilder builder = new StringBuilder(text);
        Matcher matcher = pattern.matcher(text);

        while(matcher.find()) {
            sub[i] = builder.substring(matcher.start(),matcher.end());
            i++;
        }
        return sub;
    }

    private void fillByDataType(ColumnDatatype datatype, PreparedStatement statement, String data) throws SQLException {
        switch (datatype) {
            case DATE -> statement.setDate(1, getDateFromString(data));
            case TEXT -> statement.setString(1, data);
            case INT -> statement.setInt(1, (int) Double.parseDouble(data));
            case DOUBLE -> statement.setDouble(1, Double.parseDouble(data));
        }
    }

    private void fillNullByDataType(ColumnDatatype datatype, PreparedStatement statement) throws SQLException {
        switch (datatype) {
            case DATE -> statement.setDate(1, null);
            case TEXT -> statement.setString(1, null);
            case INT -> statement.setInt(1, 0);
            case DOUBLE -> statement.setDouble(1, 0);
        }
    }

    private Date getDateFromString(String date) {
        LocalDate dataToDate = LocalDate.from(DateTimeFormatter.ofPattern("dd-MM-yyyy").parse(date));
        return Date.valueOf(dataToDate);
    }

}
