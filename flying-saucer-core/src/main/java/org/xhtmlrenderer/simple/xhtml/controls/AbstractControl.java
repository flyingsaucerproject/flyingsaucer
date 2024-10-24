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
import org.w3c.dom.Text;
import org.xhtmlrenderer.simple.xhtml.FormControl;
import org.xhtmlrenderer.simple.xhtml.FormControlListener;
import org.xhtmlrenderer.simple.xhtml.FormListener;
import org.xhtmlrenderer.simple.xhtml.XhtmlForm;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractControl implements FormControl {

    private final XhtmlForm _form;
    private final Element _element;
    private final String _name;

    private String _initialValue;
    private String _value;
    private boolean _successful;
    private boolean _enabled;

    private final List<FormControlListener> _listeners = new ArrayList<>();

    protected AbstractControl(XhtmlForm form, Element e) {
        _form = form;
        _element = e;
        _name = getNameOrId(e);
        _initialValue = e.getAttribute("value");
        _value = _initialValue;
        _enabled = e.getAttribute("disabled").isEmpty();
        _successful = _enabled;

        if (form != null) {
            form.addFormListener(new FormListener() {
                @Override
                public void submitted(XhtmlForm form) {
                }

                @Override
                public void resetted(XhtmlForm form) {
                    reset();
                }
            });
        }
    }

    private static String getNameOrId(Element e) {
        String name = e.getAttribute("name");
        return name.isEmpty() ? e.getAttribute("id") : name;
    }

    protected void fireChanged() {
        for (FormControlListener listener : _listeners) {
            listener.changed(this);
        }
    }

    protected void fireSuccessful() {
        for (FormControlListener listener : _listeners) {
            listener.successful(this);
        }
    }

    protected void fireEnabled() {
        for (FormControlListener listener : _listeners) {
            listener.enabled(this);
        }
    }

    @Override
    public void addFormControlListener(FormControlListener listener) {
        _listeners.add(listener);
    }

    @Override
    public void removeFormControlListener(FormControlListener listener) {
        _listeners.remove(listener);
    }

    @Override
    public Element getElement() {
        return _element;
    }

    @Override
    public XhtmlForm getForm() {
        return _form;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getInitialValue() {
        return _initialValue;
    }

    protected final void setInitialValue(String value) {
        _initialValue = value;
        _value = value;
    }

    @Override
    public final String getValue() {
        if (isMultiple()) {
            return null;
        } else {
            return _value;
        }
    }

    @Override
    public void setValue(String value) {
        if (!isMultiple()) {
            _value = value;
            fireChanged();
        }
    }

    @Override
    public String[] getMultipleValues() {
        return null;
    }

    @Override
    public void setMultipleValues(String[] values) {
        // do nothing
    }

    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return _enabled;
    }

    @Override
    public boolean isSuccessful() {
        return _successful && _enabled;
    }

    @Override
    public boolean isMultiple() {
        return false;
    }

    @Override
    public void setSuccessful(boolean successful) {
        _successful = successful;
        fireSuccessful();
    }

    @Override
    public void setEnabled(boolean enabled) {
        _enabled = enabled;
        fireEnabled();
    }

    @Override
    public void reset() {
        setValue(_initialValue);
    }

    public static String collectText(Element e) {
        StringBuilder result = new StringBuilder();
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

    @SuppressWarnings("EmptyCatch")
    public static int getIntAttribute(Element e, String attribute, int def) {
        int result = def;
        String str = e.getAttribute(attribute);
        if (!str.isEmpty()) {
            try {
                result = Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

}
