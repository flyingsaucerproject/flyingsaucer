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

/**
 * Interface describing a form control.
 * 
 * @author Vianney le Clément
 */
public interface FormControl {
    /**
     * @return the associated element
     */
    Element getElement();

    /**
     * @return the associated form
     */
    XhtmlForm getForm();

    void addFormControlListener(FormControlListener listener);

    void removeFormControlListener(FormControlListener listener);

    /**
     * @return the name of the control
     */
    String getName();

    /**
     * Is this control enabled?
     * 
     * @return <code>true</code> if this control is enabled
     */
    boolean isEnabled();

    /**
     * Enable/disable this control
     * 
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * @return the initial value
     */
    String getInitialValue();

    /**
     * Is this control successful?
     * 
     * @return <code>true</code> if this control is successful and its
     *         name-value pair should be submitted, <code>false</code>
     *         otherwise.
     */
    boolean isSuccessful();

    /**
     * @return <code>true</code> if this control accepts multiple values,
     *         <code>false</code> otherwise
     */
    boolean isMultiple();

    /**
     * Sets this control's successful state.
     * 
     * @param successful
     */
    void setSuccessful(boolean successful);

    /**
     * @return the control's current value or <code>null</code> if isMultiple
     *         returns true
     */
    String getValue();

    /**
     * Sets the control's current value. This has no effect when isMultiple
     * returns true.
     * 
     * @param value
     */
    void setValue(String value);

    /**
     * @return the control's current values or <code>null</code> if isMultiple
     *         returns false
     */
    String[] getMultipleValues();

    /**
     * Sets the control's current values (when isMultiple returns true). This
     * has no effect when isMultiple returns false.
     * 
     * @param values
     */
    void setMultipleValues(String[] values);

    /**
     * Reset the control to it's initial state
     */
    void reset();
}
