/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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
package org.xhtmlrenderer.event;

/**
 * Implementations of this listener interface receive notifications about various
 * document and layout events.
 */
public interface DocumentListener {
    /**
     * Indicates document layout has completed.  This will be called on every layout
     * run (including, for example, after panel resizes).
     */
    public void documentLoaded();
    
    /**
     * Called when document layout failed with an exception.  All <code>Throwable</code>
     * objects thrown (except for <code>ThreadDeath</code>) during layout and not 
     * otherwise handled will be provided to this method.  If a <code>DocumentListener</code>
     * has been defined an XHTML panel, the listener is entirely responsibile for
     * handling the exception.  No other action will be taken.
     */
    public void onLayoutException(Throwable t);
    
    /**
     * Called when document render failed with an exception.  All <code>Throwable</code>
     * objects thrown (except for <code>ThreadDeath</code>) during render and not 
     * otherwise handled will be provided to this method.  If a <code>DocumentListener</code>
     * has been defined an XHTML panel, the listener is entirely responsibile for
     * handling the exception.  No other action will be taken.
     */
    public void onRenderException(Throwable t);
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2007/04/03 13:38:13  peterbrant
 * Javadoc clarification
 *
 * Revision 1.4  2007/04/03 13:12:07  peterbrant
 * Add notification interface for layout and render exceptions / Minor clean up (remove obsolete body expand hack, remove unused API, method name improvements)
 *
 * Revision 1.3  2005/01/29 20:22:17  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.2  2004/10/23 13:37:29  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

