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

@SuppressWarnings("unused")
public class LMSModal implements Modal {

    private final String NO_LMS_FOUND = "No LMSs found!";
    private final DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setOpaque(false);
            return super.getListCellRendererComponent(list, value, index, false, false);
        }
    };

    private JDialog jDialog;
    private final RMCDriver rmcDriver;
    private final SQLiteClient sqLiteClient;

    public LMSModal(RMCDriver rmcDriver){
        this.rmcDriver = rmcDriver;
        this.sqLiteClient = SQLiteClient.getInstance();
    }

    public void showModal() {
        List<String> names = sqLiteClient.getStoredLMSNames();

        if(names.size() == 0){
            names.add(NO_LMS_FOUND);
        }

        final JList<String> list = new JList<>(names.toArray(new String[names.size()]));

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                String value = list.getSelectedValue();
                if(!NO_LMS_FOUND.equals(value)){
                    displayLMSData(value);
                }
            }
        });
        list.setCellRenderer(defaultListCellRenderer);
        list.setOpaque(false);
        list.setFixedCellHeight(20);

        JOptionPane optionPane = new JOptionPane(list, JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, new Object[]{});

        jDialog = optionPane.createDialog("Select An LMS:");
        jDialog.setVisible(true);
    }

    private void displayLMSData(String lms){
        jDialog.dispose();

        Alert[] alerts = rmcDriver.getDistrictState(lms);

        Object component;

        if(alerts.length == 0){
            component = "No alerts found at the current time.";
        } else {

            String[] readings = new String[alerts.length];

            for(int i = 0; i < alerts.length; i++){
                Alert alert = alerts[i];
                readings[i] = alert.meta.sensorMeta.zone + " registered alert of " + alert.reading.measurement +
                        "% at " + new Date(alert.reading.time);
            }

            JList<String> list = new JList<>(readings);

            list.setCellRenderer(defaultListCellRenderer);
            list.setOpaque(false);
            list.setFixedCellHeight(20);

            component = list;
        }

        JOptionPane.showMessageDialog(null, component, "Current State", JOptionPane.INFORMATION_MESSAGE);
    }
}