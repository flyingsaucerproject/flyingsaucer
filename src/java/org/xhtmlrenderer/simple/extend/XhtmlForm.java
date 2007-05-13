/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
 * Copyright (c) 2007 Sean Bright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.simple.extend;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.simple.extend.form.FormField;
import org.xhtmlrenderer.simple.extend.form.FormFieldFactory;
import org.xhtmlrenderer.util.XRLog;

/**
 * Represents a form object
 *
 * @author Torbjörn Gannholm
 * @author Sean Bright
 */
public class XhtmlForm {
    private UserAgentCallback _userAgentCallback;
    private Map _componentCache;
    private Map _buttonGroups;
    // TODO: Bring this back for the method/action
    // private Element _parentFormElement;

    public XhtmlForm(UserAgentCallback uac, Element e) {
        _userAgentCallback = uac;
        _buttonGroups = new HashMap();
        _componentCache = new LinkedHashMap();
        // TODO: Bring this back for the method/action
        // _parentFormElement = e;
    }

    public UserAgentCallback getUserAgentCallback() {
        return _userAgentCallback;
    }
    
    public void addButtonToGroup(String groupName, AbstractButton button) {
        ButtonGroup group = (ButtonGroup) _buttonGroups.get(groupName);
        
        if (group == null) {
            group = new ButtonGroup();
            
            _buttonGroups.put(groupName, group);
        }

        group.add(button);
    }

    private boolean isFormField(Element e) {
        String nodeName = e.getNodeName();
        
        if (nodeName.equals("input") || nodeName.equals("select") || nodeName.equals("textarea")) {
            return true;
        }
        
        return false;
    }

    public JComponent addComponent(Element e) {
        FormField field = null;

        if (_componentCache.containsKey(e)) {
            field = (FormField) _componentCache.get(e);
        } else {
            if (!isFormField(e)) {
                return null;
            }
            
            field = FormFieldFactory.create(e, this);
    
            if (field == null) {
                XRLog.layout("Unknown field type: " + e.getNodeName());

                return null;
            }
            
            _componentCache.put(e, field);
        }

        return field.getComponent();
    }
    
    public void reset() {
        Iterator fields = _componentCache.entrySet().iterator();

        while (fields.hasNext()) {
            Map.Entry entry = (Map.Entry) fields.next();

            FormField field = (FormField) entry.getValue();
            
            field.applyOriginalState();
        }
    }

    public void submit(JComponent source) {
        StringBuffer data = new StringBuffer();
        Iterator fields = _componentCache.entrySet().iterator();

        while (fields.hasNext()) {
            Map.Entry entry = (Map.Entry) fields.next();

            FormField field = (FormField) entry.getValue();

            String [] dataStrings = field.getFormDataStrings();
            
            for (int i = 0; i < dataStrings.length; i++) {
                if (data.length() > 0) {
                    data.append('&');
                }

                data.append(dataStrings[i]);
            }
        }
        
        System.out.println("Form Submitted!");
        System.out.println("Data: " + data.toString());

        JOptionPane.showMessageDialog(null, "Check System.out for the submitted content.", "Form Submission", JOptionPane.INFORMATION_MESSAGE);
    }

    public static String collectText(Element e) {
        StringBuffer result = new StringBuffer();
        Node node = e.getFirstChild();
        if (node != null) {
            do {
                if (node.getNodeType() == Node.TEXT_NODE) {
                    Text text = (Text) node;
                    result.append(text.getData());
                }
            } while ((node = node.getNextSibling()) != null);
        }
        return result.toString().trim();
    }
}
