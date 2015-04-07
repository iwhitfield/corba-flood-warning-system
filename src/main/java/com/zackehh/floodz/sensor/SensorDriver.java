package com.zackehh.floodz.sensor;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.Reading;
import com.zackehh.corba.common.SensorMeta;
import com.zackehh.corba.common.SensorTuple;
import com.zackehh.corba.lms.LMS;
import com.zackehh.corba.lms.LMSHelper;
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
    private SensorTuple tuple;

    private Boolean power_on;
    private Integer alert_level = 0;

    private LMS lms;
    private InputReader console;
    private ORB orb;

    SensorDriver(SensorMeta meta){
        // testing ctor
        this.alert_level = meta.alert_level;
        this.orb = null;
        this.tuple = meta.tuple;
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
        } catch(Exception e) {
            throw new IllegalStateException("Retrieved name service is null!");
        }

        NamingContextExt nameService = namingPair.getNamingService();

        console = new InputReader(System.in);

        String zoneName = sArgs.zone;
        if(zoneName == null){
            zoneName = console.readString("Please enter the sensor zone: ");
        }

        String lmsName = sArgs.lms;
        if(lmsName == null){
            lmsName = console.readString("Please enter the local station name: ");
        }

        try {
            // Retrieve a name service
            lms = LMSHelper.narrow(nameService.resolve_str(lmsName));
            if(lms == null){
                throw new Exception();
            }
        } catch(Exception e) {
            throw new IllegalStateException("Unable to find an LMS with name `" + lmsName + "`");
        }

        System.out.println("");
        System.out.println("Sensor in zone " + zoneName + " connecting to " + lmsName + "...");

        SensorMeta meta = lms.registerSensor(zoneName);

        try {
            NamingServiceHandler.bind(
                    namingPair.getNamingService(),
                    NamingServiceHandler.createRef(namingPair, this, SensorHelper.class),
                    meta.tuple.sensor
            );
        } catch(Exception e) {
            throw new IllegalStateException("Unable to bind Sensor to NameService!");
        }

        tuple = meta.tuple;
        alert_level = meta.alert_level;

        System.out.println("Connected and assigned id " + meta.tuple.sensor + " by LMS.");
    }

    @Override
    public String id() {
        return tuple.sensor;
    }

    @Override
    public String zone() {
        return tuple.zone;
    }

    @Override
    public boolean powerOff() {
        if(power_on){
            lms.removeSensor(tuple);
            power_on = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean powerOn() {
        if(!power_on){
            lms.registerSensor(tuple.zone);
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

        if(lms != null) {
            lms.receiveAlert(new Alert(reading, tuple));
        }

        current = reading;
        readingLog.add(reading);

        if(measurement > alert_level){
            System.err.println("Reading is above alert level of " + alert_level + " at " + measurement + "!");
        } else {
            System.out.println("Registered new reading: " + measurement);
        }
    }
}
