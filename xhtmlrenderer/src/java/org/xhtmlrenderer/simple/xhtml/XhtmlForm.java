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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.xhtmlrenderer.simple.extend.URLUTF8Encoder;
import org.xhtmlrenderer.simple.xhtml.controls.ButtonControl;
import org.xhtmlrenderer.simple.xhtml.controls.CheckControl;
import org.xhtmlrenderer.simple.xhtml.controls.HiddenControl;
import org.xhtmlrenderer.simple.xhtml.controls.SelectControl;
import org.xhtmlrenderer.simple.xhtml.controls.TextControl;

public class XhtmlForm {

    protected String _action, _method;

    public XhtmlForm(String action, String method) {
        _action = action;
        _method = method;
    }

    protected List _controls = new LinkedList();

    private List _listeners = new ArrayList();

    public void addFormListener(FormListener listener) {
        _listeners.add(listener);
    }

    public void removeFormListener(FormListener listener) {
        _listeners.remove(listener);
    }

    public FormControl getControl(String name) {
        for (Iterator iter = _controls.iterator(); iter.hasNext();) {
            FormControl control = (FormControl) iter.next();
            if (control.getName().equals(name)) {
                return control;
            }
        }
        return null;
    }

    public List getAllControls(String name) {
        List result = new ArrayList();
        for (Iterator iter = _controls.iterator(); iter.hasNext();) {
            FormControl control = (FormControl) iter.next();
            if (control.getName().equals(name)) {
                result.add(control);
            }
        }
        return result;
    }

    public Iterator getControls() {
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
        if (name.equals("input")) {
            String type = e.getAttribute("type");
            if (type.equals("text") || type.equals("password")) {
                control = new TextControl(form, e);
            } else if (type.equals("hidden")) {
                control = new HiddenControl(form, e);
            } else if (type.equals("button") || type.equals("submit")
                    || type.equals("reset")) {
                control = new ButtonControl(form, e);
            } else if (type.equals("checkbox") || type.equals("radio")) {
                control = new CheckControl(form, e);
            } else {
                return null;
            }
        } else if (name.equals("textarea")) {
            control = new TextControl(form, e);
        } else if (name.equals("button")) {
            control = new ButtonControl(form, e);
        } else if (name.equals("select")) {
            control = new SelectControl(form, e);
        } else {
            return null;
        }

        if (form != null) {
            form._controls.add(control);
        }
        return control;
    }

    public void reset() {
        for (Iterator iter = _listeners.iterator(); iter.hasNext();) {
            ((FormListener) iter.next()).resetted(this);
        }
    }

    public void submit() {
        // TODO other encodings than urlencode?
        StringBuffer data = new StringBuffer();
        for (Iterator iter = getControls(); iter.hasNext();) {
            FormControl control = (FormControl) iter.next();
            if (control.isSuccessful()) {
                if (control.isMultiple()) {
                    String[] values = control.getMultipleValues();
                    for (int i = 0; i < values.length; i++) {
                        if (data.length() > 0) {
                            data.append('&');
                        }
                        data.append(URLUTF8Encoder.encode(control.getName()));
                        data.append('=');
                        data.append(URLUTF8Encoder.encode(values[i]));
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
        System.out.println("Action: ".concat(_action));
        System.out.println("Method: ".concat(_method));
        System.out.println("Data: ".concat(data.toString()));

        for (Iterator iter = _listeners.iterator(); iter.hasNext();) {
            ((FormListener) iter.next()).submitted(this);
        }
    }

}
