package com.zackehh.floodz.lms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zackehh.floodz.util.InputReader;
import corba.*;
import org.apache.commons.io.IOUtils;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
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

        console = new InputReader(System.in);

        levels = mapper.readValue(
                IOUtils.toString(LMS.class.getResourceAsStream("/levels.json")),
                new TypeReference<HashMap<String, Levels>>() {}
        );

        try {
            name = console.readString("Please enter the station name: ");

            logger.info("Registered Local Monitoring Station: {}", name);

            // Initialise the ORB
            ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            if (rootpoa != null) {
                rootpoa.the_POAManager().activate();
            } else {
                logger.error("Unable to retrieve POA!");
                return;
            }

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(new LMS());
            corba.LMS server_ref = LMSHelper.narrow(ref);

            // Get a reference to the Naming service
            org.omg.CORBA.Object nameServiceObj = orb.resolve_initial_references("NameService");
            if (nameServiceObj == null) {
                logger.error("Retrieved name service is null!");
                return;
            }

            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt nameService = NamingContextExtHelper.narrow(nameServiceObj);

            // bind the Count object in the Naming service
            NameComponent[] countName = nameService.to_name(name);
            nameService.rebind(countName, server_ref);

            // Obtain the Sensor reference in the Naming service
            rmc = RMCHelper.narrow(nameService.resolve_str("Regional Monitoring Station"));

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
        logger.info("Received alert from sensor #{} in {}", alert.pair.sensor, alert.pair.zone);

        ConcurrentHashMap<String, Reading> zone = zoneMapping.get(alert.pair.zone);

        if(zone != null){
            zone.put(alert.pair.sensor, alert.reading);
        } else {
            logger.warn("Measurement received from unregistered zone; {}", alert.pair.zone);
            return;
        }

        alertLog.add(alert);

        logger.info("Registered alert {} from sensor #{}", alert.reading, alert.pair.sensor);

        if(alert.reading.measurement > 50){
            int warnings = 0;
            for(Map.Entry<String, Reading> zoneMap : zone.entrySet()){
                if(zoneMap.getValue().measurement > 50){
                    warnings++;
                }
            }

            if(warnings > Math.ceil(zone.size() / 2)){

                logger.info("Multiple warnings for {}, forwarding to RMC...", alert.pair.zone);

                rmc.receiveAlert(alert);

            }
        }
    }

    @Override
    public void removeSensor(SensorPair pair) {
        logger.info("Removed Sensor #{} from {}", pair.sensor, pair.zone);
        if(zoneMapping.containsKey(pair.zone)){
            zoneMapping.get(pair.zone).remove(pair.sensor);
        }
    }

    @Override
    public SensorMeta registerSensor(String zone) {

        final Reading reading = new Reading();
        final String id;

        if (zoneMapping.containsKey(zone)) {
            ConcurrentHashMap<String, Reading> zoneMap = zoneMapping.get(zone);

            id = zoneMap.size() + "";

            zoneMap.put(id, reading);
        } else {
            id = "1";
            zoneMapping.put(zone, new ConcurrentHashMap<String, Reading>() {{
                put(id, reading);
            }});
        }
        logger.info("Added Sensor #{} to {}", id, zone);

        Levels sensorLevels;
        if(levels.containsKey(zone)){
            sensorLevels = levels.get(zone);
        } else {
            sensorLevels = levels.get("default");
        }
        return new SensorMeta(new SensorPair(zone, id), sensorLevels.getAlertLevel());
    }

    ConcurrentHashMap<String, Reading> getZone(String zone){
        return zoneMapping.get(zone);
    }

    ConcurrentHashMap<String, ConcurrentHashMap<String, Reading>> getZoneMapping(){
        return zoneMapping;
    }

}
