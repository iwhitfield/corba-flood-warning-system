package com.zackehh.floodz.sensor;

import com.beust.jcommander.Parameter;

class SensorArgs {

    @SuppressWarnings("unused")
    @Parameter(names = "-lms", description = "Name of the LMS to connect to")
    public String lms;

    @SuppressWarnings("unused")
    @Parameter(names = "-zone", description = "Zone of the current sensor")
    public String zone;

}
