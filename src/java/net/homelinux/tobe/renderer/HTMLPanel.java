/*
 *
 * HTMLPanel.java
 * Copyright (c) 2004 Joshua Marinacci, Torbjörn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

package net.homelinux.tobe.renderer;



import java.io.*;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import javax.swing.JScrollPane;

import java.awt.Color;

import java.awt.Graphics2D;

import java.awt.RenderingHints;

import java.awt.event.*;

import java.io.InputStream;

import java.io.InputStreamReader;

import java.awt.Dimension;

import java.awt.Graphics;

import java.awt.Point;

import java.awt.Rectangle;

import javax.swing.JPanel;

import javax.swing.JComponent;

import javax.swing.Scrollable;



import java.util.logging.*;



//import org.w3c.dom.Document;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.*;

import org.xml.sax.*;


//import org.joshy.x;

import java.net.URL;

import java.io.File;

import org.apache.xpath.XPathAPI;

import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.css.StyleReference;

import org.xhtmlrenderer.forms.*;

//import org.joshy.html.css.StyleReference;

//import org.joshy.html.Context;
//import org.joshy.html.BodyLayout;

public class HTMLPanel extends JPanel implements  ComponentListener {

    public static Logger logger = Logger.getLogger("app.browser");

    public XRDocument doc;
    public UserAgentCallback _userAgent;
    public Context c;
    public Box body_box = null;
   public Box getRootBox() {
       return body_box;
   }
   private JScrollPane enclosingScrollPane;
    BodyLayout layout;

    private Dimension intrinsic_size;



    public Context getContext() {
        return c;
    }

    public HTMLPanel(UserAgentCallback userAgent) {

        c = new Context();
        c.css = new net.homelinux.tobe.renderer.css.TBStyleReference(userAgent);

        layout = new BodyLayout();

        setLayout(new AbsoluteLayoutManager());
        documentListeners = new HashMap();
        
        _userAgent = userAgent;
    }
    
    Map documentListeners;
    public void addDocumentListener(DocumentListener listener) {
        this.documentListeners.put(listener,listener);
    }
    protected void fireDocumentLoaded() {
        Iterator it = this.documentListeners.keySet().iterator();
        while(it.hasNext()) {
            DocumentListener list = (DocumentListener)it.next();
            list.documentLoaded();
        }
    }

    protected ErrorHandler error_handler;

    public void setErrorHandler(ErrorHandler error_handler) {

        this.error_handler = error_handler;

    }
    
   public void resetScrollPosition()
   {
      if (this.enclosingScrollPane != null)
      {
         this.enclosingScrollPane.getVerticalScrollBar().setValue(0);
      }
   }

    //public void setDocument(Document doc, URL url) {
    public void setDocument(XRDocument doc) {
        resetScrollPosition();
        this.doc = doc;

        Element html = (Element)doc.getDomDocument().getDocumentElement();
        
        ((net.homelinux.tobe.renderer.css.TBStyleReference) c.css).setDocumentContext(c, doc);
        
        //HACK: for now. Fix this properly via _userAgent
        try {
            //c.setBaseURL((new java.io.File(".")).toURI().resolve(doc.getURI()).toURL());
            c.setBaseURL(doc.getURI().toURL());
        }
        catch(java.net.MalformedURLException e) {
            e.printStackTrace();
        }
        
        this.body_box = null;

                    long st = System.currentTimeMillis();
        calcLayout();
                    long el = System.currentTimeMillis() - st;
                    System.out.println("TIME: calcLayout()  " + el);
        

        //repaint();

    }

    

    /**
     * Overrides the default implementation to test for and configure any {@link JScrollPane}
     * parent.
     */
    public void addNotify() {
        super.addNotify();
        java.awt.Container p = getParent();
        if (p instanceof javax.swing.JViewport) {
            java.awt.Container vp = p.getParent();
            if (vp instanceof JScrollPane) {
                setEnclosingScrollPane((JScrollPane) vp);
            }
        }
    }

    /**
     * Overrides the default implementation unconfigure any {@link JScrollPane}
     * parent.
     */
    public void removeNotify() {
        super.removeNotify();
        setEnclosingScrollPane(null);
    }

    /**
     * The method is invoked by {@link #addNotify} and {@link #removeNotify} to ensure that
     * any enclosing {@link JScrollPane} works correctly with this panel.  This method can be
     * safely invoked with a <tt>null</tt> scrollPane.
     * @param scrollPane the enclosing {@link JScrollPane} or <tt>null</tt> if the panel is no longer
     * enclosed in a {@link JScrollPane}.
     */
    private void setEnclosingScrollPane(JScrollPane scrollPane) {
        // if a scrollpane is already installed we remove it.
        if (enclosingScrollPane != null)
            enclosingScrollPane.removeComponentListener(this);

        enclosingScrollPane = scrollPane;

        if (enclosingScrollPane != null)
            enclosingScrollPane.addComponentListener(this);
    }

    

    public void setAntiAliased(boolean anti_aliased) {

        this.anti_aliased = anti_aliased;

    }

    private boolean anti_aliased = true;

    

    public void paintComponent(Graphics g) {

        if(anti_aliased) {

            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        }

                    long st = System.currentTimeMillis();
        doPaint(g);
                    long el = System.currentTimeMillis() - st;
                    System.out.println("TIME: doPaint(g)  " + el);

    }


    public void doPaint(Graphics g) {

        /*if(body_box == null) {
            calcLayout(g);
        }*/

        if(doc == null) { return; }

        newContext(g);

        layout.paint(c,body_box);

    }

    

    public void calcLayout() {

        calcLayout(this.getGraphics()); 

        this.setOpaque(false);

    }

    

    private void newContext(Graphics g) {

        Point origin = new Point(0,0);

        Point last = new Point(0,0);

        c.canvas = this;

        c.graphics = g;

      if (enclosingScrollPane != null)
      {
         c.setExtents(new Rectangle(enclosingScrollPane.getViewportBorderBounds()));
      }
      else
      {
         c.setExtents(new Rectangle(200, 200));
      }

      //c.setExtents(new Rectangle(0,0,viewport.getWidth(),viewport.getHeight()));
      c.viewport = this.enclosingScrollPane;
      c.cursor = last;
      c.setMaxWidth(0);
    }

    

    public void calcLayout(Graphics g) {
        
        this.removeAll();

        if(g == null) {
            return;
        }
        if(doc == null) { return; }

        

        Element body = (Element)doc.getDomDocument().getDocumentElement();

        newContext(g);

        //c.setMaxWidth(0);
        c.setMaxWidth(this.getWidth());

        long start_time = System.currentTimeMillis();

        body_box = layout.layout(c,body);

        long end_time = System.currentTimeMillis();
System.out.println("body box is "+body_box+" layout body took "+(end_time-start_time));

      //kanske blabla
      if (enclosingScrollPane != null)
      {
         if (this.body_box != null)
         {
            // CLN
            //u.p("bcolor = " + body_box.background_color);
            //u.p("body box = " + body_box.hashCode());
            this.enclosingScrollPane.getViewport().setBackground(body_box.background_color);
         }
      }

        intrinsic_size = new Dimension(c.getMaxWidth(),layout.contents_height);
        if(!intrinsic_size.equals(this.getSize())) {
            this.setPreferredSize(intrinsic_size);
            this.revalidate();//strangely needed whenever loading a new document???
        }
      //this.fireDocumentLoaded();
    }

    public void setSize(Dimension d) {
        super.setSize(d);
        //this.calcLayout();
    }


    public Box findBox(int x, int y) {

        return findBox(this.body_box,x,y);

    }

    public Box findBox(Box box, int x, int y) {

        Iterator it = box.getChildIterator();

        while(it.hasNext()) {

            Box bx = (Box)it.next();

            int tx = x;

            int ty = y;

            tx -= bx.x;

            tx -= bx.totalLeftPadding();

            ty -= bx.y;

            ty -= bx.totalTopPadding();

            

            // test the contents

            Box retbox = null;

            retbox = findBox(bx,tx,ty);

            if(retbox != null) {

                return retbox;

            }

            // test the box itself

            //u.p("bx test = " + bx + " " + x +","+y);

            int tty = y;

            if(bx instanceof InlineBox) {

                InlineBox ibx = (InlineBox)bx;

                LineBox lbx = (LineBox)box;

                //u.p("inline = " + ibx);

                //u.p("inline y = " + ibx.y);

                //u.p("inline height = " + ibx.height);

                //u.p("line = " + lbx);

                int off = lbx.baseline + ibx.y - ibx.height;

                //u.p("off = " + off);

                tty -= off;

            }

            if(bx.contains(x-bx.x,tty-bx.y)) {

                return bx;

            }

        }



        return null;

    }

    

    public int findBoxX(int x, int y) {

        return findBoxX(this.body_box,x, y);

    }

    

    public int findBoxX(Box box, int x, int y) {

        //u.p("findBox(" + box + " at ("+x+","+y+")");

        Iterator it = box.getChildIterator();

        while(it.hasNext()) {

            Box bx = (Box)it.next();

            int tx = x;

            int ty = y;

            tx -= bx.x;

            tx -= bx.totalLeftPadding();

            ty -= bx.y;

            ty -= bx.totalTopPadding();

            

            // test the contents

            int retbox = findBoxX(bx,tx,ty);

            if(retbox != -1) {

                return retbox;

            }

            int tty = y;

            if(bx instanceof InlineBox) {

                InlineBox ibx = (InlineBox)bx;

                LineBox lbx = (LineBox)box;

                //u.p("inline = " + ibx);

                //u.p("inline y = " + ibx.y);

                //u.p("inline height = " + ibx.height);

                //u.p("line = " + lbx);

                int off = lbx.baseline + ibx.y - ibx.height;

                //u.p("off = " + off);

                tty -= off;

            }

            // test the box itself

            //u.p("bx test = " + bx + " " + x +","+y);

            if(bx.contains(x-bx.x,tty-bx.y)) {

                return x-bx.x;

            }

        }

        

        return -1;

    }



    

    public void componentHidden(ComponentEvent e) { }

    public void componentMoved(ComponentEvent e) { }

    public void componentResized(ComponentEvent e) {

        calcLayout();

    }

    public void componentShown(ComponentEvent e) { }



    

    public void printTree() {

        printTree(this.body_box, "");

    }

    private void printTree(Box box, String tab) {

        System.out.println(tab + "Box = " + box);

        Iterator it = box.getChildIterator();

        while(it.hasNext()) {

            Box bx = (Box)it.next();

            printTree(bx,tab+ " ");

        }

        if(box instanceof InlineBox) {

            InlineBox ib = (InlineBox)box;

            if(ib.sub_block != null) {

                printTree(ib.sub_block,tab + " ");

            }

        }

    }

}



