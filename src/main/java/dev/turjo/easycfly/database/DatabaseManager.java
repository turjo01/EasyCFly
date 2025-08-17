package dev.turjo.easycfly.database;

import dev.turjo.easycfly.EasyCFly;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {
    
    private final EasyCFly plugin;
    private HikariDataSource dataSource;
    
    public DatabaseManager(EasyCFly plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        String databaseType = plugin.getConfigManager().getConfig().getString("database.type", "sqlite");
        
        if (databaseType.equalsIgnoreCase("sqlite")) {
            setupSQLite();
        } else if (databaseType.equalsIgnoreCase("mysql")) {
            setupMySQL();
        } else {
            plugin.getLogger().severe("Unknown database type: " + databaseType);
            return;
        }
        
        createTables();
        plugin.getLogger().info("Database initialized successfully!");
    }
    
    private void setupSQLite() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            String fileName = plugin.getConfigManager().getConfig().getString("database.sqlite.file", "easycfly.db");
            File databaseFile = new File(dataFolder, fileName);
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            config.setMaximumPoolSize(plugin.getConfigManager().getConfig().getInt("database.pool.maximum-pool-size", 10));
            config.setMinimumIdle(plugin.getConfigManager().getConfig().getInt("database.pool.minimum-idle", 2));
            config.setConnectionTimeout(plugin.getConfigManager().getConfig().getLong("database.pool.connection-timeout", 30000));
            
            dataSource = new HikariDataSource(config);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to setup SQLite database", e);
        }
    }
    
    private void setupMySQL() {
        try {
            String host = plugin.getConfigManager().getConfig().getString("database.mysql.host", "localhost");
            int port = plugin.getConfigManager().getConfig().getInt("database.mysql.port", 3306);
            String database = plugin.getConfigManager().getConfig().getString("database.mysql.database", "easycfly");
            String username = plugin.getConfigManager().getConfig().getString("database.mysql.username", "root");
            String password = plugin.getConfigManager().getConfig().getString("database.mysql.password", "password");
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&autoReconnect=true");
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(plugin.getConfigManager().getConfig().getInt("database.pool.maximum-pool-size", 10));
            config.setMinimumIdle(plugin.getConfigManager().getConfig().getInt("database.pool.minimum-idle", 2));
            config.setConnectionTimeout(plugin.getConfigManager().getConfig().getLong("database.pool.connection-timeout", 30000));
            
            dataSource = new HikariDataSource(config);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to setup MySQL database", e);
        }
    }
    
    private void createTables() {
        try (Connection connection = getConnection()) {
            // Create flight_data table
            String flightDataTable = """
                CREATE TABLE IF NOT EXISTS flight_data (
                    uuid VARCHAR(36) PRIMARY KEY,
                    total_flight_time BIGINT DEFAULT 0,
                    total_distance DOUBLE DEFAULT 0,
                    times_flown INT DEFAULT 0,
                    last_flight BIGINT DEFAULT 0
                )
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(flightDataTable)) {
                stmt.executeUpdate();
            }
            
            // Create trust_data table
            String trustDataTable = """
                CREATE TABLE IF NOT EXISTS trust_data (
                    owner_uuid VARCHAR(36),
                    trusted_uuid VARCHAR(36),
                    trusted_at BIGINT DEFAULT 0,
                    PRIMARY KEY (owner_uuid, trusted_uuid)
                )
                """;
            
            try (PreparedStatement stmt = connection.prepareStatement(trustDataTable)) {
                stmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create database tables", e);
        }
    }
    
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not initialized");
        }
        return dataSource.getConnection();
    }
    
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}