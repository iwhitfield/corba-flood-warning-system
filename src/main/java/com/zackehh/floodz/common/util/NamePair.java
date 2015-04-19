package com.zackehh.floodz.common.util;

import org.omg.CosNaming.NamingContextExt;
import org.omg.PortableServer.POA;

/**
 * A wrapping pair for use by {@link NameServiceHandler} in order
 * to return a tuple of {@link org.omg.CosNaming.NamingContextExt} and {@link org.omg.PortableServer.POA}.
 */
public class NamePair {

    /**
     * The NameService.
     */
    private final NamingContextExt namingService;

    /**
     * The RootPOA after retrieval.
     */
    private final POA rootPOA;

    /**
     * Simple constructor, purely setting both values to the passed in values.
     *
     * @param namingContextExt the {@link org.omg.CosNaming.NamingContextExt} instance
     * @param rootPOA the {@link org.omg.PortableServer.POA} instance
     */
    public NamePair(NamingContextExt namingContextExt, POA rootPOA){
        this.namingService = namingContextExt;
        this.rootPOA = rootPOA;
    }

    /**
     * Get the contained NameService.
     *
     * @return a {@link org.omg.CosNaming.NamingContextExt} instance
     */
    public NamingContextExt getNamingService(){
        return this.namingService;
    }

    /**
     * Get the contained RootPOA.
     *
     * @return a {@link org.omg.PortableServer.POA} instance
     */
    public POA getRootPOA(){
        return this.rootPOA;
    }

}