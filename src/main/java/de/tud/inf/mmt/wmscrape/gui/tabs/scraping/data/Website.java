package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeDeactivated;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentTypeDeactivatedUrl;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Website {

    @Id
    @GeneratedValue
    private int id;
    private String description;
    private String url;
    private String username;
    private String password;
    @Enumerated(value = EnumType.STRING)
    private IdentType usernameIdentType = IdentType.ID;
    private String usernameIdent;
    @Enumerated(value = EnumType.STRING)
    private IdentType passwordIdentType = IdentType.ID;
    private String passwordIdent;
    @Enumerated(value = EnumType.STRING)
    private IdentTypeDeactivated loginButtonIdentType = IdentTypeDeactivated.ID;
    private String loginButtonIdent;
    @Enumerated(value = EnumType.STRING)
    private IdentTypeDeactivatedUrl logoutIdentType = IdentTypeDeactivatedUrl.DEAKTIVIERT;
    private String logoutIdent;
    @Enumerated(value = EnumType.STRING)
    private IdentTypeDeactivated cookieAcceptIdentType = IdentTypeDeactivated.DEAKTIVIERT;
    private String cookieAcceptIdent;
    @Enumerated(value = EnumType.STRING)
    private IdentTypeDeactivated cookieHideIdentType = IdentTypeDeactivated.DEAKTIVIERT;
    private String cookieHideIdent;

    @OneToMany(mappedBy = "website", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<WebsiteElement> websiteElements = new ArrayList<>();

    public Website() {
    }

    public Website(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public IdentType getUsernameIdentType() {
        return usernameIdentType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsernameIdentType(IdentType usernameIdentType) {
        this.usernameIdentType = usernameIdentType;
    }

    public String getUsernameIdent() {
        return usernameIdent;
    }

    public void setUsernameIdent(String usernameIdent) {
        this.usernameIdent = usernameIdent;
    }

    public IdentType getPasswordIdentType() {
        return passwordIdentType;
    }

    public void setPasswordIdentType(IdentType passwordIdentType) {
        this.passwordIdentType = passwordIdentType;
    }

    public String getPasswordIdent() {
        return passwordIdent;
    }

    public void setPasswordIdent(String passwordIdent) {
        this.passwordIdent = passwordIdent;
    }

    public IdentTypeDeactivated getLoginButtonIdentType() {
        return loginButtonIdentType;
    }

    public void setLoginButtonIdentType(IdentTypeDeactivated loginButtonIdentType) {
        this.loginButtonIdentType = loginButtonIdentType;
    }

    public String getLoginButtonIdent() {
        return loginButtonIdent;
    }

    public void setLoginButtonIdent(String loginButtonIdent) {
        this.loginButtonIdent = loginButtonIdent;
    }

    public IdentTypeDeactivatedUrl getLogoutIdentType() {
        return logoutIdentType;
    }

    public void setLogoutIdentType(IdentTypeDeactivatedUrl logoutIdentType) {
        this.logoutIdentType = logoutIdentType;
    }

    public String getLogoutIdent() {
        return logoutIdent;
    }

    public void setLogoutIdent(String logoutIdent) {
        this.logoutIdent = logoutIdent;
    }

    public IdentTypeDeactivated getCookieAcceptIdentType() {
        return cookieAcceptIdentType;
    }

    public void setCookieAcceptIdentType(IdentTypeDeactivated cookieAcceptIdentType) {
        this.cookieAcceptIdentType = cookieAcceptIdentType;
    }

    public String getCookieAcceptIdent() {
        return cookieAcceptIdent;
    }

    public void setCookieAcceptIdent(String cookieAcceptIdent) {
        this.cookieAcceptIdent = cookieAcceptIdent;
    }

    public IdentTypeDeactivated getCookieHideIdentType() {
        return cookieHideIdentType;
    }

    public void setCookieHideIdentType(IdentTypeDeactivated cookieHideIdentType) {
        this.cookieHideIdentType = cookieHideIdentType;
    }

    public String getCookieHideIdent() {
        return cookieHideIdent;
    }

    public void setCookieHideIdent(String cookieHideIdent) {
        this.cookieHideIdent = cookieHideIdent;
    }

    public List<WebsiteElement> getWebsiteElements() {
        return websiteElements;
    }

    public void setWebsiteElements(List<WebsiteElement> websiteElements) {
        this.websiteElements = websiteElements;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
