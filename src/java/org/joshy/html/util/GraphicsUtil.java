package org.joshy.html.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import org.joshy.html.box.Box;

public class GraphicsUtil {
    public static void drawBox(Graphics g, Box box, Color color) {
        Color oc = g.getColor();
        g.setColor(color);
        //g.drawLine(-5,-5,5,5);
        //g.drawLine(-5,5,5,-5);
        g.drawRect(box.x,box.y,box.width,box.height);
        g.setColor(oc);
    }
    public static void draw(Graphics g, Rectangle box, Color color) {
        Color oc = g.getColor();
        g.setColor(color);
        g.drawRect(box.x,box.y,box.width,box.height);
        g.setColor(oc);
    }
}

