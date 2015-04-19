package com.zackehh.floodz.common.ui;

import com.zackehh.floodz.rmc.RMCDriver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;

/**
 * The Options Panel is a JPanel displayed on the top side of the
 * RMC GUI. It provides a number of buttons to access features available
 * to the client. In order to keep it simple, the listener simply opens
 * the Modal with the corresponding name via reflection.
 */
public class OptionsPanel extends JPanel {

    /**
     * The RMC Driver instance for complex queries.
     */
    private final RMCDriver rmcDriver;

    /**
     * The package containing the Modal dialogs.
     */
    private final String packageName = getClass().getPackage().toString()
            .substring(8) + ".modals.";

    /**
     * Default constructor. Takes and RMCDriver instance in order to
     * be able to provide it to the Modal classes. Simply creates
     * buttons and sets names accordingly.
     *
     * @param rmcDriver the driver instance
     */
    public OptionsPanel(RMCDriver rmcDriver){
        this.rmcDriver = rmcDriver;

        setLayout(new FlowLayout());

        add(createButton("View LMS Alerts", "LMSModal"));
        add(createButton("View Region Map", "RegionModal"));
    }

    /**
     * Creates JButton instance containing the designated message.
     * Sets the name of the button to the name of a Modal which will
     * be displayed on click. The click listener pulls the name of the
     * button and opens the Modal with the corresponding class name.
     *
     * Throws a RuntimeException if the reflection fails; this will only
     * fail if there is an error in the source.
     *
     * @param message the message to display
     * @param name the name of the button
     * @return a JButton instance
     */
    private JButton createButton(String message, String name){
        // create a button with chosen text
        JButton b = new JButton(message);

        // set the name of the button
        b.setName(name);

        // set a listener to open teh modal
        b.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = ((JButton) e.getSource()).getName();

                try {
                    // find the class of the modal
                    Class<?> clazz = Class.forName(packageName + name);
                    // find the constructor to use
                    Constructor<?> ctor = clazz.getConstructor(RMCDriver.class);
                    // create an instance
                    ctor.newInstance(rmcDriver);
                } catch(Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // return the button
        return b;
    }

}