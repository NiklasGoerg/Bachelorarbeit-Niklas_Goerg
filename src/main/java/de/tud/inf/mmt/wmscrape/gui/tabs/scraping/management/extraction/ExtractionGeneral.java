package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.extraction;

import de.tud.inf.mmt.wmscrape.dynamicdb.ColumnDatatype;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.correlation.identification.ElementIdentCorrelation;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.selection.ElementSelection;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebElementInContext;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteScraper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class ExtractionGeneral {

    private static final String[] DATE_FORMATS = {"dd-MM-yyyy", "dd-MM-yy", "MM-dd-yyyy", "MM-dd-yy", "yy-MM-dd"};

    protected HashMap<String , PreparedStatement> preparedStatements = new HashMap<>();
    protected final Connection connection;
    protected final SimpleStringProperty logText;
    protected final WebsiteScraper scraper;
    protected final Date date;

    /**
     * @param connection jdbc connection used for every database access
     * @param logText some text property where errors/infos will be written into
     * @param scraper the scraper that manages the abstract process of scraping
     * @param date the date used for information that is saved for a specific date (atm. always today)
     */
    protected ExtractionGeneral(Connection connection, SimpleStringProperty logText, WebsiteScraper scraper, Date date) {
        this.connection = connection;
        this.logText = logText;
        this.scraper = scraper;
        this.date = date;
    }

    /**
     * used to create sql statements based on the table. the first parameter is reserved for the data that will be inserted
     * with the statement
     * note: make sure that the to be inserted value is the first attribute in the statement
     *
     * @param connection the jdbc connection
     * @param carrier the carrier holds all the necessary information to fill the statement.
     *                the carrier has to be prepared beforehand
     * @return the prepared statement ready to insert data into
     */
    protected abstract PreparedStatement prepareStatement(Connection connection, InformationCarrier carrier);

    /**
     * second step after {@link #prepareCarrier(ElementIdentCorrelation, ElementSelection)}
     * adds additional information that is needed to the carrier (e.g. statement creation).
     *
     * @param carrier the information carrier
     * @param correlation the element correlation used to extend the carrier
     * @param selection the selection used to extend the carrier
     * @return the prepared carrier
     */
    protected abstract InformationCarrier extendCarrier(InformationCarrier carrier, ElementIdentCorrelation correlation, ElementSelection selection);

    /**
     * processes the data extracted from the website including the sub element extraction with a given regex and
     * sanitizing according t the datatype set in the prepared carrier
     *
     * @param carrier the carrier associated with the data
     * @param data the extracted text data from the website
     * @return text data ready to be validated and parsed
     */
    protected String processData(InformationCarrier carrier, String data) {
        if(!scraper.getWebsite().isHistoric()) {
            log("INFO:\tDaten gefunden für "+carrier.getDbColName()+":\t\t'"+ data.replace("\n", "\\n") +"'");
        }

        data = regexFilter(carrier.getRegexFilter(), data);
        data = sanitize(data, carrier.getDatatype());

        if(!scraper.getWebsite().isHistoric()) {
            log("INFO:\tDaten bereinigt für "+carrier.getDbColName()+":\t\t'"+ data.replace("\n", "\\n") +"'");
        }

        return data;
    }

    /**
     * adds the basic information that do not vary between element types
     *
     * @param selection the selection used to extend the carrier
     * @param correlation the element correlation used to extend the carrier
     * @return the prepared carrier
     */
    protected InformationCarrier prepareCarrier(ElementIdentCorrelation correlation, ElementSelection selection) {
        ColumnDatatype datatype = correlation.getColumnDatatype();
        IdentType identType = correlation.getIdentType();
        String identification = correlation.getIdentification();
        String regex = correlation.getRegex();
        return extendCarrier(new InformationCarrier(date, datatype, identType, identification, regex), correlation, selection);
    }

    /**
     * tries to find the first substring based on a given regex.
     * used to search the data for a specific datatype
     *
     * @param regex the regex used for the search
     * @param text the text data that will be searched
     * @return the substring or an empty string
     */
    private String findFirst(String regex, String text) {
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

    /**
     * used to prefilter text data with an optional user defined regex
     *
     * @param regex the regex used for the filtering
     * @param text the text data that will be searched
     * @return the substring or the original string
     */
    private String regexFilter(String regex, String text) {
        if (regex!= null && !regex.trim().isBlank()) {
            var tmp = findFirst(regex, text);
            log("INFO:\tRegex angewandt. '"+removeNewLine(tmp)+
                    "' aus '"+removeNewLine(text)+"' extrahiert.");
            return tmp;
        }
        return text;
    }

    /**
     * transforms various date formats to dd-MM-yyyy.
     * this solves some issues regarding date formats that could be parsed in the wrong format
     *
     * @param text the text string containing the date
     * @return the ordered date or an empty string
     */
    private String getDateInRegularFormat(String text) {

        // matches every date format
        String match = findFirst("(\\d{4}|\\d{1,2}|\\d)[^0-9]{1,3}\\d{1,2}[^0-9]{1,3}(\\d{4}|\\d{1,2}|\\d)", text);

        if (match == null || match.isBlank()) return "";

        String[] sub = getDateSubstringParts(match);

        // building new date from parts
        if (sub[0].length()==2 && sub[1].length()==1 && sub[2].length()==1) {
            // yy-m-d -> dd-MM-yyyy
            reorderArray(sub, 0, 2);
        } else if(match.matches("^[^0-9]*\\d{4}[^0-9]+\\d{1,2}[^0-9]+\\d{1,2}[^0-9]*$")) {
            // matches yyyy-?-? -> ?-?-yyyy assuming yyyy-MM-dd
            assumeDMOrder(sub, 2, 1);
            reorderArray(sub, 0,2);
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

    /**
     * transforms text containing a numerical value with separators into one with only on point
     * separating the decimal values
     *
     * @param data the text data containing the numerical value
     * @return the transformed value as text
     */
    private String getNumberInRegularFormat(String data) {
        // every following digit are cut off at the double->int cast

        // imagine 10.000,023
        String findings = findFirst("[+-]?([0-9]*[,.]?[0-9])*", data).replace(",",".");

        // parts > 10 000 023
        List<String> sections = new ArrayList<>();
        Arrays.stream(findings.split("\\.")).toList().forEach(str ->
                sections.add(str.replace("[,.]", "")));


        // 10 000 023 -> 10000.023
        if(sections.size() > 1) {
            StringBuilder sectionString = new StringBuilder("." + sections.get(sections.size()-1));

            // building the number backwards
            for(int i=sections.size()-2; i>=0; i--) {
                sectionString.insert(0, sections.get(i));
            }

            return sectionString.toString();
        } else return findings;

    }

    /**
     * simple assumption about the order of the date and month. if one of them is lager than 12 and the other equal or
     * less than 12, it can be assumed what is the month and what the day
     *
     * @param array an array containing parts of a date
     * @param x the index of day or month
     * @param y the index of day or month
     */
    private void assumeDMOrder(String[] array, int x, @SuppressWarnings("SameParameterValue") int y) {
        // Assume Date Month array

        int a = Integer.parseInt(array[x]);
        int b = Integer.parseInt(array[y]);

        if(a <= 12 && b > 12) {
            // assuming 'b' is day an 'a' is month
            reorderArray(array,x,y);
        }
    }

    /**
     * swaps array positions
     *
     * @param x swap with y position
     * @param y swap with x position
     */
    private static void reorderArray(String[] array, int x, int y) {
        String tmp = array[x];
        array[x] = array[y];
        array[y] = tmp;
    }

    /**
     * splits a date in text form into its daym month and year parts
     *
     * @param text the text containing the date
     * @return the split date
     */
    private String[] getDateSubstringParts(String text) {
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

    /**
     * fills a prepared statement with data according to the datatype
     *
     * @param index the index where the data will be inserted
     * @param datatype the data datatype
     * @param statement the sql statement already prepared ond only missing the data
     * @param data the validated text data
     */
    private void fillByDataType(int index, ColumnDatatype datatype, PreparedStatement statement, String data) throws SQLException {
        if (data == null || data.isBlank()) {
            fillNullByDataType(index, datatype, statement);
            return;
        }

        switch (datatype) {
            case DATE -> statement.setDate(index, getDateFromString(data));
            case TEXT -> statement.setString(index, data);
            case INTEGER -> statement.setInt(index, (int) Double.parseDouble(data));
            case DOUBLE -> statement.setDouble(index, Double.parseDouble(data));
        }
    }

    /**
     *
     * @param index the index where the data will be inserted
     * @param datatype the data datatype
     * @param statement the sql statement already prepared ond only missing the data
     */
    private void fillNullByDataType(int index, ColumnDatatype datatype, PreparedStatement statement) throws SQLException {
        switch (datatype) {
            case DATE -> statement.setNull(index, Types.DATE);
            case TEXT -> statement.setNull(index, Types.VARCHAR);
            case INTEGER -> statement.setNull(index, Types.INTEGER);
            case DOUBLE -> statement.setNull(index, Types.DOUBLE);
        }
    }

    /**
     * tries multiple data formats to parse the extracted and prepared date string. the first one should work
     * as this is the one the date was prepared for
     *
     * @param date the date value as text
     * @return the text date parsed to a java date
     */
    private Date getDateFromString(String date) {

        // last option with try/error. date should be prepared to be accepted with the first/second format
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

    /**
     * cleans the input based on the given datatype and returns only that part of the given string that matches the datatype
     *
     * @param data the extracted text data from the website
     * @param datatype the known datatype of the text string
     * @return the part that matches the datatype or an empty string
     */
    private String sanitize(String data, ColumnDatatype datatype) {
        if(data == null) return "";

        switch (datatype) {
            case INTEGER, DOUBLE -> {
                return getNumberInRegularFormat(data);
            }
            case DATE -> {
                return getDateInRegularFormat(data);
            }
            case TEXT -> {
                return data;
            }
            default -> {
                return "";
            }
        }
    }

    /**
     * validates the text data based on the datatype
     *
     * @param data the extracted text data from the website
     * @param datatype the known datatype of the text string
     * @param colName the column the data will be inserted into
     * @return true if it matches the datatype
     */
    protected boolean isValid(String data, ColumnDatatype datatype, String colName) {
        if(datatype == null) return false;

        boolean valid;

        switch (datatype) {
            case INTEGER, DOUBLE -> valid =  data.matches("^[\\-+]?[0-9]+([.]?[0-9]+)?$");
            case DATE -> valid = data.matches("^(\\d{1,2}|\\d{4})-\\d{1,2}-(\\d{1,2}|\\d{4})$");
            default -> valid = true;
        }

        if(!valid) log("ERR:\t\tDie extrahierten Daten '"+data+"' haben einen unpassenden Datentyp "+datatype+" für "+colName);

        return valid;
    }

    /**
     * fills the prepared statement with the given data and adds it to the statement batch
     *
     * @param index the index of statemen position for the data to be inserted
     * @param statement the prepared statement
     * @param data the extracted text data from the website
     * @param datatype the known datatype of the text string
     */
    protected void fillStatement(@SuppressWarnings("SameParameterValue") int index, PreparedStatement statement,
                                 String data, ColumnDatatype datatype) {
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

    /**
     * executes all prepared statements
     */
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

    /**
     * adds a text line to the log
     * @param line the log message
     */
    protected void log(String line) {
        // not doing this would we be a problem due to the multithreaded execution
        Platform.runLater(() -> logText.set(this.logText.getValue() +"\n" + line));
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

    /**
     * uses the scraper to extract the text data from the website
     *
     * @param element the website element in context containing the information about the html frame and web element parents
     * @param carrier the carrier containing the identification of the data (xpath /css)
     * @return the extracted data or null if not found
     */
    protected String getTextData(WebElementInContext element, InformationCarrier carrier) {
        return scraper.findTextInContext(
                carrier.getIdentType(),
                carrier.getIdentifier(),
                carrier.getDbColName(),
                element);
    }

    protected void logStart(String description) {
        log("\n----------------------------------------------------------------------------\n\n" +
                "INFO:\tBeginne Datenextraktion für: "+description+"\n");
    }
}
