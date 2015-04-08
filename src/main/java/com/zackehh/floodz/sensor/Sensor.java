package com.zackehh.floodz.sensor;

import com.beust.jcommander.JCommander;
import com.zackehh.corba.common.Reading;
import com.zackehh.floodz.common.Constants;
import java.util.Date;
import java.util.Scanner;

public class Sensor {

    private static SensorDriver sensorDriver;

    public static void main(String args[]) throws Exception {
        SensorArgs sensorArgs = new SensorArgs();
        JCommander j = new JCommander(sensorArgs);

        j.setAcceptUnknownOptions(true);
        j.parse(args);

        sensorDriver = new SensorDriver(args, sensorArgs);

        processInput();
    }

    private static void processInput(){
        Scanner scanner = new Scanner(System.in);

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

            System.out.print("Please select an option: ");

            String input = scanner.next();

            if (input == null) {
                System.err.println("Invalid measurement provided!" + "\n");
                continue;
            }

            if (input.equals("exit") || input.startsWith("bye")) {
                return;
            }

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

            if (input.equals("history")) {
                Reading[] readings = sensorDriver.getReadingLog();

                String readingLog = "\n";

                for(int i = 0, j = readings.length; i < j; i++){
                    if(i != 0){
                        readingLog += "\n";
                    }
                    readingLog += "[" + new Date(readings[i].time) + " => " + readings[i].measurement + "]";
                }

                if(readings.length != 0){
                    System.out.println(readingLog + "\n");
                } else {
                    System.err.println("No readings have been registered yet!" + "\n");
                }

                continue;
            }

            if (input.equals("poweroff")) {
                if(sensorDriver.powerOff()){
                    System.out.println("Turned sensor off.\n");
                } else {
                    System.err.println("System is already powered off!\n");
                }
                continue;
            }

            if (input.equals("poweron")) {
                if(sensorDriver.powerOn()){
                    System.out.println("Turned sensor on.\n");
                } else {
                    System.err.println("System is already switched on!\n");
                }
                continue;
            }

            if (input.equals("reset")) {
                sensorDriver.reset();
                System.out.println("System reset.\n");
                continue;
            }

            if (input.equals("100") || input.matches("\\d{1,2}")) {
                sensorDriver.sendAlert(Integer.parseInt(input));
            } else {
                System.err.println("Invalid measurement provided!");
            }

            System.out.println("");

        }
    }
}
