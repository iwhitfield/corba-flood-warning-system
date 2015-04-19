package com.zackehh.floodz.lms;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;
import com.zackehh.corba.common.Reading;
import com.zackehh.corba.common.SensorMeta;
import com.zackehh.corba.lms.LMSHelper;
import com.zackehh.corba.lms.LMSPOA;
import com.zackehh.corba.rmc.RMC;
import com.zackehh.floodz.common.util.Levels;
import com.zackehh.floodz.common.util.NameServiceHandler;
import com.zackehh.floodz.util.InputReader;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * The main LMS driver instance, used to retrieve alerts from a
 * Sensor and forward them to an RMC when appropriate. Keeps an
 * internal log of alerts and zone->sensor mappings.
 */
public class LMSDriver extends LMSPOA {

    /**
     * Logging instance via log4j.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * A map of alert states, keeping track of inbound alerts from the sensors.
     */
    private final ConcurrentSkipListMap<String, Alert>
            alertStates = new ConcurrentSkipListMap<>();

    /**
     * Creates a map to store zone to sensor mappings to keep track of all sensor readings
     * for a zone.
     */
    private final ConcurrentSkipListMap<String, ConcurrentSkipListMap<String, Reading>>
            zoneMapping = new ConcurrentSkipListMap<>();

    /**
     * A log of previously received alerts, most recent at the end.
     */
    private final List<Alert> alertLog = new ArrayList<>();

    /**
     * A Map of the zone alert/warning boundaries.
     */
    private final HashMap<String, Levels> levels;

    /**
     * The CORBA ORB instance.
     */
    private final ORB orb;

    /**
     * The Naming Service instance.
     */
    private NamingContextExt nameService;

    /**
     * The RMC this station is connected to.
     */
    private RMC rmc;

    /**
     * The name of this LMS as decided by the user.
     */
    private String name;

    /**
     * Testing constructor to create a basic LMSDriver.
     */
    LMSDriver(){
        this.levels = LMSUtil.retrieveZoneLevels();
        this.orb = null;
    }

