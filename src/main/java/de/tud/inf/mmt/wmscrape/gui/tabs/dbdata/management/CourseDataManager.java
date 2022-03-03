package de.tud.inf.mmt.wmscrape.gui.tabs.dbdata.management;

import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.DbTableColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.VisualDatatype;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumn;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseColumnRepository;
import de.tud.inf.mmt.wmscrape.dynamicdb.course.CourseTableManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CourseDataManager extends StockAndCourseManager {

    @Autowired
    CourseColumnRepository courseColumnRepository;
    @Autowired
    CourseTableManager courseTableManager;

    @SuppressWarnings("unused")
    @Override
    protected void setColumnRepositoryAndManager(){
        dbTableColumnRepository = courseColumnRepository;
        dbTableManger = courseTableManager;
    }

    protected <T extends DbTableColumn> List<? extends DbTableColumn> getTableColumns(DbTableColumnRepository<T, Integer> repository) {
        List<CourseColumn> cols = courseColumnRepository.findAll();
        cols.add(new CourseColumn("r_par", VisualDatatype.Int));
        cols.add(new CourseColumn("name", VisualDatatype.Text));
        cols.add(new CourseColumn("wkn", VisualDatatype.Text));
        cols.add(new CourseColumn("typ", VisualDatatype.Text));
        return cols;
    }

    @Override
    protected String getSelectionStatement(LocalDate startDate, LocalDate endDate) {
        // for every stock in the course table exists a stock so there can't be any null values
        // adds the r_par column to the table
        return "SELECT WP.* , KD.* FROM wertpapier WP RIGHT OUTER JOIN `"+CourseTableManager.TABLE_NAME+"` KD ON WP.isin = KD.isin" + getStartAndEndDateQueryPart(startDate, endDate, "datum");
    }

    @Override
    protected String getSelectionStatementOnlyLatestRows() {
        return "SELECT WP.* , KD.* FROM wertpapier WP RIGHT OUTER JOIN(select * from `"+CourseTableManager.TABLE_NAME+"` WP inner join ( select isin as isin_kd, max(datum) as MaxDate from `"+CourseTableManager.TABLE_NAME+"` group by isin) WPL on WP.isin = WPL.isin_kd and WP.datum = WPL.MaxDate ) KD ON WP.isin = KD.isin";
    }
}
