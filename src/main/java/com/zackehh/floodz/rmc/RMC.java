package com.zackehh.floodz.rmc;

import com.zackehh.corba.common.Alert;
import com.zackehh.floodz.common.ui.BaseTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class RMC extends JFrame {

    private BaseTable lotTable;
    private JScrollPane itemListPanel;
    private OverviewCard overviewCard;

    private static RMCDriver rmcDriver;
    private static SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Vector<Vector<String>> messageList = new Vector<>();

    public static void main(String[] args) {
        RMC rmc = new RMC();

        rmcDriver = new RMCDriver(args, rmc);

        // Start the initial loading of the existing items
        new Thread(new Runnable() {
            public void run() {
                rmcDriver.getEmbeddedOrb().run();
            }
        }).start();
    }

    public RMC(){
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

        overviewCard = new OverviewCard();

        // Add the card to the CardLayout
        cards.add(overviewCard, "Overview");

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

        // Display!
        setVisible(true);
    }

    public void addAlert(Alert alert){
        Date d = new Date(alert.reading.time);

        overviewCard.getTableModel().addRow(new String[]{
                alert.meta.lms,
                hourFormat.format(d) + " on " + dateFormat.format(d),
                alert.meta.sensorMeta.zone + "/" + alert.meta.sensorMeta.sensor,
                ((int) alert.reading.measurement) + "%"
        });
    }

    private class OverviewCard extends JPanel {

        public OverviewCard(){
            // Border layout to use full window
            setLayout(new BorderLayout());

            // Create an initial base table with the given column names
            lotTable = new BaseTable(messageList, new Vector<String>(){{
                add("LMS ID");
                add("Time");
                add("Reporting Zone/Sensor");
                add("Received Level");
            }});

            // Add the table to a scrolling pane
            itemListPanel = new JScrollPane(
                    lotTable,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            );

            // Add the scrolling pane to the main panel
            add(itemListPanel, BorderLayout.SOUTH);
        }

        /**
         * Returns the table model of lotTable for access outside
         * this class. This is used to populate the table initially.
         *
         * @return DefaultTableModel    the table model
         */
        public DefaultTableModel getTableModel(){
            return ((DefaultTableModel) lotTable.getModel());
        }

    }
}
