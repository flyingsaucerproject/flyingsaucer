/*
 * {{{ header & license
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
package org.xhtmlrenderer.simple.extend.form;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.simple.extend.XhtmlForm;
import org.xhtmlrenderer.util.GeneralUtil;

class SelectField extends FormField {
    public SelectField(Element e, XhtmlForm form) {
        super(e, form);
    }

    public JComponent create() {
        List optionList = createList();

        // Either a select list or a drop down/combobox
        if (shouldRenderAsList()) {
            JList select = new JList(optionList.toArray());

            if (hasAttribute("multiple") && getAttribute("multiple").equals("true")) {
                select.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            } else {
                select.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            }
            
            int size = 0;

            if (hasAttribute("size")) {
                size = GeneralUtil.parseIntRelaxed(getAttribute("size"));
            }
            
            if (size == 0) {
                // Default to the number of items in the list or 20 - whichever is less
                select.setVisibleRowCount(Math.min(select.getModel().getSize(), 20));
            } else {
                select.setVisibleRowCount(size);
            }

            return new JScrollPane(select);
        } else {
            JComboBox select = new JComboBox(optionList.toArray());

            select.setEditable(false);

            return select;
        }
    }
    
    protected FormFieldState loadOriginalState() {
        ArrayList list = new ArrayList();
        
        NodeList options = getElement().getElementsByTagName("option");

        for (int i = 0; i < options.getLength(); i++) {
            Element option = (Element) options.item(i);

            if (option.hasAttribute("selected") && option.getAttribute("selected").equals("selected")) {
                list.add(new Integer(i));
            }
        }

        return FormFieldState.fromList(list);
    }
    
    protected void applyOriginalState() {
        if (shouldRenderAsList()) {
            JList select = (JList) ((JScrollPane) getComponent()).getViewport().getView();

            select.setSelectedIndices(getOriginalState().getSelectedIndices());
        } else {
            JComboBox select = (JComboBox) getComponent();
            
            // This looks strange, but basically since this is a single select, and
            // someone might have put selected="selected" on more than a single option
            // I believe that the correct play here is to select the _last_ option with
            // that attribute.
            int [] indices = getOriginalState().getSelectedIndices();
            
            if (indices.length == 0) {
                select.setSelectedIndex(0);
            } else {
                select.setSelectedIndex(indices[indices.length - 1]);
            }
        }
    }

    protected String[] getFieldValues() {
        if (shouldRenderAsList()) {
            JList select = (JList) ((JScrollPane) getComponent()).getViewport().getView();
            
            Object [] selectedValues = select.getSelectedValues();
            String [] submitValues = new String [selectedValues.length];
            
            for (int i = 0; i < selectedValues.length; i++) {
                submitValues[i] = ((NameValuePair) selectedValues[i]).getValue();
            }
            
            return submitValues;
        } else {
            JComboBox select = (JComboBox) getComponent();
            
            NameValuePair selectedValue = (NameValuePair) select.getSelectedItem();
            
            if (selectedValue == null) {
                return new String [] {};
            } else {
                return new String [] { selectedValue.getValue() };
            }
        }
    }

    private List createList() {
        List list = new ArrayList();

        NodeList options = getElement().getElementsByTagName("option");

        for (int i = 0; i < options.getLength(); i++) {
            Element option = (Element) options.item(i);
            
            String optionText = XhtmlForm.collectText(option);
            String optionValue = optionText;
            
            if (option.hasAttribute("value")) {
                optionValue = option.getAttribute("value");
            }

            list.add(new NameValuePair(optionText, optionValue));
        }
        
        return list;
    }
    
    private boolean shouldRenderAsList() {
        boolean result = false;
        
        if (hasAttribute("multiple") && getAttribute("multiple").equals("true")) {
            result = true;
        } else if (hasAttribute("size")) {
            int size = GeneralUtil.parseIntRelaxed(getAttribute("size"));
            
            if (size > 0) {
                result = true;
            }
        }
        
        return result;
    }
    
    /**
     * Provides a simple container for name/value data, such as that used
     * by the &lt;option&gt; elements in a &lt;select&gt; list.
     */
    private static class NameValuePair {
        private String _name;
        private String _value;

        public NameValuePair(String name, String value) {
            _name = name;
            _value = value;
        }
        
        public String getName() {
            return _name;
        }
        
        public String getValue() {
            return _value;
        }

        public String toString() {
            return getName();
        }
    }

}
