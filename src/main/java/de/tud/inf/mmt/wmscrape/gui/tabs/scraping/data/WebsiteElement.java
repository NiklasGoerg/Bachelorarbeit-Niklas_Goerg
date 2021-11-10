package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data;

import javax.persistence.*;

@Entity
public class WebsiteElement {
    @Id
    @GeneratedValue
    private int id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "websiteId", referencedColumnName = "id")
    private Website website;
}
