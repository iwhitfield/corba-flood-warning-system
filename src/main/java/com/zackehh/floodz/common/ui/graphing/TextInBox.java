package com.zackehh.floodz.common.ui.graphing;

public class TextInBox {

    private final String text;
    private final int height;
    private final int width;

    public TextInBox(String text, int width) {
        this.text = text;
        this.width = width;
        this.height = 20;
    }

    public TextInBox(String text, int width, int height) {
        this.text = text;
        this.width = width;
        this.height = height;
    }

    public String getText(){
        return text;
    }

    public int getHeight(){
        return height;
    }

    public int getWidth(){
        return width;
    }
}