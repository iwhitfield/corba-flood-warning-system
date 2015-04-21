package com.zackehh.floodz.rmc;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;
import com.zackehh.corba.lms.LMS;
import com.zackehh.corba.rmc.RMCServerHelper;
import com.zackehh.corba.rmc.RMCServerPOA;
import com.zackehh.corba.rmc.RMCClient;
import com.zackehh.floodz.common.Constants;
import com.zackehh.floodz.common.util.NameServiceHandler;
import com.zackehh.floodz.util.SQLiteClient;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The main handling behind an RMC. Allows registration of Alerts
 * and notifies the client GUI in order to display such results to
 * a user. Due to the importance of this component, all pieces are
 * persisted to disk using SQLite. Also keeps track of known and
 * connected LMS stations.
 */
public class RMCDriver extends RMCServerPOA {

    private final HashSet<String> localStations = new HashSet<>();
    private final HashSet<String> clientIds = new HashSet<>();
    private final List<RMCClient> clients = new ArrayList<>();

    /**
     * Logging system for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(RMCDriver.class);

    /**
     * A list of received alerts.
     */
    private final List<Alert> alerts = new ArrayList<>();

    /**
     * The Naming Service used to talk to the LMSs.
     */
    private final NamingContextExt nameService;

    /**
     * The CORBA ORB instance.
     */
    private final ORB orb;

    /**
     * The name of this RMC. Currently only one exists, so it is
     * simply `Regional Monitoring Centre`.
     */
    private final String name = Constants.REGIONAL_MONITORING_CENTRE;

    /**
     * Reference to the SQLite instance.
     */
    private final SQLiteClient sqLiteClient;

    /**
     * Testing constructor, to allow unit tests to create an instance.
     */
    @SuppressWarnings("unused")
    RMCDriver(){
        // testing ctor
        this.nameService = null;
        this.orb = null;
        this.sqLiteClient = SQLiteClient.getInstance();
    }

    /**
     * Main entry point of the RMC GUI. Initializes the new window and
     * runs the RMCDriver ORB inside a new thread.
     *
     * @param args the program arguments
     */
    public static void main(String[] args) throws SQLException {
        // initialize the GUI
        final RMCDriver r = new RMCDriver(args);

        // start the initial loading of the existing items
        new Thread(new Runnable() {
            public void run() {
                r.getEmbeddedOrb().run();
            }
        }).start();
    }

    /**
     * Takes in the program arguments and the GUI instance. Sets up
     * a NamingService connection and initializes an ORB instance.
     *
     * @param args the program arguments
     */
    public RMCDriver(String[] args){
        // initialise the ORB
        this.orb = ORB.init(args, null);

        // grab the SQLite singleton
        this.sqLiteClient = SQLiteClient.getInstance();

        try {
            // retrieve a name service
            nameService = NameServiceHandler.register(orb, this, name, RMCServerHelper.class);
            if(nameService == null){
                throw new Exception();
            }
        } catch(Exception e) {
            throw new IllegalStateException("Retrieved name service is null!");
        }

        // load all alerts from the DB
        List<Alert> oldAlerts = sqLiteClient.retrieveAllAlerts();
        // for each alert in the list
        for(Alert oldAlert : oldAlerts){
            // add it to the table
            receiveAlert(oldAlert, false);
        }

        // Server has loaded up correctly
        logger.info("Remote Monitoring Centre is operational.");
    }

    /**
     * Simply returns true to the caller. Used simply as a connection
     * test between components.
     *
     * @return true
     */
    @Override
    public boolean ping() {
        return true;
    }

    /**
     * Returns the name of this RMC.
     *
     * @return a String name
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Cancels an Alert and removes it from the known alert list.
     * Notifies the GUI in order to remove the alert from the client.
     *
     * @param metaData the alert metadata
     */
    @Override
    public synchronized void cancelAlert(final MetaData metaData) {
        // unfortunately we cannot override #equals inside the CORBA
        // objects, so this has to be done manually, rather than a
        // combination of #indexOf and #contains.
        for(int i = 0, j = alerts.size(); i < j; i++){
            // if the alert matches
            if(alerts.get(i).meta.sensorMeta.zone.equals(metaData.sensorMeta.zone)){
                // log our a message
                logger.info("Removed alert from sensor #{} in {}", metaData.sensorMeta.sensor, metaData.sensorMeta.zone);

                // remove the alert
                alerts.remove(i);

                // we're done
                break;
            }
        }
        // remove it from the DB
        sqLiteClient.deleteAlert(metaData);
        // notify client UIs
        forAllClients(new RMCCommand() {
            @Override
            public void execute(RMCClient client) {
                client.removeAlert(metaData);
            }
        });
    }

