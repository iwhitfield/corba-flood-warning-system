package com.zackehh.floodz.sensor;

import com.beust.jcommander.Parameter;

class SensorArgs {

    @Parameter(names = "-lms", description = "Name of the LMS to connect to")
    public String lms;

    @Parameter(names = "-zone", description = "Zone of the current sensor")
    public String zone;

}
