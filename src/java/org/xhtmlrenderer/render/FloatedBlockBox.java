package org.xhtmlrenderer.render;

public class FloatedBlockBox extends BlockBox {
    private boolean _pending;

    public boolean isPending() {
        return _pending;
    }

    public void setPending(boolean pending) {
        _pending = pending;
    }
    
    public String toString() {
        return super.toString() + " (floated)";
    }
}