    /**
     * Main constructor taking program args and a parsed
     * LMSArgs instance. Connects to the NamingService and
     * finds an RMC to connect to.
     *
     * @param args the program args
     * @param lArgs the parsed LMS args
     */
    public LMSDriver(String[] args, LMSArgs lArgs){
        // grab the level mappings
        levels = LMSUtil.retrieveZoneLevels();

        // create a new InputReader
        InputReader console = new InputReader(System.in);

        // set the LMS name
        name = lArgs.name;
        if (name == null) {
            name = console.readString("Please enter the station name: ");
        }

        System.out.println("");
        logger.info("Registered Local Monitoring Station: {}", name);

        // initialise the ORB
        orb = ORB.init(args, null);

        try {
            // retrieve a name service
            nameService = NameServiceHandler.register(orb, this, name, LMSHelper.class);
            // check null and short-circuit
            if(nameService == null){
                throw new Exception();
            }
        } catch(Exception e) {
            throw new IllegalStateException("Retrieved name service is null!");
        }

        // obtain the Sensor reference in the Naming service
        rmc = LMSUtil.findRMCBinding(nameService);

        // test out the RMC connection
        if (rmc == null || !rmc.registerLMSConnection(name)) {
            throw new IllegalStateException("RMC Connection failed!");
        } else {
            logger.info("Made successful connection to RMC");
        }

        // shutdown hook to unregister from the RMC
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    if(!rmc.removeLMSConnection(name)){
                        logger.warn("Unable to unregister from RMC!");
                    }
                } catch(Exception e) {
                    logger.warn("Unable to unregister from RMC!");
                }
            }
        });
    }

    /**
     * Retrieves the list of previous alerts received.
     *
     * @return an array of Alerts.
     */
    @Override
    public Alert[] alertLog() {
        return alertLog.toArray(new Alert[alertLog.size()]);
    }

    /**
     * Retrieves the name of this LMS.
     *
     * @return the String name
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Simple accessor method to check connection.
     *
     * @return true
     */
    @Override
    public boolean ping() {
        return true;
    }

    /**
     * Returns the current alerted zones as an array.
     *
     * @return an array of Alerts
     */
    @Override
    public Alert[] getCurrentState() {
        // initialize an empty list
        List<Alert> currentState = new ArrayList<>();
        // for every zone in the alert states
        for(Map.Entry<String, Alert> zone : alertStates.entrySet()){
            // add the alert value
            currentState.add(zone.getValue());
        }
        // return the list as an array
        return currentState.toArray(new Alert[currentState.size()]);
    }

    /**
     * Returns a list of SensorMeta objects representing the
     * currently registered Sensors. This is derived via the
     * zoneMapping, so it's not very efficient (O(n^2)) but
     * it's better than needlessly storing yet another list.
     *
     * @return a SensorMeta[] instance
     */
    @Override
    public SensorMeta[] getRegisteredSensors() {
        // create an empty list
        List<SensorMeta> metaList = new ArrayList<>();
        // loop all zone entries in the mapping
        for(Map.Entry<String, ConcurrentSkipListMap<String, Reading>> zoneMap : zoneMapping.entrySet()){
            // cast the keyset to a List
            List<String> keySet = new ArrayList<>(zoneMap.getValue().keySet());
            // sort the list
            Collections.sort(keySet);
            // for each key
            for(String key : keySet){
                // add a new SensorMeta entry
                metaList.add(new SensorMeta(
                        zoneMap.getKey(), key,
                        LMSUtil.getLevelsForZone(levels, zoneMap.getKey()).getAlertLevel()
                ));
            }
        }
        // return the list cast to an array
        return metaList.toArray(new SensorMeta[metaList.size()]);
    }

    /**
     * Receives an Alert from a sensor. If the alert is above the
     * warning and alert levels for the sensor, the information will
     * be relayed back to the RMC. This is potentially wrongly named
     * as not all Alert objects reaching this method are over the limit.
     *
     * If an alert arrives below alert limits, it is removed from the
     * status of alerts (where appropriate).
     *
     * @param alert the received alert
     */
    @Override
    public void receiveAlert(final Alert alert) {
        // log an acknowledgement
        logger.info("Received alert from sensor #{} in zone `{}`", alert.meta.sensorMeta.sensor, alert.meta.sensorMeta.zone);

        // find the existing zone entry
        ConcurrentSkipListMap<String, Reading> zone = zoneMapping.get(alert.meta.sensorMeta.zone);

        // if there is one
        if(zone != null){
            // set a reading for this sensor
            zone.put(alert.meta.sensorMeta.sensor, alert.reading);
        } else {
            // log potential warning because it might mean something is amiss
            logger.warn("Adding measurement from previously unregistered zone: {}", alert.meta.sensorMeta.zone);
            // create the new zone
            zone = new ConcurrentSkipListMap<String, Reading>(){{
                put(alert.meta.sensorMeta.sensor, alert.reading);
            }};
            // place it in the mapping
            zoneMapping.put(alert.meta.sensorMeta.zone, zone);
        }

        // add the alert to the log
        alertLog.add(alert);

        // retrieve the assigned alert level for this zone
        int alert_level = LMSUtil.getLevelsForZone(levels, alert.meta.sensorMeta.zone).getAlertLevel();

        // if the measurement is unsafe
        if(alert.reading.measurement > alert_level){
            // log out a warning
            logger.warn("Registered alert {} from Sensor #{}", alert.reading.measurement, alert.meta.sensorMeta.sensor);
        } else {
            // otherwise log out an info message
            logger.info("Registered reading {} from Sensor #{}", alert.reading.measurement, alert.meta.sensorMeta.sensor);
        }

        // avg counter
        int avg = 0;

        // sum all measurements
        for(Map.Entry<String, Reading> zoneMap : zone.entrySet()){
            avg += zoneMap.getValue().measurement;
        }

        // get the number of sensors in the zone
        int size = zone.size();

        // calculate the average (mean) based on sum/size
        avg = Math.round((avg / size) * 100) / 100;

        // try ensure an RMC connection
        try {
            rmc.ping();
        } catch(Exception e) {
            rmc = LMSUtil.findRMCBinding(nameService);
        }

        // figure out if the average across all sensors is above the alert_level and
        // do not report if there is only a single sensor in operation.
        if((avg >= alert_level && size > 2) || (avg > alert_level && size > 1)){

            // log a warning message
            logger.warn("Multiple warnings for zone `{}`, forwarding to RMC...", alert.meta.sensorMeta.zone);

            // set the reading measurement to the average
            alert.reading.measurement = avg;

            // forward to the RMC
            if(rmc != null) {
                rmc.receiveAlert(alert);
            } else {
                // warn if unavailable
                logger.warn("RMC is unreachable!");
            }

            // add the average as an alert
            alertStates.put(alert.meta.sensorMeta.zone, new Alert(alert.meta, new Reading(alert.reading.time, avg)));
        } else {

            // try to cancel the alert
            if(rmc != null) {
                rmc.cancelAlert(new MetaData(name, alert.meta.sensorMeta));
            } else {
                // warn if unavailable
                logger.warn("RMC is unreachable!");
            }

            // remove the alert from the alert states if it's there (might not be)
            if(alertStates.containsKey(alert.meta.sensorMeta.zone)){
                // log acknowledgement
                logger.info("Removed alert state for zone `{}`", alert.meta.sensorMeta.zone);
                // remove from the alert states
                alertStates.remove(alert.meta.sensorMeta.zone);
            }
        }

    }

    /**
     * Removes a Sensor from the zoneMapping object.
     *
     * @param sensorMeta the sensor to remove
     * @return true if the sensor was removed
     */
    @Override
    public boolean removeSensor(SensorMeta sensorMeta) {
        // log out an info message
        logger.info("Removed Sensor #{} from zone `{}`", sensorMeta.sensor, sensorMeta.zone);
        // if the mapping contains the key
        if(zoneMapping.containsKey(sensorMeta.zone)){
            // remove the sensor from the zone and short-circuit
            zoneMapping.get(sensorMeta.zone).remove(sensorMeta.sensor);
            return true;
        }
        return false;
    }

    /**
     * Registers a sensor with the LMS. Adds a new entry for the
     * designated zone in the zoneMapping object. This method will
     * return a SensorMeta instance bundled with the id of the sensor.
     * The LMS decides the id of the sensor to ensure unique-ness.
     *
     * @param zone the zone the sensor is apart of.
     * @return a SensorMeta instance.
     */
    @Override
    public SensorMeta registerSensor(String zone) {
        // create empty reading
        final Reading reading = new Reading();
        // identifier
        final String id;

        // if the mapping contains the zone name
        if (zoneMapping.containsKey(zone)) {
            // get the zone entry
            ConcurrentSkipListMap<String, Reading> zoneMap = zoneMapping.get(zone);
            // increment the counter
            id = (zoneMap.size() + 1) + "";
            // add a new mapping
            zoneMap.put(id, reading);
        } else {
            // id starts at 1
            id = "1";
            // add a new mapping
            zoneMapping.put(zone, new ConcurrentSkipListMap<String, Reading>() {{
                put(id, reading);
            }});
        }
        // log out the information
        logger.info("Added Sensor #{} to zone `{}`", id, zone);

        // return the new SensorMeta instance
        return new SensorMeta(zone, id, LMSUtil.getLevelsForZone(levels, zone).getAlertLevel());
    }

    /**
     * Returns the CORBA ORB instance of this class.
     *
     * @return a CORBA ORB instance.
     */
    public ORB getEmbeddedOrb(){
        return this.orb;
    }
}
