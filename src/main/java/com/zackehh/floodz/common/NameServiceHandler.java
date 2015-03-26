package com.zackehh.floodz.common;

import corba.LMSHelper;
import corba.RMCHelper;
import corba.SensorHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NameServiceHandler {

    private static final Logger logger = LoggerFactory.getLogger(NameServiceHandler.class);

    public static void bind(NamingContextExt nameService, org.omg.CORBA.Object ref, String name) throws Exception {
        nameService.rebind(nameService.to_name(name), ref);
    }

    public static org.omg.CORBA.Object createRef(NamingPair namingPair, Servant o, Class helperClass) throws Exception {

        // get object reference from the servant
        org.omg.CORBA.Object ref = namingPair.getRootPOA().servant_to_reference(o);

        org.omg.CORBA.Object server_ref;
        switch(helperClass.getSimpleName()){
            case "LMSHelper":
                server_ref = LMSHelper.narrow(ref);
                break;
            case "RMCHelper":
                server_ref = RMCHelper.narrow(ref);
                break;
            case "SensorHelper":
                server_ref = SensorHelper.narrow(ref);
                break;
            default:
                throw new IllegalArgumentException("Unrecognised helper passed to #register: " + helperClass.getSimpleName());
        }

        return server_ref;
    }

    public static NamingContextExt register(ORB orb, Servant o, String name, Class helperClass) throws Exception {

        // Use NamingContextExt which is part of the Interoperable
        // Naming Service (INS) specification.
        NamingPair namingPair = retrieveNameService(orb);
        if(namingPair == null){
            return null;
        }

        NamingContextExt nameService = namingPair.getNamingService();

        // get object reference from the servant
        org.omg.CORBA.Object server_ref = createRef(namingPair, o, helperClass);

        bind(namingPair.getNamingService(), server_ref, name);

        return nameService;
    }

    public static NamingPair retrieveNameService(ORB orb) throws Exception {

        // get reference to rootpoa & activate the POAManager
        POA rootpoa = POAHelper.narrow(orb.resolve_initial_references(Constants.ROOT_POA));
        if (rootpoa != null) {
            rootpoa.the_POAManager().activate();
        } else {
            logger.error("Unable to retrieve POA!");
            return null;
        }

        // Get a reference to the Naming service
        org.omg.CORBA.Object nameServiceObj = orb.resolve_initial_references(Constants.NAME_SERVICE);
        if (nameServiceObj == null) {
            return null;
        }

        // Use NamingContextExt which is part of the Interoperable
        // Naming Service (INS) specification.
        return new NamingPair(NamingContextExtHelper.narrow(nameServiceObj), rootpoa);
    }

}
