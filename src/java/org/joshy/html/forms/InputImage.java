package org.joshy.html.forms;

import java.util.*;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.ImageIcon;
import java.awt.Insets;
import org.joshy.u;
import org.joshy.html.box.*;
import org.joshy.html.*;
import org.w3c.dom.*;

public class InputImage extends InputButton {
    
    public InputImage() {
    }
    
    public JComponent createComponent(Context c, Element elem) {
        JButton comp = (JButton)super.createComponent(c,elem);
        
        if(elem.hasAttribute("src")) {
            String src = elem.getAttribute("src");
            comp.setIcon(new ImageIcon(src));
            comp.setText(null);
            comp.setBorderPainted(false);
            comp.setMargin(new Insets(0,0,0,0));
            comp.setPreferredSize(new Dimension(comp.getIcon().getIconHeight(),
                comp.getIcon().getIconHeight()));

        }
        return comp;
    }
    
}
