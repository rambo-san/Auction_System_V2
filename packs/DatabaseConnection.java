package packs;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/auction_db?characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    // Static block to register JDBC driver
    static {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found. Ensure the JDBC driver is in the classpath.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
