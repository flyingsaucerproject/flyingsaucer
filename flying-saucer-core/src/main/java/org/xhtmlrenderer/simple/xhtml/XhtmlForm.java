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
package org.xhtmlrenderer.simple.xhtml;

import org.w3c.dom.Element;
import org.xhtmlrenderer.simple.extend.URLUTF8Encoder;
import org.xhtmlrenderer.simple.xhtml.controls.ButtonControl;
import org.xhtmlrenderer.simple.xhtml.controls.CheckControl;
import org.xhtmlrenderer.simple.xhtml.controls.HiddenControl;
import org.xhtmlrenderer.simple.xhtml.controls.SelectControl;
import org.xhtmlrenderer.simple.xhtml.controls.TextControl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XhtmlForm {

    private final String _action;
    private final String _method;

    public XhtmlForm(String action, String method) {
        _action = action;
        _method = method;
    }

    private final List<FormControl> _controls = new ArrayList<>();
    private final List<FormListener> _listeners = new ArrayList<>();

    public void addFormListener(FormListener listener) {
        _listeners.add(listener);
    }

    public void removeFormListener(FormListener listener) {
        _listeners.remove(listener);
    }

    public FormControl getControl(String name) {
        for (FormControl control : _controls) {
            if (control.getName().equals(name)) {
                return control;
            }
        }
        return null;
    }

    public List<FormControl> getAllControls(String name) {
        List<FormControl> result = new ArrayList<>();
        for (FormControl control : _controls) {
            if (control.getName().equals(name)) {
                result.add(control);
            }
        }
        return result;
    }

    public Iterable<FormControl> controls() {
        return _controls;
    }

    /**
     * @deprecated Use method {@link #controls()} instead
     */
    @Deprecated
    public Iterator<FormControl> getControls() {
        return _controls.iterator();
    }

    public FormControl createControl(Element e) {
        return createControl(this, e);
    }

    public static FormControl createControl(XhtmlForm form, Element e) {
        if (e == null)
            return null;

        FormControl control;
        String name = e.getNodeName();
        switch (name) {
            case "input":
                String type = e.getAttribute("type");
                switch (type) {
                    case "text":
                    case "password":
                        control = new TextControl(form, e);
                        break;
                    case "hidden":
                        control = new HiddenControl(form, e);
                        break;
                    case "button":
                    case "submit":
                    case "reset":
                        control = new ButtonControl(form, e);
                        break;
                    case "checkbox":
                    case "radio":
                        control = new CheckControl(form, e);
                        break;
                    default:
                        return null;
                }
                break;
            case "textarea":
                control = new TextControl(form, e);
                break;
            case "button":
                control = new ButtonControl(form, e);
                break;
            case "select":
                control = new SelectControl(form, e);
                break;
            default:
                return null;
        }

        if (form != null) {
            form._controls.add(control);
        }
        return control;
    }

    public void reset() {
        for (FormListener listener : _listeners) {
            listener.resetted(this);
        }
    }

    public void submit() {
        // TODO other encodings than urlencode?
        StringBuilder data = new StringBuilder();
        for (FormControl control : _controls) {
            if (control.isSuccessful()) {
                if (control.isMultiple()) {
                    String[] values = control.getMultipleValues();
                    for (String value : values) {
                        if (data.length() > 0) {
                            data.append('&');
                        }
                        data.append(URLUTF8Encoder.encode(control.getName()));
                        data.append('=');
                        data.append(URLUTF8Encoder.encode(value));
                    }
                } else {
                    if (data.length() > 0) {
                        data.append('&');
                    }
                    data.append(URLUTF8Encoder.encode(control.getName()));
                    data.append('=');
                    data.append(URLUTF8Encoder.encode(control.getValue()));
                }
            }
        }

        // TODO effectively submit form
        System.out.println("Form submitted!");
        System.out.println("Action: " + _action);
        System.out.println("Method: " + _method);
        System.out.println("Data: " + data);

        for (FormListener listener : _listeners) {
            listener.submitted(this);
        }
    }

}
