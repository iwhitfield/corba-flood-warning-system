package com.zackehh.floodz.common;

/**
 * Set values to be shared across each module (Sensor/LMS/RMC). Used as a
 * central way to avoid errors due to things such as typos.
 */
public final class Constants {

    /**
     * Shouldn't ever make a new instance of this class, it's static.
     */
    private Constants(){ }

    /**
     * A default value for an alert level if none are previously set.
     */
    public static final Integer DEFAULT_ALERT_LEVEL = 60;

    /**
     * A default value for a warning level if none are previously set.
     */
    public static final Integer DEFAULT_WARNING_LEVEL = 40;

    /**
     * Constant name for a Local Monitoring System.
     */
    public static final String LOCAL_MONITORING_STATION = "Local Monitoring Station";

    /**
     * The NameService constant, in order to avoid any typos when retrieving.
     */
    public static final String NAME_SERVICE = "NameService";

    /**
     * The name of the (singular) Regional Monitoring Centre, to avoid typos.
     */
    public static final String REGIONAL_MONITORING_CENTRE = "Regional Monitoring Centre";

    /**
     * The name of the RootPOA to retrieve via the CORBA service.
     */
    public static final String ROOT_POA = "RootPOA";

    /**
     * Constant name for a sensor.
     */
    public static final String SENSOR = "Sensor";

}
