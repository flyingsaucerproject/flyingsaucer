/*
todo:

recombine the tablelayout and tablelayout2
move more of the calculation code into the table package
make a set of tests in addition to the demo
    no span
    col span
    row span
    col and row span
    col span contents that are too big
    row span contents that are too big
    col and row span contents that are too big
implement row height growing based on row spanned contents

investigate margin collapsing
support captions, headers, and footers





 - joshy
*/

package org.joshy.html;

import java.awt.Point;
import java.awt.Rectangle;

import java.util.*;

import org.joshy.html.box.*;
import org.joshy.html.table.*;

import org.joshy.u;

import org.w3c.dom.*;


public class TableLayout2
    extends TableLayout {
    public Box createBox(Context c, Node node) {

        TableBox table = new TableBox();
        table.node = node;

        // set up the box properties
        getMargin(c, table);
        getPadding(c, table);
        getBorder(c, table);

        return table;
    }

    public Box layout(Context c, Element elem) {

        try {

            //u.p("\n====\nLayout");
            // create the table box
            TableBox table_box = (TableBox)createBox(c, elem);

            // set up the border spacing
            float border_spacing = c.css.getFloatProperty(elem, 
                                                          "border-spacing");
            table_box.spacing = new Point((int)border_spacing, 
                                          (int)border_spacing);

            // set up the width
            int fixed_width = c.getExtents().width;

            if (c.css.hasProperty(elem, "width", false)) {
                fixed_width = (int)c.css.getFloatProperty(elem, "width", false);
            }

            int orig_fixed_width = fixed_width;

            //subtract off the margin, border, and padding
            fixed_width -= table_box.margin.left + table_box.border.left +
                table_box.padding.left + table_box.spacing.x + 
                table_box.padding.right + table_box.border.right + table_box.margin.right;

            // create the table
            Table table = new Table();
            table.addTable(elem);

            //calculate the widths
            table.calculateWidths(fixed_width, c);

            //pull out the boxes
            Box bx = calculateBoxes(fixed_width, table_box, c, table);
            bx.width += table_box.margin.left + table_box.border.left + table_box.padding.left 
                + table_box.margin.right + table_box.border.right + table_box.padding.right;
            bx.height += table_box.margin.top + table_box.border.top + table_box.padding.top +
                table_box.margin.bottom + table_box.border.bottom + table_box.padding.bottom;

            //bx.width
            return bx;
        } catch (Exception ex) {
            u.p(ex);

            return null;
        }
    }
    
    
    public Box calculateBoxes(int avail_width, TableBox box, Context c, Table table) {

        box.width = avail_width;
        box.height = 100;
        box.x = 5;
        box.y = 5;
        
        // create a dummy prev row
        RowBox prev_row = new RowBox(0,0,0,0);
        int max_width = 0;
        
        // loop throw the rows
        CellGrid grid = table.getCellGrid();
        for(int y=0; y<grid.getHeight(); y++) { 
            // create a new row box for this row
            RowBox row_box = new RowBox(0,0,0,0);
            //row_box.node = row.node;
            box.rows.add(row_box);
            
            int row_height = 0;
            int column_count = 0;
            
            // loop through the cells
            for(int x=0; x<grid.getWidth(); x++) {
                u.p("x = " + x);
                //u.p("grid width = " + grid.getWidth());
                if(grid.isReal(x,y)) {
                    u.p("it's real");
                    
                    Cell cell = grid.getCell(x,y);
                    
                    // create a new cell box for this cell
                    CellBox cell_box = new CellBox(0,0,10,10);
                    cell.cb = cell_box;
                    cell_box.rb = row_box;
                    // set the x coord based on the current column
                    cell_box.x = table.calcColumnX(column_count);
                    // set the width
                    //u.p("column count = " + column_count + " col span = " + cell.col_span);
                    cell_box.width = table.calcColumnWidth(column_count, cell.col_span);
                    cell_box.node = cell.node;
                    
                    // do the internal layout
                        // save the old extents and create new with smaller width
                        Rectangle oe = c.getExtents();
                        c.setExtents(new Rectangle(c.getExtents().x,c.getExtents().y,
                            cell_box.width, 100));
                        // do child layout
                        Layout layout = LayoutFactory.getLayout(cell.node);
                        Box cell_contents = layout.layout(c,(Element)cell_box.node);
                        cell_box.sub_box = cell_contents;
                        cell_box.height = cell_box.sub_box.height;
                        column_count += cell.col_span;
                        // restore old extents
                        c.setExtents(oe);
                    
                    
                    // y is relative to the rowbox so it's just 0
                    cell_box.y = 0;
                    // add the cell to the row
                    row_box.cells.add(cell_box);
                    
                                    
                    // if this is a non row spanning cell then
                    // adjust the row height to fit this cell
                    if(cell.row_span == 1) {
                        if(cell_box.height > row_box.height) {
                            row_box.height = cell_box.height;
                        }
                    }
                    row_box.width += cell_box.width;
                    
                } else {
                    u.p("it's virtual");
                    Cell cell = grid.getCell(x,y);
                    // create a virtual cell box for this cell
                    CellBox cell_box = CellBox.createVirtual(cell.cb);
                    // skip doing layout
                    row_box.cells.add(cell_box);
                    // skip adjusting the row height for now
                    // set row height based on real cell contents
                    // set row width based on real cell contents
                }
                //u.p("looping");
            }
            //u.p("loop done");
            
            // move the row to the right y position
            row_height = 0;
            row_box.y = prev_row.y + prev_row.height;
            prev_row = row_box;
            // adjust the max width
            if(row_box.width > max_width) {
                max_width = row_box.width;
            }
            
            // adjust the height of each cell in this row to be the height of
            // the row
            for(int k=0; k<row_box.cells.size(); k++) {
                CellBox cb = (CellBox)row_box.cells.get(k);
                if(cb.isReal()) {
                    cb.height = row_box.height;
                    cb.sub_box.height = row_box.height;
                } else {
                    // adjusting height based on virtual
                    //u.p("adjusting height based on virtual");
                    CellBox real = cb.getReal();
                    //u.p("the real cb = " + real);
                    RowBox orig_row = real.rb;
                    //u.p("orig row = " + orig_row);
                    RowBox cur_row = row_box;
                    //u.p("cur row = " + cur_row);
                    
                    real.height = cur_row.y - orig_row.y + cur_row.height;
                    real.sub_box.height = real.height;
                    //u.p("now real = " + real);
                }
                u.p("cell = " + cb);
            }
            
            
        }
        
        box.height = prev_row.y + prev_row.height;
        box.width = max_width;
        return box;
        
    }

}

/* to support row spanning
as we go across each row we have to figure out if the current cell
is spanned to the one above or not.  first we need a growable grid
object to manage the cells.

addCell(x,y,col_span,row_span)
getWidth()
getHeight()
isReal(x,y)
//isVirtual(x,y)
//getColSpan(x,y)
//getRowSpan(x,y)
//getRealCell(x,y)

loop through all cells and add them
calc the column widths
for each row
    for each cell
        if isReal()
            add to row_box
            do internal layout
            set x based on column widths
            set y based on row
            set w based on contents and column widths
            set h based on row height
        if is virtual()
            update w based on column
            update h based on row heights between orig row and this row

*/


