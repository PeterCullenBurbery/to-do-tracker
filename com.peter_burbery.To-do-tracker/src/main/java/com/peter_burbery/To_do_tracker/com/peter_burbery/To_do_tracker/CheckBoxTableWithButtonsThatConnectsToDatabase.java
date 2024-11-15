package com.peter_burbery.To_do_tracker.com.peter_burbery.To_do_tracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CheckBoxTableWithButtonsThatConnectsToDatabase {

    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521/to_do.localdomain";
    private static final String DB_SYS_USER = "sys as sysdba"; // Connect as SYS initially
    private static final String DB_PASSWORD = "1234";
    private static final String TO_DO_SCHEMA = "to_do";

    private static Connection connection = null; // Shared connection

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Establish database connection before starting the GUI
            if (initializeDatabaseConnection()) {
                JFrame frame = new JFrame("Checkbox Table with Add/Remove");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());

                DefaultTableModel model = new DefaultTableModel(new Object[]{"Item", "Selected"}, 0);
                JTable table = new JTable(model) {
                    @Override
                    public Class<?> getColumnClass(int column) {
                        return column == 1 ? Boolean.class : String.class;
                    }
                };

                JScrollPane scrollPane = new JScrollPane(table);
                frame.add(scrollPane, BorderLayout.CENTER);

                JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
                JButton addButton = new JButton("Add Row");
                JButton removeButton = new JButton("Remove Row");

                addButton.addActionListener(e -> {
                    String newItem = "New Item"; // Default new item
                    model.addRow(new Object[]{newItem, Boolean.FALSE});
                    addToDatabase(newItem, 0);
                });

                removeButton.addActionListener((ActionEvent e) -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        model.removeRow(selectedRow);
                    } else {
                        JOptionPane.showMessageDialog(frame, "No row selected!", "Error", JOptionPane.WARNING_MESSAGE);
                    }
                });

                buttonPanel.add(addButton);
                buttonPanel.add(removeButton);

                frame.add(buttonPanel, BorderLayout.SOUTH);
                frame.setSize(500, 300);
                frame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Failed to connect to the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Exit if the connection fails
            }
        });
    }

    private static boolean initializeDatabaseConnection() {
        try {
            // Connect as SYS and alter session to TO_DO schema
            connection = DriverManager.getConnection(DB_URL, DB_SYS_USER, DB_PASSWORD);
            try (PreparedStatement alterSessionStmt = connection.prepareStatement("ALTER SESSION SET CURRENT_SCHEMA = " + TO_DO_SCHEMA)) {
                alterSessionStmt.execute();
                System.out.println("Session altered to schema: " + TO_DO_SCHEMA);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void addToDatabase(String toDo, int done) {
        String insertSQL = "INSERT INTO to_do (to_do, done) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, toDo);
            pstmt.setInt(2, done);
            pstmt.executeUpdate();
            System.out.println("Data inserted successfully.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error inserting into database: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Ensure the connection is closed properly on exit
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Database connection closed.");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}
