package org.joshy.html.forms;

import java.util.*;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.*;
import javax.swing.JComponent;
import org.joshy.u;
import org.joshy.html.box.*;
import org.joshy.html.*;
import org.w3c.dom.*;

public class InputTextArea extends FormItemLayout {
    
    public InputTextArea() {
    }
    
    public JComponent createComponent(Context c, Element elem) {
        //u.p("created a TextArea");
        int rows = 4;
        int cols = 10;
        if(elem.hasAttribute("rows")) {
            rows = Integer.parseInt(elem.getAttribute("rows"));
        }
        if(elem.hasAttribute("cols")) {
            cols = Integer.parseInt(elem.getAttribute("cols"));
        }
        
        JTextArea comp = new JTextArea(rows,cols);
        commonPrep(comp,elem);
        JScrollPane sp = new JScrollPane(comp);
        sp.setVerticalScrollBarPolicy(sp.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setHorizontalScrollBarPolicy(sp.HORIZONTAL_SCROLLBAR_ALWAYS);
        if(elem.getFirstChild() != null) {
            //u.p("setting text to: " + elem.getFirstChild().getNodeValue());
            comp.setText(elem.getFirstChild().getNodeValue());
        }
        
        return sp;
    }
    
}
