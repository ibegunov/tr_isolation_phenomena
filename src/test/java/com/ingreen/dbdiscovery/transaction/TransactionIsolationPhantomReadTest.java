package com.ingreen.dbdiscovery.transaction;

import lombok.extern.log4j.Log4j;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class TransactionIsolationPhantomReadTest {

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
            conn.commit();

            //
            try (var conn2 = getConnection()) {
                conn2.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//                conn2.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                try (Statement stmt = conn2.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT * FROM test WHERE value > 99");
                    log.warn("Records found: " + rs.next());
                }

                update("INSERT INTO test (id, value) VALUES (1, 100)", conn);
                conn.commit();

                try (Statement stmt = conn2.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT * FROM test WHERE value > 99");
                    log.warn("Records found: " + rs.next());
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
