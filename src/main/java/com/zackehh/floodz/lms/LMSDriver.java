package com.zackehh.floodz.lms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zackehh.floodz.common.Constants;
import com.zackehh.floodz.common.NameServiceHandler;
import com.zackehh.floodz.util.InputReader;
import corba.Alert;
import corba.LMSHelper;
import corba.LMSPOA;
import corba.RMCHelper;
import corba.Reading;
import corba.SensorMeta;
import corba.SensorTuple;
import org.apache.commons.io.IOUtils;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LMSDriver extends LMSPOA {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Reading>> zoneMapping = new ConcurrentHashMap<>();
    private final List<Alert> alertLog = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    private HashMap<String, Levels> levels;

    private InputReader console;
    private corba.RMC rmc;
    private String name;
    private final ORB orb;

    LMSDriver(){
        // testing ctor
        this.levels = getOnMyLevels();
        this.orb = null;
    }

    public LMSDriver(String[] args, LMSArgs lArgs){
        levels = getOnMyLevels();

        name = lArgs.name;
        if (name == null) {
            console = new InputReader(System.in);
            name = console.readString("Please enter the station name: ");
        }

        logger.info("Registered Local Monitoring Station: {}", name);

        // Initialise the ORB
        orb = ORB.init(args, null);

        NamingContextExt nameService;
        try {
            // Retrieve a name service
            nameService = NameServiceHandler.register(orb, this, name, LMSHelper.class);
            if(nameService == null){
                throw new Exception();
            }
        } catch(Exception e) {
            throw new IllegalStateException("Retrieved name service is null!");
        }

        // Obtain the Sensor reference in the Naming service
        try {
            // Retrieve a name service
            rmc = RMCHelper.narrow(nameService.resolve_str(Constants.REGIONAL_MONITORING_CENTRE));
            if(rmc == null){
                throw new Exception();
            }
        } catch(Exception e) {
            throw new IllegalStateException("Unable to find RMC!");
        }


        // test out RMC connection
        if (!rmc.testConnection(name)) {
            throw new IllegalStateException("RMC Connection test failed!");
        } else {
            logger.info("Made successful connection to RMC");
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

        int alert_level = levels.get(alert.pair.zone).getAlertLevel();

        if(alert.reading.measurement > alert_level){

            logger.warn("Registered alert {} from Sensor #{}", alert.reading.measurement, alert.pair.sensor);

            int warnings = 0;
            for(Map.Entry<String, Reading> zoneMap : zone.entrySet()){
                if(zoneMap.getValue().measurement > alert_level){
                    warnings++;
                }
            }

            double half = Math.floor(zone.size() / 2);

            if(warnings >= half && zone.size() > 1){

                logger.info("Multiple warnings for zone `{}`, forwarding to RMC...", alert.pair.zone);

                if(rmc != null) {
                    rmc.receiveAlert(alert);
                }

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

        if(!levels.containsKey(zone)){
            levels.put(zone, levels.get("default"));
        }
        return new SensorMeta(new SensorTuple(zone, id), levels.get(zone).getAlertLevel());
    }

    public ORB getEmbeddedOrb(){
        return this.orb;
    }

    ConcurrentHashMap<String, Reading> getZone(String zone){
        return zoneMapping.get(zone);
    }

    ConcurrentHashMap<String, ConcurrentHashMap<String, Reading>> getZoneMapping(){
        return zoneMapping;
    }

    private HashMap<String, Levels> getOnMyLevels(){
        try {
            return mapper.readValue(
                    IOUtils.toString(LMS.class.getResourceAsStream("/levels.json")),
                    new TypeReference<HashMap<String, Levels>>() { }
            );
        } catch(IOException e) {
            logger.warn("Unable to retrieve level mapping, creating default...");

            final Levels def = new Levels(Constants.DEFAULT_WARNING_LEVEL, Constants.DEFAULT_ALERT_LEVEL);

            logger.info("Set default warning level to {}, alert level to {}", def.getWarningLevel(), def.getAlertLevel());

            return new HashMap<String, Levels>(){{
                put("default", def);
            }};
        }
    }
}
