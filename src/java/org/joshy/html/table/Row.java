package org.joshy.html.table;

import java.util.*;
import org.w3c.dom.*;

public class Row {
    List cells = new ArrayList();
    public Node node;
    public void addCell(Cell cell) {
        cells.add(cell);
    }
    public Iterator getCellIterator() {
        return cells.iterator();
    }
}

