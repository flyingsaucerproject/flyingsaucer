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

public class InputRadio extends FormItemLayout {
    
    public InputRadio() {
    }
    
    public JComponent createComponent(Element elem) {
        JRadioButton comp = new JRadioButton();
        comp.setText("");
        comp.setOpaque(false);
        if(elem.hasAttribute("checked") &&
            elem.getAttribute("checked").equals("checked")) {
            comp.setSelected(true);
        }
        commonPrep(comp,elem);
        return comp;
    }
    
}
