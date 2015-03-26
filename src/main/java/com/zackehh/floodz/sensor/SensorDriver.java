package com.zackehh.floodz.sensor;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.Reading;
import com.zackehh.corba.common.SensorMeta;
import com.zackehh.corba.common.SensorTuple;
import com.zackehh.corba.lms.LMS;
import com.zackehh.corba.lms.LMSHelper;
import com.zackehh.corba.sensor.SensorHelper;
import com.zackehh.corba.sensor.SensorPOA;
import com.zackehh.floodz.common.NameServiceHandler;
import com.zackehh.floodz.common.NamingPair;
import com.zackehh.floodz.util.InputReader;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SensorDriver extends SensorPOA {

    private final Logger logger = LoggerFactory.getLogger(SensorDriver.class);
    private final List<Reading> readingLog = new ArrayList<>();

    private Reading current = null;
    private SensorTuple tuple;

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

        // Initialise the ORB
        orb = ORB.init(args, null);

        // Retrieve a name service
        NamingPair namingPair;
        try {
            // Retrieve a name service
            namingPair = NameServiceHandler.retrieveNameService(orb);
            if(namingPair == null){
                throw new Exception();
            }
        } catch(Exception e) {
            throw new IllegalStateException("Retrieved name service is null!");
        }

        NamingContextExt nameService = namingPair.getNamingService();

        console = new InputReader(System.in);

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

        String zoneName = sArgs.zone;
        if(zoneName == null){
            zoneName = console.readString("Please enter the sensor zone: ");
        }

        logger.info("Sensor of zone {} connecting to LMS: {}", zoneName, lmsName);

        SensorMeta meta = lms.registerSensor(zoneName);

        try {
            NameServiceHandler.bind(
                    namingPair.getNamingService(),
                    NameServiceHandler.createRef(namingPair, this, SensorHelper.class),
                    meta.tuple.sensor
            );
        } catch(Exception e) {
            throw new IllegalStateException("Unable to bind Sensor to NameService!");
        }

        tuple = meta.tuple;
        alert_level = meta.alert_level;

        logger.info("Assigned id {} by LMS", meta.tuple.sensor);
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
    public Reading currentReading() {
        return current;
    }

    @Override
    public Reading[] getReadingLog() {
        return readingLog.toArray(new Reading[readingLog.size()]);
    }

    public void sendAlert(Integer input){
        Reading reading = new Reading(System.currentTimeMillis(), input);

        if(lms != null) {
            lms.receiveAlert(new Alert(reading, tuple));
        }

        current = reading;
        readingLog.add(reading);

        if(input > alert_level){
            logger.warn("Reading is above alert level of {} at {}!", alert_level, input);
        } else {
            logger.info("Registered new reading: {}", input);
        }
    }
}
