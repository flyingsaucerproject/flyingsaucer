package org.joshy.html.forms;

import java.util.*;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JComponent;
import org.joshy.u;
import org.joshy.html.box.Box;
import org.joshy.html.*;
import org.w3c.dom.*;

public class FormLayout extends Layout {
    
    public FormLayout() {
    }
    
    public Box createBox(Context c, Node node) {
        FormBox form = new FormBox();
        form.node = node;
        return form;
    }
    
    public Box layout(Context c, Element elem) {
        FormBox form = (FormBox)createBox(c,elem);
        layoutChildren(c, form);
        return form;
    }
    
    public Box layoutChildren(Context c, Box box) {
        u.p("calling layout on the children with the box: " + box);
        return super.layoutChildren(c,box);
    }

    public void paint(Context c, Box box) {
        paintChildren(c,box);
    }
    public void paintChildren(Context c, Box box) {
        u.p("calling paint on the children : " + box);
        super.paintChildren(c, box);
    }
    
}
