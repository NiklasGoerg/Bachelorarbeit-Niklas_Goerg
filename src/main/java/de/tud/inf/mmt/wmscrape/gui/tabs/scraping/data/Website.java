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

    @Column(columnDefinition = "TEXT", name = "login_url")
    private String loginUrl;

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

    @Enumerated(value = EnumType.STRING)
    @Column(name = "notification_decline_ident_type", nullable = false)
    private IdentType declineNotificationIdentType = IdentType.XPATH;

    @Column(columnDefinition = "TEXT", name = "notification_decline_ident")
    private String notificationDeclineIdent;

    @Column(columnDefinition = "BOOLEAN", name = "is_historic")
    private boolean isHistoric = false;

    @Column(columnDefinition = "TEXT")
    private String searchUrl;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "search_field_ident_type", nullable = false)
    private IdentType searchFieldIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "search_field_ident")
    private String searchFieldIdent;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "search_button_ident_type", nullable = false)
    private IdentType searchButtonIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "search_button_ident")
    private String searchButtonIdent;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "historic_link_ident_type", nullable = false)
    private IdentType historicLinkIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "historic_link_ident")
    private String historicLinkIdent;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "date_from_day_ident_type", nullable = false)
    private IdentType dateFromDayIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "date_from_day_ident")
    private String dateFromDayIdent;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "date_from_month_ident_type", nullable = false)
    private IdentType dateFromMonthIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "date_from_month_ident")
    private String dateFromMonthIdent;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "date_from_year_ident_type", nullable = false)
    private IdentType dateFromYearIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "date_from_year_ident")
    private String dateFromYearIdent;

    @Column(columnDefinition = "TEXT", name = "date_from")
    private String dateFrom;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "date_until_day_ident_type", nullable = false)
    private IdentType dateUntilDayIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "date_until_day_ident")
    private String dateUntilDayIdent;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "date_until_month_ident_type", nullable = false)
    private IdentType dateUntilMonthIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "date_until_month_ident")
    private String dateUntilMonthIdent;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "date_until_year_ident_type", nullable = false)
    private IdentType dateUntilYearIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "date_until_year_ident")
    private String dateUntilYearIdent;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "load_button_ident_type", nullable = false)
    private IdentType loadButtonIdentType = IdentType.ID;

    @Column(columnDefinition = "TEXT", name = "load_button_ident")
    private String loadButtonIdent;

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

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String url) {
        this.loginUrl = url;
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
        return id == website.id && Objects.equals(description, website.description) && Objects.equals(loginUrl, website.loginUrl) && Objects.equals(username, website.username) && Objects.equals(password, website.password) && usernameIdentType == website.usernameIdentType && Objects.equals(usernameIdent, website.usernameIdent) && passwordIdentType == website.passwordIdentType && Objects.equals(passwordIdent, website.passwordIdent) && loginButtonIdentType == website.loginButtonIdentType && Objects.equals(loginButtonIdent, website.loginButtonIdent) && logoutIdentType == website.logoutIdentType && Objects.equals(logoutIdent, website.logoutIdent) && cookieAcceptIdentType == website.cookieAcceptIdentType && Objects.equals(cookieAcceptIdent, website.cookieAcceptIdent);
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

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public IdentType getSearchFieldIdentType() {
        return searchFieldIdentType;
    }

    public void setSearchFieldIdentType(IdentType searchFieldIdentType) {
        this.searchFieldIdentType = searchFieldIdentType;
    }

    public String getSearchFieldIdent() {
        return searchFieldIdent;
    }

    public void setSearchFieldIdent(String searchFieldIdent) {
        this.searchFieldIdent = searchFieldIdent;
    }

    public IdentType getHistoricLinkIdentType() {
        return historicLinkIdentType;
    }

    public void setHistoricLinkIdentType(IdentType historicLinkIdentType) {
        this.historicLinkIdentType = historicLinkIdentType;
    }

    public String getHistoricLinkIdent() {
        return historicLinkIdent;
    }

    public void setHistoricLinkIdent(String historicLinkIdent) {
        this.historicLinkIdent = historicLinkIdent;
    }

    public IdentType getLoadButtonIdentType() {
        return loadButtonIdentType;
    }

    public void setLoadButtonIdentType(IdentType loadButtonIdentType) {
        this.loadButtonIdentType = loadButtonIdentType;
    }

    public String getLoadButtonIdent() {
        return loadButtonIdent;
    }

    public void setLoadButtonIdent(String loadButtonIdent) {
        this.loadButtonIdent = loadButtonIdent;
    }

    public IdentType getDeclineNotificationIdentType() {
        return declineNotificationIdentType;
    }

    public void setDeclineNotificationIdentType(IdentType declineNotificationIdentType) {
        this.declineNotificationIdentType = declineNotificationIdentType;
    }

    public String getNotificationDeclineIdent() {
        return notificationDeclineIdent;
    }

    public void setNotificationDeclineIdent(String notificationDeclineIdent) {
        this.notificationDeclineIdent = notificationDeclineIdent;
    }

    public IdentType getDateFromDayIdentType() {
        return dateFromDayIdentType;
    }

    public void setDateFromDayIdentType(IdentType dateFromDayIdentType) {
        this.dateFromDayIdentType = dateFromDayIdentType;
    }

    public String getDateFromDayIdent() {
        return dateFromDayIdent;
    }

    public void setDateFromDayIdent(String dateFromDayIdent) {
        this.dateFromDayIdent = dateFromDayIdent;
    }

    public IdentType getDateFromMonthIdentType() {
        return dateFromMonthIdentType;
    }

    public void setDateFromMonthIdentType(IdentType dateFromMonthIdentType) {
        this.dateFromMonthIdentType = dateFromMonthIdentType;
    }

    public String getDateFromMonthIdent() {
        return dateFromMonthIdent;
    }

    public void setDateFromMonthIdent(String dateFromMonthIdent) {
        this.dateFromMonthIdent = dateFromMonthIdent;
    }

    public IdentType getDateFromYearIdentType() {
        return dateFromYearIdentType;
    }

    public void setDateFromYearIdentType(IdentType dateFromYearIdentType) {
        this.dateFromYearIdentType = dateFromYearIdentType;
    }

    public String getDateFromYearIdent() {
        return dateFromYearIdent;
    }

    public void setDateFromYearIdent(String dateFromYearIdent) {
        this.dateFromYearIdent = dateFromYearIdent;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public IdentType getDateUntilDayIdentType() {
        return dateUntilDayIdentType;
    }

    public void setDateUntilDayIdentType(IdentType dateUntilDayIdentType) {
        this.dateUntilDayIdentType = dateUntilDayIdentType;
    }

    public IdentType getDateUntilMonthIdentType() {
        return dateUntilMonthIdentType;
    }

    public void setDateUntilMonthIdentType(IdentType dateUntilMonthIdentType) {
        this.dateUntilMonthIdentType = dateUntilMonthIdentType;
    }

    public String getDateUntilMonthIdent() {
        return dateUntilMonthIdent;
    }

    public void setDateUntilMonthIdent(String dateUntilMonthIdent) {
        this.dateUntilMonthIdent = dateUntilMonthIdent;
    }

    public String getDateUntilDayIdent() {
        return dateUntilDayIdent;
    }

    public void setDateUntilDayIdent(String dateUntilDayIdent) {
        this.dateUntilDayIdent = dateUntilDayIdent;
    }

    public IdentType getDateUntilYearIdentType() {
        return dateUntilYearIdentType;
    }

    public void setDateUntilYearIdentType(IdentType dateUntilYearIdentType) {
        this.dateUntilYearIdentType = dateUntilYearIdentType;
    }

    public String getDateUntilYearIdent() {
        return dateUntilYearIdent;
    }

    public void setDateUntilYearIdent(String dateUntilYearIdent) {
        this.dateUntilYearIdent = dateUntilYearIdent;
    }

    public IdentType getSearchButtonIdentType() {
        return searchButtonIdentType;
    }

    public void setSearchButtonIdentType(IdentType searchButtonIdentType) {
        this.searchButtonIdentType = searchButtonIdentType;
    }

    public String getSearchButtonIdent() {
        return searchButtonIdent;
    }

    public void setSearchButtonIdent(String searchButtonIdent) {
        this.searchButtonIdent = searchButtonIdent;
    }
}
