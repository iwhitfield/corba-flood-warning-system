package com.zackehh.floodz.sensor;

import com.zackehh.corba.lms.LMS;
import com.zackehh.corba.lms.LMSHelper;
import org.omg.CosNaming.NamingContextExt;

class SensorUtil {

    public static LMS findLMSBinding(NamingContextExt namingContextExt, String lmsName){
        LMS lms;
        // Obtain the Sensor reference in the Naming service
        try {
            // Retrieve a name service
            lms = LMSHelper.narrow(namingContextExt.resolve_str(lmsName));
            if(lms == null){
                return null;
            }
            return lms.ping() ? lms : null;
        } catch(Exception e) {
            return null;
        }
    }

}
