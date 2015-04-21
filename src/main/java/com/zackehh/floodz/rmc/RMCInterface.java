package com.zackehh.floodz.rmc;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;
import com.zackehh.corba.rmc.RMCClientHelper;
import com.zackehh.corba.rmc.RMCClientPOA;
import com.zackehh.corba.rmc.RMCServer;
import com.zackehh.floodz.common.Constants;
import com.zackehh.floodz.common.ui.InterfaceUtils;
import com.zackehh.floodz.common.ui.OptionsPanel;
import com.zackehh.floodz.common.ui.table.BaseTable;
import com.zackehh.floodz.common.ui.table.RMCTableModel;
import com.zackehh.floodz.common.util.NameServiceHandler;
import com.zackehh.floodz.util.InputReader;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;

/**
 * The LMS client, basically a command line listener. There is
 * no user interaction with this component aside from specifying
 * the name of the LMS. This option can also be provided via
 * command line arguments.
 */
@SuppressWarnings("FieldCanBeLocal")
public class RMCInterface extends RMCClientPOA {

    private final List<String> lmsNames;

    /**
     * The CORBA ORB instance.
     */
    private final ORB orb;

    /**
     * The table model of the main view panel.
     */
    private final RMCTableModel rmcTableModel = new RMCTableModel(
        new Vector<Vector<String>>(), new Vector<String>(){{
            add("LMS ID");
            add("Time");
            add("Reporting Zone/Sensor");
            add("Received Level");
        }}
    );

    /**
     * The id of the client.
     */
    private final String id;

    /**
     * Main entry point for an LMS. Parses arguments and creates
     * a new driver.
     *
     * @param args the program arguments
     */
    public static void main(String[] args) throws Exception {
        new RMCInterface(args);
    }

    /**
     * Creates a new RMCInterface and registers it with the RMC.
     * Adds a shutdown hook in order to unregister as needed.
     *
     * @param args the program arguments
     */
    private RMCInterface(String[] args) {
        // initialise the ORB
        this.orb = ORB.init(args, null);

        // create a new InputReader
        InputReader console = new InputReader(System.in);

        // check for tracked local stations
        List<String> names = console.readList("Enter a CSV set of LMS to register to (or 'all' for all alerts): ");

        // check for needed null (null represents no filter)
        if(names != null && names.size() == 1 && "all".equals(names.get(0))){
            this.lmsNames = null;
        } else {
            this.lmsNames = names;
        }

        // set up client id
        this.id = UUID.randomUUID().toString();

        // Retrieve a name service
        NamingContextExt nameService;
        try {
            // Retrieve a name service
            nameService = NameServiceHandler.register(this.orb, this, this.id, RMCClientHelper.class);
            if(nameService == null){
                throw new Exception();
            }
        } catch(Exception e) {
            throw new IllegalStateException("Retrieved name service is null!");
        }

        // obtain the Sensor reference in the Naming service
        final RMCServer rmc = NameServiceHandler.retrieveObject(
                nameService, Constants.REGIONAL_MONITORING_CENTRE, RMCServer.class);

        // ensure an RMC
        if(rmc == null){
            throw new IllegalStateException("Retrieved RMC Server is null!");
        }

        // register the client
        if(!rmc.registerClient(id)){
            throw new IllegalStateException("Unable to register client!");
        }

        // shutdown hook to unregister from the RMC
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    rmc.removeClient(id);
                } catch (Exception e) {
                    System.err.println("Unable to unregister client!");
                }
            }
        });

        // create a JFrame
        new RegionalInterface(rmc);
    }

    /**
     * Returns the id of this client instance.
     *
     * @return a String id
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Adds an alert to the client, displaying a new alert in the
     * table.
     *
     * @param alert the alert to add
     */
    @Override
    public void addAlert(Alert alert) {
        rmcTableModel.addAlert(alert);
    }

    /**
     * Cancels an alert and removes it from the table.
     *
     * @param metadata the metadata of the alert to remove
     */
    @Override
    public void removeAlert(MetaData metadata) {
        rmcTableModel.removeAlert(metadata);
    }

    @Override
    public String[] getLMSList(){
        if(lmsNames == null){
            return new String[0];
        }
        return lmsNames.toArray(new String[lmsNames.size()]);
    }

    /**
     *
     */
    private class RegionalInterface extends JFrame {

        /**
         * Creates a new JFrame and adds the appropriate panels.
         * Passes an RMCServer through to the OptionsPanel, and
         * loads an initial list of alerts from the server.
         *
         * @param rmcServer the RMCServer instance
         */
        public RegionalInterface(RMCServer rmcServer){
            // set the application title
            setTitle(Constants.APPLICATION_NAME);

            // exit on a button press
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    System.exit(0);
                }
            });

            // set the container BorderLayout
            Container cp = getContentPane();
            cp.setLayout(new BorderLayout());

            // setup the interface via the utils
            InterfaceUtils.setup(cp);

            // create a panel for the main frame
            JPanel mainLayout = new JPanel(new BorderLayout());

            // add components to the main frame
            mainLayout.add(new OptionsPanel(rmcServer), BorderLayout.NORTH);
            mainLayout.add(new OverviewCard(), BorderLayout.SOUTH);

            // add the frame to the container
            cp.add(mainLayout);

            // pack the ui
            pack();

            // get the screen size as a java dimension
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            // make the width a 1/4 of the screen
            setPreferredSize(new Dimension(screenSize.width * 2 / 4, getSize().height));

            // unfortunate re-pack
            pack();

            // disable resize
            setResizable(false);

            // load all alerts from the DB
            java.util.List<Alert> oldAlerts = Arrays.asList(rmcServer.getAlerts());
            // for each alert in the list
            for(Alert oldAlert : oldAlerts){
                // add it to the table
                rmcTableModel.addAlert(oldAlert);
            }

            // display!
            setVisible(true);
        }

        /**
         * The main card containing the scrollable table to display at the
         * bottom of the main frame.
         */
        private class OverviewCard extends JPanel {

            /**
             * Creates a new table, placed inside a JScrollPane in order
             * to enable an 'infinite' list of Alerts.
             */
            public OverviewCard(){
                // border layout to use full window
                setLayout(new BorderLayout());

                // add a base table to a scrolling pane
                JScrollPane itemListPanel = new JScrollPane(
                        new BaseTable(rmcTableModel),
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                );

                // add the scrolling pane to the main panel
                add(itemListPanel, BorderLayout.SOUTH);
            }

        }

    }

}
