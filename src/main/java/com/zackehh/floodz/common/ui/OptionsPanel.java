package com.zackehh.floodz.common.ui;

import com.zackehh.floodz.common.ui.modals.Modal;
import com.zackehh.floodz.rmc.RMCDriver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;

public class OptionsPanel extends JPanel {

    private final RMCDriver rmcDriver;
    private final String packageName = getClass().getPackage().toString()
            .substring(8).replace("cards", "modals");

    private final ActionListener defaultButtonListener = new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = ((JButton) e.getSource()).getName();

            try {
                Class<?> clazz = Class.forName(packageName + "." + name);
                Constructor<?> ctor = clazz.getConstructor(RMCDriver.class);
                ((Modal) ctor.newInstance(rmcDriver)).showModal();
            } catch(Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    };

    public OptionsPanel(RMCDriver rmcDriver){
        this.rmcDriver = rmcDriver;

        setLayout(new FlowLayout());

        add(createButton("View LMS Alerts", "LMSModal"));
        add(createButton("View Region Map", "RegionModal"));
    }

    private JButton createButton(String message, String name){
        JButton b = new JButton(message);

        b.setName(name);
        b.addActionListener(defaultButtonListener);

        return b;
    }

}