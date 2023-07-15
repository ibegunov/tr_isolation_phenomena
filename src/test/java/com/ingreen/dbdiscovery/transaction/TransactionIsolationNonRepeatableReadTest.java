package com.ingreen.dbdiscovery.transaction;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class TransactionIsolationNonRepeatableReadTest {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Test
    public void testDirtyRead() throws SQLException {
        try (var conn = getConnection()) {
            // Create a new table
            update("DROP TABLE IF EXISTS test", conn);
            update("CREATE TABLE test (id INT PRIMARY KEY, value INT)", conn);
            update("INSERT INTO test (id, value) VALUES (1, 100)", conn);
            conn.commit();

            //
            try (var conn2 = getConnection()) {
                conn2.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//                conn2.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                try (Statement stmt = conn2.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT value FROM test WHERE id = 1");
                    rs.next();
                    log.warn("Records found: " + rs.getString("value"));
                }

                update("UPDATE test SET value = 200 WHERE id = 1", conn);
                conn.commit();

                try (Statement stmt = conn2.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT value FROM test WHERE id = 1");
                    rs.next();
                    log.warn("Records found: " + rs.getString("value"));
                }
            }

            update("DROP table test", conn);
            conn.commit();
        }
    }

    private Connection getConnection() throws SQLException {
        var conn =  DriverManager.getConnection(url, username, password);
        conn.setAutoCommit(false);
        return conn;
    }

    private void update(String sql, Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

}
