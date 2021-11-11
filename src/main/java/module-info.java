module de.tud.inf.mmt.wmscrape {
    requires javafx.controls;
    requires javafx.fxml;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.boot;
    requires spring.core;
    requires spring.beans;
    requires java.persistence;
    requires spring.data.commons;
    requires java.sql;
    requires org.hibernate.orm.core;
    requires commons.dbcp2;
    requires spring.data.jpa;
    requires java.management;
    requires spring.tx;
    requires spring.aop;
    requires org.controlsfx.controls;
    requires java.annotation;

    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.commons.compress;

    requires org.seleniumhq.selenium.firefox_driver;
    requires org.seleniumhq.selenium.support;
    requires org.seleniumhq.selenium.remote_driver;

    opens de.tud.inf.mmt.wmscrape;
    exports de.tud.inf.mmt.wmscrape;
    exports de.tud.inf.mmt.wmscrape.appdata;
    opens de.tud.inf.mmt.wmscrape.appdata;
    exports de.tud.inf.mmt.wmscrape.gui.tabs;
    opens de.tud.inf.mmt.wmscrape.gui.tabs;
    exports de.tud.inf.mmt.wmscrape.gui;
    opens de.tud.inf.mmt.wmscrape.gui;
    exports de.tud.inf.mmt.wmscrape.gui.login.controller;
    opens de.tud.inf.mmt.wmscrape.gui.login.controller;
    opens de.tud.inf.mmt.wmscrape.gui.tabs.accounts.data;
    opens de.tud.inf.mmt.wmscrape.gui.tabs.data.data;
    opens de.tud.inf.mmt.wmscrape.gui.tabs.depots.data to org.hibernate.orm.core, spring.core;
    exports de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller to spring.beans;
    exports de.tud.inf.mmt.wmscrape.gui.tabs.imports.management to spring.beans;
    opens de.tud.inf.mmt.wmscrape.gui.tabs.imports.data to org.hibernate.orm.core, spring.core, javafx.base;
    opens de.tud.inf.mmt.wmscrape.gui.tabs.imports.controller to javafx.fxml, spring.core;
    exports de.tud.inf.mmt.wmscrape.gui.tabs.data.management to spring.beans;
    opens de.tud.inf.mmt.wmscrape.gui.tabs.data.management to spring.core;
    opens de.tud.inf.mmt.wmscrape.gui.tabs.imports.management to spring.core;
    exports de.tud.inf.mmt.wmscrape.gui.tabs.imports.data to spring.beans;
    exports de.tud.inf.mmt.wmscrape.gui.tabs.data.data;
    exports de.tud.inf.mmt.wmscrape.gui.tabs.depots.data to spring.beans;
    exports de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller to spring.beans;
    exports de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management to spring.beans;
    opens de.tud.inf.mmt.wmscrape.gui.tabs.scraping.controller to spring.core, javafx.fxml;
    exports de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data to spring.beans;
    opens de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management to javafx.fxml, org.hibernate.orm.core, spring.core;
    opens de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data to javafx.base, javafx.fxml, org.hibernate.orm.core, spring.core;
    exports de.tud.inf.mmt.wmscrape.dynamicdb;
    opens de.tud.inf.mmt.wmscrape.dynamicdb;

}