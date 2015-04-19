package com.zackehh.floodz.lms;

import com.beust.jcommander.Parameter;

/**
 * LMS arguments object, parsing LMS-required flags
 * to allow a user to specify setups view command line when
 * starting.
 */
class LMSArgs {

    /**
     * The name of the new LMS.
     */
    @Parameter(names = "-name", description = "Name of the Local Monitoring Station")
    public String name;

}
