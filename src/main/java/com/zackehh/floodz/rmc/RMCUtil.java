package com.zackehh.floodz.rmc;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;

public class RMCUtil {

    public static String generateAlertId(Alert alert){
        return generateAlertId(alert.meta);
    }

    public static String generateAlertId(MetaData metadata){
        return metadata.lms + ":" + metadata.sensorMeta.zone;
    }

}
