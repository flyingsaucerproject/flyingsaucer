/*
 * StylesheetFactory.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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
 *
 */
package org.xhtmlrenderer.css.extend;

import java.io.Reader;

import org.xhtmlrenderer.css.sheet.Ruleset;
import org.xhtmlrenderer.css.sheet.Stylesheet;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;


/**
 * A Factory class for Cascading Style Sheets. Sheets are parsed using a single
 * parser instance for all sheets. Sheets are cached by URI using a LRU test,
 * but timestamp of file is not checked.
 *
 * @author Torbjoern Gannholm
 */
public interface StylesheetFactory {
    Stylesheet parse(Reader reader, StylesheetInfo info);
    Ruleset parseStyleDeclaration(int author, String style);

    Stylesheet getStylesheet(StylesheetInfo si);
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2007/05/16 22:27:14  peterbrant
 * Only load default stylesheet once
 *
 * Revision 1.1  2005/06/23 17:03:42  tobega
 * css now independent of DOM
 *
 * Revision 1.21  2005/06/22 23:48:41  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.20  2005/06/16 12:59:23  pdoubleya
 * Cleaned up support for reloading documents.
 *
 * Revision 1.19  2005/06/16 11:29:12  pdoubleya
 * First cut support for reload page, flushes inline stylesheets.
 *
 * Revision 1.18  2005/06/16 07:24:46  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.17  2005/06/15 11:53:45  tobega
 * Changed UserAgentCallback to getInputStream instead of getReader. Fixed up some consequences of previous change.
 *
 * Revision 1.16  2005/06/01 21:36:36  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.15  2005/03/24 23:17:58  pdoubleya
 * Added debug dump on bad CSS input.
 *
 * Revision 1.14  2005/02/03 23:08:26  pdoubleya
 * .
 *
 * Revision 1.13  2005/01/29 20:19:21  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.12  2005/01/10 23:24:46  tobega
 * Created image cache
 *
 * Revision 1.11  2004/12/11 18:18:07  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.10  2004/12/02 19:46:36  tobega
 * Refactored handling of inline styles to fit with StylesheetInfo and media handling (is also now correct if there should be more than one style element)
 *
 * Revision 1.9  2004/11/30 23:47:57  tobega
 * At-media rules should now work (not tested). Also fixed at-import rules, which got broken at previous modification.
 *
 * Revision 1.8  2004/11/29 23:25:40  tobega
 * Had to redo thinking about Stylesheets and StylesheetInfos. Now StylesheetInfos are passed around instead of Stylesheets because any Stylesheet should only be linked to its URI. Bonus: the external sheets get lazy-loaded only if needed for the medium.
 *
 * Revision 1.7  2004/11/28 23:29:02  tobega
 * Now handles media on Stylesheets, still need to handle at-media-rules. The media-type should be set in Context.media (set by default to "screen") before calling setContext on StyleReference.
 *
 * Revision 1.6  2004/11/15 22:22:08  tobega
 * Now handles @import stylesheets
 *
 * Revision 1.5  2004/11/15 20:06:31  tobega
 * Should now handle @import stylesheets, at least those with absolute urls
 *
 * Revision 1.4  2004/11/15 19:46:14  tobega
 * Refactoring in preparation for handling @import stylesheets
 *
 * Revision 1.3  2004/11/15 12:42:23  pdoubleya
 * Across this checkin (all may not apply to this particular file)
 * Changed default/package-access members to private.
 * Changed to use XRRuntimeException where appropriate.
 * Began move from System.err.println to std logging.
 * Standard code reformat.
 * Removed some unnecessary SAC member variables that were only used in initialization.
 * CVS log section.
 *
 *
 */

