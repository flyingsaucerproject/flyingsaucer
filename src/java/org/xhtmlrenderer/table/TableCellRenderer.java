package org.xhtmlrenderer.table;

import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;
import java.awt.*;


public class TableCellRenderer extends InlineRenderer {
    /**
     * Description of the Method
     *
     * @param c    PARAM
     * @param box  PARAM
     */
    public void paintBackground( Context c, Box box ) {

        //contents.height = c.getExtents().height;

        //u.p("painting a cell background: " + box);

        super.paintBackground( c, box );

    }
}
