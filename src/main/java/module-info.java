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
}