package de.tud.inf.mmt.wmscrape.gui.tabs.scraping.management;

//public class SingleCourseOrStockExtraction extends SingleExtraction {
//
//    @Override
//    protected PreparedCorrelation prepare(ElementIdentCorrelation correlation, ElementSelection selection) {
//
//        String colName = correlation.getCourseDataDbTableColumn().getName();
//        String tableName = correlation.getCourseDataDbTableColumn().getTableName();
//        ColumnDatatype datatype = correlation.getColumnDatatype();
//
//        var preparedCorrelation = new PreparedCorrelation(tableName, colName, date, datatype, correlation.getIdentType(), correlation.getIdentification());
//        preparedCorrelation.setIsin(selection.getIsin());
//        return preparedCorrelation;
//    }
//
//    @Override
//    protected String findData(PreparedCorrelation correlation) {
//
//        return null;
//    }
//
//    @Override
//    protected boolean isValid(PreparedCorrelation correlation) {
//        return false;
//    }
//
//    @Override
//    protected boolean save(PreparedCorrelation correlation) {
//        return false;
//    }
//}
