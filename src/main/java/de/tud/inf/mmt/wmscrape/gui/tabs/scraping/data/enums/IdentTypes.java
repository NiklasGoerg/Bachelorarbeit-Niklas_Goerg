package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.data.enums;

public abstract class IdentTypes {
    public final static IdentType[] IDENT_TYPE_DEACTIVATED_URL = {IdentType.ID, IdentType.XPATH, IdentType.CSS, IdentType.URL, IdentType.DEAKTIVIERT};
    public final static IdentType[] IDENT_TYPE_DEACTIVATED_ENTER = {IdentType.ID, IdentType.XPATH, IdentType.CSS, IdentType.ENTER};
    public final static IdentType[] IDENT_TYPE_DEACTIVATED = {IdentType.ID, IdentType.XPATH, IdentType.CSS, IdentType.DEAKTIVIERT};
    public final static IdentType[] IDENT_TYPE_TABLE = {IdentType.ID, IdentType.XPATH, IdentType.CSS, IdentType.SPALTENNR, IdentType.TITEL, IdentType.DEAKTIVIERT};
    public final static IdentType[] IDENT_TYPE_SIMPLE = {IdentType.ID, IdentType.XPATH, IdentType.CSS};
}
