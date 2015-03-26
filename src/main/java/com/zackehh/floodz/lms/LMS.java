package com.zackehh.floodz.lms;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zackehh.floodz.common.Constants;
import com.zackehh.floodz.common.NameServiceHandler;
import com.zackehh.floodz.util.InputReader;
import corba.*;
import org.apache.commons.io.IOUtils;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LMS extends LMSPOA {

    private static final Logger logger = LoggerFactory.getLogger(LMS.class);

    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Reading>> zoneMapping = new ConcurrentHashMap<>();
    private static final List<Alert> alertLog = new ArrayList<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static HashMap<String, Levels> levels;
    private static InputReader console;
    private static corba.RMC rmc;
    private static String name;

    public static void main(String[] args) throws Exception {

        levels = mapper.readValue(
                IOUtils.toString(LMS.class.getResourceAsStream("/levels.json")),
                new TypeReference<HashMap<String, Levels>>() {}
        );

        try {

            LMSArgs lArgs = new LMSArgs();
            JCommander j = new JCommander(lArgs);

            j.setAcceptUnknownOptions(true);
            j.parse(args);

            console = new InputReader(System.in);

            name = lArgs.name;
            if(name == null){
                name = console.readString("Please enter the station name: ");
            }

            logger.info("Registered Local Monitoring Station: {}", name);

            // Initialise the ORB
            ORB orb = ORB.init(args, null);

            // Retrieve a name service
            NamingContextExt nameService = NameServiceHandler.register(orb, new LMS(), name, LMSHelper.class);
            if(nameService == null){
                logger.error("Retrieved name service is null!");
                return;
            }

            // Obtain the Sensor reference in the Naming service
            rmc = RMCHelper.narrow(nameService.resolve_str(Constants.REGIONAL_MONITORING_CENTRE));

            orb.run();
        } catch (Exception e) {
            logger.error("Error occurred: " + e);
            e.printStackTrace();
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Alert[] alertLog() {
        return alertLog.toArray(new Alert[alertLog.size()]);
    }

    @Override
    public void receiveAlert(Alert alert) {
        logger.info("Received alert from sensor #{} in zone `{}`", alert.pair.sensor, alert.pair.zone);

        ConcurrentHashMap<String, Reading> zone = zoneMapping.get(alert.pair.zone);

        if(zone != null){
            zone.put(alert.pair.sensor, alert.reading);
        } else {
            logger.warn("Measurement received from unregistered zone: {}", alert.pair.zone);
            return;
        }

        alertLog.add(alert);

        if(alert.reading.measurement > 50){

            logger.warn("Registered alert {} from Sensor #{}", alert.reading.measurement, alert.pair.sensor);

            int warnings = 0;
            for(Map.Entry<String, Reading> zoneMap : zone.entrySet()){
                if(zoneMap.getValue().measurement > 50){
                    warnings++;
                }
            }

            double half = Math.ceil(zone.size() / 2);

            if(warnings > half && half > 1){

                logger.info("Multiple warnings for zone `{}`, forwarding to RMC...", alert.pair.zone);

                rmc.receiveAlert(alert);

            }
        } else {
            logger.info("Registered reading {} from Sensor #{}", alert.reading.measurement, alert.pair.sensor);
        }
    }

    @Override
    public void removeSensor(SensorTuple tuple) {
        logger.info("Removed Sensor #{} from zone `{}`", tuple.sensor, tuple.zone);
        if(zoneMapping.containsKey(tuple.zone)){
            zoneMapping.get(tuple.zone).remove(tuple.sensor);
        }
    }

    @Override
    public SensorMeta registerSensor(String zone) {

        final Reading reading = new Reading();
        final String id;

        if (zoneMapping.containsKey(zone)) {
            ConcurrentHashMap<String, Reading> zoneMap = zoneMapping.get(zone);

            id = (zoneMap.size() + 1) + "";

            zoneMap.put(id, reading);
        } else {
            id = "1";
            zoneMapping.put(zone, new ConcurrentHashMap<String, Reading>() {{
                put(id, reading);
            }});
        }
        logger.info("Added Sensor #{} to zone `{}`", id, zone);

        Levels sensorLevels;
        if(levels.containsKey(zone)){
            sensorLevels = levels.get(zone);
        } else {
            sensorLevels = levels.get("default");
        }
        return new SensorMeta(new SensorTuple(zone, id), sensorLevels.getAlertLevel());
    }

    ConcurrentHashMap<String, Reading> getZone(String zone){
        return zoneMapping.get(zone);
    }

    ConcurrentHashMap<String, ConcurrentHashMap<String, Reading>> getZoneMapping(){
        return zoneMapping;
    }

}
