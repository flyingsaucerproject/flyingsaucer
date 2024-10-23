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
package org.xhtmlrenderer.swt.simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.simple.xhtml.FormControl;
import org.xhtmlrenderer.simple.xhtml.FormControlAdapter;
import org.xhtmlrenderer.simple.xhtml.controls.SelectControl;
import org.xhtmlrenderer.swt.BasicRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class SWTSelectControl extends SWTXhtmlControl {

    private boolean _combo;
    private java.util.List<String> _values;

    public SWTSelectControl(FormControl control, BasicRenderer parent,
            LayoutContext c, CalculatedStyle style, UserAgentCallback uac) {
        super(control, parent, c, style, uac);
    }

    @Override
    protected Control createSWTControl(FormControl control,
                                       BasicRenderer parent, LayoutContext c, CalculatedStyle style,
                                       UserAgentCallback uac) {
        final SelectControl sc = (SelectControl) control;
        Map<String, String> options = sc.getOptions();
        _values = new ArrayList<>(options.keySet());
        java.util.List<String> _labels = new ArrayList<>(options.values());

        if (sc.getSize() > 1 || sc.isMultiple()) {
            _combo = false;

            final List list = new List(parent, SWT.BORDER | SWT.V_SCROLL
                    | (sc.isMultiple() ? SWT.MULTI : SWT.SINGLE));
            list.setItems(_labels.toArray(new String[0]));

            list.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (sc.isMultiple()) {
                        int[] indices = list.getSelectionIndices();
                        String[] values = new String[indices.length];
                        for (int i = 0; i < indices.length; i++) {
                            values[i] = _values.get(indices[i]);
                        }
                        sc.setMultipleValues(values);
                    } else {
                        sc.setValue(_values.get(list.getSelectionIndex()));
                    }
                }
            });

            sc.addFormControlListener(new FormControlAdapter() {
                @Override
                public void changed(FormControl control) {
                    if (sc.isSuccessful()) {
                        if (sc.isMultiple()) {
                            String[] values = requireNonNull(sc.getMultipleValues());
                            int[] indices = new int[values.length];
                            for (int i = 0; i < values.length; i++) {
                                indices[i] = _values.indexOf(values[i]);
                            }
                            list.setSelection(indices);
                        } else {
                            list.setSelection(_values.indexOf(sc.getValue()));
                        }
                    } else {
                        list.deselectAll();
                    }
                }

                @Override
                public void successful(FormControl control) {
                    changed(control);
                }
            });

            if (sc.isSuccessful()) {
                if (sc.isMultiple()) {
                    String[] values = requireNonNull(sc.getMultipleValues());
                    int[] indices = new int[values.length];
                    for (int i = 0; i < values.length; i++) {
                        indices[i] = _values.indexOf(values[i]);
                    }
                    list.setSelection(indices);
                } else {
                    list.setSelection(_values.indexOf(sc.getValue()));
                }
            }

            return list;
        } else {
            _combo = true;

            final Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
            combo.setItems(_labels.toArray(new String[0]));

            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    int selection = combo.getSelectionIndex();
                    if (selection < 0) {
                        sc.setSuccessful(false);
                    } else {
                        sc.setValue(_values.get(selection));
                    }
                }
            });

            sc.addFormControlListener(new FormControlAdapter() {
                @Override
                public void changed(FormControl control) {
                    if (sc.isSuccessful()) {
                        combo.select(_values.indexOf(sc.getValue()));
                    } else {
                        combo.deselectAll();
                    }
                }

                @Override
                public void successful(FormControl control) {
                    changed(control);
                }
            });

            if (sc.isSuccessful()) {
                combo.select(_values.indexOf(sc.getValue()));
            }

            return combo;
        }
    }

    @Override
    public int getIdealHeight() {
        if (_combo) {
            getSWTControl().pack();
            return getSWTControl().getSize().y;
        } else {
            SelectControl sc = (SelectControl) getFormControl();
            List list = (List) getSWTControl();
            String[] oldSelection = list.getSelection();
            String[] oldItems = list.getItems();
            String[] items = new String[sc.getSize()];
            Arrays.fill(items, "Gg");
            list.setItems(items);
            list.pack();
            int height = list.getSize().y;
            list.setItems(oldItems);
            list.setSelection(oldSelection);
            return height;
        }
    }

    @Override
    public int getIdealWidth() {
        getSWTControl().pack();
        return getSWTControl().getSize().x;
    }

}
