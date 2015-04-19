package com.zackehh.floodz.sensor;

import com.beust.jcommander.Parameter;

/**
 * Sensor arguments object, parsing sensor-required flags
 * to allow a user to specify setups view command line when
 * starting.
 */
class SensorArgs {

    /**
     * The name of the Local Monitoring Station to connect to.
     */
    @Parameter(names = "-lms", description = "Name of the LMS to connect to")
    public String lms;

    /**
     * The name of the zone the Sensor will belong to.
     */
    @Parameter(names = "-zone", description = "Zone of the current sensor")
    public String zone;

}
