package com.zackehh.floodz.lms;

import com.beust.jcommander.JCommander;

public class LMS {

    private static LMSDriver lmsDriver;

    public static void main(String[] args) throws Exception {
        LMSArgs lArgs = new LMSArgs();
        JCommander j = new JCommander(lArgs);

        j.setAcceptUnknownOptions(true);
        j.parse(args);

        lmsDriver = new LMSDriver(args, lArgs);

        lmsDriver.getEmbeddedOrb().run();
    }

}
