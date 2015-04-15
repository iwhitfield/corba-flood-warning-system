package com.zackehh.floodz.common.ui.cards;

import com.zackehh.floodz.common.ui.modals.ModalInterface;
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

    public OptionsPanel(RMCDriver rmcDriver){
        this.rmcDriver = rmcDriver;

        setLayout(new FlowLayout());

        ButtonListener buttonListener = new ButtonListener();

        JButton viewLMSButton = new JButton("View LMS Alerts");
        viewLMSButton.setName("LMSModal");
        viewLMSButton.addActionListener(buttonListener);

        add(viewLMSButton);
    }

    private class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            selectOption(((JButton) e.getSource()).getName());
        }

    }

    private void selectOption(String option){
        try {
            Class<?> clazz = Class.forName(packageName + "." + option);
            Constructor<?> ctor = clazz.getConstructor();
            ((ModalInterface) ctor.newInstance()).showModal(rmcDriver);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

}