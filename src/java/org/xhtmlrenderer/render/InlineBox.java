package org.xhtmlrenderer.render;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.BoxCollector;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;

public class InlineBox extends Box {
    private int baseline;
    
    private boolean startsHere;
    private boolean endsHere;
    
    private List inlineChildren;
    
    private boolean pending;
    
    private int inlineWidth;
    
    public InlineBox(Element elem, CalculatedStyle style, int cbWidth) {
        this();
        this.element = elem;
        setStyle(new Style(style, cbWidth));
        getStyle().setMarginTopOverride(0.0f);
        getStyle().setMarginBottomOverride(0.0f);
        setPending(true);
    }
    
    private InlineBox() {
        setState(Box.DONE);
    }
    
    public InlineBox copyOf() {
        InlineBox result = new InlineBox();
        result.element = this.element;
        
        // XXX not right when first-line transition is happening
        result.setStyle(getStyle());
        result.setHeight(getHeight());
        
        result.setPending(isPending(), true);
        
        return result;
    }
    
    public void calculateHeight(LayoutContext c) {
        BorderPropertySet border = getStyle().getCalculatedStyle().getBorder(c);
        RectPropertySet padding = getStyle().getPaddingWidth(c);
        
        LineMetrics metrics = getStyle().getLineMetrics(c);
        
        setHeight((int)(border.top() + padding.top() + metrics.getAscent() + metrics.getDescent() +
                padding.bottom() + border.bottom()));
    }

    public int getBaseline() {
        return baseline;
    }

    public void setBaseline(int baseline) {
        this.baseline = baseline;
    }

    public int getInlineChildCount() {
        return inlineChildren == null ? 0 : inlineChildren.size();
    }
    
    public void addInlineChild(Object child) {
        if (inlineChildren == null) {
            inlineChildren = new ArrayList();
        }
        
        inlineChildren.add(child);
        
        if (child instanceof Box) {
            ((Box)child).setParent(this);
        } else if (child instanceof InlineText) {
            ((InlineText)child).setParent(this);
        } else {
            throw new IllegalArgumentException();
        }
        
        if (isPending()) {
            setPending(false);
        }
    }
    
    public List getInlineChildren() {
        return inlineChildren == null ? Collections.EMPTY_LIST : inlineChildren;
    }
    
    public Object getInlineChild(int i) {
        if (inlineChildren == null) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            return inlineChildren.get(i);
        }
    }
    
    public int getInlineWidth(CssContext cssCtx) {
        return inlineWidth;
    }
    
    public void prunePending() {
        if (getInlineChildCount() > 0) {
            for (int i = getInlineChildCount() - 1; i >= 0; i--) {
                Object child = (Object)getInlineChild(i);
                if (! (child instanceof InlineBox)) {
                    break;
                }
                
                InlineBox iB = (InlineBox)child;
                iB.prunePending();
                
                if (iB.isPending()) {
                    removeChild(i);
                } else {
                    break;
                }
            }
        }
    }

    public boolean isEndsHere() {
        return endsHere;
    }

    public void setEndsHere(boolean endsHere) {
        this.endsHere = endsHere;
    }

    public boolean isStartsHere() {
        return startsHere;
    }

    public void setStartsHere(boolean startsHere) {
        this.startsHere = startsHere;
    }

    public boolean isPending() {
        return pending;
    }
    
    private void setPending(boolean pending, boolean copying) {
        this.pending = pending;
        
        if (! copying && ! pending) {
            if (getParent() instanceof InlineBox) {
                ((InlineBox)getParent()).setPending(false);
            }
            
            setStartsHere(true);
        }
    }

    public void setPending(boolean pending) {
        setPending(pending, false);
    }
    
    public void paint(RenderingContext c) {
        paintBackground(c);
        paintBorder(c);
        
        // paint order (???)
        // text-decoration
        
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = getInlineChild(i);
            
            if (child instanceof InlineText) {
                ((InlineText)child).paint(c);
            }
        }        
        
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = getInlineChild(i);
            
            if (child instanceof InlineBox) {
                ((InlineBox)child).paint(c);
            } else if (child instanceof Box) {
                Box b = (Box)child;
                if (b.getLayer() == null) {
                    Point offset = c.getOriginOffset();
                    c.translate(-offset.x, -offset.y);
                    Layer.paintAsLayer(c, b);
                    c.translate(offset.x, offset.y);
                }
            }
        }
    }
    
    protected int getBorderSides() {
        int result = BorderPainter.TOP + BorderPainter.BOTTOM;
        
        if (startsHere) {
            result += BorderPainter.LEFT;
        }
        if (endsHere) {
            result += BorderPainter.RIGHT;
        }
        
        return result;
    }
    
    protected Rectangle getPaintingBorderEdge(CssContext cssCtx) {
        // x, y pins the content area of the box so subtract off top border and padding
        // too
        
        int marginLeft = 0;
        int marginRight = 0;
        if (startsHere || endsHere) {
            RectPropertySet margin = (RectPropertySet)getStyle().getMarginWidth(cssCtx);
            if (startsHere) {
                marginLeft = (int)margin.left();
            } 
            if (endsHere) {
                marginRight = (int)margin.right();
            }
        }
        BorderPropertySet border = getStyle().getCalculatedStyle().getBorder(cssCtx);
        RectPropertySet padding = getStyle().getPaddingWidth(cssCtx);
        
        Rectangle result = new Rectangle(
                this.x + marginLeft, this.y - (int)border.top() - (int)padding.top(), 
                getInlineWidth(cssCtx) - marginLeft - marginRight, getHeight());
        return result;
    }
    
    public int getLeftMarginBorderPadding(CssContext cssCtx) {
        if (startsHere) {
            return getStyle().getLeftMarginBorderPadding(cssCtx);
        } else {
            return 0;
        }
    }
    
    public int getRightMarginPaddingBorder(CssContext cssCtx) {
        if (endsHere) {
            return getStyle().getRightMarginBorderPadding(cssCtx);
        } else {
            return 0;
        }
    }    
    
    public int getInlineWidth() {
        return inlineWidth;
    }

    public void setInlineWidth(int inlineWidth) {
        this.inlineWidth = inlineWidth;
    }
    
    // XXX Could we just not add them in the first place?
    public boolean containsContent() {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object child = (Object)getInlineChild(i);
            if (child instanceof InlineText) {
                InlineText iT = (InlineText)child;
                if (! iT.isEmpty()) {
                    return true;
                }
            } else if (child instanceof InlineBox) {
                InlineBox iB = (InlineBox)child;
                if (iB.containsContent()) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }
    
    public boolean intersectsInlineBlocks(CssContext cssCtx, Shape clip) {
        for (int i = 0; i < getInlineChildCount(); i++) {
            Object obj = getInlineChild(i);
            
            if (obj instanceof InlineBox) {
                boolean possibleResult = 
                    ((InlineBox)obj).intersectsInlineBlocks(cssCtx, clip);
                if (possibleResult) {
                    return true;
                }
            } else if (obj instanceof Box) {
                BoxCollector collector = new BoxCollector();
                if (collector.intersectsAny(cssCtx, clip, (Box)obj)) {
                    return true;
                }
            }
        }
        
        return false;
    }    
}
