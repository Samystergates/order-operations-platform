package com.web.conf;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;

@Configuration
@EntityScan(basePackages = "com.web.entities")
@EnableJpaRepositories(basePackages = "com.web.repositories")
public class PrimaryDatabaseConfig {
    @Value("${app.primary-db.url}")
    private String dbUrl;

    @Value("${app.primary-db.username}")
    private String dbUsername;

    @Value("${app.primary-db.password}")
    private String dbPassword;

    @Value("${app.primary-db.driver-class-name}")
    private String dbDriverClassName;

    @Bean(name = "primaryDataSource")
    @Primary
    public DataSource primaryDataSource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(dbUrl);
        dataSourceBuilder.username(dbUsername);
        dataSourceBuilder.password(dbPassword);
        dataSourceBuilder.driverClassName(dbDriverClassName);
        return dataSourceBuilder.build();
    }

}
