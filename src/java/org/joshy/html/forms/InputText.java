package org.joshy.html.forms;

import java.util.*;
import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JTextField;
import javax.swing.JComponent;
import org.joshy.u;
import org.joshy.html.box.*;
import org.joshy.html.*;
import org.w3c.dom.*;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;

public class InputText extends FormItemLayout {
    
    public InputText() {
    }
    
    protected JTextField _createComponent() {
        return new JTextField();
    }
    public JComponent createComponent(Element elem) {
        JTextField comp = _createComponent();
        commonPrep(comp,elem);
        if(elem.hasAttribute("value")) {
            comp.setText(elem.getAttribute("value"));
        }
        if(elem.hasAttribute("size")) {
            comp.setColumns(Integer.parseInt(elem.getAttribute("size")));
        } else {
            comp.setColumns(15);
        }
        if(elem.hasAttribute("maxlength")) {
            final int maxlength = Integer.parseInt(elem.getAttribute("maxlength"));
            comp.setDocument(new PlainDocument() {
                public void insertString(int offset, String str, AttributeSet attr)
                throws BadLocationException {
                    if(str == null) { return; }
                    if((getLength() + str.length()) <= maxlength) {
                        super.insertString(offset,str,attr);
                    }
                }
            });
        }
        if(elem.hasAttribute("readonly") && 
           elem.getAttribute("readonly").equals("readonly")) {
            comp.setEditable(false);
        }
        return comp;
    }
    
}
