package com.zackehh.floodz.common;

/**
 * Set values to be shared across each module (Sensor/LMS/RMC). Used as a
 * central way to avoid errors due to things such as typos.
 */
public final class Constants {

    private Constants(){ }

    public static final Integer DEFAULT_ALERT_LEVEL = 60;
    public static final Integer DEFAULT_WARNING_LEVEL = 40;

    public static final String LOCAL_MONITORING_STATION = "Local Monitoring Station";
    public static final String NAME_SERVICE = "NameService";
    public static final String REGIONAL_MONITORING_CENTRE = "Regional Monitoring Centre";
    public static final String ROOT_POA = "RootPOA";
    public static final String SENSOR = "Sensor";

}
