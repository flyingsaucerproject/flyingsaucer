package com.pdoubleya.xhtmlrenderer.css.value;


import java.awt.Color;


/** Adapted from org.joshy.html.Border  by Josh M. */
public class BorderColor {

    public Color topColor;

    public Color bottomColor;

    public Color leftColor;

    public Color rightColor;

    

    public String toString() {

        return "BorderColor:\n" + 
               "    topColor = " + topColor + 
               "    rightColor = " + rightColor + 
               "    bottomColor = " + bottomColor + 
               "    leftColor = " + leftColor;

    }

}



