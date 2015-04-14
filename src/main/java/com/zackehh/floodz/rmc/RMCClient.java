package com.zackehh.floodz.rmc;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;
import com.zackehh.floodz.common.SQLiteClient;
import com.zackehh.floodz.common.ui.table.BaseTable;
import com.zackehh.floodz.common.ui.table.RMCTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class RMCClient extends JFrame {

    private static RMCDriver rmcDriver;
    private static RMCTableModel rmcTableModel;

    private final SQLiteClient sqLiteClient;

    private Vector<Vector<String>> messageList = new Vector<>();

    public static void main(String[] args) throws SQLException {
        RMCClient rmc = new RMCClient();

        rmcDriver = new RMCDriver(args, rmc);

        // Start the initial loading of the existing items
        new Thread(new Runnable() {
            public void run() {
                rmcDriver.getEmbeddedOrb().run();
            }
        }).start();
    }

    public RMCClient() throws SQLException {
        // Set up variables
        sqLiteClient = new SQLiteClient();

        // Set the application title as well as the username
        setTitle("YoloSwagz");

        // Exit on the exit button press
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });

        // Set the container BorderLayout
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        // Create a new card layout
        JPanel cards = new JPanel(new CardLayout());

        // Add the card to the CardLayout
        cards.add(new OverviewCard(), "Overview");

        // Add the CardLayout to the Container
        cp.add(cards);

        // Pack the UI and set the frame
        pack();

        // Get the screen size as a java dimension
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Make the width a 1/4 of the screen
        setPreferredSize(new Dimension(screenSize.width * 2 / 4, getSize().height));

        // Unfortunate re-pack
        pack();

        // Disable resize
        setResizable(false);

        // Load any existing alerts
        loadExistingAlerts();

        // Display!
        setVisible(true);
    }

    public void addAlert(Alert alert){
        rmcTableModel.addAlert(alert);
    }

    public void cancelAlert(MetaData metadata){
        rmcTableModel.removeAlert(metadata);
    }

    private void loadExistingAlerts(){
        List<Alert> oldAlerts = sqLiteClient.retrieveAllAlerts();

        for(Alert oldAlert : oldAlerts){
            rmcTableModel.addAlert(oldAlert, false);
        }
    }

    private class OverviewCard extends JPanel {

        public OverviewCard(){
            // Border layout to use full window
            setLayout(new BorderLayout());

            // Set the table model
            rmcTableModel = new RMCTableModel(sqLiteClient, messageList, new Vector<String>(){{
                add("LMS ID");
                add("Time");
                add("Reporting Zone/Sensor");
                add("Received Level");
            }});

            // Create an initial base table with the given column names
            BaseTable lotTable = new BaseTable(rmcTableModel);

            // Add the table to a scrolling pane
            JScrollPane itemListPanel = new JScrollPane(
                    lotTable,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );

            // Add the scrolling pane to the main panel
            add(itemListPanel, BorderLayout.SOUTH);
        }

    }
}
