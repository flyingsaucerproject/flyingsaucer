package com.pdoubleya.xhtmlrenderer.css.value;

import java.awt.Color;


/**
 * Adapted from org.joshy.html.Border by Josh M.
 *
 * @author    Patrick Wright
 *
 */
public class BorderColor {

    /** Color for top of the border. */
    public Color topColor;

    /** Color for bottom of the border. */
    public Color bottomColor;

    /** Color for left of the border. */
    public Color leftColor;

    /** Color for right of the border. */
    public Color rightColor;


    /**
     * ...
     *
     * @return   Returns
     */
    public String toString() {

        return "BorderColor:\n" +
                "    topColor = " + topColor +
                "    rightColor = " + rightColor +
                "    bottomColor = " + bottomColor +
                "    leftColor = " + leftColor;
    }

}


