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
 * Implementations of this listener interface receive notifications about
 * various document and layout events. Events are called on the Event Dispatch Thread, and will block
 * any EDT activity until the methods return; make sure you do as little as possible in each method, or where necessary,
 * spin the task off to a separate thread.
 */
public interface DocumentListener {
    /**
     * Indicates document has been requested (e.g. a new document is going to be
     * loaded). This will be called before any activity takes place for the
     * document.
     */
    void documentStarted();

    /**
     * Indicates document layout has complete, e.g. document is fully "loaded"
     * for display; this is not a callback for the document source (e.g. XML)
     * being loaded. This method will be called on every layout run (including,
     * for example, after panel resizes).
     */
    void documentLoaded();

    /**
     * Called when document layout failed with an exception. All
     * <code>Throwable</code> objects thrown (except for
     * <code>ThreadDeath</code>) during layout and not otherwise handled will
     * be provided to this method. If a <code>DocumentListener</code> has been
     * defined an XHTML panel, the listener is entirely responsible for
     * handling the exception. No other action will be taken.
     */
    void onLayoutException(Throwable t);

    /**
     * Called when document render failed with an exception. All
     * <code>Throwable</code> objects thrown (except for
     * <code>ThreadDeath</code>) during render and not otherwise handled will
     * be provided to this method. If a <code>DocumentListener</code> has been
     * defined an XHTML panel, the listener is entirely responsible for
     * handling the exception. No other action will be taken.
     */
    void onRenderException(Throwable t);

}

/*
 * $Id$
 * 
 * $Log$
 * Revision 1.8  2008/02/16 12:27:55  pdoubleya
 * On calling DocumentListener methods, make sure we catch exceptions from listener implementations which could otherwise kill our work on the EDT.
 *
 * Revision 1.7  2007/08/23 20:52:31  peterbrant
 * Begin work on AcroForm support
 * Revision 1.6 2007/06/19 21:25:58 pdoubleya
 * Add document start event
 * 
 * Revision 1.5 2007/04/03 13:38:13 peterbrant Javadoc clarification
 * 
 * Revision 1.4 2007/04/03 13:12:07 peterbrant Add notification interface for
 * layout and render exceptions / Minor clean up (remove obsolete body expand
 * hack, remove unused API, method name improvements)
 * 
 * Revision 1.3 2005/01/29 20:22:17 pdoubleya Clean/reformat code. Removed
 * commented blocks, checked copyright.
 * 
 * Revision 1.2 2004/10/23 13:37:29 pdoubleya Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io,
 * java.util, etc). Added CVS log comments at bottom.
 * 
 * 
 */

