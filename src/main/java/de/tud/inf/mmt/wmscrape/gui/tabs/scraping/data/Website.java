package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data;

import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.element.WebsiteElement;
import de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums.IdentType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * contains all information for a website configuration and can be assigned to multiple website element configurations
 */
@Entity
@Table(name = "webseiten_konfiguration")
public class Website extends WebRepresentation<WebsiteElement>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToMany(mappedBy = "website", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    private final List<WebsiteElement> websiteElements = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(columnDefinition = "TEXT")
    private String username;

    @Column(columnDefinition = "TEXT")
    private String password;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "username_ident_type", nullable = false)
    private IdentType usernameIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "username_ident")
    private String usernameIdent;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "password_ident_type", nullable = false)
    private IdentType passwordIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "password_ident")
    private String passwordIdent;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "login_button_ident_type", nullable = false)
    private IdentType loginButtonIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "login_button_ident")
    private String loginButtonIdent;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "logout_ident_type", nullable = false)
    private IdentType logoutIdentType = IdentType.DEAKTIVIERT;

    @Column(columnDefinition = "TEXT", name = "logout_ident")
    private String logoutIdent;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "cookie_accept_ident_type", nullable = false)
    private IdentType cookieAcceptIdentType = IdentType.DEAKTIVIERT;

    @Column(columnDefinition = "TEXT", name = "cookie_accept_ident")
    private String cookieAcceptIdent;

    @Column(columnDefinition = "BOOLEAN", name = "is_historic")
    private boolean isHistoric = false;

    /**
     * only used by hibernate. do not save an instance without setting the necessary fields
     */
    public Website() {}

    public Website(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
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

    /**
     * used at the creation of the selection tree inside the scraping tab
     *
     * @return all connected website element configurations
     */
    @Override
    public List<WebsiteElement> getChildren() {
        return websiteElements;
    }


    // TODO change when hibernate/jpa adds option for "on delete set null" when cascading persist
    /**
     * hibernate does not offer an option to set foreign key fields to null as MySQL does. this emulates the behaviour
     * by setting all fields to null before deleting the entity. if this wouldn't be done, website
     * element configurations would have invalid reverences to website configurations that do not exist anymore.
     */
    @PreRemove
    private void onDeleteSetNull() {
        websiteElements.forEach(e -> e.setWebsite(null));
    }


    @Override
    public String toString() {
        return this.description;
    }

    /**
     * due to the fact that hibernate creates proxies (subclasses of the actual entities) one has to use "instanceof" to compare
     * objects. normal checking of equality can cause unexpected results.
     * lazy loaded fields are omitted because one can not know if a session is still attached.
     *
     * @param o the object to compare to
     * @return true if equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Website website = (Website) o;
        return id == website.id && Objects.equals(description, website.description) && Objects.equals(url, website.url) && Objects.equals(username, website.username) && Objects.equals(password, website.password) && usernameIdentType == website.usernameIdentType && Objects.equals(usernameIdent, website.usernameIdent) && passwordIdentType == website.passwordIdentType && Objects.equals(passwordIdent, website.passwordIdent) && loginButtonIdentType == website.loginButtonIdentType && Objects.equals(loginButtonIdent, website.loginButtonIdent) && logoutIdentType == website.logoutIdentType && Objects.equals(logoutIdent, website.logoutIdent) && cookieAcceptIdentType == website.cookieAcceptIdentType && Objects.equals(cookieAcceptIdent, website.cookieAcceptIdent);
    }

    /**
     * used for saving the selected elements inside the selection tree in the scraping menu as hash values
     * extra value 1 to differentiate between website elements
     * @return the hash value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, description, 2);
    }

    public boolean isHistoric() {
        return isHistoric;
    }

    public void setHistoric(boolean historic) {
        isHistoric = historic;
    }
}
