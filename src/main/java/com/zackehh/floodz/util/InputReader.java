package com.zackehh.floodz.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputReader {

    private final InputStream stream;

    public InputReader(InputStream stream){
        this.stream = stream;
    }

    public String readString(String msg){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            System.out.print(msg);
            return br.readLine();
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
