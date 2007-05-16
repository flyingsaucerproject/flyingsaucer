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

import javax.swing.JComponent;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.simple.extend.URLUTF8Encoder;
import org.xhtmlrenderer.simple.extend.XhtmlForm;

public abstract class FormField {
    private XhtmlForm _parentForm;
    private Element _element;
    private FormFieldState _originalState;
    private JComponent _component;

    public FormField(Element e, XhtmlForm form) {
        _element = e;
        _parentForm = form;
        
        initialize();
    }

    protected Element getElement() {
        return _element;
    }
    
    public JComponent getComponent() {
        return _component;
    }
    
    public XhtmlForm getParentForm() {
        return _parentForm;
    }
    
    public void reset() {
        applyOriginalState();
    }

    protected UserAgentCallback getUserAgentCallback() {
        return _parentForm.getUserAgentCallback();
    }

    protected FormFieldState getOriginalState() {
        if (_originalState == null) {
            _originalState = loadOriginalState();
        }

        return _originalState;
    }
    
    protected boolean hasAttribute(String attributeName) {
        return getElement().hasAttribute(attributeName);
    }

    protected String getAttribute(String attributeName) {
        return getElement().getAttribute(attributeName);
    }
    
    private void initialize() {
        _component = create();
        
        if (_component != null) {
            _component.setSize(_component.getPreferredSize());

            if (_element.hasAttribute("disabled") &&
                _element.getAttribute("disabled").equals("disabled")) {
                _component.setEnabled(false);
            }
        }

        applyOriginalState();
    }

    public abstract JComponent create();

    protected FormFieldState loadOriginalState() {
        return FormFieldState.fromString("");
    }
    
    protected void applyOriginalState() {
        // Do nothing
    }
    
    /**
     * Returns true if the value of the current FormField should be
     * sent along with the current submission.  This is used so that
     * only the value of the submit button that is used to trigger the
     * form's submission is sent.
     * 
     * @param source The JComponent that caused the submission
     * @return true if it should
     */
    public boolean includeInSubmission(JComponent source) {
        return true;
    }

    // These two methods are temporary but I am using them to clean up
    // the code in XhtmlForm
    public String[] getFormDataStrings() {
        // Fields MUST have at least a name attribute to get sent.  The attr
        // can be empty, or just white space, but it must be present
        if (!hasAttribute("name")) {
            return new String[] {};
        }

        String name = getAttribute("name");
        String[] values = getFieldValues();

        for (int i = 0; i < values.length; i++) {
            values[i] = URLUTF8Encoder.encode(name) + "=" + URLUTF8Encoder.encode(values[i]);
        }

        return values;
    }

    protected abstract String[] getFieldValues();
}
