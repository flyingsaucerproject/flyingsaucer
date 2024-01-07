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

import org.w3c.dom.Element;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.XhtmlForm;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public class FormFieldFactory {
    private FormFieldFactory() {
    }

    public static FormField create(XhtmlForm form, LayoutContext context, BlockBox box) {
        Element e = requireNonNull(box.getElement());

        String typeKey = getTypeKey(e);
        if (typeKey == null) return null;

        switch (typeKey) {
            case "submit":
                return new SubmitField(e, form, context, box);
            case "reset":
                return new ResetField(e, form, context, box);
            case "button":
                return new ButtonField(e, form, context, box);
            case "image":
                return new ImageField(e, form, context, box);
            case "hidden":
                return new HiddenField(e, form, context, box);
            case "password":
                return new PasswordField(e, form, context, box);
            case "checkbox":
                return new CheckboxField(e, form, context, box);
            case "radio":
                return new RadioButtonField(e, form, context, box);
            case "file":
                return new FileField(e, form, context, box);
            case "textarea":
                return new TextAreaField(e, form, context, box);
            case "select":
                return new SelectField(e, form, context, box);
            default:
                return new TextField(e, form, context, box);
        }
    }

    @Nullable
    private static String getTypeKey(Element e) {
        switch (e.getNodeName()) {
            case "input":
                return e.getAttribute("type");
            case "textarea":
                return "textarea";
            case "select":
                return "select";
            default:
                return null;
        }
    }
}
