package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.render.*;

public class LayoutLoop implements Runnable {
    public LayoutLoop(RootPanel root) {
        this.root = root;
    }
    
    RootPanel root;
    
    public void run() {
        Uu.p("layout thread starting");
        startLayoutLoop();
    }
    
	public void startLayoutLoop() {
		while(true) {
			//Uu.p("waiting for a layout event");
			ReflowEvent evt = root.queue.waitForNextLayoutEvent();
			//Uu.p("got a layout event: " + evt);
			evt = collapseLayoutEvents(evt);
			
			// if only the height changed, then lets just skip the event
			int mxw = root.getContext().getMaxWidth();
			//Uu.p("current max width = " + mxw);
			//Uu.p("current width = " + getWidth());
			//Uu.p("evt width = " + evt.getDimension().getWidth());
			//Uu.p("rendered width = " + getRenderWidth());
            if(evt.getDimension() != null) {
                if(root.getRenderWidth() == evt.getDimension().getWidth()) {
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
			if(root.layoutInProgress) {
				//Uu.p("layout already in progress. stopping  for" + evt);
				root.layout_context.stopRendering();
				while(root.layoutInProgress) { Uu.sleep(1000); }
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
    
    
	public ReflowEvent collapseLayoutEvents(ReflowEvent evt) {
		if(evt.getType() != evt.CANVAS_RESIZED) {
			return evt;
		}
		boolean changed = false;
		while(root.queue.isNextLayoutEvent()) {
			ReflowEvent evt2 = root.queue.peekNextLayoutEvent();
			//Uu.p("peeking at next layout event: " + evt2);
			if(evt2.getType() == evt.CANVAS_RESIZED) {
				//Uu.p("it's a resize event");
				evt2 = root.queue.getNextLayoutEvent();
				if(evt2.getDimension().equals(evt.getDimension())) {
					//Uu.p("they are the same size");
					//Uu.p("collapsing");
				} else {
					//Uu.p("not the same size. still collapsing though");
					changed = true;
				}
				//Uu.p("collapsing: " + evt2);
				evt = evt2;
			}
		}
		//Uu.p("returning the final event: " + evt);
		return evt;
	}

}
