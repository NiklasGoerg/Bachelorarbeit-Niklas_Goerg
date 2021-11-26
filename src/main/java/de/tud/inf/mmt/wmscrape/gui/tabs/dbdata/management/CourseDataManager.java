package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseDataDbManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourseDataManager extends StockAndCourseManager {

    @Autowired
    CourseDataColumnRepository courseDataColumnRepository;
    @Autowired
    CourseDataDbManager courseDataDbManager;

    @Override
    protected void setColumnRepositoryAndManager(){
        dynamicDbRepository = courseDataColumnRepository;
        dynamicDbManger = courseDataDbManager;
    }
}
