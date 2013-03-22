/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.extend;

import org.w3c.dom.Element;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

public interface ReplacedElementFactory {
    
    /**
     * <b>NOTE:</b> Only block equivalent elements can be replaced.
     * 
     * @param cssWidth The CSS width of the element in dots (or <code>-1</code> if
     * width is <code>auto</code>)
     * @param cssHeight The CSS height of the element in dots (or <code>-1</code>
     * if the height should be treated as <code>auto</code>)
     * @return The <code>ReplacedElement</code> or <code>null</code> if no
     * <code>ReplacedElement</code> applies 
     */
    public ReplacedElement createReplacedElement(
            LayoutContext c, BlockBox box,
            UserAgentCallback uac, int cssWidth, int cssHeight);
    
    /**
     * Instructs the <code>ReplacedElementFactory</code> to discard any cached
     * data (typically because a new page is about to be loaded).
     */
    public void reset();
    
    /**
     * Removes any reference to <code>Element</code> <code>e</code>.
     * @param e
     */
    public void remove(Element e);

    /**
     * Identifies the FSL which will be used for callbacks when a form submit action is executed; you can use a
     * {@link org.xhtmlrenderer.simple.extend.DefaultFormSubmissionListener} if you don't want any action to be taken.
     *
     * @param listener the listener instance to receive callbacks on form submission.
     */
    public void setFormSubmissionListener(FormSubmissionListener listener);
}
