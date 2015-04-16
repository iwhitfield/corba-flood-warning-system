package com.zackehh.floodz.sensor;

import com.zackehh.corba.lms.LMS;
import com.zackehh.corba.lms.LMSHelper;
import com.zackehh.floodz.common.util.NamingServiceHandler;
import org.omg.CosNaming.NamingContextExt;

class SensorUtil {

    public static LMS findLMSBinding(NamingContextExt namingContextExt, String lmsName){
        LMS lms = NamingServiceHandler.retrieveObject(namingContextExt, lmsName, LMS.class, LMSHelper.class);
        try {
            return lms != null && lms.ping() ? lms : null;
        } catch(Exception e) {
            return null;
        }
    }

}
