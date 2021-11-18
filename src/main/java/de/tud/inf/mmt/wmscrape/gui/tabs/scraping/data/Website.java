package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Website {

    @Id
    @GeneratedValue
    private int id;
    private String description;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(columnDefinition = "TEXT")
    private String username;

    @Column(columnDefinition = "TEXT")
    private String password;

    @Enumerated(value = EnumType.STRING)
    private IdentType usernameIdentType = IdentType.ID;
    @Column(columnDefinition = "TEXT")
    private String usernameIdent;

    @Enumerated(value = EnumType.STRING)
    private IdentType passwordIdentType = IdentType.ID;
    @Column(columnDefinition = "TEXT")
    private String passwordIdent;

    @Enumerated(value = EnumType.STRING)
    private IdentType loginButtonIdentType = IdentType.ID;
    @Column(columnDefinition = "TEXT")
    private String loginButtonIdent;

    @Enumerated(value = EnumType.STRING)
    private IdentType logoutIdentType = IdentType.DEAKTIVIERT;
    @Column(columnDefinition = "TEXT")
    private String logoutIdent;

    @Enumerated(value = EnumType.STRING)
    private IdentType cookieAcceptIdentType = IdentType.DEAKTIVIERT;
    @Column(columnDefinition = "TEXT")
    private String cookieAcceptIdent;

    @Enumerated(value = EnumType.STRING)
    private IdentType cookieHideIdentType = IdentType.DEAKTIVIERT;
    @Column(columnDefinition = "TEXT")
    private String cookieHideIdent;

    @OneToMany(mappedBy = "website", fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
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

    public IdentType getLoginButtonIdentType() {
        return loginButtonIdentType;
    }

    public void setLoginButtonIdentType(IdentType loginButtonIdentType) {
        this.loginButtonIdentType = loginButtonIdentType;
    }

    public String getLoginButtonIdent() {
        return loginButtonIdent;
    }

    public void setLoginButtonIdent(String loginButtonIdent) {
        this.loginButtonIdent = loginButtonIdent;
    }

    public IdentType getLogoutIdentType() {
        return logoutIdentType;
    }

    public void setLogoutIdentType(IdentType logoutIdentType) {
        this.logoutIdentType = logoutIdentType;
    }

    public String getLogoutIdent() {
        return logoutIdent;
    }

    public void setLogoutIdent(String logoutIdent) {
        this.logoutIdent = logoutIdent;
    }

    public IdentType getCookieAcceptIdentType() {
        return cookieAcceptIdentType;
    }

    public void setCookieAcceptIdentType(IdentType cookieAcceptIdentType) {
        this.cookieAcceptIdentType = cookieAcceptIdentType;
    }

    public String getCookieAcceptIdent() {
        return cookieAcceptIdent;
    }

    public void setCookieAcceptIdent(String cookieAcceptIdent) {
        this.cookieAcceptIdent = cookieAcceptIdent;
    }

    public IdentType getCookieHideIdentType() {
        return cookieHideIdentType;
    }

    public void setCookieHideIdentType(IdentType cookieHideIdentType) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Website website = (Website) o;
        return id == website.id && Objects.equals(description, website.description) && Objects.equals(url, website.url) && Objects.equals(username, website.username) && Objects.equals(password, website.password) && usernameIdentType == website.usernameIdentType && Objects.equals(usernameIdent, website.usernameIdent) && passwordIdentType == website.passwordIdentType && Objects.equals(passwordIdent, website.passwordIdent) && loginButtonIdentType == website.loginButtonIdentType && Objects.equals(loginButtonIdent, website.loginButtonIdent) && logoutIdentType == website.logoutIdentType && Objects.equals(logoutIdent, website.logoutIdent) && cookieAcceptIdentType == website.cookieAcceptIdentType && Objects.equals(cookieAcceptIdent, website.cookieAcceptIdent) && cookieHideIdentType == website.cookieHideIdentType && Objects.equals(cookieHideIdent, website.cookieHideIdent) && Objects.equals(websiteElements, website.websiteElements);
    }


}
