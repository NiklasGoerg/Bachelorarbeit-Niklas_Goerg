package de.tud.inf.mmt.wmscrape.dynamicdb;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.Optional;

@NoRepositoryBean
public interface DynamicDbRepository<TableColumn, ID> extends JpaRepository<TableColumn, ID>, QueryByExampleExecutor<TableColumn> {
    Optional<TableColumn> findByName(String name);
}