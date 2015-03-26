package com.zackehh.floodz.sensor;

import com.beust.jcommander.JCommander;
import com.zackehh.floodz.util.InputReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sensor {

    private static final Logger logger = LoggerFactory.getLogger(Sensor.class);

    private static InputReader console;
    private static SensorDriver sensorDriver;

    public static void main(String args[]) throws Exception {

        SensorArgs sensorArgs = new SensorArgs();
        JCommander j = new JCommander(sensorArgs);

        j.setAcceptUnknownOptions(true);
        j.parse(args);

        console = new InputReader(System.in);
        sensorDriver = new SensorDriver(args, sensorArgs);

        processInput();
    }

    private static void processInput(){
        while(true) {
            // Obtain the user input
            Integer input = console.readInteger("Please enter a new measurement value, or type 'exit' to quit: ");

            if(input == null){
                logger.error("Invalid measurement provided!");
                break;
            } else {

                sensorDriver.sendAlert(input);

                System.out.println("");

            }
        }
    }
}
