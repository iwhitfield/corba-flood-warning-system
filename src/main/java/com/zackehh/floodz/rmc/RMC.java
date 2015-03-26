package com.zackehh.floodz.rmc;

public class RMC {

    private static RMCDriver rmcDriver;

    public static void main(String[] args) {
        rmcDriver = new RMCDriver(args);

        rmcDriver.getEmbeddedOrb().run();
    }
}
