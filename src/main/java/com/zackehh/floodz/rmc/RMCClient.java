package com.zackehh.floodz.rmc;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;
import com.zackehh.floodz.common.ui.InterfaceUtils;
import com.zackehh.floodz.util.SQLiteClient;
import com.zackehh.floodz.common.ui.OptionsPanel;
import com.zackehh.floodz.common.ui.table.BaseTable;
import com.zackehh.floodz.common.ui.table.RMCTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * The main GUI client of the application. Used to view
 * existing alerts, and to view connected LMS instances.
 * Also provides a neat way to view a map of the region in
 * tree form.
 */
public class RMCClient extends JFrame {

    /**
     * An instance of the RMCDriver.
     */
    private static RMCDriver rmcDriver;

    /**
     * The table model of the main view panel.
     */
    private final RMCTableModel rmcTableModel;

    /**
     * A reference to the SQLite singleton.
     */
    private final SQLiteClient sqLiteClient;

    /**
     * Main entry point of the RMC GUI. Initializes the new window and
     * runs the RMCDriver ORB inside a new thread.
     *
     * @param args the program arguments
     */
    public static void main(String[] args) throws SQLException {
        // initialize the GUI
        new RMCClient(args);

        // start the initial loading of the existing items
        new Thread(new Runnable() {
            public void run() {
                rmcDriver.getEmbeddedOrb().run();
            }
        }).start();
    }

    /**
     * Constructor to draw the initial frame whilst setting up the
     * required instances.
     *
     * @param args the program arguments
     */
    private RMCClient(String[] args) {
        // get a reference to the SQLite singleton
        sqLiteClient = SQLiteClient.getInstance();

        // create a new RMCDriver instance
        rmcDriver = new RMCDriver(args, this);

        // create the initial table model
        rmcTableModel = new RMCTableModel(sqLiteClient, new Vector<Vector<String>>(), new Vector<String>(){{
            add("LMS ID");
            add("Time");
            add("Reporting Zone/Sensor");
            add("Received Level");
        }});

        // set the application title
        setTitle("YoloSwagz");

        // exit on a button press
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });

        // set the container borderlayout
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        // setup the interface via the utils
        InterfaceUtils.setup(cp);

        // create a panel for the main frame
        JPanel mainLayout = new JPanel(new BorderLayout());

        // add components to the main frame
        mainLayout.add(new OptionsPanel(rmcDriver), BorderLayout.NORTH);
        mainLayout.add(new OverviewCard(), BorderLayout.SOUTH);

        // add the frame to the container
        cp.add(mainLayout);

        // pack the ui
        pack();

        // get the screen size as a java dimension
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // make the width a 1/4 of the screen
        setPreferredSize(new Dimension(screenSize.width * 2 / 4, getSize().height));

        // unfortunate re-pack
        pack();

        // disable resize
        setResizable(false);

        // load any existing alerts
        loadExistingAlerts();

        // display!
        setVisible(true);
    }

    /**
     * Adds an alert to the table model.
     *
     * @param alert the alert to add
     */
    public void addAlert(Alert alert){
        rmcTableModel.addAlert(alert);
    }

    /**
     * Removes an alert from the table model.
     *
     * @param metadata the alert meta data
     */
    public void cancelAlert(MetaData metadata){
        rmcTableModel.removeAlert(metadata);
    }

    /**
     * Retrieves a list of existing alerts from the SQLite
     * DB locally. Used for persistence and initial loading
     * when opening the application.
     */
    private void loadExistingAlerts(){
        // load all alerts from the DB
        List<Alert> oldAlerts = sqLiteClient.retrieveAllAlerts();
        // for each alert in the list
        for(Alert oldAlert : oldAlerts){
            // add it to the table
            rmcTableModel.addAlert(oldAlert, false);
        }
    }

    /**
     * The main card containing the scrollable table to display at the
     * bottom of the main frame.
     */
    private class OverviewCard extends JPanel {

        /**
         * Creates a new table, placed inside a JScrollPane in order
         * to enable an 'infinite' list of Alerts.
         */
        public OverviewCard(){
            // border layout to use full window
            setLayout(new BorderLayout());

            // add a base table to a scrolling pane
            JScrollPane itemListPanel = new JScrollPane(
                    new BaseTable(rmcTableModel),
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );

            // add the scrolling pane to the main panel
            add(itemListPanel, BorderLayout.SOUTH);
        }

    }
}
