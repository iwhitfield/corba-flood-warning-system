package com.zackehh.floodz.sensor;

import com.beust.jcommander.JCommander;
import com.zackehh.floodz.common.NameServiceHandler;
import com.zackehh.floodz.common.NamingPair;
import com.zackehh.floodz.util.InputReader;
import corba.Alert;
import corba.LMSHelper;
import corba.Reading;
import corba.SensorHelper;
import corba.SensorMeta;
import corba.SensorPOA;
import corba.SensorPair;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Sensor extends SensorPOA {

    private static final Logger logger = LoggerFactory.getLogger(Sensor.class);
    private static final List<Reading> readingLog = new ArrayList<>();

    private static Integer alert_level = 0;
    private static Integer current = 0;
    private static SensorPair pair;

    private static corba.LMS lms;
    private static InputReader console;

    public static void main(String args[]) throws Exception {

        SensorArgs sensorArgs = new SensorArgs();
        JCommander j = new JCommander(sensorArgs);

        j.setAcceptUnknownOptions(true);
        j.parse(args);

        console = new InputReader(System.in);

        String lmsName = sensorArgs.lms;
        if(lmsName == null){
            lmsName = console.readString("Please enter the local station name: ");
        }

        String zoneName = sensorArgs.zone;
        if(zoneName == null){
            zoneName = console.readString("Please enter the sensor zone: ");
        }

        logger.info("Sensor of zone {} connecting to LMS: {}", zoneName, lmsName);

        // Initialise the ORB
        ORB orb = ORB.init(args, null);

        // Retrieve a name service
        NamingPair namingPair = NameServiceHandler.retrieveNameService(orb);
        if(namingPair == null){
            logger.error("Retrieved name service is null!");
            return;
        }

        NamingContextExt nameService = namingPair.getNamingService();

        lms = LMSHelper.narrow(nameService.resolve_str(lmsName));
        if(lms == null){
            logger.error("Unable to find an LMS!");
            return;
        }

        SensorMeta meta = lms.registerSensor(zoneName);

        NameServiceHandler.bind(
                namingPair.getNamingService(),
                NameServiceHandler.createRef(namingPair, new Sensor(), SensorHelper.class),
                meta.pair.sensor
        );

        pair = meta.pair;
        alert_level = meta.alert_level;

        logger.info("Assigned id {} by LMS", meta.pair.sensor);

        processInput();
    }

    @Override
    public String id() {
        return pair.sensor;
    }

    @Override
    public String zone() {
        return pair.zone;
    }

    @Override
    public int currentReading() {
        return current;
    }

    @Override
    public Reading[] getReadingLog() {
        return readingLog.toArray(new Reading[readingLog.size()]);
    }

    private static void processInput(){
        while(true) {
            // Obtain the user input
            Integer input = console.readInteger("Please enter a new measurement value, or type 'exit' to quit: ");

            if(input == null){
                logger.error("Invalid measurement provided!");
                break;
            } else {

                Reading reading = new Reading(System.currentTimeMillis(), input);

                lms.receiveAlert(new Alert(reading, pair));

                readingLog.add(reading);

                if(input > alert_level){
                    logger.warn("Reading is above alert level of {} at {}!", alert_level, input);
                } else {
                    logger.info("Registered new reading: {}", input);
                }

            }
        }
    }
}
