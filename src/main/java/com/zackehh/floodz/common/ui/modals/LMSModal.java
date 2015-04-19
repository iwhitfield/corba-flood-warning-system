package com.zackehh.floodz.common.ui.modals;

import com.zackehh.corba.common.Alert;
import com.zackehh.floodz.util.SQLiteClient;
import com.zackehh.floodz.rmc.RMCDriver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;

/**
 * A Modal displaying a list of connected Local Monitoring Stations.
 * On clicking a station name, a list of currently tracked alerts will
 * be retrieved. The difference between this and the main UI panel is
 * due to the fact that the main UI only shows one alert per zone.
 */
@SuppressWarnings("unused") // called via reflection
public class LMSModal {

    /**
     * The main dialog to display.
     */
    private final JDialog jDialog;

    /**
     * The warning to show if no LMS are found.
     */
    private final String NO_LMS_FOUND = "No LMSs found!";

    /**
     * The main constructor, creating a dialog containing
     * a list of connection local stations. On click, loads
     * the list of current alerts being monitored by the LMS.
     *
     * @param rmcDriver the RMC instance
     */
    public LMSModal(final RMCDriver rmcDriver){
        // create the cell renderer for JList instances
        final DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                setOpaque(false);
                return super.getListCellRendererComponent(list, value, index, false, false);
            }
        };
        // grab a SQLiteClient instance
        final SQLiteClient sqLiteClient = SQLiteClient.getInstance();

        // retrieve all LMS names
        List<String> names = sqLiteClient.retrieveLocalNames();

        // if none are found, add a warning
        if(names.size() == 0){
            names.add(NO_LMS_FOUND);
        }

        // create a JList from the LMS names
        final JList<String> list = new JList<>(names.toArray(new String[names.size()]));

        // add a listener to the list
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                // retrieve the selected name
                String lms = list.getSelectedValue();

                // disable onclick for the warning message
                if(NO_LMS_FOUND.equals(lms)){
                    return;
                }

                // close the existing dialog
                jDialog.dispose();

                // get the state of the chosen LMS
                Alert[] alerts = rmcDriver.getDistrictState(lms);

                // object to display
                Object component;

                // if there are no alerts
                if(alerts == null){
                    // the LMS is not reachable
                    component = lms + " is no longer reachable.";
                } else if(alerts.length == 0){
                    // the list is empty
                    component = "No alerts found at the current time.";
                } else {
                    // create an array to store the alerts
                    String[] readings = new String[alerts.length];

                    // for each alert found
                    for(int i = 0; i < alerts.length; i++){
                        // get current alert
                        Alert alert = alerts[i];
                        // create a string representation of the alert
                        // unfortunately, we cannot edit the #toString method
                        // inside the CORBA class
                        readings[i] = alert.meta.sensorMeta.zone + " registered alert of " + alert.reading.measurement +
                                "% at " + new Date(alert.reading.time);
                    }

                    // create a list from the readings
                    JList<String> list = new JList<>(readings);

                    // set new list properties
                    list.setCellRenderer(cellRenderer);
                    list.setOpaque(false);
                    list.setFixedCellHeight(20);

                    // set the list to be displayed
                    component = list;
                }

                // display the chosen component
                JOptionPane.showMessageDialog(null, component, "Current State", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // set the list properties
        list.setCellRenderer(cellRenderer);
        list.setOpaque(false);
        list.setFixedCellHeight(20);

        // create an option pane from the list
        JOptionPane optionPane = new JOptionPane(list, JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, new Object[]{});

        // create a dialog from the option pane
        jDialog = optionPane.createDialog("Select An LMS:");

        // set resizable and visible
        jDialog.setResizable(false);
        jDialog.setVisible(true);
    }

}