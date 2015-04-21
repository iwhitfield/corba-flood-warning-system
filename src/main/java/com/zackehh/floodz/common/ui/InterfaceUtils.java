package com.zackehh.floodz.common.ui;

import java.awt.*;

/**
 * A simple class to hold any utilities in order to interact
 * with the interface. Designed to be static, although the class
 * should be set up in the main GUI window.
 */
public class InterfaceUtils {

    /**
     * The FontMetrics instance of the main window.
     */
    private static FontMetrics fontMetrics;

    /**
     * Singleton instance of InterfaceUtils.
     */
    private static InterfaceUtils instance = null;

    /**
     * Constructor to set the FontMetrics from the Container.
     *
     * @param c the main Container
     */
    private InterfaceUtils(Container c) {
        fontMetrics = c.getFontMetrics(c.getFont());
    }

    /**
     * Returns the instance of InterfaceUtils. Throws an
     * Exception if the instance has not been set up.
     *
     * @return the InterfaceUtils singleton
     */
    public static InterfaceUtils getInstance() {
        if (instance == null) {
            throw new RuntimeException(InterfaceUtils.class + " has not been setup!");
        }
        return instance;
    }

    /**
     * Sets up the singleton, taking in a Container
     * instance.
     *
     * @param c the main Container
     * @return the instance
     */
    public static void setup(Container c) {
        if (instance == null) {
            instance = new InterfaceUtils(c);
        }
    }

    /**
     * Returns the needed width for a string to fit
     * inside.
     *
     * @param s the string to fit
     * @return the length
     */
    public int getStringLength(String s){
        return fontMetrics.stringWidth(s) + 10;
    }
}
