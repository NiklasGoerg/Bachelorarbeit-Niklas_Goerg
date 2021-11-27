package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseTableManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourseDataManager extends StockAndCourseManager {

    @Autowired
    CourseColumnRepository courseColumnRepository;
    @Autowired
    CourseTableManager courseTableManager;

    @Override
    protected void setColumnRepositoryAndManager(){
        dbTableColumnRepository = courseColumnRepository;
        dbTableManger = courseTableManager;
    }
}
