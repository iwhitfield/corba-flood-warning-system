package com.zackehh.floodz.common.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.util.Vector;

/**
 * A simple class to represent a list of bids associated with
 * a given lot. This allows a specific styling of table which
 * can be inherited to stay constant throughout the application.
 */
public class BaseTable extends JTable {

    /**
     * Initializes a new table using Vectors.
     *
     * @param data          the data Vector
     * @param columns       the columns Vector
     */
    public BaseTable(Vector<Vector<String>> data, Vector<String> columns){
        setModel(new UneditableTableModel(data, columns));
        init();
    }

    /**
     * Initializes a new table using Object Arrays.
     *
     * @param data          the data Array
     * @param columns       the columns Array
     */
    public BaseTable(Object[][] data, Object[] columns){
        setModel(new UneditableTableModel(data, columns));
        init();
    }

    /**
     * The main initialization of the BaseTable. This is
     * abstracted out to its own method in order to handle
     * multiple constructors with different models.
     */
    private void init(){
        setShowHorizontalLines(true);
        setRowSelectionAllowed(true);
        setDefaultRenderer(String.class, new DefaultTableCellRenderer() {{
            setHorizontalAlignment(JLabel.CENTER);
        }});

        JTableHeader tableHeader = getTableHeader();

        tableHeader.setReorderingAllowed(false);
        tableHeader.setResizingAllowed(false);

        DefaultTableCellRenderer renderer =
                (DefaultTableCellRenderer) tableHeader.getDefaultRenderer();

        renderer.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Extremely simply table model to remove the ability to
     * edit the table, and the marks all fields as a String.
     */
    private class UneditableTableModel extends DefaultTableModel {

        /**
         * Accept Vector input.
         *
         * @param data          the data Vector
         * @param columns       the columns Vector
         */
        public UneditableTableModel(Vector<Vector<String>> data, Vector<String> columns){
            super(data, columns);
        }

        /**
         * Accept Array input.
         *
         * @param data          the data Array
         * @param columns       the columns Array
         */
        public UneditableTableModel(Object[][] data, Object[] columns){
            super(data, columns);
        }

        /**
         * Overrides isCellEditable to always return false.
         * This stops the user from modifying any table instances.
         *
         * @param  row          the table row
         * @param  column       the table column
         * @return false
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        /**
         * Flags all columns as String types in the table.
         *
         * @param  column       the table column
         * @return String.class
         */
        @Override
        public Class getColumnClass(int column) {
            return String.class;
        }

    }

}