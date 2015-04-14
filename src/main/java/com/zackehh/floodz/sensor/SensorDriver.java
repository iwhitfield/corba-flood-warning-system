package com.zackehh.floodz.sensor;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;
import com.zackehh.corba.common.Reading;
import com.zackehh.corba.common.SensorMeta;
import com.zackehh.corba.lms.LMS;
import com.zackehh.corba.sensor.SensorHelper;
import com.zackehh.corba.sensor.SensorPOA;
import com.zackehh.floodz.common.NamingServiceHandler;
import com.zackehh.floodz.common.NamingPair;
import com.zackehh.floodz.util.InputReader;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;

import java.util.ArrayList;
import java.util.List;

public class SensorDriver extends SensorPOA {

    private final List<Reading> readingLog = new ArrayList<>();

    private Reading current = null;
    private MetaData metadata;

    private Boolean power_on;

    private LMS lms;
    private NamingContextExt namingContextExt;
    private ORB orb;

    @SuppressWarnings("unused")
    SensorDriver(MetaData meta){
        // testing ctor
        this.metadata = meta;
        this.orb = null;
    }

    public SensorDriver(String[] args, SensorArgs sArgs){

        // Turn on
        power_on = true;

        // Initialise the ORB
        orb = ORB.init(args, null);

        // Retrieve a name service
        NamingPair namingPair;
        try {
            // Retrieve a name service
            namingPair = NamingServiceHandler.retrieveNameService(orb);
            if(namingPair == null){
                throw new Exception();
            }
            namingContextExt = namingPair.getNamingService();
        } catch(Exception e) {
            throw new IllegalStateException("Retrieved name service is null!");
        }

        InputReader console = new InputReader(System.in);

        String zoneName = sArgs.zone;
        if(zoneName == null){
            zoneName = console.readString("Please enter the sensor zone: ");
        }

        String lmsName = sArgs.lms;
        if(lmsName == null){
            lmsName = console.readString("Please enter the local station name: ");
        }

        lms = SensorUtil.findLMSBinding(namingContextExt, lmsName);
        if(lms == null){
            throw new IllegalStateException("Unable to find an LMS with name `" + lmsName + "`");
        }

        System.out.println("");
        System.out.println("Sensor in zone " + zoneName + " connecting to " + lmsName + "...");

        SensorMeta meta = lms.registerSensor(zoneName);

        try {
            NamingServiceHandler.bind(
                    namingPair.getNamingService(),
                    NamingServiceHandler.createRef(namingPair, this, SensorHelper.class),
                    meta.sensor
            );
        } catch(Exception e) {
            throw new IllegalStateException("Unable to bind Sensor to NameService!");
        }

        metadata = new MetaData(lmsName, meta);

        System.out.println("Connected and assigned id " + meta.sensor + " by LMS.");
    }

    @Override
    public String id() {
        return metadata.sensorMeta.sensor;
    }

    @Override
    public String zone() {
        return metadata.sensorMeta.zone;
    }

    @Override
    public boolean powerOff() {
        if(power_on){
            lms.removeSensor(metadata.sensorMeta);
            power_on = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean powerOn() {
        if(!power_on){
            lms.registerSensor(metadata.sensorMeta.zone);
            power_on = true;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        current = null;
        readingLog.clear();
    }

    @Override
    public Reading currentReading() {
        return current;
    }

    @Override
    public Reading[] getReadingLog() {
        return readingLog.toArray(new Reading[readingLog.size()]);
    }

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
            lms = SensorUtil.findLMSBinding(namingContextExt, metadata.lms);
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
}
