package org.joshy.html;
import org.joshy.html.box.*;
import org.w3c.dom.*;
import java.util.*;
import java.awt.Point;
import java.awt.Rectangle;
import org.joshy.u;

public class TableLayout2 extends TableLayout {
    public Box createBox(Context c, Node node) {
        TableBox table = new TableBox();
        table.node = node;
        // set up the box properties
        getMargin(c,table);
        getPadding(c,table);
        getBorder(c,table);
        return table;
    }
    public Box layout(Context c, Element elem) {
        try {
        //u.p("\n====\nLayout");
        // create the table box
        TableBox table_box = (TableBox)createBox(c,elem);
        
        // set up the border spacing
        float border_spacing = c.css.getFloatProperty(elem,"border-spacing");
        table_box.spacing = new Point((int)border_spacing,(int)border_spacing);
        
        // set up the width
        int fixed_width = c.getExtents().width;
        if(c.css.hasProperty(elem,"width",false)) {
            fixed_width = (int) c.css.getFloatProperty(elem,"width",false);
        }
        int orig_fixed_width = fixed_width;
        //subtract off the margin, border, and padding
        fixed_width -= table_box.margin.left + table_box.border.left + table_box.padding.left + 
            table_box.spacing.x +
            table_box.padding.right + table_box.border.right + table_box.margin.right;
            
            
        // create the table
        Table table = new Table();
        table.addTable(elem);
        
        //calculate the widths
        table.calculateWidths(fixed_width,c);
        //pull out the boxes
        Box bx = table.calculateBoxes(fixed_width,table_box,c);
        bx.width += table_box.margin.left + table_box.border.left + table_box.padding.left +
                    table_box.margin.right + table_box.border.right + table_box.padding.right;
        bx.height += table_box.margin.top + table_box.border.top + table_box.padding.top +
                    table_box.margin.bottom + table_box.border.bottom + table_box.padding.bottom;
        //bx.width
        return bx;
        } catch (Exception ex) {
            u.p(ex);
            return null;
        }
    }

    
    
}

class Table {
    List top_cells = new ArrayList();
    List rows = new ArrayList();
    
    
    public void addTable(Element elem) throws Exception {
        // for each tr
        NodeList rows = elem.getChildNodes();
        boolean first_row = true;
        for(int i=0; i<rows.getLength(); i++) {
            Node row = rows.item(i);
            if(row.getNodeName().equals("tr")) {
                if(first_row) {
                    addFirstRow(row);
                    first_row = false;
                } else {
                    addRow(row);
                }
            }
        }
    }
    
    public void addRow(Node row) throws Exception {
        addRow(row,false);
    }
    public void addFirstRow(Node row) throws Exception {
        addRow(row,true);
    }
    public void addRow(Node row, boolean first_row) throws Exception {
        // for each td
        Row rw = new Row();
        rw.node = row;
        NodeList cells = row.getChildNodes();
        int col_counter = 0;
        for(int j=0; j<cells.getLength(); j++) {
            Node cell = cells.item(j);
            if(cell.getNodeName().equals("td") || cell.getNodeName().equals("th")) {
                // add the cell
                if(first_row) {
                    Cell cl = addTopCell(cell);
                    rw.addCell(cl);
                } else {
                    Cell cl = addCell(cell);
                    rw.addCell(cl);
                }
            }
        }
        first_row = false;
        rows.add(rw);
    }

    
    public void addColumn(Element elem) {}
    
    // add cells from the first row
    public Cell addTopCell(Node node) throws Exception {
        Cell cl = addCell(node);
        top_cells.add(cl);
        return cl;
    }
    
    public Cell addCell(Node node) throws Exception {
        if(node.getNodeType() != node.ELEMENT_NODE) {
            throw new Exception("this isn't an element" + node);
        }
        Element cell = (Element)node;
        Cell cl = new Cell();
        cl.node = node;
        if(cell.hasAttribute("colspan")) {
            cl.col_span = Integer.parseInt(cell.getAttribute("colspan"));
        }
        return cl;
    }
    
