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
    
    public JComponent createComponent(Context c, Element elem) {
        JRadioButton comp = new JRadioButton();
        comp.setText("");
        comp.setOpaque(false);
        if(elem.hasAttribute("checked") &&
            elem.getAttribute("checked").equals("checked")) {
            comp.setSelected(true);
        }
        commonPrep(comp,elem);
        
        if(elem.hasAttribute("name")) {
            String name = elem.getAttribute("name");
            List other_comps = c.getInputFieldComponents(c.getForm(),name);
            if(other_comps.size() > 0) {
                for(int i=0; i<other_comps.size(); i++) {
                    Context.FormComponent other_comp = (Context.FormComponent)other_comps.get(i);
                    if(other_comp.component instanceof JRadioButton) {
                        JRadioButton other_radio = (JRadioButton)other_comp.component;
                        //u.p("found a matching component: " + other_radio);
                    }
                }
            }
        }
        return comp;
    }
    
}
