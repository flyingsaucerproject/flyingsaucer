/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjšrn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.render;

import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.util.Uu;


/**
 * Created by IntelliJ IDEA. User: tobe Date: 2005-jan-06 Time: 08:46:37 To
 * change this template use File | Settings | File Templates.
 *
 * @author Joshua Marinacci
 * @author Torbjörn Gannholm
 */
public class InlineTextBox extends InlineBox {

    /**
     * Description of the Field
     */
    public IdentValue whitespace = IdentValue.NORMAL;

    /**
     * Description of the Field
     */
    public String pseudoElement;

    /**
     * Description of the Field
     */
    private String master;

    public InlineTextBox() {

    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    public InlineBox copy() {
        InlineTextBox newBox = new InlineTextBox();
        InlineTextBox box = this;
        newBox.setStyle(box.getStyle());
        newBox.x = box.x;
        newBox.y = box.y;
        newBox.contentWidth = box.contentWidth;
        newBox.leftPadding = box.leftPadding;
        newBox.rightPadding = box.rightPadding;
        newBox.height = box.height;
        newBox.element = box.element;
        newBox.master = box.master;
        newBox.pseudoElement = box.pseudoElement;
        return newBox;
    }

    /**
     * Sets the substring attribute of the InlineTextBox object
     *
     * @param start The new substring value
     * @param end   The new substring value
     */
    public void setSubstring(int start, int end) {
        if (end < start) {
            Uu.p("setting substring to: " + start + " " + end);
            throw new RuntimeException("set substring length too long: " + this);
        } else if (end < 0 || start < 0) {
            throw new RuntimeException("Trying to set negative index to inline box");
        }
        start_index = start;
        end_index = end;
    }

    /**
     * Sets the masterText attribute of the InlineTextBox object
     *
     * @param master The new masterText value
     */
    public void setMasterText(String master) {
        //Uu.p("set master text to: \"" + master + "\"");
        this.master = master;
    }

    /**
     * Gets the endOfParentContent attribute of the InlineTextBox object
     *
     * @return The endOfParentContent value
     */
    public boolean isEndOfParentContent() {
        return end_index == master.length();
    }

    /**
     * Gets the substring attribute of the InlineBox object
     *
     * @return The substring value
     */
    public String getSubstring() {
        // new code for whitepsace handling
        if (getMasterText() != null) {
            if (start_index == -1 || end_index == -1) {
                throw new RuntimeException("negative index in InlineBox");
            }
            if (end_index < start_index) {
                throw new RuntimeException("end is less than setStartStyle");
            }
            return getMasterText().substring(start_index, end_index);
        } else {
            throw new RuntimeException("No master text set!");
        }

    }

    /**
     * Gets the masterText attribute of the InlineTextBox object
     *
     * @return The masterText value
     */
    public String getMasterText() {
        return master;
    }
}