    // calculate the widths
    void calculateWidths(int avail_width, Context c) {
        //u.p("calculating columns from total space of: " + avail_width);
        //u.p("total column width = " + total_cols);
        
        // get number of columns and init array
        int total_cols = getTotalColumnCount();
        int[] widths = new int[total_cols];
        
        // loop over top cells looking for explict widths
        int col_count = 0;
        for(int i=0; i<top_cells.size(); i++) {
            Cell cell = (Cell)top_cells.get(i);
            if(c.css.hasProperty((Element)cell.node,"width",false)) {
                int width = (int)c.css.getFloatProperty((Element)cell.node,"width",false);
                //u.p("got width: " + width);
                for(int j=col_count; j<col_count+cell.col_span; j++) {
                    widths[j] = width/cell.col_span;
                    avail_width -= width/cell.col_span;
                }
            }
            col_count += cell.col_span;
        }
        //u.p("widths");
        //u.p(widths);
        
        // get number of unset columns
        int unset_cols = 0;
        for(int i=0; i<widths.length; i++) {
            if(widths[i] <= 0) {
                unset_cols++;
            }
        }
        //u.p("unset cols count = " + unset_cols);
        
        
        for(int i=0; i<total_cols; i++) {
            //Cell cell = (Cell)top_cells.get(i);
            if(widths[i] == 0) {
                widths[i] = avail_width/unset_cols;
            }
            //u.p("looking at: " + cell);
            //u.p("set width to: " + widths[i]);
        }
        column_widths = widths;
        //u.p("final widths");
        //u.p(column_widths);
    }
    
    int[] column_widths;
    
    int[] getWidths() {
        return column_widths;
    }
    int getTotalColumnCount() {
        int total_cols = 0;
        Iterator it = top_cells.iterator();
        while(it.hasNext()) {
            Cell cell = (Cell)it.next();
            total_cols += cell.col_span;
        }
        return total_cols;
    }
    
    
    Iterator getRowIterator() {
        return rows.iterator();
    }
    
    Box calculateBoxes(int avail_width, TableBox box, Context c) {
        box.width = avail_width;
        box.height = 100;
        box.x = 5;
        box.y = 5;
        
        
        int max_width = 0;
        Iterator row_it = getRowIterator();
        RowBox prev_row = new RowBox(0,0,0,0);
        while(row_it.hasNext()) {
            Row row = (Row)row_it.next();
            //u.p("generating boxes for: " + row);
            RowBox row_box = new RowBox(0,0,0,0);
            row_box.node = row.node;
            box.rows.add(row_box);
            
            int row_height = 0;
            Iterator cell_it = row.getCellIterator();
            int column_count = 0;
            while(cell_it.hasNext()) {
                Cell cell = (Cell)cell_it.next();
                CellBox cell_box = new CellBox(0,0,10,10);
                //cell_box.width = column_widths[column_count];
                cell_box.x = calcColumnX(column_count);
                cell_box.width = calcColumnWidth(column_count,cell.col_span);
                cell_box.node = cell.node;
                row_box.cells.add(cell_box);
                
                // save the old extents and create new with smaller width
                Rectangle oe = c.getExtents();
                c.setExtents(new Rectangle(c.getExtents().x,c.getExtents().y,
                    cell_box.width, 100));
                // do child layout
                Layout layout = LayoutFactory.getLayout(cell.node);
                Box cell_contents = layout.layout(c,(Element)cell_box.node);
                // restore old extents
                c.setExtents(oe);
                
                cell_box.sub_box = cell_contents;
                cell_box.height = cell_box.sub_box.height;
                //u.p("cellbox = " + cell_box);
                column_count += cell.col_span;
                
                // adjust the row to fit this cell
                if(cell_box.height > row_box.height) {
                    row_box.height = cell_box.height;
                }
                row_box.width += cell_box.width;
                //u.p("cellbox = " + cell_box);
            }
            
            //u.p("max rowbox height = " + row_box);
            for(int k=0; k<row_box.cells.size(); k++) {
                CellBox cb = (CellBox)row_box.cells.get(k);
                cb.height = row_box.height;
                cb.sub_box.height = row_box.height;
                //u.p("setting height on cell: " + cb);
            }
            
            //row_box.height = row_height;
            row_height = 0;
            row_box.y = prev_row.y + prev_row.height;
            prev_row = row_box;
            if(row_box.width > max_width) {
                max_width = row_box.width;
            }
            //u.p("rowbox  = " + row_box);
        }
        box.height = prev_row.y + prev_row.height;
        box.width = max_width;
        return box;
    }
    
    public int calcColumnX(int col) {
        int x = 0;
        for(int i=0; i<col; i++) {
            x += column_widths[i];
        }
        return x;
    }
    public int calcColumnWidth(int col, int span) {
        int x = 0;
        for(int i=col; i<col+span; i++) {
            x += column_widths[i];
        }
        return x;
    }
}

class Row {
    List cells = new ArrayList();
    Node node;
    public void addCell(Cell cell) {
        cells.add(cell);
    }
    Iterator getCellIterator() {
        return cells.iterator();
    }
}

class Cell {
    Node node;
    public int width;
    public int height;
    public int col_span = 1;
    int getColumnSpan() {
        return 1;
    }
    int getRowSpan() {
        return 1;
    }
    int getWidth() {
        return this.width;
    }
    int getHeight() {
        return this.height;
    }
}

