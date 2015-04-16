package com.zackehh.floodz.common.ui.modals;

import com.zackehh.corba.common.Alert;
import com.zackehh.floodz.util.SQLiteClient;
import com.zackehh.floodz.rmc.RMCDriver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
public class LMSModal implements ModalInterface {

    private final String NO_LMS_FOUND = "No LMSs found!";

    private JDialog jDialog;
    private RMCDriver rmcDriver;
    private SQLiteClient sqLiteClient;

    public void showModal(RMCDriver rmcDriver) {
        this.rmcDriver = rmcDriver;
        this.sqLiteClient = SQLiteClient.getInstance();

        List<String> names = sqLiteClient.getStoredLMSNames();

        if(names.size() == 0){
            names.add(NO_LMS_FOUND);
        }

        String[] namesAsArray = names.toArray(new String[names.size()]);

        JList<String> list = new JList<>(namesAsArray);

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                String value = list.getSelectedValue().toString();
                if(!NO_LMS_FOUND.equals(value)){
                    displayLMSData(value);
                }
            }
        });
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                setOpaque(false);
                return super.getListCellRendererComponent(list, value, index, false, false);
            }
        });
        list.setOpaque(false);
        list.setFixedCellHeight(20);

        JOptionPane optionPane = new JOptionPane(list, JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, new Object[]{}); // no buttons

        jDialog = optionPane.createDialog("Select An LMS:");
        jDialog.setVisible(true);
    }

    private void displayLMSData(String lms){
        jDialog.dispose();

        Alert[] alerts = rmcDriver.getDistrictState(lms);

        if(alerts.length == 0){
            JOptionPane.showMessageDialog(null, "No alerts found at the current time.",
                    "Current State", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<String> readingsAsList = new ArrayList<>();

        for(Alert alert : alerts){
            readingsAsList.add(alert.meta.sensorMeta.zone + " registered alert of " + alert.reading.measurement +
                    "% at " + new Date(alert.reading.time));
        }

        String[] readingsAsStrings = readingsAsList.toArray(new String[readingsAsList.size()]);

        JList<String> list = new JList<>(readingsAsStrings);

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                displayLMSData(list.getSelectedValue().toString());
            }
        });
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                setOpaque(false);
                return super.getListCellRendererComponent(list, value, index, false, false);
            }
        });
        list.setOpaque(false);
        list.setFixedCellHeight(20);

        JOptionPane.showMessageDialog(null, list, "Current State", JOptionPane.INFORMATION_MESSAGE); // no buttons
    }
}