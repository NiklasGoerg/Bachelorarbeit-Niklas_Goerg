package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.VisualDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseTableManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    protected <T extends DbTableColumn> List<? extends DbTableColumn> getTableColumns(DbTableColumnRepository<T, Integer> repository) {
        List<CourseColumn> cols = courseColumnRepository.findAll();
        cols.add(new CourseColumn("r_par", VisualDatatype.Int));
        return cols;
    }

    @Override
    protected String getSelectionStatement() {
        // for every stock in the course table exists a stock so there can't be any null values
        // adds the r_par column to the table
        return "SELECT WP.r_par , KD.* FROM wertpapier WP RIGHT OUTER JOIN `"+CourseTableManager.TABLE_NAME+"` KD ON WP.isin = KD.isin";
    }
}
