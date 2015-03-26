package com.zackehh.floodz.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

}
