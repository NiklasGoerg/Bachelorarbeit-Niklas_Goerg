package de.tud.inf.mmt.wmscrape.dynamicdb;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.Optional;

@NoRepositoryBean
public interface DbTableColumnRepository<TableColumn, ID> extends JpaRepository<TableColumn, ID>, QueryByExampleExecutor<TableColumn> {
    Optional<TableColumn> findByName(String name);
    void deleteByName(String name);
    <T extends DbTableColumn> void delete(T dbTableColumn);
    <T extends DbTableColumn> void save(T entity);
}
