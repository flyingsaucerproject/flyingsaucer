package org.xhtmlrenderer.table;

import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineRenderer;


public class TableCellRenderer extends InlineRenderer {
    /**
     * Description of the Method
     *
     * @param c   PARAM
     * @param box PARAM
     */
    public void paintBackground(Context c, Box box) {

        //contents.height = c.getExtents().height;

        //Uu.p("painting a cell background: " + box);

        super.paintBackground(c, box);

    }
}
