package org.joshy.html;

import java.util.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Rectangle;

import org.w3c.dom.*;
import org.joshy.*;
import org.joshy.html.box.*;

/** a table cell is a normal inline layout box, except that
when it does the actual painting it uses the height of the bounds
instead of it's intrinsic height. (hopefully this will change to be less
clunky and more explict when I redesign it all to use a separate Box
pass. 
*/
public class TableCellLayout extends InlineLayout {

    
    public void paintBackground(Context c, Box box) {
        //contents.height = c.getExtents().height;
        //u.p("painting a cell background: " + box);
        super.paintBackground(c,box);
    }

}

