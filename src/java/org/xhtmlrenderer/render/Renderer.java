package org.xhtmlrenderer.render;


import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.layout.*;
import java.awt.Point;
import java.awt.Rectangle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.render.Box;

public interface Renderer {
    public void paint(Context c, Box box); 
    public void paintBackground(Context c, Box box);
    public void paintComponent(Context c, Box box);
    public void paintBorder(Context c, Box box);
    public void paintChildren(Context c, Box box);
}
