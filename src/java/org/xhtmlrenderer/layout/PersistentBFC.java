package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
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
    protected RectPropertySet padding;

    private PersistentBFC() {
        left_floats = new ArrayList();
        right_floats = new ArrayList();
        abs_bottom = new ArrayList();
        offset_map = new HashMap();
    }

    public PersistentBFC(Box master, LayoutContext c) {
        this();
        int parent_width = (int) c.getExtents().getWidth();
        CalculatedStyle style = c.getCurrentStyle();
        BorderPropertySet border = style.getBorder(c);
        //note: percentages here refer to width of containing block
        RectPropertySet margin = master.getStyle().getMarginWidth(c);
        padding = style.getPaddingRect(parent_width, parent_width, c);
        // CLEAN: cast to int
        insets = new Border((int) margin.top() + (int) border.top() + (int) padding.top(),
                (int) padding.right() + (int) border.right() + (int) margin.right(),
                (int) padding.bottom() + (int) border.bottom() + (int) margin.bottom(),
                (int) margin.left() + (int) border.left() + (int) padding.left());
        this.master = master;
        master.setPersistentBFC(this);
    }


}
