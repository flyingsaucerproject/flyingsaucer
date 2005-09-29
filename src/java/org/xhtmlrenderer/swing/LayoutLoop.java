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

    public void startLayoutLoop() throws InterruptedException {
        while (true) {
            //Uu.p("waiting for a layout event");
            ReflowEvent evt = root.queue.waitForNextLayoutEvent();
            if (evt == null) {
                return;
            }
            //Uu.p("got a layout event: " + evt);
            evt = root.queue.collapseLayoutEvents(evt);

            // if only the height changed, then lets just skip the event
            int mxw = root.getContext().getMaxWidth();
            //Uu.p("current max width = " + mxw);
            //Uu.p("current width = " + getWidth());
            //Uu.p("evt width = " + evt.getDimension().getWidth());
            //Uu.p("rendered width = " + getRenderWidth());
            if (evt.getDimension() != null) {
                if (root.getRenderWidth() == evt.getDimension().getWidth()) {
                    Uu.p("same width. Just continuing");
                    continue;
                } else {
                    Uu.p("evt width : " + evt.getDimension().getWidth() +
                            " differs from last rendered width: " + root.getRenderWidth());
                }
            } else {
                Uu.p("empty dimension. doing a full re-layout");
            }

            // if layout is already in progress
            if (root.layoutInProgress) {
                //Uu.p("layout already in progress. stopping  for" + evt);
                root.layout_context.stopRendering();
                while (root.layoutInProgress) {
                    Uu.sleep(1000);
                }
                //Uu.p("layout stopped now");
            }

            // spawn a new thread to start up the layout
            new Thread(new Runnable() {
                public void run() {
                    //Uu.p("starting up another thread for layout");
                    root.layoutInProgress = true;
                    root.doActualLayout(root.getGraphics());
                    root.layoutInProgress = false;
                    //Uu.p("layout thread finished");
                }
            }).start();
            Uu.sleep(1000);
        }
    }
}
