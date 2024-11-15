package com.peter_burbery.To_do_tracker.com.peter_burbery.To_do_tracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestToDoUserSession {

    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521/to_do.localdomain";
    private static final String DB_SYS_USER = "sys as sysdba";
    private static final String DB_PASSWORD = "1234";
    private static final String TO_DO_SCHEMA = "to_do";

    public static void main(String[] args) {
        String alterSessionSQL = "ALTER SESSION SET CURRENT_SCHEMA = " + TO_DO_SCHEMA;
        String countQuerySQL = "SELECT COUNT(*) FROM to_do";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_SYS_USER, DB_PASSWORD);
             PreparedStatement alterSessionStmt = conn.prepareStatement(alterSessionSQL);
             PreparedStatement countStmt = conn.prepareStatement(countQuerySQL)) {

            // Alter session to switch to 'to_do' schema
            alterSessionStmt.execute();
            System.out.println("Session altered to schema: " + TO_DO_SCHEMA);

            // Execute the count query
            ResultSet rs = countStmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Total rows in to_do table: " + count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

