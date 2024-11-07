package com.peter_burbery.To_do_tracker.com.peter_burbery.To_do_tracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CheckBoxTableWithButtons {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Checkbox Table with Add/Remove");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            // Table model with 2 columns (String and Boolean for CheckBox)
            DefaultTableModel model = new DefaultTableModel(new Object[]{"Item", "Selected"}, 0);
            JTable table = new JTable(model) {
                @Override
                public Class<?> getColumnClass(int column) {
                    return column == 1 ? Boolean.class : String.class;
                }
            };

            // Add the table inside a scroll pane
            JScrollPane scrollPane = new JScrollPane(table);
            frame.add(scrollPane, BorderLayout.CENTER);

            // Button Panel for Add/Remove buttons
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
            JButton addButton = new JButton("Add Row");
            JButton removeButton = new JButton("Remove Row");

            // Add row functionality
            addButton.addActionListener(e -> model.addRow(new Object[]{"New Item", Boolean.FALSE}));

            // Remove row functionality
            removeButton.addActionListener((ActionEvent e) -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {  // Ensure a row is selected
                    model.removeRow(selectedRow);
                } else {
                    JOptionPane.showMessageDialog(frame, "No row selected!", "Error", JOptionPane.WARNING_MESSAGE);
                }
            });

            // Add buttons to the button panel
            buttonPanel.add(addButton);
            buttonPanel.add(removeButton);

            // Add the button panel at the bottom of the frame
            frame.add(buttonPanel, BorderLayout.SOUTH);

            frame.setSize(500, 300);
            frame.setVisible(true);
        });
    }
}

