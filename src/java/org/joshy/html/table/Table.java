
/* 
 * {{{ header & license 
 * Copyright (c) 2004 Joshua Marinacci 
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; either version 2.1 
 * of the License, or (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 
 * }}} 
 */

package org.joshy.html.table;

import org.joshy.u;
import java.util.*;
import org.w3c.dom.*;
import org.joshy.html.box.*;
import java.awt.Rectangle;
import org.joshy.html.*;

public class Table {
    List top_cells = new ArrayList();
    //List rows = new ArrayList();
    private int[] column_widths;
    private CellGrid grid;
    public Table() {
        this.grid = new CellGrid();
    }
    
    
    public void addTable(Element elem) {
        // for each tr
        NodeList rows = elem.getChildNodes();
        boolean first_row = true;
        int row_count = 0;
        for(int i=0; i<rows.getLength(); i++) {
            Node row = rows.item(i);
            if(row.getNodeName().equals("tr")) {
                if(first_row) {
                    addFirstRow(row);
                    first_row = false;
                } else {
                    addRow(row,row_count);
                }
                row_count++;
            }
            
        }
    }
    
    public void addRow(Node row, int y) {
        addRow(row,false, y);
    }
    
    public void addFirstRow(Node row) {
        addRow(row,true, 0);
    }
    
    public void addRow(Node row, boolean first_row, int y) {
        // for each td
        //Row rw = new Row();
        //rw.node = row;
        //u.p("Table.addRow("+row+","+first_row+","+y+")");
        NodeList cells = row.getChildNodes();
        int col_counter = 0;
        for(int j=0; j<cells.getLength(); j++) {
            Node cell = cells.item(j);
            if(cell.getNodeName().equals("td") || cell.getNodeName().equals("th")) {
                //u.p("adding: " + col_counter + " " + y);
                // add the cell
                Cell cl = null;
                if(first_row) {
                    cl = addTopCell(cell,col_counter,y);
                    //rw.addCell(cl);
                } else {
                    cl = addCell(cell,col_counter,y);
                    //rw.addCell(cl);
                }
                col_counter += cl.getColumnSpan();
            }
        }
        first_row = false;
        //rows.add(rw);
    }

    
    public void addColumn(Element elem) {
    }
    
    // add cells from the first row
    public Cell addTopCell(Node node, int x, int y) {
        Cell cl = addCell(node, x, y);
        top_cells.add(cl);
        return cl;
    }
    
    public Cell addCell(Node node, int x, int y) {
        //u.p("addCell("+node+","+x+","+y+")");
        if(node.getNodeType() != node.ELEMENT_NODE) {
            throw new Error("this isn't an element" + node);
        }
        Element cell = (Element)node;
        Cell cl = new Cell();
        cl.node = node;
        if(cell.hasAttribute("colspan")) {
            cl.col_span = Integer.parseInt(cell.getAttribute("colspan"));
        }
        if(cell.hasAttribute("rowspan")) {
            cl.row_span = Integer.parseInt(cell.getAttribute("rowspan"));
        }
        grid.addCell(x,y, cl.col_span,cl.row_span,cl);
        return cl;
    }
    
    // calculate the widths
    public void calculateWidths(int avail_width, Context c) {
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
                // fixed bug that made cell sizing fail w/ %s
                int width = (int)c.css.getFloatProperty((Element)cell.node,"width",avail_width,false);
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
    /*
    public Iterator getRowIterator() {
        return rows.iterator();
    }
    */
    public CellGrid getCellGrid() {
        return grid;
    }
    
    public int calcColumnX(int col) {
        int x = 0;
        for(int i=0; i<col; i++) {
            x += column_widths[i];
        }
        return x;
    }
    
    public int calcColumnWidth(int col, int span) {
        //u.p("calc column width: " + col + " " + span);
        int x = 0;
        for(int i=col; i<col+span; i++) {
            x += column_widths[i];
        }
        return x;
    }
}

