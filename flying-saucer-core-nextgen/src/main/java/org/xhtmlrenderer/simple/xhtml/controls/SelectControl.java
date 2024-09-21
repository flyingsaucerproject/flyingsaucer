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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.simple.xhtml.XhtmlForm;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SelectControl extends AbstractControl {

    private final int _size;
    private final boolean _multiple;
    private final List<String> _values = new ArrayList<>(1);

    private String _initialValue;
    private String[] _initialValues;

    private final Map<String, String> _options = new LinkedHashMap<>();

    public SelectControl(XhtmlForm form, Element e) {
        super(form, e);

        _size = getIntAttribute(e, "size", 1);
        _multiple = !e.getAttribute("multiple").isEmpty();
        super.setValue(null);
        setSuccessful(false);
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
                    String valueAttribute = child.getAttribute("value");
                    String labelAttribute = child.getAttribute("label");
                    String content = collectText(child);
                    String value = valueAttribute.isEmpty() ? content : valueAttribute;
                    String label = labelAttribute.isEmpty() ? content : prefix + labelAttribute;
                    _options.put(value, label);
                    if (!child.getAttribute("selected").isEmpty()) {
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

    @Override
    public boolean isMultiple() {
        return _multiple;
    }

    public Map<String, String> getOptions() {
        return new LinkedHashMap<>(_options);
    }

    @Override
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

    @Override
    @Nullable
    public final String[] getMultipleValues() {
        if (isMultiple()) {
            return _values.toArray(new String[0]);
        } else {
            return null;
        }
    }

    @Override
    public void setMultipleValues(String[] values) {
        if (isMultiple()) {
            _values.clear();
            for (String value : values) {
                if (_options.get(value) != null && !_values.contains(value)) {
                    _values.add(value);
                }
            }
            setSuccessful(!_values.isEmpty());
            fireChanged();
        }
    }

    @Override
    public void reset() {
        if (isMultiple()) {
            setMultipleValues(_initialValues);
        } else {
            setValue(_initialValue);
        }
    }

}
