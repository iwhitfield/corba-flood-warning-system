package com.zackehh.floodz.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SuppressWarnings("unused")
public class InputReader {

    private final BufferedReader reader;

    public InputReader(InputStream stream){
        this.reader = new BufferedReader(new InputStreamReader(stream));
    }

    public String readString(String msg){
        try {
            System.out.print(msg);
            return reader.readLine();
        } catch(IOException e) {
            return null;
        }
    }

    public Integer readInteger(String msg){
        try {
            return Integer.parseInt(readString(msg));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    public Double readDouble(String msg){
        try {
            return Double.parseDouble(readString(msg));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    public Float readFloat(String msg){
        try {
            return Float.parseFloat(readString(msg));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    public Boolean readBoolean(String msg){
        try {
            return Boolean.valueOf(readString(msg));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

}
