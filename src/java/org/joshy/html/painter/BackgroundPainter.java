package org.joshy.html.painter;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import org.joshy.u;
import org.joshy.html.*;
import org.joshy.html.box.Box;

public class BackgroundPainter {
    public static void paint(Context c, Box block) {
        
         Rectangle box = new Rectangle(
                block.x + block.margin.left + block.border.left,
                block.y + block.margin.top + block.border.top,
                block.width - block.margin.left - block.margin.right - block.border.left - block.border.right,
                block.height - block.margin.top - block.border.top - block.border.bottom - block.margin.bottom
                );
        /*
        u.off();
        if(block.node.getNodeName().equals("img")) {
            u.on();
        }
        u.p("-------------");
        u.p("BackgroundPainter.paint(" + block + " bounds = " + box);
        u.p("bg color = " + block.background_color);
        u.p("canvas = " + c.canvas);
        */
        // paint the background
        if(block.background_color != null) {
            u.p("filling background: " + block.background_color + " " + block);
            c.getGraphics().setColor(block.background_color);
            c.getGraphics().fillRect(box.x,box.y,box.width,box.height);
        }
        
        int xoff = 0;
        int yoff = 0;
        if(block.attachment != null && block.attachment.equals("fixed")) {
            //u.p("fixed: offset = " + c.canvas.getLocation());
            yoff = c.canvas.getLocation().y;
            //c.graphics.setColor(Color.GREEN);
            //u.p("drawing line at " + (50 - yoff));
            //c.graphics.setClip(c.canvas.getX(),c.canvas.getY(),c.canvas.getWidth(),c.canvas.getHeight());
            c.graphics.setClip(c.canvas.getVisibleRect());
            //c.graphics.drawLine(0,0-yoff,50,50-yoff);
            /*
            u.p("clip bounds = " + c.graphics.getClipBounds());
            u.p("bounds = " + c.canvas.getBounds());
            u.p("vis rec t= " + c.canvas.getVisibleRect());
            if(c.graphics.getClipBounds() != null) {
            if(c.graphics.getClipBounds().equals(c.canvas.getVisibleRect())) {
                u.p("equal");
            } else {
                u.p("not equal");
                //c.canvas.repaint(c.canvas.getVisibleRect());
                //RepaintManager.currentManager(c.canvas).markCompletelyDirty(c.canvas);
            }
            } else {
                u.p("null");
            }
            */
        }
        
        if(block.background_image != null) {
            //u.p("back image");
            int left_insets = box.x;
            int top_insets  = box.y;
            int back_width = box.width;
            int back_height = box.height;
            Shape oldclip = c.getGraphics().getClip();
            c.getGraphics().setClip(left_insets, top_insets, back_width, back_height);
            
            // calculate repeat indecies
            int repeatx = 1;
            int repeaty = 1;
            
            if(block.repeat == null) {
                repeatx = 1;
                repeaty = 1;
            } else if (block.repeat.equals("repeat-x")) {
                repeatx = back_width;
            } else if (block.repeat.equals("repeat-y")) {
                repeaty = back_height;
            } else if (block.repeat.equals("repeat")) {
                repeatx = back_width;
                repeaty = back_height;
            }
            
            
            double iwd = block.background_image.getWidth(null);
            double ihd = block.background_image.getHeight(null);
            int iw = block.background_image.getWidth(null);
            int ih = block.background_image.getHeight(null);
            
            // handle image position offsets
            //u.p("doing vertical bottom align");
            //u.p("yoff = " + yoff);
            //u.p("vert = " + block.background_position_vertical);
            yoff -= (int)((double)(back_height - ih)*(double)((double)block.background_position_vertical/(double)100));
            xoff -= (int)((double)(back_width  - iw)*(double)((double)block.background_position_horizontal/(double)100));
            //u.p("yoff = " + yoff);

            // calculations for fixed tile images
            int starty = (int) Math.ceil((double)(top_insets+yoff)/ih);
            int endy = (int) Math.ceil((double)(back_height+top_insets+yoff)/ih);
            int startx = (int) Math.ceil((double)(left_insets)/iw);
            int endx = (int) Math.ceil((double)(back_width+left_insets)/iw);
            //u.p("startx = " + startx + " endx = " + endx);

            //u.p("back height = " + back_height + " img height = " + img.getHeight(null));
            //u.p("div = " + ((double)back_height+c.getExtents().y/(double)img.getHeight(null)));
            //u.p("start y = " + starty + " end y = " + endy);
            //u.p("clip = " + c.getGraphics().getClip());
            // tile the image as appropriate
            
            //u.p("got here 3");
            // do fixed tile image

            boolean horiz = false;
            boolean vert = false;
            if(block.repeat.equals("repeat-x")) {
                horiz = true;
                vert = false;
            }
            if(block.repeat.equals("repeat-y")) {
                horiz = false;
                vert = true;
            }
            if(block.repeat.equals("repeat")) {
                horiz = true;
                vert = true;
            }

            if(block.attachment != null && block.attachment.equals("fixed")) {
                tileFill(c.getGraphics(), block.background_image, 
                    new Rectangle( left_insets, top_insets, back_width, back_height ),
                    0, -yoff, horiz, vert);
                /*
                for(int i=startx; i<=endx; i++) {
                    for(int j=starty; j<=endy; j++) {
                        int fx = iw * i;
                        //u.p("yoff = " + yoff);
                        int fy = block.background_image.getHeight(null) * j - yoff;
                        //u.p("drawing at: " + fx + " " + fy + " " +
                        //    img.getWidth(null) + " " + img.getHeight(null));
                        c.graphics.drawImage(
                            block.background_image,
                            fx-block.background_image.getWidth(null),
                            fy-block.background_image.getHeight(null),null);
                        //c.graphics.fillRect(fx-img.getWidth(null),fy-img.getHeight(null),
                        //                  img.getWidth(null),img.getHeight(null));
                    }
                }
                */
            } else {
            // do normal tile image
            //u.p("normal looping");
            
                tileFill(c.getGraphics(), block.background_image, 
                    new Rectangle( left_insets, top_insets, back_width, back_height ),
                    0, -yoff, horiz, vert);
                /*
                for(int i=0; i<repeatx; i+=block.background_image.getWidth(null)) {
                    for(int j=0; j<repeaty; j+=block.background_image.getHeight(null)) {
                        //u.p("width = " + block.background_image.getWidth(null));
                        int fx = left_insets+i;
                        int fy = top_insets+j-yoff;
                        //u.p("fx = " + fx + " fy = " + fy);
                        c.graphics.drawImage(block.background_image,fx,fy,null);
                        //c.getGraphics().drawImage(img,fx,fy,null);
                    }
                }
                */
                //u.p("finished loop");
            }
            c.getGraphics().setClip(oldclip);
        }
        
        u.off();

    }
    
    private static void tileFill(Graphics g, Image img, Rectangle rect, int xoff, int yoff, boolean horiz, boolean vert) {
        int iwidth = img.getWidth(null);
        int iheight = img.getHeight(null);
        int rwidth = rect.width;
        int rheight = rect.height;
        
        if(!horiz) {
            rwidth = iwidth;
        }
        if(!vert) {
            rheight = iheight;
        }
        
        if(horiz) {
            xoff = xoff%iwidth-iwidth;
            rwidth += iwidth;
        }
        if(vert) {
            yoff = yoff%iheight-iheight;
            rheight += iheight;
        }
        
        for(int i=0; i<rwidth; i+=iwidth) {
            for(int j=0; j<rheight; j+=iheight) {
                g.drawImage(img, i+rect.x+xoff, j+rect.y+yoff, null);
            }
        }
        
    }
    
    
    
    
}
