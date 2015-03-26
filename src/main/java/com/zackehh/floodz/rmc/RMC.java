package com.zackehh.floodz.rmc;

import corba.Alert;
import corba.RMCHelper;
import corba.RMCPOA;
import corba.SensorPair;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RMC extends RMCPOA {

    private static final Logger logger = LoggerFactory.getLogger(RMC.class);

    private final List<Alert> alerts = new ArrayList<>();

    private static NamingContextExt nameService;

    public static void main(String[] args) {
        try {
            // create and initialise the ORB
            final ORB orb = ORB.init(args, null);

            // get reference to rootpoa & activate the POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            if (rootpoa != null) {
                rootpoa.the_POAManager().activate();
            } else {
                logger.error("Unable to retrieve POA!");
                return;
            }

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(new RMC());
            corba.RMC server_ref = RMCHelper.narrow(ref);

            // Get a reference to the Naming service
            org.omg.CORBA.Object nameServiceObj = orb.resolve_initial_references("NameService");
            if (nameServiceObj == null) {
                logger.error("Retrieved name service is null!");
                return;
            }

            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            nameService = NamingContextExtHelper.narrow(nameServiceObj);

            // bind the Count object in the Naming service
            NameComponent[] countName = nameService.to_name("Regional Monitoring Station");
            nameService.rebind(countName, server_ref);

            // Server has loaded up correctly
            logger.info("Remote Monitoring Centre is operational.");

            // Wait for invocations from clients upon a new thread
            orb.run();
        } catch (Exception e) {
            logger.error("{}", e);
            e.printStackTrace(System.out);
        }
    }

    @Override
    public synchronized void cancelAlert(SensorPair pair) {

        int size = alerts.size();

        for(int i = 0; i < size; i++){

            Alert alert = alerts.get(i);

            if(alert.pair.equals(pair)){
                alerts.remove(i);
                break;
            }

        }

        if(alerts.size() == size - 1){
            logger.info("Removed alert from sensor #{} in {}", pair.sensor, pair.zone);
        } else {
            logger.warn("Request to remove unknown alert from sensor #{} in {}", pair.sensor, pair.zone);
        }

    }

    @Override
    public synchronized void receiveAlert(Alert alert) {

        boolean stored = false;

        for(int i = 0, j = alerts.size(); i < j; i++){

            Alert storedAlert = alerts.get(i);

            if(storedAlert.pair.equals(alert.pair)){
                alerts.set(i, alert);
                stored = true;
                break;
            }

        }

        if(stored){
            alerts.add(alert);
        }

        logger.info("Received alert from sensor #{} in {}", alert.pair.sensor, alert.pair.zone);
    }

    @Override
    public Alert[] getAlerts(){
        return alerts.toArray(new Alert[alerts.size()]);
    }
}
