package org.joshy.html.table;

import java.util.*;
import org.w3c.dom.*;

public class Cell {
    public Node node;
    public int width;
    public int height;
    public int col_span = 1;
    public int row_span = 1;
    int getColumnSpan() {
        return col_span;
    }
    int getRowSpan() {
        return row_span;
    }
    int getWidth() {
        return this.width;
    }
    int getHeight() {
        return this.height;
    }
}

