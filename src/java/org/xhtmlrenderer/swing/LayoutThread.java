package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.util.u;

import java.awt.*;

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
            //u.p("really starting new thread");
            //u.p("threaded = " + threaded);
            //done = false;
            graphics = g;
            if (threaded) {
                new Thread(this).start();
            } else {
                run();
            }
        } else {
            //u.p("layout already in progress. skipping layout");
        }
    }

    public void run() {
        // u.p("layout thread starting");
        // u.p("graphics = " + graphics);
        panel.startLayout(graphics);
        this.completeLayout();
    }

    // skip for now
    private synchronized void completeLayout() {
        // u.p("layout thread ending");
        done = true;
        graphics = null;
        panel.repaint();
        // u.p("body box = " + panel.body_box );
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

    public void startRender(Graphics g) {
        g.setColor(Color.black);
        if (this.isLayoutDone()) {
            if (panel.body_box != null) {
                try {
                    panel.doRender();
                } catch (Throwable thr) {
                    u.p("current thread = " + Thread.currentThread());
                    u.p(thr);
                    thr.printStackTrace();
                }
            } else {
                g.drawString("body box is null", 50, 50);
                u.p("body box is null");
            }
        } else {
            g.drawString("still doing layout", 50, 50);
            //u.p("still doing layout");
        }
    }
}
