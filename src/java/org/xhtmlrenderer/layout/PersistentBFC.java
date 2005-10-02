package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.value.Border;
import org.xhtmlrenderer.render.Box;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-okt-02
 * Time: 21:53:53
 * To change this template use File | Settings | File Templates.
 */
public class PersistentBFC {
    protected Box master = null;
    protected int width;
    protected List left_floats;
    protected List right_floats;
    protected Map offset_map;
    protected List abs_bottom;
    protected Border insets;
    protected Border padding;

    private PersistentBFC() {
        left_floats = new ArrayList();
        right_floats = new ArrayList();
        abs_bottom = new ArrayList();
        offset_map = new HashMap();
    }

    public PersistentBFC(Box master, Context c) {
        this();
        int parent_width = (int) c.getExtents().getWidth();
        CalculatedStyle style = c.getCurrentStyle();
        Border border = style.getBorderWidth(c.getCtx());
        //note: percentages here refer to width of containing block
        Border margin = master.getMarginWidth(c, parent_width);
        padding = style.getPaddingWidth(parent_width, parent_width, c.getCtx());
        insets = new Border(margin.top + border.top + padding.top,
                padding.right + border.right + margin.right,
                padding.bottom + border.bottom + margin.bottom,
                margin.left + border.left + padding.left);
        this.master = master;
        master.setPersistentBFC(this);
    }
}
