package com.zackehh.floodz.common;

import org.omg.CosNaming.NamingContextExt;
import org.omg.PortableServer.POA;

public class NamingPair {

    private final NamingContextExt namingService;
    private final POA rootPOA;

    public NamingPair(NamingContextExt namingContextExt, POA rootPOA){
        this.namingService = namingContextExt;
        this.rootPOA = rootPOA;
    }

    public NamingContextExt getNamingService(){
        return this.namingService;
    }

    public POA getRootPOA(){
        return this.rootPOA;
    }

}