/*
 * {{{ header & license
 * Copyright (c) 2007 Vianney le Clément
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
package org.xhtmlrenderer.simple.xhtml.controls;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.simple.xhtml.XhtmlForm;

public class SelectControl extends AbstractControl {

    private int _size;
    private boolean _multiple;
    private List _values;

    private String _initialValue;
    private String[] _initialValues;

    private Map _options;

    public SelectControl(XhtmlForm form, Element e) {
        super(form, e);

        _size = getIntAttribute(e, "size", 1);
        _multiple = e.getAttribute("multiple").length() != 0;
        if (_multiple) {
            _values = new ArrayList();
        }
        super.setValue(null);
        setSuccessful(false);

        _options = new LinkedHashMap();
        traverseOptions(e, "");

        if (_multiple) {
            _initialValues = getMultipleValues();
            if (_initialValues.length > 0) {
                setSuccessful(true);
            }
        } else {
            _initialValue = getValue();
            if (_initialValue != null) {
                setSuccessful(true);
            }
        }
    }

    private void traverseOptions(Element e, String prefix) {
        NodeList children = e.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) children.item(i);
                if (child.getNodeName().equalsIgnoreCase("optgroup")) {
                    traverseOptions(child, prefix + child.getAttribute("label")
                            + " ");
                } else if (child.getNodeName().equalsIgnoreCase("option")) {
                    String value = child.getAttribute("value");
                    String label = child.getAttribute("label");
                    String content = collectText(child);
                    if (value.length() == 0) {
                        value = content;
                    }
                    if (label.length() == 0) {
                        label = content;
                    } else {
                        label = prefix.concat(label);
                    }
                    _options.put(value, label);
                    if (child.getAttribute("selected").length() != 0) {
                        if (isMultiple()) {
                            if (!_values.contains(value)) {
                                _values.add(value);
                            }
                        } else {
                            setValue(value);
                        }
                    }
                }
            }
        }
    }

    public int getSize() {
        return _size;
    }

    public boolean isMultiple() {
        return _multiple;
    }

    public Map getOptions() {
        return new LinkedHashMap(_options);
    }

    public void setValue(String value) {
        if (!isMultiple()) {
            if (_options.containsKey(value)) {
                super.setValue(value);
                setSuccessful(true);
            } else {
                setSuccessful(false);
                super.setValue(null);
            }
        }
    }

    public String[] getMultipleValues() {
        if (isMultiple()) {
            return (String[]) _values.toArray(new String[_values.size()]);
        } else {
            return null;
        }
    }

    public void setMultipleValues(String[] values) {
        if (isMultiple()) {
            _values.clear();
            for (int i = 0; i < values.length; i++) {
                if (_options.get(values[i]) != null
                        && !_values.contains(values[i])) {
                    _values.add(values[i]);
                }
            }
            if (_values.isEmpty()) {
                setSuccessful(false);
            } else {
                setSuccessful(true);
            }
            fireChanged();
        }
    }

    public void reset() {
        if (isMultiple()) {
            setMultipleValues(_initialValues);
        } else {
            setValue(_initialValue);
        }
    }

}
