package org.joshy.html;


//import java.awt.Color;
//import java.util.ArrayList;
//import java.util.List;
//import org.joshy.html.box.BlockBox;
import java.awt.Point;
import java.awt.Rectangle;

import org.joshy.html.box.*;
import org.joshy.html.util.*;

import org.joshy.u;
import org.joshy.x;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/* 
joshy: 
    new features to add
    columns to specify width
    first row of cells to specify width
    //calculate height
    //draw margins, border, padding of outside table
    //draw borders and padding and background of inside cells
    //make cells fill their width
    
    do inner cell vertical and horizontal alignment
    implement spanned cells
    
    include these boxes in the redesign of the overall layout flow

*/

/**
    TableLayout performs the layout and painting of an XHTML Table
    on screen. It makes use of the TableBox and CellBox classes in the
    box package.  It currently implements only the fixed layout algorithm,
    meaning width must be explictly set on the table or the columns. Width
    will not be calculated by the size of the contents of each cell. That
    will be implemented later.
*/

public class TableLayout
    extends BoxLayout {

    private static final int fudge = 0; // is this used anymore?

    public Box createBox(Context c, Node node) {

        Box box = new TableBox(0, 0, 0, 0);
        box.node = node;

        return box;
    }

    
    
    
    /** this is the core layout code of the table. it should be
    heavily overhauled.*/
    public Box layout(Context c, Element elem) {

        TableBox table = (TableBox)createBox(c, elem);
        
        // calculate the available space 
        getMargin(c, table);
        getPadding(c, table);
        getBorder(c, table);
        float border_spacing = c.css.getFloatProperty(elem, "border-spacing");
        table.spacing = new Point((int)border_spacing, (int)border_spacing);
        //input available width
        //int fixed_width = (int) c.css.getFloatProperty(elem,"width");
        int fixed_width = c.getExtents().width;

        //u.p("initial fixed width = " + fixed_width);
        int orig_fixed_width = fixed_width;
        fixed_width -= table.margin.left + table.border.left + table.padding.left + 
            table.spacing.x + table.padding.right + table.border.right + table.margin.right;

        //u.p("fixed width = " + fixed_width);
        int col_count = getColumnCount(elem);

        //u.p("col count = " + col_count);
        int[] col_widths = new int[col_count];

        // initalize them all to -1
        for (int i = 0; i < col_count; i++) {
            col_widths[i] = -1;
        }

        //leftover space = table.width - sum(columns.widths)
        int leftover_width = (int)fixed_width - 0;

        // calculate how wide each column should be and return the total
        leftover_width -= calculateColumnWidths(c,elem,col_widths);

        // distribute the remaining space to the unset columns
        distributeRemainingColumnWidth(c,col_widths,leftover_width,table);

        //table.width = max(table.width, sum(columns.widths))
        table.width = fixed_width;
            
        layoutTableRows(c, table, elem, col_widths, orig_fixed_width);
        return table;
    }
    
    protected int calculateColumnWidths(Context c, Element elem, int[] col_widths) {
        int total_width = 0;
        
        Element tr = x.child(elem, "tr");
        NodeList nl = elem.getChildNodes();

        //NodeList nl = x.children(tr,"td");
        int count = 0;

        for (int i = 0; i < nl.getLength(); i++) {

            if (nl.item(i).getNodeName().equals("td") || 
                nl.item(i).getNodeName().equals("th")) {

                Element td = (Element)nl.item(i);
                u.p("got td: " + td + " " + i + " count = " + count);

                //if(cell.width)
                if (c.css.hasProperty(td, "width", false)) {

                    //save column.width;
                    if (count > col_widths.length) {
                        u.p("elem = ");

                        //x.p(elem);
                    }

                    col_widths[count] = (int)c.css.getFloatProperty(td, 
                                                                    "width");
                    
                    total_width += col_widths[count];
                }

                count++;
            }
        }
        return total_width;
    }

    public void distributeRemainingColumnWidth(Context c, int[]col_widths, int leftover_width, TableBox table) {
        
        // count the remaining unset columns
        int unset_count = 0;

        for (int i = 0; i < col_widths.length; i++) {

            if (col_widths[i] == -1) {
                unset_count++;
            }
        }

        //if(leftover space > 0) {
        if (leftover_width > 0) {

            //distribute leftover space to columns
            for (int i = 0; i < col_widths.length; i++) {

                // set width only if it's not already set
                if (col_widths[i] == -1) {
                    col_widths[i] = (leftover_width - 
                                    table.spacing.x * col_widths.length) / unset_count;
                }
            }
        }
    }
    
    protected void layoutTableRows(Context c, TableBox table, Element elem, int[] col_widths, int orig_fixed_width) {
        
        // create dummy previous row
        RowBox prev_row = new RowBox(0, 0, 0, 0);
        prev_row.y = table.margin.top + table.border.top + 
                     table.padding.top - fudge;

        // loop through all of the table rows
        NodeList rows = elem.getChildNodes();
        for (int i = 0; i < rows.getLength(); i++) {
            Node row = rows.item(i);
            if (row.getNodeName().equals("tr")) {

                prev_row = layoutRow(c,row,prev_row,table,col_widths);
            }
        }
        
        
        table.height = prev_row.y + prev_row.height + table.spacing.y + 
                       table.padding.bottom + table.border.bottom + 
                       table.margin.bottom;
        table.width = orig_fixed_width;
    }
    
    
    public RowBox layoutRow(Context c, Node row, RowBox prev_row, TableBox table, int[] col_widths) {
        // create a new rowbox
        RowBox rowbox = new RowBox(0, 0, 0, 0);
        rowbox.node = row;

        // create dummy previous cell
        CellBox prev_cell = new CellBox(0, 0, 0, 0);
        
        // loop through all of the cells in the current row
        NodeList cells = row.getChildNodes();
        int col_counter = 0;
        for (int j = 0; j < cells.getLength(); j++) {
            Node cell = cells.item(j);
            if (cell.getNodeName().equals("td") || 
                cell.getNodeName().equals("th")) {
                
                // if there are too many cells on this line then skip the rest
                if (col_counter >= col_widths.length) {
                    u.p("WARNING: too many cells on this row");
                    continue;
                }

                prev_cell = layoutCell(c, cell, prev_cell, rowbox, table, col_widths[col_counter]);

                //u.p("col counter = " + col_counter);
                col_counter++;

                //u.p("now its: " + col_counter);
            }
        }

        // x is always 0 (rel to the parent table)
        rowbox.x = +table.margin.left + table.border.left + 
                   table.padding.left;

        // y is prev row.y + prev row.height
        rowbox.y = prev_row.y + prev_row.height + table.spacing.y + 
                   fudge;

        // width is width of table
        rowbox.width = table.width;

        // set the heights on all of the cells
        for (int k = 0; k < rowbox.cells.size(); k++) {
            ((CellBox)rowbox.cells.get(k)).height = rowbox.height;
        }

        table.rows.add(rowbox);

        //u.p("row = " + rowbox);
        return rowbox;
    }
    
    public CellBox layoutCell(Context c, Node cell, CellBox prev_cell, RowBox rowbox, TableBox table, int cellwidth) {
        CellBox cellbox = new CellBox(0, 0, cellwidth, 0);
        
        // attach the node
        cellbox.node = cell;
        getBorder(c, cellbox);
        getMargin(c, cellbox);
        getPadding(c, cellbox);

        //layout cell w/ modified inline
        cellbox.x = prev_cell.x + prev_cell.width + 
                    table.spacing.x + fudge;

        // y is 0 relative to the parent row
        cellbox.y = 0;

        // set height to 50 until it's set by the cell contents
        cellbox.height = 50;

        Rectangle oe = c.getExtents();

        // new extents = old extents but smaller. same origin tho
        c.setExtents(new Rectangle(c.getExtents().x, 
                                   c.getExtents().y, 
                                   cellbox.width, 100));

        // lay out the cell's contents
        Layout layout = LayoutFactory.getLayout(cell);
        Box cell_contents = layout.layout(c, (Element)cellbox.node);
        cellbox.sub_box = cell_contents;

        // restore old extents
        c.setExtents(oe);

        // height of the cell will be based on the height of it's
        // contents
        cellbox.height = cell_contents.height;

        //save cellbox
        // height of row is max height of cells
        rowbox.height = Math.max(cellbox.height, rowbox.height);
        rowbox.cells.add(cellbox);

        return cellbox;
    }
    
    
    /* =========== painting code ============= */
    /** The entry point to the painting routines. It takes the
    table box to be painted and the current context. It will call
    paintBackground(), paintComponent(), and paintBorder() on it's
    own. */
    public void paint(Context c, Box box) {

        //u.p("TableLayout.paint( " + c);
        // copy the bounds to we don't mess it up
        Rectangle oldBounds = new Rectangle(c.getExtents());

        //Rectangle contents = layout(c,elem);
        //adjustWidth(c,elem);
        paintBackground(c, box);
        paintComponent(c, box);

        //paintChildren(c,elem);
        paintBorder(c, box);

        // move the origin down to account for the contents plus the margin, borders, and padding
        oldBounds.y = oldBounds.y + box.height;
        c.setExtents(oldBounds);
    }

    public void paintComponent(Context c, Box box) {
        paintTable(c, (TableBox)box);
    }

    public void paintChildren(Context c, Box box) {
    }

    protected void paintTable(Context c, TableBox table) {
        c.getGraphics().translate(table.x, table.y);
        c.getGraphics().translate(
                table.margin.left + table.border.left + table.padding.left, 
                table.margin.top + table.border.top + table.padding.top);

        // loop over the rows
        for (int i = 0; i < table.rows.size(); i++) {

            RowBox row = (RowBox)table.rows.get(i);

            // save the old extents
            Rectangle oe = c.getExtents();

            // move origin by row.x and row.y
            c.setExtents(new Rectangle(oe.x + row.x, oe.y + row.y, oe.width, 
                                       oe.height));
            c.getGraphics().translate(row.x, row.y);

            // paint the row
            paintRow(c, row);

            // restore the old extents and translate
            c.getGraphics().translate(-row.x, -row.y);
            c.setExtents(oe);
        }

        c.getGraphics().translate(
                -table.margin.left - table.border.left - table.padding.left, 
                -table.margin.top - table.border.top - table.padding.top);
        c.getGraphics().translate(-table.x, -table.y);

        //c.getGraphics().translate(-c.getExtents().x, -c.getExtents().y);
    }

    protected void paintRow(Context c, RowBox row) {

        //u.p("Paint Row c = " + c);
        //u.p("paint row = " + row);
        // debug
        for (int i = 0; i < row.cells.size(); i++) {

            CellBox cell = (CellBox)row.cells.get(i);
            Rectangle oe = c.getExtents();
            c.setExtents(new Rectangle(cell.x, cell.y, oe.width, oe.height));
            paintCell(c, cell);
            c.setExtents(oe);
        }
    }

    protected void paintCell(Context c, CellBox cell) {
        if(cell.isReal()) {
            Rectangle oe = c.getExtents();
            c.getGraphics().translate(oe.x, oe.y);
            c.setExtents(new Rectangle(0, 0, cell.width, cell.height));
    
            Layout layout = LayoutFactory.getLayout(cell.node);
            //u.p("doing cell: " + cell);
            layout.paint(c, cell.sub_box);
    
            c.getGraphics().translate(-oe.x, -oe.y);
            c.setExtents(oe);
    
        }
    }

    
    
    
    
    
    
    /* =============== utility code ================ */
    private int getColumnCount(Element tb) {

        int count = 0;
        NodeList nl = tb.getChildNodes();

        for (int i = 0; i < nl.getLength(); i++) {

            Node row = nl.item(i);

            if (row.getNodeName().equals("tr")) {

                NodeList cells = row.getChildNodes();

                for (int j = 0; j < cells.getLength(); j++) {

                    Node cell = cells.item(j);

                    if (cell.getNodeName().equals("td") || 
                        cell.getNodeName().equals("th")) {
                        count++;
                    }
                }

                // return now since we only go through the first row
                return count;
            }
        }

        return count;
    }
}
