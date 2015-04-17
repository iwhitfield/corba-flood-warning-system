package com.zackehh.floodz.lms;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;
import com.zackehh.corba.common.Reading;
import com.zackehh.corba.common.SensorMeta;
import com.zackehh.corba.lms.LMSHelper;
import com.zackehh.corba.lms.LMSPOA;
import com.zackehh.corba.rmc.RMC;
import com.zackehh.floodz.common.util.Levels;
import com.zackehh.floodz.common.util.NamingServiceHandler;
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

public class LMSDriver extends LMSPOA {

    // log4j
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // log based variables
    private final ConcurrentSkipListMap<String, Alert> alertStates = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<String, ConcurrentSkipListMap<String, Reading>> zoneMapping = new ConcurrentSkipListMap<>();
    private final List<Alert> alertLog = new ArrayList<>();

    // final variables
    private final HashMap<String, Levels> levels;
    private final ORB orb;

    // corba variables
    private NamingContextExt nameService;
    private RMC rmc;
    private String name;

    // testing ctor
    LMSDriver(){
        this.levels = LMSUtil.getOnMyLevels();
        this.orb = null;
    }

    public LMSDriver(String[] args, LMSArgs lArgs){
        levels = LMSUtil.getOnMyLevels();

        InputReader console = new InputReader(System.in);

        name = lArgs.name;
        if (name == null) {
            name = console.readString("Please enter the station name: ");
        }

        System.out.println("");
        logger.info("Registered Local Monitoring Station: {}", name);

        // Initialise the ORB
        orb = ORB.init(args, null);

        try {
            // Retrieve a name service
            nameService = NamingServiceHandler.register(orb, this, name, LMSHelper.class);
            if(nameService == null){
                throw new Exception();
            }
        } catch(Exception e) {
            throw new IllegalStateException("Retrieved name service is null!");
        }

        // Obtain the Sensor reference in the Naming service
        rmc = LMSUtil.findRMCBinding(nameService);
        if(rmc == null){
            throw new IllegalStateException("Unable to find RMC!");
        }

        // test out RMC connection
        if (!rmc.registerLMSConnection(name)) {
            throw new IllegalStateException("RMC Connection failed!");
        } else {
            logger.info("Made successful connection to RMC");
        }

        // shutdown hook
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

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean ping() {
        return true;
    }

    @Override
    public Alert[] getCurrentState() {
        List<Alert> currentState = new ArrayList<>();

        for(Map.Entry<String, Alert> zone : alertStates.entrySet()){
            currentState.add(zone.getValue());
        }

        return currentState.toArray(new Alert[currentState.size()]);
    }

    @Override
    public SensorMeta[] getRegisteredSensors() {
        List<SensorMeta> metaList = new ArrayList<>();

        for(Map.Entry<String, ConcurrentSkipListMap<String, Reading>> zoneMap : zoneMapping.entrySet()){

            List<String> keySet = new ArrayList<>(zoneMap.getValue().keySet());

            Collections.sort(keySet);

            for(String key : keySet){
                metaList.add(
                        new SensorMeta(
                                zoneMap.getKey(), key,
                                LMSUtil.getLevelsForZone(levels, zoneMap.getKey()).getAlertLevel()
                        )
                );
            }
        }

        return metaList.toArray(new SensorMeta[metaList.size()]);
    }

    @Override
    public Alert[] alertLog() {
        return alertLog.toArray(new Alert[alertLog.size()]);
    }

    @Override
    public void receiveAlert(final Alert alert) {
        logger.info("Received alert from sensor #{} in zone `{}`", alert.meta.sensorMeta.sensor, alert.meta.sensorMeta.zone);

        ConcurrentSkipListMap<String, Reading> zone = zoneMapping.get(alert.meta.sensorMeta.zone);

        if(zone != null){
            zone.put(alert.meta.sensorMeta.sensor, alert.reading);
        } else {
            logger.warn("Adding measurement from previously unregistered zone: {}", alert.meta.sensorMeta.zone);
            zone = new ConcurrentSkipListMap<String, Reading>(){{
                put(alert.meta.sensorMeta.sensor, alert.reading);
            }};
            zoneMapping.put(alert.meta.sensorMeta.zone, zone);
        }

        alertLog.add(alert);

        int alert_level = LMSUtil.getLevelsForZone(levels, alert.meta.sensorMeta.zone).getAlertLevel();

        if(alert.reading.measurement > alert_level){
            logger.warn("Registered alert {} from Sensor #{}", alert.reading.measurement, alert.meta.sensorMeta.sensor);
        } else {
            logger.info("Registered reading {} from Sensor #{}", alert.reading.measurement, alert.meta.sensorMeta.sensor);
        }

        int avg = 0;

        for(Map.Entry<String, Reading> zoneMap : zone.entrySet()){
            avg += zoneMap.getValue().measurement;
        }

        int size = zone.size();

        avg = Math.round((avg / size) * 100) / 100;

        try {
            rmc.ping();
        } catch(Exception e) {
            rmc = LMSUtil.findRMCBinding(nameService);
        }

        if((avg >= alert_level && size > 2) || (avg > alert_level && size > 1)){

            logger.info("Multiple warnings for zone `{}`, forwarding to RMC...", alert.meta.sensorMeta.zone);

            alert.reading.measurement = avg;

            if(rmc != null) {
                rmc.receiveAlert(alert);
            } else {
                logger.warn("RMC is unreachable!");
            }

            alertStates.put(alert.meta.sensorMeta.zone, new Alert(alert.meta, new Reading(alert.reading.time, avg)));
        } else {

            if(rmc != null) {
                rmc.cancelAlert(new MetaData(name, alert.meta.sensorMeta));
            } else {
                logger.warn("RMC is unreachable!");
            }

            if(alertStates.containsKey(alert.meta.sensorMeta.zone)){
                logger.info("Removed alert state for zone `{}`, forwarding to RMC...", alert.meta.sensorMeta.zone);
                alertStates.remove(alert.meta.sensorMeta.zone);
            }
        }

    }

    @Override
    public boolean removeSensor(SensorMeta sensorMeta) {
        logger.info("Removed Sensor #{} from zone `{}`", sensorMeta.sensor, sensorMeta.zone);
        if(zoneMapping.containsKey(sensorMeta.zone)){
            zoneMapping.get(sensorMeta.zone).remove(sensorMeta.sensor);
            return true;
        }
        return false;
    }

    @Override
    public SensorMeta registerSensor(String zone) {

        final Reading reading = new Reading();
        final String id;

        if (zoneMapping.containsKey(zone)) {
            ConcurrentSkipListMap<String, Reading> zoneMap = zoneMapping.get(zone);

            id = (zoneMap.size() + 1) + "";

            zoneMap.put(id, reading);
        } else {
            id = "1";
            zoneMapping.put(zone, new ConcurrentSkipListMap<String, Reading>() {{
                put(id, reading);
            }});
        }
        logger.info("Added Sensor #{} to zone `{}`", id, zone);

        return new SensorMeta(zone, id, LMSUtil.getLevelsForZone(levels, zone).getAlertLevel());
    }

    public ORB getEmbeddedOrb(){
        return this.orb;
    }

    ConcurrentSkipListMap<String, Reading> getZone(String zone){
        return zoneMapping.get(zone);
    }

    ConcurrentSkipListMap<String, ConcurrentSkipListMap<String, Reading>> getZoneMapping(){
        return zoneMapping;
    }
}
