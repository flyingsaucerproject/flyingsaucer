package org.joshy.html.forms;

import java.util.*;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.joshy.u;
import org.joshy.html.box.*;
import org.joshy.html.*;
import org.w3c.dom.*;

public class InputButton extends FormItemLayout {
    
    public InputButton() {
    }
    
    public JComponent createComponent(Element elem) {
        //u.p("created a button");
        JButton comp = new JButton();
        String type = elem.getAttribute("type");
        if(type == null || type.equals("")) {
            type = "button";
        }
        String label = elem.getAttribute("value");
        if(label == null || label.equals("")) {
            if(type.equals("reset")) {
                label = "Reset";
            }
            if(type.equals("submit")) {
                label = "Submit";
            }
        }
        comp.setText(label);
        commonPrep(comp,elem);
        return comp;
    }
    
}
