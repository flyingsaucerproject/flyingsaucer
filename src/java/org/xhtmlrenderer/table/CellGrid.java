/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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
package org.xhtmlrenderer.table;

import org.xhtmlrenderer.util.Uu;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class CellGrid {
    /** Description of the Field  */
    private Cell[][] grid;

    /** Description of the Field  */
    private int height = 0;
    /** Description of the Field  */
    private int width = 0;


    /** Constructor for the CellGrid object  */
    public CellGrid() {
        grid = new Cell[20][20];
    }

    /**
     * Adds a feature to the Cell attribute of the CellGrid object
     *
     * @param x         The feature to be added to the Cell attribute
     * @param y         The feature to be added to the Cell attribute
     * @param col_span  The feature to be added to the Cell attribute
     * @param row_span  The feature to be added to the Cell attribute
     * @param cell      The feature to be added to the Cell attribute
     */
    public void addCell( int x, int y, int col_span, int row_span, Cell cell ) {
        // set each place in the grid
        for ( int i = 0; i < col_span; i++ ) {
            for ( int j = 0; j < row_span; j++ ) {
                setCell( x + i, y + j, cell );
            }
        }
    }

    /**
     * Gets the height attribute of the CellGrid object
     *
     * @return   The height value
     */
    public int getHeight() {
        return height + 1;
    }

    /**
     * Gets the width attribute of the CellGrid object
     *
     * @return   The width value
     */
    public int getWidth() {
        return width + 1;
    }

    /**
     * Gets the cell attribute of the CellGrid object
     *
     * @param x  PARAM
     * @param y  PARAM
     * @return   The cell value
     */
    public Cell getCell( int x, int y ) {
        if ( x >= getWidth() ) {
            Uu.p( "CellGrid.getCell(" + x + "," + y + ")  but width = " + getWidth() );
        }
        if ( y >= getHeight() ) {
            Uu.p( "CellGrid.getCell(" + x + "," + y + ")  but height = " + getHeight() );
        }
        if ( grid[y][x] == null ) {
            Uu.p( "CellGrid.getCell(" + x + "," + y + ")  is null" );
        }
        //Uu.p("CellGrid.getCell("+Xx+","+y+")");
        return grid[y][x];
    }

    /**
     * Gets the real attribute of the CellGrid object
     *
     * @param x  PARAM
     * @param y  PARAM
     * @return   The real value
     */
    public boolean isReal( int x, int y ) {
        if ( !isRowVirtual( x, y ) && !isColVirtual( x, y ) ) {
            return true;
        }
        return false;
    }

    /**
     * Sets the cell attribute of the CellGrid object
     *
     * @param x     The new cell value
     * @param y     The new cell value
     * @param cell  The new cell value
     */
    private void setCell( int x, int y, Cell cell ) {
        //Uu.p("CellGrid.setCell("+Xx+","+y+","+cell+")");
        //Uu.p("grid = " + grid);
        grid[y][x] = cell;
        if ( y > height ) {
            height = y;
        }
        if ( x > width ) {
            width = x;
        }
    }


    /**
     * Gets the rowVirtual attribute of the CellGrid object
     *
     * @param x  PARAM
     * @param y  PARAM
     * @return   The rowVirtual value
     */
    private boolean isRowVirtual( int x, int y ) {
        if ( y == 0 ) {
            return false;
        }
        if ( getCell( x, y - 1 ) == getCell( x, y ) ) {
            return true;
        }
        return false;
    }

    /**
     * Gets the colVirtual attribute of the CellGrid object
     *
     * @param x  PARAM
     * @param y  PARAM
     * @return   The colVirtual value
     */
    private boolean isColVirtual( int x, int y ) {
        if ( x == 0 ) {
            return false;
        }
        if ( getCell( x - 1, y ) == getCell( x, y ) ) {
            return true;
        }
        return false;
    }

}

/*
 * $Id$
 * $Log$
 * Revision 1.5  2005/01/29 20:22:25  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.4  2004/12/12 03:33:03  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 * Revision 1.3  2004/10/23 13:59:17  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 */

