package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.beans.property.SimpleStringProperty;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class GeneralExtraction {

    private static final String[] DATE_FORMATS = {"dd-MM-yyyy", "dd-MM-yy", "MM-dd-yyyy", "MM-dd-yy", "yy-MM-dd"};

    protected Map<String , PreparedStatement> preparedStatements;
    protected Connection connection;
    protected SimpleStringProperty logText;
    protected WebsiteScraper scraper;
    protected Date date;

    /* make sure that the to be inserted value is the first attribute in the statement */
    protected abstract PreparedStatement prepareStatement(Connection connection, InformationCarrier carrier);
    protected abstract InformationCarrier extendCarrier(InformationCarrier carrier, ElementIdentCorrelation correlation, ElementSelection selection);

    protected String processData(InformationCarrier carrier, String data) {
        log("INFO:\tDaten gefunden für "+carrier.getDbColName()+":\t'"+ data.replace("\n", "\\n") +"'");
        data = regexFilter(carrier.getRegexFilter(), data);
        data = sanitize(data, carrier.getDatatype());
        log("INFO:\tDaten bereinigt für "+carrier.getDbColName()+":\t'"+ data.replace("\n", "\\n") +"'");
        return data;
    }

    protected InformationCarrier prepareCarrier(ElementIdentCorrelation correlation, ElementSelection selection) {
        ColumnDatatype datatype = correlation.getColumnDatatype();
        IdentType identType = correlation.getIdentType();
        String identification = correlation.getIdentification();
        String regex = correlation.getRegex();
        return extendCarrier(new InformationCarrier(date, datatype, identType, identification, regex), correlation, selection);
    }

    protected GeneralExtraction(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        this.connection = connection;
        this.logText = logText;
        this.scraper = scraper;
        this.date = date;
    }

    protected String findFirst(String regex, String text) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);

            // delete everything except the first match
            if (matcher.find()) return matcher.group(0);
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
            log("ERR:\t\tRegex '"+regex+"' ist fehlerhaft und kann nicht angewandt werden.");
        }
        return "";
    }

    protected String regexFilter(String regex, String text) {
        if (regex!= null && !regex.trim().equals("")) {
            var tmp = findFirst(regex, text);
            log("INFO:\tRegex angewandt. '"+removeNewLine(tmp)+
                    "' aus '"+removeNewLine(text)+"' extrahiert.");
            return tmp;
        }
        return text;
    }

    private String getRegularDate(String text) {

        // matches every date format
        String match = findFirst("(\\d{4}|\\d{1,2}|\\d)[^0-9]{1,3}\\d{1,2}[^0-9]{1,3}(\\d{4}|\\d{1,2}|\\d)", text);

        if (match == null || match.equals("")) return "";

        String[] sub = getDateSubstrings(match);

        // building new date from parts
        if (sub[0].length()==2 && sub[1].length()==1 && sub[2].length()==1) {
            // yy-m-d -> dd-MM-yyyy
            reorder(sub, 0, 2);
        } else if(match.matches("^[^0-9]*\\d{4}[^0-9]+\\d{1,2}[^0-9]+\\d{1,2}[^0-9]*$")) {
            // matches yyyy-?-? -> ?-?-yyyy assuming yyyy-MM-dd
            assumeDMOrder(sub, 2, 1);
            reorder(sub, 0,2);
        } else if(match.matches("^[^0-9]*\\d{1,2}[^0-9]+\\d{1,2}[^0-9]+\\d{4}[^0-9]*$")) {
            // matches ?-?-yyyy
            assumeDMOrder(sub, 0, 1);
        } else if(match.matches("^[^0-9]*(\\d{1,2}[^0-9]+\\d{1,2}[^0-9]+\\d{1,2})[^0-9]*$")) {
            // matches ?-?-? -> ?-?-yyyy
            assumeDMOrder(sub, 0, 1);
        } else return "";

        var firstPadding = "0".repeat(2 - sub[0].length());
        var centerPadding = "0".repeat(2 - sub[1].length());
        var lastPadding = "";

        if(sub[2].length() < 4) {
            lastPadding = "0".repeat(2 - sub[2].length());
        }

        return firstPadding+sub[0] +"-"+ centerPadding+sub[1] +"-"+ lastPadding+sub[2];
    }

    private void assumeDMOrder(String[] order, int x, int y) {
        // Assume Date Month order
        String tmp;

        int a = Integer.parseInt(order[x]);
        int b = Integer.parseInt(order[y]);

        if(a <= 12 && b > 12) {
            // assuming 'b' is day an 'a' is month
            reorder(order,x,y);
        }
    }

    private static void reorder(String[] order, int x, int y) {
        String tmp = order[x];
        order[x] = order[y];
        order[y] = tmp;
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

    protected void fillByDataType(int index, ColumnDatatype datatype, PreparedStatement statement, String data) throws SQLException {
        if (data == null || data.equals("")) {
            fillNullByDataType(index, datatype, statement);
            return;
        }

        switch (datatype) {
            case DATE -> statement.setDate(index, getDateFromString(data));
            case TEXT -> statement.setString(index, data);
            case INT -> statement.setInt(index, (int) Double.parseDouble(data));
            case DOUBLE -> statement.setDouble(index, Double.parseDouble(data));
        }
    }

    protected void fillNullByDataType(int index, ColumnDatatype datatype, PreparedStatement statement) throws SQLException {
        switch (datatype) {
            case DATE -> statement.setDate(index, null);
            case TEXT -> statement.setString(index, null);
            case INT -> statement.setInt(index, 0);
            case DOUBLE -> statement.setDouble(index, 0);
        }
    }

    private Date getDateFromString(String date) {

        // last option but date should be prepared to be accepted with the first/second format
        for (String format : DATE_FORMATS) {
            try {
                LocalDate dataToDate = LocalDate.from(DateTimeFormatter.ofPattern(format).parse(date));
                log("INFO:\tDatum "+date+" mit Format "+format+" geparsed.");
                return Date.valueOf(dataToDate);
            } catch (DateTimeParseException e) {
                log("ERR:\t\tDatum "+date+" parsen mit Format "+format+ " nicht möglich.");
            }
        }
        log("FEHLER :\tKein passendes Datumsformat gefunden für "+date);
        return null;
    }

    protected String sanitize(String data, ColumnDatatype datatype) {
        if(data == null) return "";

        switch (datatype) {
            case INT, DOUBLE -> {
                return findFirst("([-+])?[0-9]+(([.,])?[0-9]+)?", data).replace(",",".");
            }
            case DATE -> {
                return getRegularDate(data);
            }
            case TEXT -> {
                return data;
            }
            default -> {
                return "";
            }
        }
    }

    protected boolean isValid(String data, ColumnDatatype datatype) {
        if(datatype == null) return false;

        boolean valid;

        switch (datatype) {
            case INT, DOUBLE -> valid =  data.matches("^[\\-+]?[0-9]+([.,]?[0-9]+)?$");
            case DATE -> valid = data.matches("^(\\d{1,2}|\\d{4})-\\d{1,2}-(\\d{1,2}|\\d{4})$");
            default -> valid = true;
        }

        if(!valid) log("ERR:\t\tDer Datentyp "+datatype+" passt nicht zu den Daten '"+data+"'");

        return valid;
    }

    protected void fillStatement(int index, PreparedStatement statement, String data, ColumnDatatype datatype) {
        try {
            fillByDataType(index, datatype, statement, data);
            statement.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            log("ERR:\t\tSQL Statement:"+e.getMessage()+" <-> "+e.getCause());
        } catch (NumberFormatException | DateTimeParseException e) {
            e.printStackTrace();
            log("ERR:\t\tBei dem Parsen des Wertes '"+data+"' in das Format "+datatype.name()+
                    ". "+e.getMessage()+" <-> "+e.getCause());
        }
    }

    protected void storeInDb() {
        for(PreparedStatement statement : preparedStatements.values()) {
            try {
                statement.executeBatch();
                statement.close();
            } catch (SQLException e) {
                log("ERR:\t\tSQL Statements konnten nicht ausgeführt werden. "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    protected void log(String line) {
        logText.set(this.logText.getValue() +"\n" + line);
    }

    protected void handleSqlException(InformationCarrier carrier, SQLException e) {
        e.printStackTrace();
        log("ERR:\t\tSQL-Statement Erstellung. Spalte '"+ carrier.getDbColName() +"' der Tabelle "+ carrier.getDbColName()
                +". "+ e.getMessage()+" <-> "+ e.getCause());
    }

    private String removeNewLine(String text) {
        if(text == null) return null;
        return text.replace("\n","\\n");
    }
}
