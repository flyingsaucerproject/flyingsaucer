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
        u.p("created a button");
        JButton comp = new JButton();
        String label = elem.getAttribute("value");
        comp.setText(label);
        return comp;
    }
    
    /*
    public Box createBox(Context c, Node node) {
        Element elem = (Element)node;
        JButton comp = new JButton();
        String label = elem.getAttribute("value");
        comp.setText(label);
        //c.canvas.remove(comp);
        c.canvas.add(comp);
        //comp.setSize(50,50);
        comp.setLocation(100,100);
        
        //u.p("added a component to the viewport: " + comp);
        InputBox box = new InputBox();
        box.node = node;
        box.component = comp;
        return box;
    }
    */
    
    /*
    public Dimension getIntrinsicDimensions(Context c, Element elem) {
        //comp.setLocation(50,50);
        return new Dimension(50,50);
    }
    */
    
    
    
}
