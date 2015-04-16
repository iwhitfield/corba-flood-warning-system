package com.zackehh.floodz.common.ui.graphing;

public class TreeNode {

    private final String text;
    private final int height;
    private final int width;

    public TreeNode(String text, int width) {
        this.text = text;
        this.width = width;
        this.height = 20;
    }

    public TreeNode(String text, int width, int height) {
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