package com.web.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;

@Configuration
public class SecondaryDatabaseConfig {
    @Value("${app.erp.odbc.driver-class-name}")
    private String driverClassName;

    @Value("${app.erp.odbc.url}")
    private String dbUrl;

    @Value("${app.erp.odbc.username}")
    private String dbUsername;

    @Value("${app.erp.odbc.password}")
    private String dbPassword;

    @Bean(name = "secondaryDataSource")
    public DataSource secondaryDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        return dataSource;
    }
}
