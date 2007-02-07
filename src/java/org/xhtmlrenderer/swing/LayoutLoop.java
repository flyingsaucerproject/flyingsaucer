package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.render.ReflowEvent;
import org.xhtmlrenderer.util.Uu;

public class LayoutLoop implements Runnable {
    public LayoutLoop(RootPanel root) {
        this.root = root;
    }

    RootPanel root;

    public void run() {
        Uu.p("layout thread starting");
        try {
            startLayoutLoop();
        } catch (InterruptedException e) {
            // ignore, we're being shutdown
        }
    }

    //  this loop will wait for events, collapse them, then determine
    // if a full re-layout is required
    private void startLayoutLoop() throws InterruptedException {
        while (true) {
            //Uu.p("waiting for a layout event");
            ReflowEvent evt = root.queue.waitForNextLayoutEvent();
            if (evt == null) {
                return;
            }
            Uu.p("got a layout event: " + evt);
            evt = root.queue.collapseLayoutEvents(evt);

            // if only the height changed, then lets just skip the event
            // Uu.p("current max width = " + mxw);
            // the actual width of the panel
            // Uu.p("current width = " + root.getWidth());
            // the dimensions from the event
            // Uu.p("evt width = " + evt.getDimension());
            // ??? the last width we rendered to? the width we want to render to?
            // Uu.p("rendered width = " + root.getRenderWidth());
            
            if (evt.getDimension() != null) {

                // if the renderwidth != to the actual panel width, then re-layout
                if (/*root.getRenderWidth()*/ 0 != root.getLayoutWidth()) {
                    // Uu.p("render width != panel width. resizing");
                    doRelayout(evt);
                } else {

                    continue;
                    /*
                    if (root.getRenderWidth() == evt.getDimension().getWidth()) {
                        Uu.p("same width. Just continuing");
                        continue;
                    } else {
                        Uu.p("evt width : " + evt.getDimension().getWidth() +
                                " differs from last rendered width: " + root.getRenderWidth());
                        doRelayout(evt);
                    }
                    */
                }
            } else {
                //Uu.p("empty dimension. doing a full re-layout");
                doRelayout(evt);
            }

        }
    }

    private void doRelayout(ReflowEvent evt) throws InterruptedException {
        // if layout is already in progress
        //if (root.layoutInProgress) {
        // Uu.p("layout already in progress. stopping  for" + evt);
        if (root.layout_context != null) {
            root.layout_context.stopRendering();
        }
        /*while (root.layoutInProgress) {
            // NOTE: joshy: is this sleep necessary?
            Uu.sleep(100);
        }*/
        // Uu.p("layout stopped now");
        //}
    
        // spawn a new thread to start up the layout
        new Thread(new Runnable() {
            public void run() {
                // Uu.p("starting up another thread for layout");
                //root.layoutInProgress = true;
                root.doActualLayout(root.getGraphics());
                //root.layoutInProgress = false;
                // Uu.p("layout thread finished");
            }
        }, "FlyingSaucer-Layout2").start();
        // NOTE: joshy: is this sleep necessary?
        Uu.sleep(100);
    }
}
