package com.peter_burbery.To_do_tracker.com.peter_burbery.To_do_tracker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class CheckBoxTable {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Checkbox Table");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            DefaultTableModel model = new DefaultTableModel(0, 2); // Start with 2 columns
            JTable table = new JTable(model) {
                @Override
                public Class<?> getColumnClass(int column) {
                    return column == 1 ? Boolean.class : String.class;
                }
            };
            model.addRow(new Object[]{"Row 1", Boolean.FALSE});

            table.setPreferredScrollableViewportSize(new Dimension(500, 300));
            table.setFillsViewportHeight(true);

            JScrollPane scrollPane = new JScrollPane(table);
            frame.add(scrollPane, BorderLayout.CENTER);

            JButton addButton = new JButton("Add Row");
            addButton.addActionListener(e -> model.addRow(new Object[]{"New Row", Boolean.FALSE}));
            frame.add(addButton, BorderLayout.SOUTH);

            frame.pack();
            frame.setVisible(true);
        });
    }
}

