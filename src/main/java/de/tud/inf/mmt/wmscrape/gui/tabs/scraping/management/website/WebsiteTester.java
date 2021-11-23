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
                if(!startBrowser()) {step = 8; return false;}
                if(website.getUsernameIdentType() == IdentType.DEAKTIVIERT) {step = 8; return false;}
            }
            case 1 -> {
                if (!loadLoginPage()) {step = 8; return false;}

                if(website.getCookieAcceptIdentType() == IdentType.DEAKTIVIERT) {
                    if(website.getCookieHideIdentType() == IdentType.DEAKTIVIERT) {
                        step=4;
                        return false;
                    }
                    step=3;
                    return false;
                }
            }
            case 2 ->  {
                if (!acceptCookies()) {step = 8; return false;}
                if(website.getCookieHideIdentType() == IdentType.DEAKTIVIERT) {
                    step+=1;
                }
            }
            case 3, 6 -> {
                if(!hideCookies()) {step = 8; return false;}
            }
            case 4 -> {
                if(!fillLoginInformation()) {step = 8; return false;}
            }
            case 5 -> {
                if(!login()) {step = 8; return false;}
                if(website.getCookieHideIdentType() == IdentType.DEAKTIVIERT) {
                    step+=1;
                    if(website.getLogoutIdentType() == IdentType.DEAKTIVIERT) {
                        step+=1;
                    }
                }
            }
            case 7 -> logout();
            case 8 -> quit();
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
