package com.zackehh.floodz.rmc;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;
import com.zackehh.corba.lms.LMS;
import com.zackehh.corba.lms.LMSHelper;
import com.zackehh.corba.rmc.RMCHelper;
import com.zackehh.corba.rmc.RMCPOA;
import com.zackehh.floodz.common.Constants;
import com.zackehh.floodz.common.util.NamingServiceHandler;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RMCDriver extends RMCPOA {

    public final Set<String> knownStations = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(RMCDriver.class);

    private NamingContextExt nameService;

    private final List<Alert> alerts = new ArrayList<>();
    private final ORB orb;
    private final RMCClient rmcClient;
    private final String name = Constants.REGIONAL_MONITORING_CENTRE;

    @SuppressWarnings("unused")
    RMCDriver(){
        // testing ctor
        this.orb = null;
        this.rmcClient = null;
    }

    public RMCDriver(String[] args, RMCClient rmcClient){
        // Initialise the ORB
        this.orb = ORB.init(args, null);
        this.rmcClient = rmcClient;

        try {
            // Retrieve a name service
            nameService = NamingServiceHandler.register(orb, this, name, RMCHelper.class);
            if(nameService == null){
                throw new Exception();
            }
        } catch(Exception e) {
            throw new IllegalStateException("Retrieved name service is null!");
        }

        // Server has loaded up correctly
        logger.info("Remote Monitoring Centre is operational.");
    }

    @Override
    public boolean ping() {
        return true;
    }

    @Override
    public synchronized void cancelAlert(MetaData metaData) {

        int size = alerts.size();

        for(int i = 0; i < size; i++){
            if(alerts.get(i).meta.sensorMeta.zone.equals(metaData.sensorMeta.zone)){
                logger.info("Removed alert from sensor #{} in {}", metaData.sensorMeta.sensor, metaData.sensorMeta.zone);
                alerts.remove(i);
                break;
            }
        }

        rmcClient.cancelAlert(metaData);

    }

    @Override
    public synchronized void receiveAlert(Alert alert) {

        boolean stored = false;

        for(int i = 0, j = alerts.size(); i < j; i++){

            Alert storedAlert = alerts.get(i);

            if(storedAlert.meta.sensorMeta.zone.equals(alert.meta.sensorMeta.zone)){
                alerts.set(i, alert);
                stored = true;
                break;
            }

        }

        if(!stored){
            alerts.add(alert);
        }

        rmcClient.addAlert(alert);

        logger.info("Received alert from sensor #{} in {}", alert.meta.sensorMeta.sensor, alert.meta.sensorMeta.zone);
    }

    @Override
    public boolean registerLMSConnection(String name) {
        logger.info("Successfully received connection from LMS `{}`", name);
        knownStations.add(name);
        return true;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Alert[] getAlerts(){
        return alerts.toArray(new Alert[alerts.size()]);
    }

    @Override
    public Alert[] getDistrictState(String district) {
        LMS lms = NamingServiceHandler.retrieveObject(
                nameService, district, LMS.class, LMSHelper.class
        );
        if(lms == null){
            return new Alert[]{};
        }
        return lms.getCurrentState();
    }

    @Override
    public String[] getKnownStations() {
        return knownStations.toArray(new String[knownStations.size()]);
    }

    public ORB getEmbeddedOrb(){
        return this.orb;
    }

    public LMS getConnectedLMS(String name) {
        return NamingServiceHandler.retrieveObject(
                nameService, name, LMS.class, LMSHelper.class
        );
    }
}
