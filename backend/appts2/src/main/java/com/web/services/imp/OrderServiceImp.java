package com.web.services.imp;

import com.web.dto.OrderResultSetVals;
import com.web.dto.OrdersFeignData;
import com.web.services.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import java.util.stream.Collectors;

@Service
public class OrderServiceImp implements OrderService {

    @Autowired
    private ConfigurableApplicationContext configurableContext;

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImp.class);
    @Autowired
    private ErpRetryExecutor erpRetryExecutor;

    @Autowired
    @Qualifier("secondaryDataSource")
    private DataSource secondaryDataSource;

    private final Set<Connection> activeConnections = new HashSet<>();

    private static final String ODBC_DRIVER_CLASS = readRequiredEnv("APP_ERP_ODBC_DRIVER_CLASS_NAME");
    private static final String DB_URL = readRequiredEnv("APP_ERP_ODBC_URL");
    private static final String USERNAME = readRequiredEnv("APP_ERP_ODBC_USERNAME");
    private static final String PASSWORD = readRequiredEnv("APP_ERP_ODBC_PASSWORD");
    private static Connection connection;

    private static String readRequiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(key + " must be configured");
        }
        return value;
    }


    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            logger.info("creating new connection");
            connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
            connection.setAutoCommit(true);
        }
        return connection;
    }

    public void closeConnections() {
        logger.info("Closing connections. Total active: " + activeConnections.size());

        Iterator<Connection> iterator = activeConnections.iterator();
        while (iterator.hasNext()) {
            Connection connection = iterator.next();
            try {
                logger.info("connection is null? " + connection);
                if (connection != null) {
                    logger.info("connection is closed? " + connection.isClosed());
                }
                if (connection != null && !connection.isClosed()) {
                    logger.info("Closing connection: " + connection);
                    connection.close();
                    logger.info("Connection closed: " + connection);
                }
            } catch (SQLException e) {
                logger.error("Error closing connection: " + connection, e);
            } finally {
                iterator.remove(); // Remove from list even if it fails to close
            }
        }

        logger.info("All connections closed. Active connections now: " + activeConnections.size());
    }

    private void reloadJDBCDriver() {
        try {
            closeConnections();

            // Log all drivers before deregistering
            Enumeration<Driver> driversBefore = DriverManager.getDrivers();
            logger.info("Drivers before deregistration:");
            while (driversBefore.hasMoreElements()) {
                Driver driver = driversBefore.nextElement();
                logger.info(" - " + driver);
            }

            // Remove ODBC driver
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                if (driver.getClass().getName().equals(ODBC_DRIVER_CLASS)) {
                    DriverManager.deregisterDriver(driver);
                    System.gc(); // Hint JVM to clean up resources
                    logger.info("Deregistered JDBC-ODBC driver: " + driver);
                }
            }

            // Log all drivers after deregistration
            Enumeration<Driver> driversAfter = DriverManager.getDrivers();
            logger.info("Drivers after deregistration:");
            while (driversAfter.hasMoreElements()) {
                Driver driver = driversAfter.nextElement();
                logger.info(" - " + driver);
            }
            System.gc();
            Thread.sleep(2000);

            // Reload driver
            Class<?> driverClass = Class.forName(ODBC_DRIVER_CLASS);
            Driver driverInstance = (Driver) driverClass.getDeclaredConstructor().newInstance();
            DriverManager.registerDriver(driverInstance);
            logger.info("Re-registered JDBC-ODBC driver: " + driverInstance);

            // Confirm driver is reloaded
            Enumeration<Driver> driversFinal = DriverManager.getDrivers();
            logger.info("Drivers after re-registration:");
            while (driversFinal.hasMoreElements()) {
                Driver driver = driversFinal.nextElement();
                logger.info(" - " + driver);
            }

        } catch (Exception e) {
            logger.error("Failed to reload JDBC-ODBC Driver.", e);
        }
    }

    private void refreshDataSource() {
        try {
            closeConnections();

            ConfigurableListableBeanFactory beanFactory = configurableContext.getBeanFactory();

            if (beanFactory.containsBean("secondaryDataSource")) {
                beanFactory.destroyBean("secondaryDataSource");
            }

            logger.info("bean destroyed");
            secondaryDataSource = null;
            logger.info("sds made null");

            System.gc();

            Thread.sleep(2000);


            secondaryDataSource = configurableContext.getBean("secondaryDataSource", DataSource.class);

            logger.info("DataSource refreshed successfully!");

        } catch (Exception ex) {
            logger.error("Failed to refresh the ERP data source", ex);
        }
    }
    public List<OrderResultSetVals> getCRMOrders() {
        List<OrderResultSetVals> orderResults = erpRetryExecutor.execute(
                "getCRMOrders",
                this::getCRMOrdersInner,
                this::recoverErpConnection
        );
        if(orderResults != null && !orderResults.isEmpty()) {
            logger.info("returning orderResults:" + orderResults.size());
        }else{
            logger.info("orderResults got empty");
        }
        return orderResults;
    }

    private void recoverErpConnection() {
        reloadJDBCDriver();
        refreshDataSource();
    }

    public List<OrderResultSetVals> getCRMOrdersInner() {
        List<OrderResultSetVals> orderResultSetVals = new ArrayList<>();

        try {
            String query = "SELECT \"va-210\".\"cdorder\" AS 'Verkooporder', \"va-210\".\"cdordsrt\" AS 'Ordersoort'," +
                    " \"va-211\".\"cdborder\" AS 'Backorder', \"va-210\".\"cdgebruiker-init\" AS 'Gebruiker (I)', \"va-210\".\"cddeb\" AS 'Organisatie'," +
                    " \"ba-001\".\"naamorg\" AS 'Naam', \"ba-012\".\"straat\" AS 'Straat', \"ba-012\".\"huisnr\" AS 'Huisnr', \"ba-012\".\"additioneel\" AS 'Additioneel', \"ba-012\".\"postcode\" AS 'Postcode', \"ba-012\".\"plaats\" AS 'Plaats'," +
                    " \"ba-012\".\"cdland\" AS 'Land', \"va-211\".\"datum-lna\" AS 'Leverdatum', \"va-210\".\"opm-30\" AS 'Referentie'," +
                    " \"va-210\".\"datum-order\" AS 'Datum order', \"va-210\".\"SYS-DATE\" AS 'Datum laatste wijziging'," +
                    " \"va-210\".\"cdgebruiker\" AS 'Gebruiker (L)', \"va-211\".\"nrordrgl\" AS 'Regel', \"va-211\".\"aantbest\" AS 'Aantal besteld'," +
                    " \"va-211\".\"aanttelev\" AS 'Aantal geleverd', \"va-211\".\"cdprodukt\" AS 'Product', \"af-801\".\"tekst\" AS 'Omschrijving'," +
                    " \"va-211\".\"volgorde\" AS 'regelvolgorde', \"bb-043\".\"cdprodgrp\", \"bb-040\".\"zoeknaam\" FROM DATA.PUB.\"af-801\" , DATA.PUB.\"ba-001\" ," +
                    " DATA.PUB.\"ba-012\" , DATA.PUB.\"bb-043\" , DATA.PUB.\"bb-040\" , DATA.PUB.\"va-210\" , DATA.PUB.\"va-211\" " +
                    "WHERE \"ba-001\".\"cdorg\" = \"va-210\".\"cdorg\" AND \"va-211\".\"cdadmin\" = \"va-210\".\"cdadmin\" " +
                    "AND \"va-211\".\"cdorder\" = \"va-210\".\"cdorder\" " +
                    "AND \"va-211\".\"cdprodukt\" = \"af-801\".\"cdsleutel1\" AND \"va-211\".\"cdprodukt\" = \"bb-040\".\"cdprodukt\" AND \"ba-012\".\"id-cdads\" = \"va-211\".\"id-cdads\" " +
                    "AND \"bb-043\".\"cdprodukt\" = \"va-211\".\"cdprodukt\" AND ((\"af-801\".\"cdtabel\"='bb-062') AND (\"va-210\".\"cdadmin\"='01') " +
                    "AND (\"va-211\".\"cdadmin\"='01') AND (\"va-210\".\"cdvestiging\"='ree') AND (\"va-210\".\"cdstatus\" <> 'Z' " +
                    "And \"va-210\".\"cdstatus\" <> 'B') AND (\"bb-043\".\"cdprodcat\"='pro'))";

            Statement statement = null;
            ResultSet resultSet = null;
            try {
                Connection connection = getConnection();
                connection.clearWarnings();

                statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);


                resultSet = statement.executeQuery(query);


                if (activeConnections.isEmpty()) {
                    activeConnections.add(connection);
                }

                if (statement != null) {
                    String orderNumber = null;
                    while (resultSet.next()) {
                        if (resultSet.wasNull()) {
                            continue;
                        }
                        OrderResultSetVals orderResultSetVal = new OrderResultSetVals();

                        orderNumber = resultSet.getString("Verkooporder");

                        orderResultSetVal.setOrderNumber(orderNumber);

                        logger.debug("Mapped ERP order {}", orderNumber);
                        String orderType = resultSet.getString("Ordersoort");
                        orderResultSetVal.setOrderType(orderType);
                        String backOrder = resultSet.getString("Backorder");
                        orderResultSetVal.setBackOrder(backOrder);

                        String user = resultSet.getString("Gebruiker (I)");
                        orderResultSetVal.setUser(user);

                        String organization = resultSet.getString("Organisatie");
                        orderResultSetVal.setOrganization(organization);

                        String customerName = resultSet.getString("Naam");
                        orderResultSetVal.setCustomerName(customerName);

                        String street = resultSet.getString("Straat");
                        orderResultSetVal.setStreet(street);

                        String houseNR = resultSet.getString("Huisnr");
                        orderResultSetVal.setHouseNR(houseNR);

                        String additionalAdd = resultSet.getString("Additioneel");
                        orderResultSetVal.setAdditionalAdd(additionalAdd);

                        String postCode = resultSet.getString("Postcode");
                        orderResultSetVal.setPostCode(postCode);

                        String city = resultSet.getString("Plaats");
                        orderResultSetVal.setCity(city);

                        String country = resultSet.getString("Land");
                        orderResultSetVal.setCountry(country);

                        String deliveryDate = resultSet.getString("Leverdatum");
                        orderResultSetVal.setDeliveryDate(deliveryDate);

                        String referenceInfo = resultSet.getString("Referentie");
                        orderResultSetVal.setReferenceInfo(referenceInfo);

                        String creationDate = resultSet.getString("Datum order");
                        orderResultSetVal.setCreationDate(creationDate);

                        String modificationDate = resultSet.getString("Datum laatste wijziging");
                        orderResultSetVal.setModificationDate(modificationDate);

                        String verifierUser = resultSet.getString("Gebruiker (L)");
                        orderResultSetVal.setVerifierUser(verifierUser);

                        String regel = resultSet.getString("Regel");
                        orderResultSetVal.setRegel(regel);

                        String aantal = resultSet.getString("Aantal besteld");
                        orderResultSetVal.setAantal(aantal);

                        String product = resultSet.getString("Product");
                        orderResultSetVal.setProduct(product);

                        String omsumin = resultSet.getString("Omschrijving");
                        orderResultSetVal.setOmsumin(omsumin);

                        String cdProdGrp = resultSet.getString("cdprodgrp");
                        orderResultSetVal.setCdProdGrp(cdProdGrp);

                        String zoeknaam = resultSet.getString("zoeknaam");
                        orderResultSetVal.setZoeknaam(zoeknaam);

                        String gel = resultSet.getString("Aantal geleverd");
                        orderResultSetVal.setGel(gel);

                        String deliveryDate2 = "";
                        orderResultSetVal.setDeliveryDate2(deliveryDate2);

                        String finalOrderNumber = orderNumber;
                        orderResultSetVal.setFinalOrderNumber(finalOrderNumber);

                        orderResultSetVals.add(orderResultSetVal);

                    }

                    logger.info("main app2 orders length1: " + orderResultSetVals.size());

                    if (orderResultSetVals.isEmpty()) {
                        logger.info("main orders length empty");
                        throw new RuntimeException("no order found");
                    }

                }
                connection.clearWarnings();
                if (orderResultSetVals != null && !orderResultSetVals.isEmpty()) {
                    logger.info("returning orderResultSetVals:" + orderResultSetVals.size());
                } else {
                    logger.info("orderResultSetVals got empty");
                }
                return orderResultSetVals;

            } catch (SQLException var35) {
                getConnection().clearWarnings();

                logger.error("Failed to load CRM orders from ERP", var35);
                if (var35.getMessage().equals("[Microsoft][ODBC Driver Manager] Invalid string or buffer length")) {
                    throw new RuntimeException(var35.getMessage(), var35);
                }
                return null;
            } catch (Exception var37) {
                getConnection().clearWarnings();
                logger.error("Failed to map CRM orders from ERP", var37);
                return null;
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        logger.warn("Failed to close ERP order result set", e);
                    }
                }
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        logger.warn("Failed to close ERP order statement", e);
                    }
                }
            }
        } catch (Exception var39) {
            logger.error("Failed to prepare CRM order query", var39);
            if (var39.getMessage().equals("[Microsoft][ODBC Driver Manager] Invalid string or buffer length")) {
                throw new RuntimeException(var39.getMessage(), var39);
            }
            return null;
        }
    }

    @Override
    public List<OrdersFeignData> markExpired(String stringOfOrdersWithCommaAndQuotations) {
        List<String> orderNumbers = parseOrderNumbers(stringOfOrdersWithCommaAndQuotations);
        if (orderNumbers.isEmpty()) {
            return Collections.emptyList();
        }
        return erpRetryExecutor.execute(
                "markExpired",
                () -> markExpiredInner(orderNumbers),
                this::recoverErpConnection
        );
    }

    private List<OrdersFeignData> markExpiredInner(List<String> orderNumbers) {
        String placeholders = String.join(",", Collections.nCopies(orderNumbers.size(), "?"));
        String query = "SELECT \"va-210\".\"cdorder\" AS 'Verkooporder', " +
                "\"va-211\".\"cdprodukt\" AS 'Product', \"va-211\".\"nrordrgl\" AS 'Regel', " +
                "\"va-210\".\"cdstatus\" AS 'Status' " +
                "FROM DATA.PUB.\"va-210\" " +
                "JOIN DATA.PUB.\"va-211\" ON \"va-210\".\"cdorder\" = \"va-211\".\"cdorder\" " +
                "AND \"va-211\".\"cdadmin\" = \"va-210\".\"cdadmin\" " +
                "WHERE \"va-210\".\"cdadmin\" = '01' " +
                "AND \"va-210\".\"cdvestiging\" = 'ree' " +
                "AND \"va-210\".\"cdorder\" IN (" + placeholders + ")";

        List<OrdersFeignData> dataList = new ArrayList<>();
        try {
            Connection connection = getConnection();
            connection.clearWarnings();
            try (PreparedStatement statement = connection.prepareStatement(
                    query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                for (int i = 0; i < orderNumbers.size(); i++) {
                    statement.setString(i + 1, orderNumbers.get(i));
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (activeConnections.isEmpty()) {
                        activeConnections.add(connection);
                    }
                    while (resultSet.next()) {
                        OrdersFeignData data = new OrdersFeignData();
                        data.setOrderNumber(resultSet.getString("Verkooporder"));
                        data.setProduct(resultSet.getString("Product"));
                        data.setRegel(resultSet.getString("Regel"));
                        data.setStatus(resultSet.getString("Status"));
                        dataList.add(data);
                    }
                }
            }
            connection.clearWarnings();
            return dataList;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load ERP order statuses", e);
        }
    }

    @Override
    public List<OrdersFeignData> checkOrderExistence(String stringOfOrdersWithCommaAndQuotations) {
        List<String> orderNumbers = parseOrderNumbers(stringOfOrdersWithCommaAndQuotations);
        if (orderNumbers.isEmpty()) {
            return Collections.emptyList();
        }
        return erpRetryExecutor.execute(
                "checkOrderExistence",
                () -> checkOrderExistenceInner(orderNumbers),
                this::recoverErpConnection
        );
    }

    private List<OrdersFeignData> checkOrderExistenceInner(List<String> orderNumbers) {
        String placeholders = String.join(",", Collections.nCopies(orderNumbers.size(), "?"));
        String query = "SELECT \"va-210\".\"cdorder\" AS 'Verkooporder', " +
                "\"va-211\".\"nrordrgl\" AS 'Regel' " +
                "FROM DATA.PUB.\"va-210\" " +
                "JOIN DATA.PUB.\"va-211\" ON \"va-210\".\"cdorder\" = \"va-211\".\"cdorder\" " +
                "AND \"va-211\".\"cdadmin\" = \"va-210\".\"cdadmin\" " +
                "WHERE \"va-210\".\"cdadmin\" = '01' " +
                "AND \"va-210\".\"cdvestiging\" = 'ree' " +
                "AND \"va-210\".\"cdorder\" IN (" + placeholders + ")";

        List<OrdersFeignData> dataList = new ArrayList<>();
        try {
            Connection connection = getConnection();
            connection.clearWarnings();
            try (PreparedStatement statement = connection.prepareStatement(
                    query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                for (int i = 0; i < orderNumbers.size(); i++) {
                    statement.setString(i + 1, orderNumbers.get(i));
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (activeConnections.isEmpty()) {
                        activeConnections.add(connection);
                    }
                    while (resultSet.next()) {
                        OrdersFeignData data = new OrdersFeignData();
                        data.setOrderNumber(resultSet.getString("Verkooporder"));
                        data.setRegel(resultSet.getString("Regel"));
                        dataList.add(data);
                    }
                }
            }
            connection.clearWarnings();
            return dataList;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check ERP order existence", e);
        }
    }

    @Override
    public List<OrdersFeignData> deleteOrders(List<OrdersFeignData> dataList) {
        if (dataList == null) {
            throw new IllegalArgumentException("Order deletion checks are required");
        }
        return erpRetryExecutor.execute(
                "deleteOrders",
                () -> deleteOrdersInner(dataList),
                this::recoverErpConnection
        );
    }

    private List<OrdersFeignData> deleteOrdersInner(List<OrdersFeignData> dataList) {
        String query = "SELECT \"va-210\".\"cdstatus\" AS 'Status' " +
                "FROM DATA.PUB.\"va-210\" " +
                "JOIN DATA.PUB.\"va-211\" ON \"va-210\".\"cdorder\" = \"va-211\".\"cdorder\" " +
                "AND \"va-211\".\"cdadmin\" = \"va-210\".\"cdadmin\" " +
                "WHERE \"va-210\".\"cdadmin\" = '01' " +
                "AND \"va-210\".\"cdvestiging\" = 'ree' " +
                "AND \"va-210\".\"cdorder\" = ? " +
                "AND \"va-211\".\"nrordrgl\" = ?";

        List<OrdersFeignData> returnData = new ArrayList<>();
        try {
            Connection connection = getConnection();
            for (OrdersFeignData data : dataList) {
                if (data == null || data.getOrderNumber() == null || data.getRegel() == null) {
                    throw new IllegalArgumentException("Order number and line are required for ERP deletion checks");
                }
                connection.clearWarnings();
                try (PreparedStatement statement = connection.prepareStatement(
                        query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
                    statement.setString(1, data.getOrderNumber());
                    statement.setString(2, data.getRegel());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (activeConnections.isEmpty()) {
                            activeConnections.add(connection);
                        }
                        if (resultSet.next()) {
                            OrdersFeignData result = new OrdersFeignData();
                            result.setOrderNumber(data.getOrderNumber());
                            result.setRegel(data.getRegel());
                            result.setStatus(resultSet.getString("Status"));
                            returnData.add(result);
                        }
                    }
                }
            }
            connection.clearWarnings();
            return returnData;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check ERP orders before deletion", e);
        }
    }

    private List<String> parseOrderNumbers(String quotedOrderNumbers) {
        if (quotedOrderNumbers == null || quotedOrderNumbers.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> orderNumbers = new ArrayList<>();
        for (String value : quotedOrderNumbers.trim().split("(?<=')\\s*,\\s*(?=')")) {
            String trimmedValue = value.trim();
            if (trimmedValue.length() < 2 ||
                    trimmedValue.charAt(0) != '\'' ||
                    trimmedValue.charAt(trimmedValue.length() - 1) != '\'') {
                throw new IllegalArgumentException("Order numbers must be provided as quoted values");
            }
            String orderNumber = trimmedValue.substring(1, trimmedValue.length() - 1);
            if (orderNumber.isEmpty() || orderNumber.indexOf('\'') >= 0) {
                throw new IllegalArgumentException("Order numbers must be non-empty and cannot contain quotes");
            }
            orderNumbers.add(orderNumber);
        }
        return orderNumbers;
    }

    public List<OrdersFeignData> updateProductNotes(String stringOfOrdersWithCommaAndQuotations) {
        return erpRetryExecutor.execute(
                "updateProductNotes",
                () -> updateProductNotesInner(stringOfOrdersWithCommaAndQuotations),
                this::recoverErpConnection
        );
    }


    public List<OrdersFeignData> updateProductNotesInner(String stringOfOrdersWithCommaAndQuotations) {
        try {
            List<OrdersFeignData> dataList = new ArrayList<>();

            String query = "SELECT \"af-801\".\"cdsleutel1\" AS 'Verkooporder', \"af-801\".\"tekst\" AS 'TekstDescription', \"af-801\".\"cdsleutel2\" AS 'Regel' " +
                    "FROM DATA.PUB.\"af-801\" " +
                    "WHERE \"af-801\".\"cdsoort\" = 'ORR' " +
                    "AND \"af-801\".\"cdsleutel1\" IN (" + stringOfOrdersWithCommaAndQuotations + ")";

            Statement statement = null;
            ResultSet resultSet = null;
            try {
                Connection connection = getConnection();
                connection.clearWarnings();

                statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

                resultSet = statement.executeQuery(query);
                if (activeConnections.isEmpty()) {
                    activeConnections.add(connection);
                }


                if (statement != null) {
                    if (resultSet != null) {
                        logger.debug("Reading ERP product-note rows");
                        while (resultSet.next()) {

                            String orderNumber = resultSet.getString("Verkooporder");
                            String regel = resultSet.getString("Regel");
                            String text = resultSet.getString("TekstDescription");

                            OrdersFeignData data = new OrdersFeignData();

                            data.setOrderNumber(orderNumber);
                            data.setRegel(regel);
                            data.setNotes(text);

                            dataList.add(data);
                        }
                    }
                }
                connection.clearWarnings();
                return dataList;
            } catch (Exception var39) {
                getConnection().clearWarnings();
                logger.error("Failed to load product notes from ERP", var39);
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        logger.warn("Failed to close ERP product-note result set", e);
                    }
                }
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        logger.warn("Failed to close ERP product-note statement", e);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Failed to prepare ERP product-note query", e);
        }
        return null;
    }

    @Override
    public List<OrdersFeignData> createMonSub(String stringOfOrdersWithCommaAndQuotations) {
        return erpRetryExecutor.execute(
                "createMonSub",
                () -> createMonSubInner(stringOfOrdersWithCommaAndQuotations),
                this::recoverErpConnection
        );
    }

    public List<OrdersFeignData> createMonSubInner(String stringOfOrdersWithCommaAndQuotations) {
        try {
            List<OrdersFeignData> dataList = new ArrayList<>();
            String query = "SELECT \"va-229\".\"cdorder\" AS 'Verkooporder', \"va-229\".\"nrordrgl\" AS 'Regel', \"va-229\".\"cdprodukt\" AS 'Product', " +
                    "\"va-229\".\"cdadmin\" AS 'Admin', \"af-801\".\"tekst\" AS 'Omschrijving' " +
                    "FROM DATA.PUB.\"va-229\" va " +
                    "LEFT JOIN DATA.PUB.\"af-801\" af ON va.\"cdprodukt\" = af.\"cdsleutel1\" " +
                    "AND af.\"cdtabel\" = 'bb-062' " +
                    "WHERE va.\"cdstatus\" <> 'Z' " +
                    "AND va.\"cdadmin\" = '01' " +
                    "AND va.\"cdorder\" IN (" + stringOfOrdersWithCommaAndQuotations + ")";

            Statement statement = null;
            ResultSet resultSet = null;
            try {
                Connection connection = getConnection();
                connection.clearWarnings();

                statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

                resultSet = statement.executeQuery(query);

                if (activeConnections.isEmpty()) {
                    activeConnections.add(connection);
                }
                if (statement != null && resultSet != null) {
                    String orderNumber = null;
                    while (resultSet.next()) {
                        orderNumber = resultSet.getString("Verkooporder");
                        if (resultSet.wasNull()) {
                            continue;
                        }

                        OrdersFeignData data = new OrdersFeignData();
                        String regel = resultSet.getString("Regel");
                        String product = resultSet.getString("Product");
                        String description = resultSet.getString("Omschrijving");

                        data.setOrderNumber(orderNumber);
                        data.setRegel(regel);
                        data.setProduct(product);
                        data.setDescription(description);

                        dataList.add(data);

                    }
                }
                connection.clearWarnings();
                return dataList;
            } catch (SQLException var35) {
                getConnection().clearWarnings();
                logger.error("Failed to load MON suborders from ERP", var35);
            } catch (Exception var37) {
                getConnection().clearWarnings();
                logger.error("Failed to map MON suborders from ERP", var37);
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        logger.warn("Failed to close ERP MON suborder result set", e);
                    }
                }
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        logger.warn("Failed to close ERP MON suborder statement", e);
                    }
                }
            }
        } catch (Exception var39) {
            logger.error("Failed to prepare ERP MON suborder query", var39);
        }

        return null;
    }
}
