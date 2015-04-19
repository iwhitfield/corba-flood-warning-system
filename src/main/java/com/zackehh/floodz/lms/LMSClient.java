package com.zackehh.floodz.lms;

import com.beust.jcommander.JCommander;

/**
 * The LMS client, basically a command line listener. There is
 * no user interaction with this component aside from specifying
 * the name of the LMS. This option can also be provided via
 * command line arguments.
 */
public class LMSClient {

    /**
     * MAin entry point for an LMS. Parses arguments and creates
     * a new driver.
     *
     * @param args the program arguments
     */
    public static void main(String[] args) throws Exception {
        // create a new LMS arguments object
        LMSArgs lArgs = new LMSArgs();

        // provide JCommander with the arguments
        JCommander j = new JCommander(lArgs);

        // set parse arguments
        j.setAcceptUnknownOptions(true);
        // parse the arguments
        j.parse(args);

        // create a new driver
        new LMSDriver(args, lArgs).getEmbeddedOrb().run();
    }

}