    /**
     * Receives an alert from an LMS. Adds the alert to the list
     * of known alerts and then notifies the GUI of the change.
     *
     * In the case the alert has been registered, update the existing
     * alert in case the measurement has increased/descreased in order
     * to accurately reflect the current status.
     *
     * @param alert the alert to register
     */
    @Override
    public synchronized void receiveAlert(Alert alert) {
        receiveAlert(alert, true);
    }

    public synchronized void receiveAlert(final Alert alert, boolean persist) {
        // flag for short circuit
        boolean stored = false;
        // check each alert, to ensure matches
        for(int i = 0, j = alerts.size(); i < j; i++){
            // current iteration
            Alert a = alerts.get(i);
            // compare the received alert with the one in the alerts
            if( a.meta.sensorMeta.zone.equals(alert.meta.sensorMeta.zone) &&
                a.meta.lms.equals(alert.meta.lms)){
                // update the alert
                alerts.set(i, alert);
                // short circuit
                stored = true;
                // exit
                break;
            }
        }
        // add if new
        if(!stored){
            alerts.add(alert);
            // persist if appropriate
            if(persist) {
                sqLiteClient.insertAlert(alert);
            }
        } else {
            // update it on disk
            sqLiteClient.updateAlert(alert);
        }
        // notify client UIs
        forAllClients(new RMCCommand() {
            @Override
            public void execute(RMCClient client) {
                client.addAlert(alert);
            }
        });
        if(persist) {
            // log message
            logger.info("Received alert from sensor #{} in {}",
                    alert.meta.sensorMeta.sensor, alert.meta.sensorMeta.zone);
        }
    }

    @Override
    public boolean registerClient(String id) {
        try {
            RMCClient binding = NameServiceHandler.retrieveObject(
                    nameService, id, RMCClient.class);
            if(binding == null){
                return false;
            }
            clients.add(binding);
        } catch(Exception e){
            return false;
        }
        clientIds.add(id);
        logger.info("Received connection to client: {}", id);
        return true;
    }

    @Override
    public boolean removeClient(String id) {
        if(!clientIds.contains(id)){
            return false;
        }
        for(int i = 0; i < clients.size(); i++){
            if(clients.get(i).id().equals(id)){
                clients.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Registers a new LMS connection with the driver. Persists
     * the name of the LMS as an entry in the SQLite DB in order
     * to keep (persistent) track of the connected stations.
     *
     * @param name the name of the LMS
     * @return true on success
     */
    @Override
    public boolean registerLMSConnection(String name) {
        logger.info("Successfully received connection from LMS `{}`", name);
        localStations.add(name);
        return true;
    }

    /**
     * Removes an LMS connection, and updates the SQLite DB
     * in order to keep track of which stations are connected.
     *
     * @param name the name of the LMS
     * @return true on success
     */
    @Override
    public boolean removeLMSConnection(String name) {
        logger.info("Removed connection from LMS `{}`", name);
        sqLiteClient.deleteAlert(name, null, null);
        localStations.remove(name);
        return true;
    }

    /**
     * Returns a list of currently tracked alerts.
     *
     * @return an array of Alerts
     */
    @Override
    public Alert[] getAlerts(){
        return alerts.toArray(new Alert[alerts.size()]);
    }

    /**
     * Retrieves the state of a district (LMS). Returns null if there
     * are any issues connecting to the LMS.
     *
     * @param district the name of the LMS
     * @return an array of Alerts, or null on error
     */
    @Override
    public Alert[] getDistrictState(String district) {
        LMS lms = getConnectedLMS(district);
        try {
            return lms != null && lms.ping() ? lms.getCurrentState() : null;
        } catch(Exception e) {
            return null;
        }
    }

    /**
     * Returns a list of known LMS stations. This comes from the persistent
     * SQLite layer to ensure being accurate across restarts. Called from the
     * GUI client.
     *
     * @return a list of String names
     */
    @Override
    public String[] getKnownStations() {
        return localStations.toArray(new String[localStations.size()]);
    }

    /**
     * Retrieves an LMS from the NamingServiceHandler.
     *
     * @param name the name to retrieve
     * @return an LMS instance
     */
    public LMS getConnectedLMS(String name) {
        return NameServiceHandler.retrieveObject(nameService, name, LMS.class);
    }

    /**
     * Returns the CORBA ORB instance of this class.
     *
     * @return a CORBA ORB instance.
     */
    public ORB getEmbeddedOrb(){
        return this.orb;
    }

    /**
     * Iterates through the clients connected to the RMC.
     *
     * @param cmd the command to run
     */
    private void forAllClients(RMCCommand cmd){
        List<RMCClient> removal = new ArrayList<>();
        for(RMCClient c : clients){
            try {
                cmd.execute(c);
            } catch(Exception e){
                removal.add(c);
            }
        }
        if(removal.size() > 0){
            clients.removeAll(removal);
        }
    }

    private interface RMCCommand {
        void execute(RMCClient client);
    }
}
