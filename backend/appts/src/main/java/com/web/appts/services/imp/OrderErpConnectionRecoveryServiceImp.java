package com.web.appts.services.imp;

import com.web.appts.services.OrderErpConnectionRecoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

@Service
public class OrderErpConnectionRecoveryServiceImp implements OrderErpConnectionRecoveryService {
    private static final Logger logger = LoggerFactory.getLogger(OrderErpConnectionRecoveryServiceImp.class);
    private static final String ODBC_DRIVER_CLASS = readEnv(
            "APP_ERP_ODBC_DRIVER_CLASS_NAME",
            "sun.jdbc.odbc.JdbcOdbcDriver"
    );

    private final ConfigurableApplicationContext configurableContext;

    public OrderErpConnectionRecoveryServiceImp(ConfigurableApplicationContext configurableContext) {
        this.configurableContext = configurableContext;
    }

    private static String readEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    @Override
    public void recover() {
        reloadJDBCDriver();
        refreshDataSource();
    }

    private void refreshDataSource() {
        try {
            ConfigurableListableBeanFactory beanFactory = configurableContext.getBeanFactory();
            if (beanFactory.containsBean("secondaryDataSource")) {
                beanFactory.destroyBean("secondaryDataSource");
            }

            logger.info("Recreating secondary ERP data source bean");
            System.gc();
            Thread.sleep(2000);

            configurableContext.getBean("secondaryDataSource");
            logger.info("DataSource refreshed successfully!");
        } catch (Exception ex) {
            logger.error("Failed to refresh the ERP data source", ex);
        }
    }

    private void reloadJDBCDriver() {
        try {
            logDrivers("Drivers before deregistration:");

            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.getClass().getName().equals(ODBC_DRIVER_CLASS)) {
                    DriverManager.deregisterDriver(driver);
                    System.gc();
                    logger.info("Deregistered JDBC-ODBC driver: {}", driver);
                }
            }

            logDrivers("Drivers after deregistration:");
            System.gc();
            Thread.sleep(2000);

            Class<?> driverClass = Class.forName(ODBC_DRIVER_CLASS);
            Driver driverInstance = (Driver) driverClass.getDeclaredConstructor().newInstance();
            DriverManager.registerDriver(driverInstance);
            logger.info("Re-registered JDBC-ODBC driver: {}", driverInstance);

            logDrivers("Drivers after re-registration:");
        } catch (Exception e) {
            logger.error("Failed to reload JDBC-ODBC Driver.", e);
        }
    }

    private void logDrivers(String message) {
        logger.info(message);
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            logger.info(" - {}", drivers.nextElement());
        }
    }
}
