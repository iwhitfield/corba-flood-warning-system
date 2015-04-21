package com.zackehh.floodz.lms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zackehh.floodz.common.Constants;
import com.zackehh.floodz.common.util.Levels;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

/**
 * Utility class for the LMS to provide methods to parse out
 * resources and connect to RMC systems.
 */
class LMSUtil {

    /**
     * Logging implementation for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(LMSUtil.class);

    /**
     * A mapper to read the resources files.
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Retrieves the correct Levels instance to use for the current zone entry.
     * If the map of zones does not include this zone, default to using a
     * pre-required set of levels.
     *
     * @param levels the levels mapping
     * @param zone the zone name
     * @return a Levels instance
     */
    public static Levels getLevelsForZone(HashMap<String, Levels> levels, String zone){
        return !levels.containsKey(zone) ? levels.get("default") : levels.get(zone);
    }

    /**
     * Parses the levels JSON configuration file and returns it. If the file
     * does not exist, a new Map will be created with default values defined
     * in Constants. This just allows for more granular level definitions.
     *
     * @return a HashMap of <String, Levels>
     */
    public static HashMap<String, Levels> retrieveZoneLevels() {
        try {
            // parse the levels.json resource
            return mapper.readValue(
                    IOUtils.toString(LMSClient.class.getResourceAsStream("/levels.json")),
                    new TypeReference<HashMap<String, Levels>>() { }
            );
        } catch(IOException e) {
            // log a warning
            logger.warn("Unable to retrieve level mapping, creating default...");

            // create a levels instance from the Constants
            final Levels def = new Levels(Constants.DEFAULT_WARNING_LEVEL, Constants.DEFAULT_ALERT_LEVEL);

            // log an info message
            logger.info("Set default warning level to {}, alert level to {}", def.getWarningLevel(), def.getAlertLevel());

            // return the map of defaults
            return new HashMap<String, Levels>(){{
                put("default", def);
            }};
        }
    }
}
