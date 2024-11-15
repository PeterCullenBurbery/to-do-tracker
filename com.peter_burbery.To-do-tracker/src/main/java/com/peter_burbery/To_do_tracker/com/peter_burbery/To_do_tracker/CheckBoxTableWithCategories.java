//package com.peter_burbery.To_do_tracker.com.peter_burbery.To_do_tracker;
//
//import javax.swing.*;
//import javax.swing.event.TableModelEvent;
//import javax.swing.event.TableModelListener;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class CheckBoxTableWithCategories {
//
//    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521/to_do.localdomain";
//    private static final String DB_SYS_USER = "sys as sysdba"; // Connect as SYS initially
//    private static final String DB_PASSWORD = "1234";
//    private static final String TO_DO_SCHEMA = "to_do";
//
//    private static Connection connection = null; // Shared connection
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            if (initializeDatabaseConnection()) {
//                JFrame frame = new JFrame("Checkbox Table with Categories");
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                frame.setLayout(new BorderLayout());
//
//                // Add columns for "Select", "Item", "Selected", and hidden to_do_id
//                DefaultTableModel model = new DefaultTableModel(new Object[]{"Select", "Item", "Selected", "to_do_id"}, 0) {
//                    @Override
//                    public boolean isCellEditable(int row, int column) {
//                        return column == 0 || column == 1 || column == 2; // Editable columns
//                    }
//                };
//
//                JTable table = new JTable(model) {
//                    @Override
//                    public Class<?> getColumnClass(int column) {
//                        return column == 0 ? Boolean.class : (column == 2 ? Boolean.class : String.class);
//                    }
//                };
//
//                table.removeColumn(table.getColumnModel().getColumn(3)); // Hide the to_do_id column
//
//                JScrollPane scrollPane = new JScrollPane(table);
//                frame.add(scrollPane, BorderLayout.CENTER);
//
//                // Add category selection and button
//                JPanel categoryPanel = new JPanel(new FlowLayout());
//                JTextField categoryField = new JTextField(20); // New or existing category
//                JButton addCategoryButton = new JButton("Add to Category");
//
//                categoryPanel.add(new JLabel("Category:"));
//                categoryPanel.add(categoryField);
//                categoryPanel.add(addCategoryButton);
//
//                frame.add(categoryPanel, BorderLayout.NORTH);
//
//                // Load data into the table
//                loadToDoData(model);
//
//                // Add action to associate selected to-dos with a category
//                addCategoryButton.addActionListener(e -> {
//                    String categoryName = categoryField.getText().trim();
//                    if (categoryName.isEmpty()) {
//                        JOptionPane.showMessageDialog(frame, "Category name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
//                        return;
//                    }
//
//                    List<String> selectedToDoIds = getSelectedToDoIds(model);
//                    if (selectedToDoIds.isEmpty()) {
//                        JOptionPane.showMessageDialog(frame, "No to-dos selected.", "Selection Error", JOptionPane.ERROR_MESSAGE);
//                        return;
//                    }
//
//                    String categoryId = getOrCreateCategory(categoryName);
//                    if (categoryId != null) {
//                        associateToDosWithCategory(selectedToDoIds, categoryId);
//                    }
//                });
//
//                frame.setSize(600, 400);
//                frame.setVisible(true);
//            } else {
//                JOptionPane.showMessageDialog(null, "Failed to connect to the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
//                System.exit(1);
//            }
//        });
//    }
//
//    private static boolean initializeDatabaseConnection() {
//        try {
//            connection = DriverManager.getConnection(DB_URL, DB_SYS_USER, DB_PASSWORD);
//            try (PreparedStatement alterSessionStmt = connection.prepareStatement("ALTER SESSION SET CURRENT_SCHEMA = " + TO_DO_SCHEMA)) {
//                alterSessionStmt.execute();
//                System.out.println("Session altered to schema: " + TO_DO_SCHEMA);
//            }
//            return true;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    private static void loadToDoData(DefaultTableModel model) {
//        String query = "SELECT to_do, done, to_do_id FROM to_do";
//        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
//            while (rs.next()) {
//                model.addRow(new Object[]{false, rs.getString("to_do"), rs.getInt("done") == 1, rs.getString("to_do_id")});
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(null, "Error loading data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    private static List<String> getSelectedToDoIds(DefaultTableModel model) {
//        List<String> selectedIds = new ArrayList<>();
//        for (int i = 0; i < model.getRowCount(); i++) {
//            if ((Boolean) model.getValueAt(i, 0)) {
//                selectedIds.add((String) model.getValueAt(i, 3)); // Get hidden to_do_id
//            }
//        }
//        return selectedIds;
//    }
//
//    private static String getOrCreateCategory(String categoryName) {
//        String selectSQL = "SELECT category_of_to_do_id FROM category_of_to_do WHERE category_of_to_do = ?";
//        String insertSQL = "INSERT INTO category_of_to_do (category_of_to_do) VALUES (?) RETURNING category_of_to_do_id INTO ?";
//        try {
//            // Check if category exists
//            try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
//                selectStmt.setString(1, categoryName);
//                try (ResultSet rs = selectStmt.executeQuery()) {
//                    if (rs.next()) {
//                        return rs.getString(1); // Return existing category ID
//                    }
//                }
//            }
//
//            // If not, create new category
//            try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL, new String[]{"category_of_to_do_id"})) {
//                insertStmt.setString(1, categoryName);
//                insertStmt.executeUpdate();
//                try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
//                    if (generatedKeys.next()) {
//                        return generatedKeys.getString(1);
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(null, "Error creating category: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
//        }
//        return null;
//    }
//
//    private static void associateToDosWithCategory(List<String> toDoIds, String categoryId) {
//        String insertSQL = "INSERT INTO to_do_many_to_many (to_do_id, category_of_to_do_id) VALUES (?, ?)";
//        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
//            for (String toDoId : toDoIds) {
//                pstmt.setString(1, toDoId);
//                pstmt.setString(2, categoryId);
//                pstmt.addBatch();
//            }
//            pstmt.executeBatch();
//            JOptionPane.showMessageDialog(null, "To-dos successfully associated with category.", "Success", JOptionPane.INFORMATION_MESSAGE);
//        } catch (SQLException e) {
//            e.printStackTrace();
//            JOptionPane.showMessageDialog(null, "Error associating to-dos with category: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//}
package com.peter_burbery.To_do_tracker.com.peter_burbery.To_do_tracker;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CheckBoxTableWithCategories {

	private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521/to_do.localdomain";
	private static final String DB_SYS_USER = "sys as sysdba"; // Connect as SYS initially
	private static final String DB_PASSWORD = "1234";
	private static final String TO_DO_SCHEMA = "to_do";

	private static Connection connection = null; // Shared connection

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			if (initializeDatabaseConnection()) {
				JFrame frame = new JFrame("Checkbox Table with Categories");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLayout(new BorderLayout());

				// Add columns for "Select", "Item", "Done", and hidden to_do_id
				DefaultTableModel model = new DefaultTableModel(new Object[] { "Select", "Item", "Done", "to_do_id" },
						0) {
					@Override
					public boolean isCellEditable(int row, int column) {
						return column != 3; // Disable editing of the hidden to_do_id column
					}
				};

				JTable table = new JTable(model) {
					@Override
					public Class<?> getColumnClass(int column) {
						return column == 0 || column == 2 ? Boolean.class : String.class;
					}
				};

				table.removeColumn(table.getColumnModel().getColumn(3)); // Hide the to_do_id column

				JScrollPane scrollPane = new JScrollPane(table);
				frame.add(scrollPane, BorderLayout.CENTER);

				// Buttons to Add and Remove Rows
				JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
				JButton addButton = new JButton("Add Row");
				JButton removeButton = new JButton("Remove Row");

				addButton.addActionListener(e -> {
					String newItem = "New Item"; // Default new item
					String newToDoId = addToDatabase(newItem, 0); // Insert and get the generated to_do_id
					if (newToDoId != null) {
						model.addRow(new Object[] { false, newItem, false, newToDoId });
					}
				});

				removeButton.addActionListener(e -> {
					int selectedRow = table.getSelectedRow();
					if (selectedRow != -1) {
						String toDoId = (String) model.getValueAt(selectedRow, 3); // Get the hidden to_do_id
						model.removeRow(selectedRow);
						deleteFromDatabase(toDoId);
					} else {
						JOptionPane.showMessageDialog(frame, "No row selected!", "Error", JOptionPane.WARNING_MESSAGE);
					}
				});

				buttonPanel.add(addButton);
				buttonPanel.add(removeButton);
				frame.add(buttonPanel, BorderLayout.SOUTH);

				// Panel for Category Management
				JPanel categoryPanel = new JPanel(new FlowLayout());
				JTextField categoryField = new JTextField(20); // Input for category name
				JButton addCategoryButton = new JButton("Add to Category");

				categoryPanel.add(new JLabel("Category:"));
				categoryPanel.add(categoryField);
				categoryPanel.add(addCategoryButton);
				frame.add(categoryPanel, BorderLayout.NORTH);

				// Load data into the table
				loadToDoData(model);

				// Listener for updates to the "Done" status
				model.addTableModelListener(e -> {
					if (e.getType() == TableModelEvent.UPDATE) {
						int row = e.getFirstRow();
						if (e.getColumn() == 2) { // Done column
							String toDoId = (String) model.getValueAt(row, 3);
							boolean isSelected = (Boolean) model.getValueAt(row, 2);
							updateToDoDoneStatus(toDoId, isSelected ? 1 : 0);
						}
					}
				});
				model.addTableModelListener(e -> {
					if (e.getType() == TableModelEvent.UPDATE) {
						int row = e.getFirstRow();
						int column = e.getColumn();

						if (column == 1) { // Item column
							String updatedItem = (String) model.getValueAt(row, 1);
							String toDoId = (String) model.getValueAt(row, 3); // Hidden to_do_id
							updateToDoItem(toDoId, updatedItem);
						}
					}
				});

				// Action to associate selected to-dos with a category
				addCategoryButton.addActionListener(e -> {
					String categoryName = categoryField.getText().trim();
					if (categoryName.isEmpty()) {
						JOptionPane.showMessageDialog(frame, "Category name cannot be empty.", "Input Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}

					List<String> selectedToDoIds = getSelectedToDoIds(model);
					if (selectedToDoIds.isEmpty()) {
						JOptionPane.showMessageDialog(frame, "No to-dos selected.", "Selection Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}

					String categoryId = getOrCreateCategory(categoryName);
					if (categoryId != null) {
						associateToDosWithCategory(selectedToDoIds, categoryId);
					}
				});

				frame.setSize(600, 400);
				frame.setVisible(true);
			} else {
				JOptionPane.showMessageDialog(null, "Failed to connect to the database.", "Database Error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(1);
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

	private static void loadToDoData(DefaultTableModel model) {
		String query = "SELECT to_do, done, to_do_id FROM to_do";
		try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
			while (rs.next()) {
				model.addRow(new Object[] { false, rs.getString("to_do"), rs.getInt("done") == 1,
						rs.getString("to_do_id") });
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error loading data: " + e.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static List<String> getSelectedToDoIds(DefaultTableModel model) {
		List<String> selectedIds = new ArrayList<>();
		for (int i = 0; i < model.getRowCount(); i++) {
			if ((Boolean) model.getValueAt(i, 0)) {
				selectedIds.add((String) model.getValueAt(i, 3)); // Get hidden to_do_id
			}
		}
		return selectedIds;
	}

	private static void updateToDoItem(String toDoId, String updatedItem) {
		String updateSQL = "UPDATE to_do SET to_do = ? WHERE to_do_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
			pstmt.setString(1, updatedItem);
			pstmt.setString(2, toDoId);
			pstmt.executeUpdate();
			System.out.println("Updated to_do item for to_do_id: " + toDoId);
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error updating to_do item: " + e.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static String addToDatabase(String toDo, int done) {
		String insertSQL = "INSERT INTO to_do (to_do, done) VALUES (?, ?)"; // No RETURNING clause here
		try (PreparedStatement pstmt = connection.prepareStatement(insertSQL, new String[] { "to_do_id" })) {
			pstmt.setString(1, toDo);
			pstmt.setInt(2, done);

			// Execute and fetch the generated keys
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
			JOptionPane.showMessageDialog(null, "Error inserting into database: " + ex.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	private static void updateToDoDoneStatus(String toDoId, int done) {
		String updateSQL = "UPDATE to_do SET done = ? WHERE to_do_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
			pstmt.setInt(1, done);
			pstmt.setString(2, toDoId);
			pstmt.executeUpdate();
			System.out.println("Updated done status for to_do_id: " + toDoId);
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error updating done status: " + e.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static void deleteFromDatabase(String toDoId) {
		String deleteSQL = "DELETE FROM to_do WHERE to_do_id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(deleteSQL)) {
			pstmt.setString(1, toDoId);
			pstmt.executeUpdate();
			System.out.println("Deleted to_do_id: " + toDoId);
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error deleting from database: " + e.getMessage(), "Database Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

//	private static String getOrCreateCategory(String categoryName) {
//		String selectSQL = "SELECT category_of_to_do_id FROM category_of_to_do WHERE category_of_to_do = ?";
//		String insertSQL = "INSERT INTO category_of_to_do (category_of_to_do) VALUES (?) RETURNING category_of_to_do_id INTO ?";
//		try {
//			// Check if category exists
//			try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
//				selectStmt.setString(1, categoryName);
//				try (ResultSet rs = selectStmt.executeQuery()) {
//					if (rs.next()) {
//						return rs.getString(1);
//					}
//				}
//			}
//
//			// Create new category if not exists
//			try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL,
//					new String[] { "category_of_to_do_id" })) {
//				insertStmt.setString(1, categoryName);
//				insertStmt.executeUpdate();
//
//				try (ResultSet rs = insertStmt.getGeneratedKeys()) {
//					if (rs.next()) {
//						return rs.getString(1);
//					}
//				}
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//			JOptionPane.showMessageDialog(null, "Error creating category: " + e.getMessage(), "Database Error",
//					JOptionPane.ERROR_MESSAGE);
//		}
//		return null;
//	}
	private static String getOrCreateCategory(String categoryName) {
	    String selectSQL = "SELECT category_of_to_do_id FROM category_of_to_do WHERE category_of_to_do = ?";
	    String insertSQL = "INSERT INTO category_of_to_do (category_of_to_do) VALUES (?)";
	    
	    try {
	        // Check if the category already exists
	        try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
	            selectStmt.setString(1, categoryName);
	            try (ResultSet rs = selectStmt.executeQuery()) {
	                if (rs.next()) {
	                    return rs.getString(1); // Return the existing category ID
	                }
	            }
	        }

	        // If not, create a new category and retrieve its ID
	        try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL, new String[]{"category_of_to_do_id"})) {
	            insertStmt.setString(1, categoryName);
	            insertStmt.executeUpdate();

	            try (ResultSet rs = insertStmt.getGeneratedKeys()) {
	                if (rs.next()) {
	                    return rs.getString(1); // Return the new category ID
	                }
	            }
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        JOptionPane.showMessageDialog(null, "Error creating category: " + e.getMessage(),
	                "Database Error", JOptionPane.ERROR_MESSAGE);
	    }
	    return null;
	}

	private static void associateToDosWithCategory(List<String> toDoIds, String categoryId) {
		String insertSQL = "INSERT INTO to_do_many_to_many (to_do_id, category_of_to_do_id) VALUES (?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
			for (String toDoId : toDoIds) {
				pstmt.setString(1, toDoId);
				pstmt.setString(2, categoryId);
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			JOptionPane.showMessageDialog(null, "To-dos successfully associated with category.", "Success",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error associating to-dos with category: " + e.getMessage(),
					"Database Error", JOptionPane.ERROR_MESSAGE);
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
