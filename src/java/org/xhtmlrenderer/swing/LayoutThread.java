package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.Configuration;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;

/*

    this class manages when to actually start laying out the panel. it serves
    as the intermediary between the various things that could trigger a repaint
    and the panel itself
    
    

*/

public class LayoutThread implements Runnable {
    private boolean done;
    private Graphics graphics;
    private BasicPanel panel;
    private boolean threaded;
	private Context context;

    public LayoutThread(BasicPanel panel) {
        this.panel = panel;
        done = true;
        graphics = null;
        threaded = true;
    }

    public void setThreadedLayout(boolean threaded) {
        this.threaded = threaded;
    }

    public void startLayout(final Graphics g, Dimension d) {
        if (shouldLayout()) {
            //Uu.p("really starting new thread: " + d);
            //Uu.p("threaded = " + threaded);
            //done = false;
            graphics = g;
            if (threaded) {
                new Thread(this).start();
            } else {
                run();
            }
        } else {
            if (Configuration.isTrue("xr.layout.bad-sizing-hack", false)) {
                requestReLayout(d);
            }
        }
    }
    
    protected boolean second_set = false;
    
    public synchronized void waitForLayoutStopped() throws InterruptedException {
        wait();
    }
    
    protected Dimension last_size;
    protected void requestReLayout(final Dimension d) {
        //Uu.p("layout already in progress. skipping layout "+d);
        if(last_size != null && d.equals(last_size)) {
            //Uu.p("we are trying to lay out the same as the last size");
        }
        if(second_set)  { return; }
        second_set = true;
        last_size = d;
        new Thread(new Runnable() {
            public void run() {
                // tell current layout to stop
                //stopLayout();
                //Uu.p("told it to stop layout");
                // wait for the current to stop
                //Uu.p("waiting for layout to stop");
                try {
                    waitForLayoutStopped();
                } catch (Exception ex) {
                    Uu.p(ex);
                }
               // Uu.p("layout is stopped now");
                // call start layout again w/ the current graphics (is that safe?)
                second_set = false;
                //Uu.p("calling layout again. dim = " + d);
                //Uu.p("panel intrinsic =  " + panel.getIntrinsicSize());
                if(panel.getIntrinsicSize()!= null &&
                    panel.getIntrinsicSize().equals(d)) {
                        //Uu.p("the old intrinsic size matches. skiping the extra repaint");
                        return;
                }
                panel.calcLayout();
                panel.repaint();
                //Uu.p("called repaint");
            }
        }).start();
    }

	
	public void stopLayout() {
		this.context.stopRendering();
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
        notifyAll();
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
		this.context = c;
        Graphics g = c.getGraphics();
        g.setColor(Color.black);
        if (this.isLayoutDone()) {
            if (panel.body_box != null) {
                try {
                    //panel.doRender(c);
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
            g.drawString(panel.getLayoutInProgressMsg(), 50, 50);
            //Uu.p("still doing layout");
        }
    }
}
