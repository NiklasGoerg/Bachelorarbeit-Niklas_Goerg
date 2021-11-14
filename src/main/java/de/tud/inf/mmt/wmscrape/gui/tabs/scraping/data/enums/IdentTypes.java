package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums;

public class IdentTypes {
    public final static IdentType[] identTypeDeactivatedUrl = {IdentType.ID, IdentType.XPATH, IdentType.CSS, IdentType.URL};
    public final static IdentType[] identTypeDeactivated = {IdentType.ID, IdentType.XPATH, IdentType.CSS, IdentType.DEAKTIVIERT};
    public final static IdentType[] identTypeTable = {IdentType.ID, IdentType.XPATH, IdentType.CSS, IdentType.DEAKTIVIERT, IdentType.SPALTENNR, IdentType.TITEL};
    public final static IdentType[] identTypeSimple = {IdentType.ID, IdentType.XPATH, IdentType.CSS};
}
