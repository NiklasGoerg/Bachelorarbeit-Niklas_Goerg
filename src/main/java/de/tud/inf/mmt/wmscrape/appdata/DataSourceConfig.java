package de.tud.inf.mmt.wmscrape.appdata;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

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
