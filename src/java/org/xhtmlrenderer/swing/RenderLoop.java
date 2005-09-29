package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.render.ReflowEvent;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.Uu;

import java.awt.*;

public class RenderLoop implements Runnable {
    private RootPanel root;

    public RenderLoop(RootPanel root) {
        this.root = root;
    }

    public void run() {
        Uu.p("repaint thread starting");
        startRepaintLoop();
    }

    public void startRepaintLoop() {
        //Uu.sleep(2000);
        while (true) {
            //Uu.sleep(1000);
            // wait for a repaint event
            ReflowEvent evt = root.queue.waitForNextRepaintEvent();
            if (evt == null) {
// we're being shutdown
                return;
            }
            //Uu.p("got a repaint event: " + evt);
            evt = root.queue.collapseRepaintEvents(evt);
            if (evt.getType() == evt.LAYOUT_COMPLETE) {
                root.repaint();
                continue;
            }

            if (Configuration.isTrue("xr.incremental.enabled", false)) {
                /*
    // i might be able to delete this now. handled in layout loop I think.
                if(getContext() != null && bh != null && bh.box != null) {
                    Dimension intrinsic_size = new Dimension(getContext().getMaxWidth(),
                        getContext().getMaxHeight());//bh.box.height);
                    Uu.p("now size = " + intrinsic_size);
                    if(!intrinsic_size.equals(this.intrinsic_size)) {
                        Uu.p("they are different. refreshing");
                        this.setPreferredSize(intrinsic_size);
                        this.revalidate();
                    }
                }
                */
                // if the height has changed due to new boxes, then set a new height
                if (evt.getType() == evt.MORE_BOXES_AVAILABLE) {
                    //Uu.p("doing a more available");
                    Dimension dim = evt.getDimension();
                    //Uu.p("current dimensions = " + this.getSize());
                    //Uu.p("new dimensions = " + dim);
                    Dimension vpdim = root.enclosingScrollPane.getViewport().getSize();
                    if (dim.getHeight() > root.getHeight()) {
                        //Uu.p("height is bigger now: " + dim + " setting");
                        root.setPreferredSize(new Dimension((int) vpdim.getWidth(), (int) dim.getHeight()));
                        root.revalidate();
                    }
                    /*
                    if(bh.box != null) {
                        Uu.p("box = " + bh.box);
                    if(this.getHeight() != bh.box.height) {
                        Uu.p("height is bigger now : " + bh.box.height);
                        this.setPreferredSize(new Dimension(getWidth(),bh.box.height));
                        this.revalidate();
                    }
                    } else {
                        Uu.p("skipping a more avail");
                    }
                    */
                }

                // coallesce if needed
                // call repaint
                //Uu.p("repainting()");
                root.repaint();
            } else {
                Uu.p("not doing incremental for event: " + evt);
            }
            // store the repaint event
            root.last_event = evt;
        }
    }
}
