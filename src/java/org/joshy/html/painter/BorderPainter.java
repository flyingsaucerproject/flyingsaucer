package org.joshy.html.painter;



import java.awt.*;

import org.w3c.dom.*;

import org.joshy.*;

import org.joshy.html.*;

import org.joshy.html.box.Box;





public class BorderPainter {

    public void paint(Context ctx, Box box) {

        Graphics g = ctx.getGraphics();

        
        // TODO: color is per-side ((PWW 13/08/04))
        box.border_color = ctx.css.getBorderColor(box.getElement()).topColor;

        // ASK: border-style is a shorthand property for 4 border sides
        // CSSBank/Accessor leave it as a single property, but XRStyleReference
        // explodes it to individual values. Which way to go? (PWW 13/08/04)
        box.border_style = ctx.css.getStringProperty(box.getElement(), "border-top-style");

        

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

        

        if(box.border_style.equals("ridge") ||
            box.border_style.equals("groove")) {
            Border bd2 = new Border();
            bd2.top = box.border.top/2;
            bd2.bottom = box.border.bottom/2;
            bd2.left = box.border.left/2;
            bd2.right = box.border.right/2;
            if(box.border_style.equals("ridge")) {
                paintBevel(g,bounds,box.border,box.border_color.darker(),box.border_color.brighter());
                paintBevel(g,bounds,bd2,box.border_color.brighter(),box.border_color.darker());
            } else {
                paintBevel(g,bounds,box.border,box.border_color.brighter(),box.border_color.darker());
                paintBevel(g,bounds,bd2,box.border_color.darker(),box.border_color.brighter());
            }
            return;
        }

        

        if(box.border_style.equals("outset")) {

            paintBevel(g,bounds,box.border,box.border_color.brighter(),box.border_color.darker());
            return;
        }

        if(box.border_style.equals("inset")) {

            paintBevel(g,bounds,box.border,box.border_color.darker(),box.border_color.brighter());
            return;

        }

        if(box.border_style.equals("solid")) {
            paintBevel(g,bounds,box.border,box.border_color,box.border_color);
        }
        
        //u.p("border-style = " + box.border_style);
        if(box.border_style.equals("dashed")) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            paintRect(g2,bounds,box.border,box.border_color, new float[] {10.0f, 4.0f});
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        }
        if(box.border_style.equals("dotted")) {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            paintRect(g2,bounds,box.border,box.border_color, new float[] {box.border.top, box.border.top});
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        }

    }
    
    public void paintRect(Graphics2D g2, Rectangle bounds, Border border, Color color, float[] pattern) {
            g2.setColor(color);
            Stroke old_stroke = g2.getStroke();
            int x = bounds.x; int y = bounds.y;
            int w = bounds.width; int h = bounds.height;
            
            g2.setStroke(new BasicStroke(border.top, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern,0));            
            g2.drawLine(x,y,x+w,y);
            g2.setStroke(new BasicStroke(border.left, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern,0));            
            g2.drawLine(x,y,x,y+h);
            g2.setStroke(new BasicStroke(border.right, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern,0));            
            g2.drawLine(x+w,y,x+w,y+h);
            g2.setStroke(new BasicStroke(border.bottom, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, pattern,0));            
            g2.drawLine(x,y+h,x+w,y+h);
            
            g2.setStroke(old_stroke);
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



