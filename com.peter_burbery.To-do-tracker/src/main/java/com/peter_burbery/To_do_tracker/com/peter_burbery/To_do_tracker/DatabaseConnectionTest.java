package com.peter_burbery.To_do_tracker.com.peter_burbery.To_do_tracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionTest {

    // Update these values with your database information
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521/to_do.localdomain";
    private static final String DB_USER = "sys as sysdba";  // Using SYS as SYSDBA
    private static final String DB_PASSWORD = "1234";

    public static void main(String[] args) {
        Connection connection = null;

        try {
            // Attempt to establish a connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connection successful!");

        } catch (SQLException e) {
            // Handle exceptions and print error details
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the connection if it was opened
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Connection closed.");
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}
