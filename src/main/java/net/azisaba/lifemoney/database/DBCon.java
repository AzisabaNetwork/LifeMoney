package net.azisaba.lifemoney.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.azisaba.lifemoney.LifeMoney;
import net.azisaba.lifemoney.money.Moneys;
import net.azisaba.lifemoney.money.coin.CoinLog;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;

@SuppressWarnings("SqlNoDataSourceInspection")
public class DBCon {

    protected static HikariDataSource dataSource;
    protected static String LOGS_COIN;

    public void initialize(@NotNull LifeMoney plugin) {
        if (!plugin.getConfig().getBoolean("Database.use", false)) return;
        LOGS_COIN = LifeMoney.getInstance().getConfig().getString("Database.table");
        setupDataSource(plugin);

        try (Connection con = dataSource.getConnection();
             Statement statement = con.createStatement()) {
            createTableIfNotExists(statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupDataSource(@NotNull LifeMoney plugin) {
        HikariConfig config = new HikariConfig();
        String host = plugin.getConfig().getString("Database.host");
        int port = plugin.getConfig().getInt("Database.port");
        String database = plugin.getConfig().getString("Database.database");
        String username = plugin.getConfig().getString("Database.username");
        String password = plugin.getConfig().getString("Database.password");
        String scheme = plugin.getConfig().getString("Database.scheme");

        config.setJdbcUrl(scheme + "://" + host + ":" + port + "/" + database);
        config.setConnectionTimeout(30000);
        config.setMaximumPoolSize(10);
        config.setUsername(username);
        config.setPassword(password);

        dataSource = new HikariDataSource(config);
    }

    private void createTableIfNotExists(@NotNull Statement statement) throws SQLException {
        ResultSet shopData = statement.executeQuery("SHOW TABLES LIKE '" + LOGS_COIN + "'");
        if (!shopData.next()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + LOGS_COIN + " (" +
                    "uuid varchar(36) NOT NULL, " +
                    "type varchar(32) NOT NULL, " +
                    "money BIGINT UNSIGNED, " +
                    "time BIGINT UNSIGNED" +
                    ");");
        }
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public double setLogsCoin(@NotNull UUID uuid, @NotNull List<CoinLog> logList) {
        try {
            try (Connection con = dataSource.getConnection()) {
                double total = 0;
                for (CoinLog log : logList) {
                    try (PreparedStatement state = con.prepareStatement("INSERT INTO " + LOGS_COIN + " VALUES (?, ?, ?, ?);")) {
                        state.setString(1, uuid.toString());
                        state.setString(2, log.moneys().name());
                        state.setDouble(3, log.coin());
                        state.setLong(4, log.time());
                        state.executeUpdate();
                        total += log.coin();
                    }
                }
                return total;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<UUID> getUUIDs() {
        try {
            try (Connection con = dataSource.getConnection()) {
                try (PreparedStatement state = con.prepareStatement("SELECT uuid FROM " + LOGS_COIN + ";")) {
                    state.executeQuery();
                    ResultSet rs = state.getResultSet();
                    Set<UUID> uuids = new HashSet<>();
                    while (rs.next()) {
                        uuids.add(UUID.fromString(rs.getString("uuid")));
                    }
                    return uuids;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<CoinLog> getLogsCoin(@NotNull UUID uuid, List<CoinLog> logList, long timeCondition, long range) {
        try {
            try (Connection con = dataSource.getConnection()) {
                try (PreparedStatement state = con.prepareStatement("SELECT * FROM " + LOGS_COIN + " WHERE uuid = ?;")) {
                    state.setString(1, uuid.toString());
                    ResultSet rs = state.executeQuery();

                    List<CoinLog> list = new ArrayList<>();
                    while (rs.next()) {
                        String type = rs.getString("type");
                        double coin = rs.getDouble("money");
                        long time = rs.getLong("time");

                        logList.forEach(it -> {
                            if (it.moneys() != Moneys.getFromName(type)) return;
                            if (range != -1) {
                                if (timeCondition - time > range) return;
                            }
                            list.add(new CoinLog(Moneys.getFromName(type), coin, time));
                        });
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearLogsCoin(@NotNull UUID uuid) {
        try {
            try (Connection con = dataSource.getConnection()) {
                try (PreparedStatement state = con.prepareStatement("DELETE FROM " + LOGS_COIN + " WHERE uuid = ?;")) {
                    state.setString(1, uuid.toString());
                    state.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
