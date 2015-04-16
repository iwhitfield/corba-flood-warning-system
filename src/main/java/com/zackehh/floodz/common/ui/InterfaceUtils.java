package com.zackehh.floodz.common.ui;

import java.awt.*;

public class InterfaceUtils {

    private static FontMetrics fontMetrics;
    private static InterfaceUtils instance = null;

    private InterfaceUtils(Container c) {
        fontMetrics = c.getFontMetrics(c.getFont());
    }

    public static InterfaceUtils getInstance() {
        if (instance == null) {
            throw new RuntimeException(InterfaceUtils.class + " has not been setup!");
        }
        return instance;
    }

    public static InterfaceUtils setup(Container c) {
        if (instance == null) {
            instance = new InterfaceUtils(c);
        }
        return instance;
    }

    public int getStringLength(String s){
        return fontMetrics.stringWidth(s) + 10;
    }
}
