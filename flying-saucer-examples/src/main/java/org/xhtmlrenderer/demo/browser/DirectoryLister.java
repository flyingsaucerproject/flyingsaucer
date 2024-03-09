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

import org.xhtmlrenderer.util.GeneralUtil;

import java.io.File;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DirectoryLister {

    public static String list(File file) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>Directory listing for ");
        sb.append(file.getPath());
        sb.append("</title>");
        sb.append("<style>");
        sb.append("body { font-family: monospaced; }");
        sb.append("ul { background-color: #ddffdd; }");
        sb.append("li { list-style-type: none; }");
        sb.append("a { text-decoration: none; }");
        sb.append(".dir { font-weight: bold; color: #ff9966; }");
        sb.append(".file { font-weight: normal; color: #003333; }");
        sb.append("</style>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<h2>Index of ");
        sb.append(file);
        sb.append("</h2>");
        sb.append("<hr />");

        if (file.isDirectory()) {
            try {
                File parent = file.getParentFile();
                if ( parent != null ) {
                    String loc = GeneralUtil.htmlEscapeSpace(file.getAbsoluteFile().getParentFile().toURI().toURL().toExternalForm()).toString();
                    sb.append("<a class='dir' href='").append(loc).append("'>Up to higher level directory</a>");
                }
            } catch (MalformedURLException e) {
                // skip
            }
            sb.append("<table style='width: 75%'>");
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.isHidden()) continue;
                long len = f.length();
                String lenDesc = (len > 1024 ? new DecimalFormat("#,###KB").format(len / 1024) : "");
                String lastMod = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(new Date(f.lastModified()));
                sb.append("<tr>");

                String cls;
                if (f.isDirectory()) {
                    cls = "dir";
                } else {
                    cls = "file";
                }
                try {
                    String loc = GeneralUtil.htmlEscapeSpace(f.toURI().toURL().toExternalForm()).toString();
                    sb.append("<td><a class='").append(cls).append("' href='").append(loc).append("'>").append(f.getName()).append("</a></td>");
                    sb.append("<td>").append(lenDesc).append("</td>").append("<td>").append(lastMod).append("</td>");
                } catch (MalformedURLException e) {
                    sb.append(f.getAbsolutePath());
                }
                sb.append("</tr>");
            }
            sb.append("</table>");
        }

        sb.append("<hr />");
        sb.append("</body></html>");

        return sb.toString();
    }

}
