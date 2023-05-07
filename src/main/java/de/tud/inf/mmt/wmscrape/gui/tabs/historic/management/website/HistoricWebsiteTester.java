package de.tud.inf.mmt.wmscrape.gui.tabs.historic.management.website;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website.WebsiteHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;

public class HistoricWebsiteTester extends WebsiteHandler {

    private int step = 0;
    private boolean hasAcceptedCookies = false;

    public HistoricWebsiteTester(Website website, SimpleStringProperty logText) {
        super(website, logText, false);
    }

    public boolean doNextStep() {
        // if closed stop test
        if(step>0 && getDriver() == null) return true;

        switch (step) {
            case 0 -> {
                if(!startBrowser()) {step = 11; return false;}
                if(isNotUsingLogin()) {step = 5; return false;}
            }
            case 1 -> {
                if (!loadLoginPage()) {step = 11; return false;}

                if(website.getCookieAcceptIdentType() == IdentType.DEAKTIVIERT) {
                    step=3; // next step 3
                    return false;
                }
                // next step 2
            }
            case 2 ->  {
                declineNotifications();
                if (!acceptCookies()) {step = 11; return false;}
                else {hasAcceptedCookies = true;}
                // next step 3
            }
            case 3 -> {
                declineNotifications();
                if(isNotUsingLogin()) {step = 5; return false;}
                else if(!fillLoginInformation()) {step = 11; return false;}
                // next step 4
            }
            case 4 -> {
                declineNotifications();
                if(!login()) {
                    step = 11; return false;
                }
                // next 5
            }
            case 5 -> {
                declineNotifications();
                if(!loadSearchPage()) {step = 11; return false;}
                if(!hasAcceptedCookies && !acceptCookies()) {step = 11; return false;}
            }
            case 6 -> {
                declineNotifications();
                if (!searchForStock("DE0005140008")) {step = 11; return false;}
            }
            case 7 -> {
                declineNotifications();
                if(!loadHistoricPage()) {step=11; return false;}
                if(!readPageCountTest()) {step=11; return false;}
            }
            case 8 -> {
                declineNotifications();
                if(!setDate()) {step=11; return false;}
            }
            case 9 -> {
                declineNotifications();
                if(!loadHistoricData()) {step=11; return false;}
                if(!nextTablePage()) {step=11; return false;}
                if(isNotUsingLogin()) {step = 11; return false;}
            }
            case 10 -> {
                declineNotifications();
                if(!logout()) {step = 11; return false;}
            }
            case 11 -> quit();
            default -> {
                return true;
            }
        }
        step++;
        return false;
    }

    public int getStep() {
        return step;
    }


    @Override
    public boolean cancel() {
        // super.cancel(); use when called as service
        quit();
        return true;
    }

    private boolean isNotUsingLogin() {
        return website.getUsernameIdentType() == IdentType.DEAKTIVIERT;
    }

    @Override
    protected Task<Void> createTask() {
        // maybe create a service if necessary
        throw new NotImplementedFunctionException("WebsiteTester can't be run as a service.");
    }
}