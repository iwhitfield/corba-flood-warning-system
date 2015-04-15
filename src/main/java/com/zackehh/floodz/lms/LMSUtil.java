package com.zackehh.floodz.lms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zackehh.corba.rmc.RMC;
import com.zackehh.corba.rmc.RMCHelper;
import com.zackehh.floodz.common.Constants;
import com.zackehh.floodz.common.Levels;
import org.apache.commons.io.IOUtils;
import org.omg.CosNaming.NamingContextExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

class LMSUtil {

    private static final Logger logger = LoggerFactory.getLogger(LMSUtil.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static RMC findRMCBinding(NamingContextExt namingContextExt){
        RMC rmc;
        // Obtain the Sensor reference in the Naming service
        try {
            // Retrieve a name service
            rmc = RMCHelper.narrow(namingContextExt.resolve_str(Constants.REGIONAL_MONITORING_CENTRE));
            if(rmc == null){
                return null;
            }
            return rmc.ping() ? rmc : null;
        } catch(Exception e) {
            return null;
        }
    }

    public static Levels getLevelsForZone(HashMap<String, Levels> levels, String zone){
        if(!levels.containsKey(zone)){
            return levels.get("default");
        }
        return levels.get(zone);
    }

    public static HashMap<String, Levels> getOnMyLevels() {
        try {
            return mapper.readValue(
                    IOUtils.toString(LMSClient.class.getResourceAsStream("/levels.json")),
                    new TypeReference<HashMap<String, Levels>>() { }
            );
        } catch(IOException e) {
            logger.warn("Unable to retrieve level mapping, creating default...");

            final Levels def = new Levels(Constants.DEFAULT_WARNING_LEVEL, Constants.DEFAULT_ALERT_LEVEL);

            logger.info("Set default warning level to {}, alert level to {}", def.getWarningLevel(), def.getAlertLevel());

            return new HashMap<String, Levels>(){{
                put("default", def);
            }};
        }
    }
}
