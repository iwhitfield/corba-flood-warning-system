package com.zackehh.floodz.sensor;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;
import com.zackehh.corba.common.Reading;
import com.zackehh.corba.common.SensorMeta;
import com.zackehh.corba.lms.LMS;
import com.zackehh.corba.lms.LMSHelper;
import com.zackehh.corba.sensor.SensorHelper;
import com.zackehh.corba.sensor.SensorPOA;
import com.zackehh.floodz.common.util.NameServiceHandler;
import com.zackehh.floodz.common.util.NamePair;
import com.zackehh.floodz.util.InputReader;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

import java.util.ArrayList;
import java.util.List;

/**
 * The main driver for a Sensor, extending the POA generated
 * by the CORBA IDL. Provides the ability to register an alert
 * and forward to a connected LMS. Keeps track of previous Readings
 * in case an LMS wishes to retrieve such a list at a later time.
 */
public class SensorDriver extends SensorPOA {

    /**
     * A list of readings to keep track of previous readings.
     */
    private final List<Reading> readingLog = new ArrayList<>();

    /**
     * Whether the sensor is powered on or not.
     */
    private Boolean power_on;

    /**
     * The connected Local Monitoring Station.
     */
    private LMS lms;

    /**
     * The metadata of this sensor.
     */
    private MetaData metadata;

    /**
     * The naming service for use when connecting.
     */
    private NamingContextExt namingContextExt;

    /**
     * The last known reading from this sensor.
     */
    private Reading current;

    /**
     * The CORBA ORB instance.
     */
    private final ORB orb;

    /**
     * No-op constructor, for use when testing.
     *
     * @param meta the metadata of the sensor.
     */
    @SuppressWarnings("unused")
    SensorDriver(MetaData meta){
        // testing ctor
        this.metadata = meta;
        this.orb = null;
    }

    /**
     * Connects to the assigned LMS after prompting the user for input
     * to designate the LMS and zone to connect to.
     *
     * @param args the program args
     * @param sArgs parsed SensorArgs instance
     */
    public SensorDriver(String[] args, SensorArgs sArgs){
        // Turn on power
        power_on = true;

        // Initialise the ORB
        orb = ORB.init(args, null);

        // Retrieve a name service
        NamePair namingPair;
        try {
            // Retrieve a name service
            namingPair = NameServiceHandler.retrieveNameService(orb);
            if(namingPair == null){
                throw new Exception();
            }
            namingContextExt = namingPair.getNamingService();
        } catch(Exception e) {
            throw new IllegalStateException("Retrieved name service is null!");
        }

        // create a new console reader
        InputReader console = new InputReader(System.in);

        // get the zone name
        String zoneName = sArgs.zone;

        // if none found, ask for input
        if(zoneName == null){
            zoneName = console.readString("Please enter the sensor zone: ");
        }

        // get the LMS name
        String lmsName = sArgs.lms;

        // if none found, ask for input
        if(lmsName == null){
            lmsName = console.readString("Please enter the local station name: ");
        }

        // find an LMS with the given name
        lms = findLMSBinding(lmsName);

        // exit if none found
        if(lms == null){
            throw new IllegalStateException("Unable to find an LMS with name `" + lmsName + "`");
        }

        System.out.println("");
        System.out.println("Sensor in zone " + zoneName + " connecting to " + lmsName + "...");

        // register the sensor with the LMS
        final SensorMeta meta = lms.registerSensor(zoneName);

        try {
            // bind to the NamingService
            NameServiceHandler.bind(
                    namingPair.getNamingService(),
                    NameServiceHandler.createRef(namingPair, this, SensorHelper.class),
                    meta.sensor
            );
        } catch(Exception e) {
            throw new IllegalStateException("Unable to bind Sensor to NameService!");
        }

        // create and store the current metadata
        metadata = new MetaData(lmsName, meta);

        System.out.println("Connected and assigned id " + meta.sensor + " by LMS.");

        // add a shutdown hook to disconnect from the LMS
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    if (!lms.removeSensor(meta)) {
                        System.err.println("Unable to unregister from LMS!");
                    }
                } catch (Exception e) {
                    System.err.println("Unable to unregister from LMS!");
                }
            }
        });
    }

    /**
     * Retrieve the current reading of the sensor.
     *
     * @return the Reading value
     */
    @Override
    public Reading currentReading() {
        return current;
    }

    /**
     * Retrieve the id of the sensor.
     *
     * @return the String id
     */
    @Override
    public String id() {
        return metadata.sensorMeta.sensor;
    }

    /**
     * Retrieve the zone of the sensor.
     *
     * @return the String zone
     */
    @Override
    public String zone() {
        return metadata.sensorMeta.zone;
    }

    /**
     * Powers of fthe sensor. Due to this being a mocked
     * implementation, the power status is simply a boolean.
     *
     * Powering off a sensor will also remove it from the LMS.
     *
     * @return true if successful
     */
    @Override
    public boolean powerOff() {
        if(power_on){
            lms.removeSensor(metadata.sensorMeta);
            power_on = false;
            return true;
        }
        return false;
    }

    /**
     * Powers on the sensor. Due to this being a mocked
     * implementation, the power status is simply a boolean.
     *
     * @return true if successful
     */
    @Override
    public boolean powerOn() {
        if(!power_on){
            try {
                lms.registerSensor(metadata.sensorMeta.zone);
            } catch(Exception e) {
                System.err.println("Unable to reconnect to LMS!\n");
                return false;
            }
            power_on = true;
            return true;
        }
        return false;
    }

    /**
     * Resets the sensor status. Internally this clears the
     * logs and the current status of the sensor.
     */
    @Override
    public void reset() {
        current = null;
        readingLog.clear();
    }

    /**
     * Retrieves the Reading log of the sensor as Reading array.
     *
     * @return a Reading[] of the known readings
     */
    @Override
    public Reading[] getReadingLog() {
        return readingLog.toArray(new Reading[readingLog.size()]);
    }

    /**
     * Sends an Alert for the passed in measurement through to the
     * connected LMS. Also adds the reading to the reading log so
     * it can be accessed later.
     *
     * @param measurement the measurement of the reading
     */
    @Override
    public void sendAlert(int measurement){
        if(!power_on){
            System.err.println("System is switched off!\n");
            return;
        }

        Reading reading = new Reading(System.currentTimeMillis(), measurement);

        try {
            lms.ping();
        } catch(Exception e) {
            lms = findLMSBinding(metadata.lms);
        }

        if(lms != null) {
            MetaData config = new MetaData(lms.name(), metadata.sensorMeta);
            lms.receiveAlert(new Alert(config, reading));
        } else {
            System.err.println("LMS `" + metadata.lms + "` is unreachable!");
        }

        current = reading;
        readingLog.add(reading);

        if(measurement > metadata.sensorMeta.alert_level){
            System.err.println("Reading is above alert level of " + metadata.sensorMeta.alert_level + " at " + measurement + "!");
        } else {
            System.out.println("Registered new reading: " + measurement);
        }
    }

    /**
     * Retrieves the designated LMS. Pings to ensure connection.
     *
     * @param lmsName the name of the LMS
     * @return an LMS instance
     */
    private LMS findLMSBinding(String lmsName){
        LMS lms = NameServiceHandler.retrieveObject(namingContextExt, lmsName, LMS.class);
        try {
            return lms != null && lms.ping() ? lms : null;
        } catch(Exception e) {
            return null;
        }
    }
}
