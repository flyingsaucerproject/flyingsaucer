package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.Uu;

import java.awt.Color;
import java.awt.Graphics;

public class LayoutThread implements Runnable {
    private boolean done;
    private Graphics graphics;
    private BasicPanel panel;
    private boolean threaded;

    public LayoutThread(BasicPanel panel) {
        this.panel = panel;
        done = true;
        graphics = null;
        threaded = true;
    }

    public void setThreadedLayout(boolean threaded) {
        this.threaded = threaded;
    }

    public void startLayout(Graphics g) {
        if (shouldLayout()) {
            //Uu.p("really starting new thread");
            //Uu.p("threaded = " + threaded);
            //done = false;
            graphics = g;
            if (threaded) {
                new Thread(this).start();
            } else {
                run();
            }
        } else {
            //Uu.p("layout already in progress. skipping layout");
        }
    }

    public void run() {
        // Uu.p("layout thread starting");
        // Uu.p("graphics = " + graphics);
        panel.startLayout(graphics);
        this.completeLayout();
    }

    // skip for now
    private synchronized void completeLayout() {
        // Uu.p("layout thread ending");
        done = true;
        graphics = null;
        panel.repaint();
        // Uu.p("body box = " + panel.body_box );
    }

    // always done because not really threaded yet
    public synchronized boolean isLayoutDone() {
        return done;
    }

    // always done because not really threaded yet
    public synchronized boolean shouldLayout() {
        if (done) {
            done = false;
            return true;
        }
        return false;
    }

    public void startRender(Context c) {
        Graphics g = c.getGraphics();
        g.setColor(Color.black);
        if (this.isLayoutDone()) {
            if (panel.body_box != null) {
                try {
                    panel.doRender(c);
                } catch (Throwable thr) {
                    Uu.p("current thread = " + Thread.currentThread());
                    Uu.p(thr);
                    thr.printStackTrace();
                }
            } else {
                g.drawString("body box is null", 50, 50);
                Uu.p("body box is null");
            }
        } else {
            g.drawString("still doing layout", 50, 50);
            //Uu.p("still doing layout");
        }
    }
}
