package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management.website;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.Website;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import org.apache.poi.ss.formula.eval.NotImplementedFunctionException;

public class WebsiteTester extends WebsiteHandler {

    private int step = 0;

    public WebsiteTester(Website website, SimpleStringProperty logText) {
        super(website, logText, false);
    }

    public boolean doNextStep() {
        // if closed stop test
        if(step>0 && getDriver() == null) return true;

        switch (step) {
            case 0 -> {
                if(!startBrowser()) {step = 6; return false;}
                if(website.getUsernameIdentType() == IdentType.DEAKTIVIERT) {step = 6; return false;}
            }
            case 1 -> {
                if (!loadLoginPage()) {step = 6; return false;}

                if(website.getCookieAcceptIdentType() == IdentType.DEAKTIVIERT) {
                    step=2; // next step 3
                    return false;
                }
                // next step 2
            }
            case 2 ->  {
                if (!acceptCookies()) {step = 6; return false;}
                // next step 3
            }
            case 3 -> {
                if(!fillLoginInformation()) {step = 6; return false;}
                // next step 4
            }
            case 4 -> {
                if(!login() || website.getLogoutIdentType() == IdentType.DEAKTIVIERT) {
                    step = 6; return false;
                }
                // next 5
            }
            case 5 -> logout();
            case 6 -> quit();
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

    @Override
    protected Task<Void> createTask() {
        // maybe create a service if necessary
        throw new NotImplementedFunctionException("WebsiteTester can't be run as a service.");
    }
}
