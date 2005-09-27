package org.xhtmlrenderer.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import javax.swing.*;
import org.w3c.dom.*;

import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.layout.content.DomToplevelNode;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.extend.UserInterface;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.layout.Boxing;
import org.xhtmlrenderer.layout.BlockBoxing;
import org.xhtmlrenderer.layout.BoxHolder;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRLog;


public class RootPanel extends JPanel implements ComponentListener, UserInterface  {


    /**
     * Description of the Field
     */
    protected Dimension intrinsic_size;

	/* can we figure out how to get rid of this?
	*/
    protected BoxHolder bh;

    /**
     * Description of the Field
     */
    protected Map documentListeners;


	
	/**
     * Gets the context attribute of the BasicPanel object
     *
     * @return The context value
     */
    public SharedContext getContext() {
        return getRenderingContext().getContext();
    }

    /**
     * Description of the Field
     */
    protected RenderingContext ctx;

    /**
     * Gets the renderingContext attribute of the BasicPanel object
     *
     * @return The renderingContext value
     */
    public RenderingContext getRenderingContext() {
        return ctx;
    }

    protected Context layout_context;

	
    /**
     * Description of the Field
     */
    protected Box body_box = null;


    /**
     * Description of the Field
     */
	protected JScrollPane enclosingScrollPane;
    /**
     * Description of the Method
     */
    public void resetScrollPosition() {
        if (this.enclosingScrollPane != null) {
            this.enclosingScrollPane.getVerticalScrollBar().setValue(0);
        }
    }

    /**
     * The method is invoked by {@link #addNotify} and {@link #removeNotify} to
     * ensure that any enclosing {@link JScrollPane} works correctly with this
     * panel. This method can be safely invoked with a <tt>null</tt> scrollPane.
     *
     * @param scrollPane the enclosing {@link JScrollPane} or <tt>null</tt> if
     *                   the panel is no longer enclosed in a {@link JScrollPane}.
     */
    protected void setEnclosingScrollPane(JScrollPane scrollPane) {
        // if a scrollpane is already installed we remove it.
        if (enclosingScrollPane != null) {
            enclosingScrollPane.removeComponentListener(this);
        }

        enclosingScrollPane = scrollPane;

        if (enclosingScrollPane != null) {
            Uu.p("added root panel as a component listener to the scroll pane");
            enclosingScrollPane.addComponentListener(this);
        }
    }

    /**
     * Gets the fixedRectangle attribute of the BasicPanel object
     *
     * @return The fixedRectangle value
     */
    public Rectangle getFixedRectangle() {
        if (enclosingScrollPane != null) {
            return enclosingScrollPane.getViewportBorderBounds();
        } else {
            Dimension dim = getSize();
            return new Rectangle(0, 0, dim.width, dim.height);
        }
    }

