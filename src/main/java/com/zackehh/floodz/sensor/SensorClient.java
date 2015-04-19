package com.zackehh.floodz.sensor;

import com.beust.jcommander.JCommander;
import com.zackehh.corba.common.Reading;
import com.zackehh.floodz.common.Constants;
import java.util.Date;
import java.util.Scanner;

/**
 * The command-line client for the Sensor, allowing the user
 * to input commands; such as inputting measurements or power
 * the sensor off/on.
 */
public class SensorClient {

    /**
     * The SensorDriver instance inheriting the POA.
     */
    private static SensorDriver sensorDriver;

    /**
     * MAin entry point for a Sensor. Parses arguments and creates
     * a new driver, before moving to accept input.
     *
     * @param args the program arguments
     */
    public static void main(String args[]) throws Exception {
        // create a new sensor arguments object
        SensorArgs sensorArgs = new SensorArgs();

        // provide JCommander with the arguments
        JCommander j = new JCommander(sensorArgs);

        // set parse arguments
        j.setAcceptUnknownOptions(true);
        // parse the arguments
        j.parse(args);

        // create a new driver
        sensorDriver = new SensorDriver(args, sensorArgs);

        // take in user input
        processInput();
    }

    /**
     * Process user input for the remainder of the program
     * execution. Adds the ability to view alert states and
     * potentially power the sensor on/off.
     */
    private static void processInput(){
        // create a new scanner
        Scanner scanner = new Scanner(System.in);

        // display menu
        System.out.println("");
        System.out.println("Enter a reading, or choose from one of the commands below:");
        System.out.println("- exit, exits the Sensor");
        System.out.println("- history, views Sensor reading history");
        System.out.println("- poweroff, turns off the Sensor");
        System.out.println("- poweron, turns on the Sensor");
        System.out.println("- reset, resets the Sensor logs");
        System.out.println("- status, views current Sensor alert status");
        System.out.println("");

        while(true){

            // prompt user
            System.out.print("Please select an option: ");

            // get next string
            String input = scanner.next();

            // error or invalid input
            if (input == null) {
                System.err.println("Invalid measurement provided!" + "\n");
                continue;
            }

            // user wishes to exit
            if (input.equals("exit") || input.startsWith("bye")) {
                return;
            }

            // user wishes to retrieve current status
            if (input.equals("status")) {
                Reading current = sensorDriver.currentReading();
                if(current != null){
                    System.out.print("Previous reading made at " + new Date(current.time) +
                            ", registering at " + current.measurement + ". ");
                    if(current.measurement > Constants.DEFAULT_ALERT_LEVEL){
                        System.out.println("This is above safety levels.\n");
                    } else {
                        System.out.println("\n");
                    }
                } else {
                    System.err.println("No readings have been registered yet!\n");
                }
                continue;
            }

            // get alert history
            if (input.equals("history")) {

                // retrieve readings from driver
                Reading[] readings = sensorDriver.getReadingLog();

                // list to display
                String readingLog = "\n";

                // translate alerts to readable format
                for(int i = 0, j = readings.length; i < j; i++){
                    if(i != 0){
                        readingLog += "\n";
                    }
                    readingLog += "[" + new Date(readings[i].time) + " => " + readings[i].measurement + "]";
                }

                // display appropriate message
                if(readings.length != 0){
                    System.out.println(readingLog + "\n");
                } else {
                    System.err.println("No readings have been registered yet!" + "\n");
                }

                continue;
            }

            // power off the sensor
            if (input.equals("poweroff")) {
                if(sensorDriver.powerOff()){
                    System.out.println("Turned sensor off.\n");
                } else {
                    System.err.println("System is already powered off!\n");
                }
                continue;
            }

            // power on the sensor
            if (input.equals("poweron")) {
                if(sensorDriver.powerOn()){
                    System.out.println("Turned sensor on.\n");
                } else {
                    System.err.println("System is already switched on!\n");
                }
                continue;
            }

            // reset the sensor
            if (input.equals("reset")) {
                sensorDriver.reset();
                System.out.println("System reset.\n");
                continue;
            }

            // measurement provided, send an alert
            if (input.equals("100") || input.matches("\\d{1,2}")) {
                sensorDriver.sendAlert(Integer.parseInt(input));
            } else {
                System.err.println("Invalid measurement provided!");
            }

            System.out.println("");

        }
    }
}
