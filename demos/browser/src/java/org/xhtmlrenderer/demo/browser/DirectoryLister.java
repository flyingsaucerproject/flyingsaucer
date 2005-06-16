/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
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
package org.xhtmlrenderer.demo.browser;

import java.io.File;


/**
 * Description of the Class
 *
 * @author empty
 */
public class DirectoryLister {

    /**
     * Description of the Method
     *
     * @param file PARAM
     * @return Returns
     */
    public static String list(File file) {
        StringBuffer sb = new StringBuffer();

        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>Directory listing of ");
        sb.append(file.getPath());
        sb.append("</title>");
        sb.append("<style>");
        sb.append("table { background-color: #ddffdd; }");
        sb.append(".dir { font-weight: bold; color: #ff9966; }");
        sb.append(".file { font-weight: normal; color: #003333; }");
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<p>the file ");
        sb.append(file.toString());
        sb.append(" is</p>");

        if (file.isDirectory()) {
            sb.append("<table>");
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                sb.append("<tr>");
                if (files[i].isDirectory()) {
                    sb.append("<td class='dir'>" + files[i].getName() + "</td>");
                } else {
                    sb.append("<td class='file'>" + files[i].getName() + "</td>");
                }
                sb.append("</tr>");
            }
            sb.append("</table>");
        }

        return sb.toString();
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.7  2005/06/16 07:24:44  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.6  2005/06/15 10:56:13  tobega
 * cleaned up a bit of URL mess, centralizing URI-resolution and loading to UserAgentCallback
 *
 * Revision 1.5  2004/12/12 03:33:07  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.4  2004/12/12 02:55:10  tobega
 * Making progress
 *
 * Revision 1.3  2004/10/23 14:38:58  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