    /**
     * Overrides the default implementation to test for and configure any {@link
     * JScrollPane} parent.
     */
    public void addNotify() {
        super.addNotify();
        System.out.println("add notify called");
        Container p = getParent();
        if (p instanceof JViewport) {
            Container vp = p.getParent();
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
     * Description of the Field
     */
    protected Document doc = null;

	/**
	 * The queue to hand painting and layout events
	 */
	RenderQueue queue;

    /**
     * Description of the Method
     */
    protected void init() {
		
		queue = RenderQueue.getInstance();
        documentListeners = new HashMap();
        setBackground(Color.white);
        super.setLayout(null);
		
		new Thread(new LayoutLoop(this)).start();
        new Thread(new RenderLoop(this)).start();
     }

	int rendered_width = 0;
	protected int getRenderWidth() {
		return rendered_width;
	}
	protected void setRenderWidth(int renderWidth) {
		this.rendered_width = renderWidth;
	}


	boolean layoutInProgress = false;

	
	public ReflowEvent last_event = null;

    protected Context newContext(Graphics2D g) {
        XRLog.layout(Level.FINEST, "new context begin");

        getContext().setCanvas(this);
        getContext().setGraphics(g);

        Rectangle extents;
		
        if (enclosingScrollPane != null) {
            Rectangle bnds = enclosingScrollPane.getViewportBorderBounds();
            extents = new Rectangle(0, 0, bnds.width, bnds.height);
			//Uu.p("bnds = " + bnds);
        } else {
            extents = new Rectangle(getWidth(), getHeight());//200, 200 ) );
			
        }

		
		//Uu.p("newContext() = extents = " + extents);
        getContext().setMaxWidth(0);
		//getContext().setMaxHeight(0);
        XRLog.layout(Level.FINEST, "new context end");
		//Uu.p("new context with extents: " + extents);
		setRenderWidth((int)extents.getWidth());
        return getContext().newContextInstance(extents);
    }

	public void doActualLayout(Graphics g) {
		//Uu.p("doActualLayout called");
        this.removeAll();
        if (g == null) {
            return;
        }
        if (doc == null) {
            return;
        }
        
        // set up CSS
        Context c = newContext((Graphics2D) g);
        //getContext().setMaxWidth(0);
        this.layout_context = c;
        getRenderingContext().getTextRenderer().setupGraphics(c.getGraphics());
        //TODO: maybe temporary hack
        if (c.getBlockFormattingContext() != null) c.popBFC();//we set one for the top level before
		// do the actual layout
        BlockBoxing.count = 0;
        bh = new org.xhtmlrenderer.layout.BoxHolder();
        //Uu.p("doing actual layout here");
        body_box = Boxing.layout(c, new DomToplevelNode(doc),bh);
        //Uu.p("body box = " + body_box);
		if (!c.isStylesAllPopped()) {
            XRLog.layout(Level.SEVERE, "mismatch in style popping and pushing");
        }

        XRLog.layout(Level.FINEST, "is a fixed child: " + body_box.isChildrenExceedBounds());
        
        // if there is a fixed child then we need to set opaque to false
        // so that the entire viewport will be repainted. this is slower
        // but that's the hit you get from using fixed layout
        if (body_box.isChildrenExceedBounds()) {
            super.setOpaque(false);
        } else {
            super.setOpaque(true);
        }

        getRenderingContext().setRootBox(body_box);

        XRLog.layout(Level.FINEST, "after layout: " + body_box);

        intrinsic_size = new Dimension(getContext().getMaxWidth(), body_box.height);
        //Uu.p("intrinsic size = " + intrinsic_size);
		
		/*
        if (enclosingScrollPane != null) {
            XRLog.layout(Level.FINEST, "enclosing scroll pane = " + this.enclosingScrollPane);
            int view_height = this.enclosingScrollPane.getViewport().getHeight();
            // resize the outter most box incase it is too small for the viewport
            if (intrinsic_size.getHeight() < view_height) {
                if (body_box != null) {
                    body_box.height = view_height;
					//bodyExpandHack(body_box,view_height);
					intrinsic_size.height = view_height;
                }
            }
        }
		*/

		
        if (intrinsic_size.width != this.getWidth()) {
			//Uu.p("intrisic and this widths don't match: " + this.getSize() + " "  + intrinsic_size);
			this.setPreferredSize(new Dimension(intrinsic_size.width,this.getHeight()));
            //this.setPreferredSize(intrinsic_size);
            this.revalidate();
        }
		// if doc is shorter than viewport
		// then stretch canvas to fill viewport exactly
		if (intrinsic_size.height < enclosingScrollPane.getViewport().getHeight()) {
			//Uu.p("int height is less than viewport height");
			if (enclosingScrollPane.getViewport().getHeight() != this.getHeight()) {
				this.setPreferredSize(new Dimension(getWidth(),enclosingScrollPane.getViewport().getHeight()));
				this.revalidate();
			}
		} else {  // if doc is taller than viewport
			if(this.getHeight() != intrinsic_size.height) {
				this.setPreferredSize(new Dimension(getWidth(),intrinsic_size.height));
				this.revalidate();
			}
			
		}
		
		

		queue.dispatchRepaintEvent(new ReflowEvent(ReflowEvent.LAYOUT_COMPLETE));
        this.fireDocumentLoaded();
    }
	
    /**
     * Description of the Method
     */
    protected void fireDocumentLoaded() {
        Iterator it = this.documentListeners.keySet().iterator();
        while (it.hasNext()) {
            DocumentListener list = (DocumentListener) it.next();
            list.documentLoaded();
        }
    }

	
	/*
	* ========= UserInterface implementation ===============
	*/

    /**
     * Description of the Field
     */
    public Element hovered_element = null;

    /**
     * Description of the Field
     */
    public Element active_element = null;

    /**
     * Description of the Field
     */
    public Element focus_element = null;


    /**
     * Gets the hover attribute of the BasicPanel object
     *
     * @param e PARAM
     * @return The hover value
     */
    public boolean isHover(org.w3c.dom.Element e) {
        if (e == hovered_element) {
            return true;
        }
        return false;
    }

    /**
     * Gets the active attribute of the BasicPanel object
     *
     * @param e PARAM
     * @return The active value
     */
    public boolean isActive(org.w3c.dom.Element e) {
        if (e == active_element) {
            return true;
        }
        return false;
    }

	/**
     * Gets the focus attribute of the BasicPanel object
     *
     * @param e PARAM
     * @return The focus value
     */
    public boolean isFocus(org.w3c.dom.Element e) {
        if (e == focus_element) {
            return true;
        }
        return false;
    }

	
    /**
     * Description of the Method
     *
     * @param e PARAM
     */
    public void componentHidden(ComponentEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e PARAM
     */
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e PARAM
     */
    public void componentResized(ComponentEvent e) {
        Uu.p("componentResized() " + this.getSize());
        Uu.p("viewport = " + enclosingScrollPane.getViewport().getSize());
		RenderQueue.getInstance().dispatchLayoutEvent(new ReflowEvent(ReflowEvent.CANVAS_RESIZED,
			enclosingScrollPane.getViewport().getSize()));
    }

    /**
     * Description of the Method
     *
     * @param e PARAM
     */
    public void componentShown(ComponentEvent e) {
    }

}
