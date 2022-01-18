package de.tud.inf.mmt.wmscrape.springdata;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    /**
     * this creates and overrides the standard DataSource-Bean where parameters can change.
     * this datasource is used by spring for all database connections and handles connection pooling.
     * that means it manages multiple connections that won't have to be closed and opened when needed but
     * the datasource provides a free connection.
     *
     * some configurations are fetched from the application.properties file
     *
     * @return the DataSource-Bean
     */
    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        //dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceBuilder.url(SpringIndependentData.getSpringConnectionPath());
        dataSourceBuilder.username(SpringIndependentData.getUsername());
        dataSourceBuilder.password(SpringIndependentData.getPassword());
        return dataSourceBuilder.build();
    }
}
