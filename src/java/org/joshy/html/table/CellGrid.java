package org.joshy.html.table;

import org.joshy.u;

public class CellGrid {
    private Cell[][] grid;
    
    private int height = 0;
    private int width = 0;
    
    public int getHeight() {
        return height+1;
    }
    
    public int getWidth() {
        return width+1;
    }
    
    public void addCell(int x, int y, int col_span, int row_span, Cell cell) {
        // set each place in the grid
        for(int i=0; i<col_span; i++) {
            for(int j=0; j<row_span; j++) {
                setCell(x+i,y+j,cell);
            }
        }
    }
    
    private void setCell(int x, int y, Cell cell) {
        //u.p("CellGrid.setCell("+x+","+y+","+cell+")");
        //u.p("grid = " + grid);
        grid[y][x] = cell;
        if(y > height) {
            height = y;
        }
        if(x > width) {
            width = x;
        }
    }
    
    public Cell getCell(int x, int y) {
        if(x >= getWidth()) {
            u.p("CellGrid.getCell("+x+","+y+")  but width = " + getWidth());
        }
        if(y >= getHeight()) {
            u.p("CellGrid.getCell("+x+","+y+")  but height = " + getHeight());
        }
        if(grid[y][x] == null) {
            u.p("CellGrid.getCell("+x+","+y+")  is null");
        }
        //u.p("CellGrid.getCell("+x+","+y+")");
        return grid[y][x];
    }
    
    public boolean isReal(int x, int y) {
        if(!isRowVirtual(x,y) && !isColVirtual(x,y)) {
            return true;
        }
        return false;
    }
    
    
    private boolean isRowVirtual(int x, int y) {
        if(y == 0) {
            return false;
        }
        if(getCell(x,y-1) == getCell(x,y)) {
            return true;
        }
        return false;
    }
    private boolean isColVirtual(int x, int y) {
        if(x == 0) {
            return false;
        }
        if(getCell(x-1,y) == getCell(x,y)) {
            return true;
        }
        return false;
    }

    
    public CellGrid() {
        grid = new Cell[20][20];
    }
    
}
