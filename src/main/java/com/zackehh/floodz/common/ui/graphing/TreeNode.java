package com.zackehh.floodz.common.ui.graphing;

import com.zackehh.floodz.common.ui.InterfaceUtils;

/**
 * Container class to represent nodes inside a Tree graph. Allows
 * for customisation of width and height of a containing box. Defaults
 * can be used if a single String is passed. This class is immutable.
 */
@SuppressWarnings("unused")
public class TreeNode {

    /**
     * The text to be displayed.
     */
    private final String text;

    /**
     * The height of the node.
     */
    private final int height;

    /**
     * The width of the node.
     */
    private final int width;

    /**
     * Accepts a String to display, and uses a default height of 20
     * to display in a the node. Uses ${@link InterfaceUtils#getStringLength(String)}
     * in order to calculate an appropriate node width.
     *
     * @param text the text to display
     */
    public TreeNode(String text) {
        this.text = text;
        this.width = InterfaceUtils.getInstance().getStringLength(text);
        this.height = 20;
    }

    /**
     * Allows use of a custom width alongside the text to display.
     * Height still defaults to 20, but the width is set manually.
     *
     * @param text the text to display
     * @param width the width of the node
     */
    public TreeNode(String text, int width) {
        this.text = text;
        this.width = width;
        this.height = 20;
    }

    /**
     * Set text and bounds of a node.
     *
     * @param text the text to display
     * @param width the width of the node
     * @param height the height of the node
     */
    public TreeNode(String text, int width, int height) {
        this.text = text;
        this.width = width;
        this.height = height;
    }

    /**
     * Retrieve the text value.
     *
     * @return a String value
     */
    public String getText(){
        return text;
    }

    /**
     * Retrieve the height value.
     *
     * @return an int value
     */
    public int getHeight(){
        return height;
    }

    /**
     * Retrieve the width value.
     *
     * @return an int value
     */
    public int getWidth(){
        return width;
    }

}