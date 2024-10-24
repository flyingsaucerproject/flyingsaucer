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

import org.xhtmlrenderer.simple.extend.URLUTF8Encoder;

import java.util.ArrayList;
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
                        if (!data.isEmpty()) {
                            data.append('&');
                        }
                        data.append(URLUTF8Encoder.encode(control.getName()));
                        data.append('=');
                        data.append(URLUTF8Encoder.encode(value));
                    }
                } else {
                    if (!data.isEmpty()) {
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
