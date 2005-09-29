package org.xhtmlrenderer.render;

import org.xhtmlrenderer.util.Uu;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class RenderQueue {
    private Vector layout_queue, repaint_queue;

    public RenderQueue() {
        layout_queue = new Vector();
        repaint_queue = new Vector();
    }

    public void dispatchLayoutEvent(ReflowEvent evt) {
        //Uu.p("dispatching: " + evt);
        //Uu.dump_stack();
        synchronized (layout_queue) {
            layout_queue.addElement(evt);
            layout_queue.notify();
        }
    }

    public void dispatchRepaintEvent(ReflowEvent evt) {
        synchronized (repaint_queue) {
            repaint_queue.addElement(evt);
            repaint_queue.notify();
        }
    }

    public ReflowEvent getNextLayoutEvent() {
        synchronized (layout_queue) {
            if (!layout_queue.isEmpty()) {
                return (ReflowEvent) layout_queue.remove(0);
            } else {
                return null;
            }
        }
    }

    private ReflowEvent getNextRepaintEvent() {
        synchronized (repaint_queue) {
            if (!repaint_queue.isEmpty()) {
                return (ReflowEvent) repaint_queue.remove(0);
            } else {
                return null;
            }
        }
    }

    public boolean isNextLayoutEvent() {
        //sUu.p("layout empty = " + layout_queue.isEmpty());
        return !layout_queue.isEmpty();
    }

    public boolean isNextRepaintEvent() {
        //sUu.p("layout empty = " + layout_queue.isEmpty());
        return !repaint_queue.isEmpty();
    }

    public ReflowEvent peekNextLayoutEvent() {
        return (ReflowEvent) layout_queue.get(0);
    }

    public ReflowEvent peekNextRepaintEvent() {
        return (ReflowEvent) repaint_queue.get(0);
    }

    public ReflowEvent waitForNextLayoutEvent() {
        //Uu.p("waiting for the next layout event");
        try {
            synchronized (layout_queue) {
                while (layout_queue.isEmpty()) {
                    layout_queue.wait();
                    //Uu.p("woke up from layout event waiting");
                }
                return getNextLayoutEvent();
            }
        } catch (InterruptedException e) {
            return null;
        } catch (Exception ex) {
            Uu.p(ex);
            return null;
        }
    }

    public ReflowEvent waitForNextRepaintEvent() {
        try {
            synchronized (repaint_queue) {
                while (repaint_queue.isEmpty()) {
                    //Uu.p("waiting for a repaint event");
                    repaint_queue.wait();
                }
                //Uu.p("got a repaint event");
                return getNextRepaintEvent();
            }
        } catch (InterruptedException e) {
            return null;
        } catch (Exception ex) {
            Uu.p(ex);
            return null;
        }
    }

    private ReflowEvent collapseEvents(ReflowEvent evt, List queue, int eventType) {
        if (evt.getType() != eventType) {
            return evt;
        }

        synchronized (queue) {
            for (Iterator i = queue.iterator(); i.hasNext();) {
                ReflowEvent evt2 = (ReflowEvent) i.next();

                if (evt2.getType() == eventType) {
                    i.remove();
                    evt = evt2;
                }
            }
        }

        return evt;
    }

    public ReflowEvent collapseLayoutEvents(ReflowEvent evt) {
        return collapseEvents(evt, layout_queue, ReflowEvent.CANVAS_RESIZED);
    }


    public ReflowEvent collapseRepaintEvents(ReflowEvent evt) {
        return collapseEvents(evt, repaint_queue, ReflowEvent.MORE_BOXES_AVAILABLE);
    }

    public static RenderQueue getInstance() {
        return single;
    }

    private static RenderQueue single = new RenderQueue();
}
