package org.joshy.html.painter;

import java.awt.*;
import org.w3c.dom.*;
import org.joshy.*;
import org.joshy.html.*;
import org.joshy.html.box.Box;


public class BorderPainter {
    public void paint(Context ctx, Box box) {
        Graphics g = ctx.getGraphics();
        
        box.border_color = ctx.css.getBorderColor(box.getElement());
        box.border_style = ctx.css.getStringProperty(box.getElement(), "border-style");
        
        Rectangle bounds = new Rectangle(box.x + box.margin.left,
                                 box.y + box.margin.top,
                                 box.width - box.margin.left - box.margin.right,
                                 box.height - box.margin.top - box.margin.bottom
                                 );

        
        //u.p("border style = " + border_style);
        if(box.border_style == null) {
            box.border_style = "none";
        }
        
        // return if border = none
        if(box.border_style.equals("none")) {
            return;
        }
        
        if(box.border_style.equals("ridge")) {
            Border bd2 = new Border();
            bd2.top = box.border.top/2;
            bd2.bottom = box.border.bottom/2;
            bd2.left = box.border.left/2;
            bd2.right = box.border.right/2;
            paintBevel(g,bounds,box.border,box.border_color.darker(),box.border_color.brighter());
            paintBevel(g,bounds,bd2,box.border_color.brighter(),box.border_color.darker());
        }
        
        if(box.border_style.equals("outset")) {
            paintBevel(g,bounds,box.border,box.border_color.brighter(),box.border_color.darker());
        }
        if(box.border_style.equals("inset")) {
            paintBevel(g,bounds,box.border,box.border_color.darker(),box.border_color.brighter());
        }
        if(box.border_style.equals("solid")) {
            paintBevel(g,bounds,box.border,box.border_color,box.border_color);
        }
    }
    
    public void paintBevel(Graphics g, Rectangle bounds, Border border, Color high, Color low) {
        g.setColor(high);
        Polygon poly = new Polygon();
        poly.addPoint(bounds.x,bounds.y);
        poly.addPoint(bounds.x+bounds.width, bounds.y);
        poly.addPoint(bounds.x+bounds.width-border.right, bounds.y+border.top);
        poly.addPoint(bounds.x+border.left, bounds.y+border.top);
        poly.addPoint(bounds.x+border.left, bounds.y+bounds.height-border.bottom);
        poly.addPoint(bounds.x,bounds.y+bounds.height);
        g.fillPolygon(poly);

        g.setColor(low);
        poly = new Polygon();
        poly.addPoint(bounds.x+bounds.width, bounds.y);
        poly.addPoint(bounds.x+bounds.width-border.right, bounds.y+border.top);
        poly.addPoint(bounds.x+bounds.width-border.right, bounds.y+bounds.height-border.bottom);
        poly.addPoint(bounds.x+border.left, bounds.y+bounds.height-border.bottom);
        poly.addPoint(bounds.x,bounds.y+bounds.height);
        poly.addPoint(bounds.x+bounds.width, bounds.y+bounds.height);
        g.fillPolygon(poly);
    }
}

