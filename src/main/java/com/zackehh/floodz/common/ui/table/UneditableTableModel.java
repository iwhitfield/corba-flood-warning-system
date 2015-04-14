package com.zackehh.floodz.common.ui.table;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;

/**
 * Extremely simply table model to remove the ability to
 * edit the table, and the marks all fields as a String.
 */
public class UneditableTableModel extends DefaultTableModel {

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