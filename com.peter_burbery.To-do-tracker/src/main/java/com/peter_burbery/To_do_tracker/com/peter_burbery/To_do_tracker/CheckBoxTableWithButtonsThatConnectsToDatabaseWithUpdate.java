
package com.peter_burbery.To_do_tracker.com.peter_burbery.To_do_tracker;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class CheckBoxTableWithButtonsThatConnectsToDatabaseWithUpdate {

	private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521/to_do.localdomain";
	private static final String DB_SYS_USER = "sys as sysdba"; // Connect as SYS initially
	private static final String DB_PASSWORD = "1234";
	private static final String TO_DO_SCHEMA = "to_do";

	private static Connection connection = null; // Shared connection

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			if (initializeDatabaseConnection()) {
				JFrame frame = new JFrame("Checkbox Table with Add/Remove");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLayout(new BorderLayout());

				// Add a hidden column for storing to_do_id (RAW(16)) along with Item and
				// Selected
				DefaultTableModel model = new DefaultTableModel(new Object[] { "Item", "Selected", "to_do_id" }, 0) {
					@Override
					public boolean isCellEditable(int row, int column) {
						return column != 2; // Disable editing of the hidden to_do_id column
					}
				};

				JTable table = new JTable(model) {
					@Override
					public Class<?> getColumnClass(int column) {
						return column == 1 ? Boolean.class : String.class;
					}
				};

				table.removeColumn(table.getColumnModel().getColumn(2)); // Hide to_do_id column from view

				JScrollPane scrollPane = new JScrollPane(table);
				frame.add(scrollPane, BorderLayout.CENTER);

				JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
				JButton addButton = new JButton("Add Row");
				JButton removeButton = new JButton("Remove Row");

				addButton.addActionListener(e -> {
					String newItem = "New Item"; // Default new item
					String newToDoId = addToDatabase(newItem, 0); // Get the generated to_do_id from DB
					if (newToDoId != null) {
						model.addRow(new Object[] { newItem, Boolean.FALSE, newToDoId });
					}
				});

				removeButton.addActionListener((ActionEvent e) -> {
					int selectedRow = table.getSelectedRow();
					if (selectedRow != -1) {
						String toDoId = (String) model.getValueAt(selectedRow, 2); // Get the to_do_id of the row
						model.removeRow(selectedRow);
						deleteFromDatabase(toDoId);
					} else {
						JOptionPane.showMessageDialog(frame, "No row selected!", "Error", JOptionPane.WARNING_MESSAGE);
					}
				});

				// Listener to update the database when a row is edited
				model.addTableModelListener(new TableModelListener() {
					@Override
					public void tableChanged(TableModelEvent e) {
						if (e.getType() == TableModelEvent.UPDATE) {
							int row = e.getFirstRow();
							String updatedItem = (String) model.getValueAt(row, 0);
							boolean isSelected = (Boolean) model.getValueAt(row, 1);
							String toDoId = (String) model.getValueAt(row, 2); // Get to_do_id
							updateDatabase(toDoId, updatedItem, isSelected ? 1 : 0);
						}
					}
				});

				buttonPanel.add(addButton);
				buttonPanel.add(removeButton);

				frame.add(buttonPanel, BorderLayout.SOUTH);
				frame.setSize(500, 300);
				frame.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(null, "Failed to connect to the database.", "Database Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(1); // Exit if the connection fails
			}
		});
	}

	private static boolean initializeDatabaseConnection() {
		try {
			connection = DriverManager.getConnection(DB_URL, DB_SYS_USER, DB_PASSWORD);
			try (PreparedStatement alterSessionStmt = connection
					.prepareStatement("ALTER SESSION SET CURRENT_SCHEMA = " + TO_DO_SCHEMA)) {
				alterSessionStmt.execute();
				System.out.println("Session altered to schema: " + TO_DO_SCHEMA);
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static String addToDatabase(String toDo, int done) {
	    String insertSQL = "INSERT INTO to_do (to_do, done) VALUES (?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(insertSQL, new String[]{"to_do_id"})) {
	        pstmt.setString(1, toDo);
	        pstmt.setInt(2, done);

	        // Execute the insert and get the generated keys
	        pstmt.executeUpdate();
	        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                return generatedKeys.getString(1); // Return the generated to_do_id
	            } else {
	                throw new SQLException("Inserting to_do failed, no ID obtained.");
	            }
	        }
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	        JOptionPane.showMessageDialog(null, "Error inserting into database: " + ex.getMessage(),
	                "Database Error", JOptionPane.ERROR_MESSAGE);
	        return null;
	    }
	}


	private static void updateDatabase(String toDoId, String updatedToDo, int done) {
		String updateSQL = "UPDATE to_do SET to_do = ?, done = ? WHERE to_do_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
			pstmt.setString(1, updatedToDo);
			pstmt.setInt(2, done);
			pstmt.setString(3, toDoId);
			pstmt.executeUpdate();
			System.out.println("Data updated successfully for to_do_id: " + toDoId);
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error updating database: " + ex.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void deleteFromDatabase(String toDoId) {
		String deleteSQL = "DELETE FROM to_do WHERE to_do_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
			pstmt.setString(1, toDoId);
			pstmt.executeUpdate();
			System.out.println("Data deleted successfully for to_do_id: " + toDoId);
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error deleting from database: " + ex.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

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
