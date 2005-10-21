package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
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
        RectPropertySet margin = master.getStyle().getMarginWidth();
        padding = style.getPaddingWidth(parent_width, parent_width, c.getCtx());
        // CLEAN: cast to int
        insets = new Border((int)margin.getTopWidth() + border.top + padding.top,
                padding.right + border.right + (int)margin.getRightWidth(),
                padding.bottom + border.bottom + (int)margin.getBottomWidth(),
                (int)margin.getLeftWidth() + border.left + padding.left);
        this.master = master;
        master.setPersistentBFC(this);
    }


    public int getWidth() {
        return width;
        //return master.width - master.totalHorizontalPadding();
    }

    public int getHeight() {
        return master.height;
    }
}
