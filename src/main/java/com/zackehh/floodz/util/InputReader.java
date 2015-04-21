package com.zackehh.floodz.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * A simple handler for reading input from a designated InputStream. Used often
 * with System.in when setting up varying components within the system.
 */
@SuppressWarnings("unused")
public class InputReader {

    /**
     * The reader to accept input from.
     */
    private final BufferedReader reader;

    /**
     * A simple constructor to create and set a BufferedReader using the
     * provided InputStream instance.
     *
     * @param stream the stream to read
     */
    public InputReader(InputStream stream){
        this.reader = new BufferedReader(new InputStreamReader(stream));
    }

    /**
     * Prints out a message and waits for use input. Returns the provided
     * input as a String. Any IOExceptions are caught and null is returned.
     *
     * @param msg the message to display
     * @return a String input
     */
    public String readString(String msg){
        try {
            System.out.print(msg);
            return reader.readLine();
        } catch(IOException e) {
            return null;
        }
    }

    /**
     * Wrapper around ${@link #readString(String)} to read input and return
     * the value as an Integer.
     *
     * @param msg the message to display
     * @return an Integer input
     */
    public Integer readInteger(String msg){
        try {
            return Integer.parseInt(readString(msg));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    /**
     * Wrapper around ${@link #readString(String)} to read input and return
     * the value as a Double.
     *
     * @param msg the message to display
     * @return an Double input
     */
    public Double readDouble(String msg){
        try {
            return Double.parseDouble(readString(msg));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    /**
     * Wrapper around ${@link #readString(String)} to read input and return
     * the value as a Float.
     *
     * @param msg the message to display
     * @return a Float input
     */
    public Float readFloat(String msg){
        try {
            return Float.parseFloat(readString(msg));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    /**
     * Wrapper around ${@link #readString(String)} to read input and return
     * the value as a Boolean.
     *
     * @param msg the message to display
     * @return a Boolean input
     */
    public Boolean readBoolean(String msg){
        try {
            return Boolean.valueOf(readString(msg));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    /**
     * Wrapper around ${@link #readString(String)} to read input and return
     * the value as a List.
     *
     * @param msg the message to display
     * @return a List input
     */
    public List<String> readList(String msg){
        try {
            return Arrays.asList(readString(msg).split(","));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

}
