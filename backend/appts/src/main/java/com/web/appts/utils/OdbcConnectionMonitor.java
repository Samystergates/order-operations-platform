package com.web.appts.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class OdbcConnectionMonitor {
    private static final Logger logger = LoggerFactory.getLogger(OdbcConnectionMonitor.class);

    private final List<Connection> activeConnections = new ArrayList<>();

    public void registerConnection(Connection connection) {
        synchronized (activeConnections) {
            activeConnections.add(connection);
        }
    }

    public void monitorConnections() {
        logger.debug("Monitoring {} ODBC connections", activeConnections.size());
        activeConnections.forEach(connection -> {
            try {
                if (connection.isClosed()) {
                    logger.debug("Found a closed ODBC connection");
                }
            } catch (SQLException e) {
                logger.warn("Failed to inspect ODBC connection state", e);
            }
        });
    }
}
