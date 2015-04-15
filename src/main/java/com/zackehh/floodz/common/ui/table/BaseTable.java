package com.zackehh.floodz.common.ui.table;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public BaseTable(Object[][] data, Object[] columns){
        setModel(new UneditableTableModel(data, columns));
        init();
    }

    /**
     * Initializes a new table a custom Table Model.
     *
     * @param model         the table model
     */
    public BaseTable(TableModel model){
        setModel(model);
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

}