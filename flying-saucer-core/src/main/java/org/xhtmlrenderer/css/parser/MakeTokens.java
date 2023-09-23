/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
package org.xhtmlrenderer.css.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.nio.file.Files.newInputStream;

public class MakeTokens {
    private static final String EOL = System.getProperty("line.separator");
    private static final String INPUT = "C:/eclipseWorkspaceQT/xhtmlrenderer/src/java/org/xhtmlrenderer/css/parser/tokens.txt";

    public static void main(String[] args) throws IOException {
        List<String> tokens = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                newInputStream(Paths.get(INPUT))))) {
            String s;
            while ((s = reader.readLine()) != null) {
                tokens.add(s);
            }
        }
        // ignore

        StringBuilder buf = new StringBuilder();

        int offset = 1;
        for (Iterator<String> i = tokens.iterator(); i.hasNext(); offset++) {
            String s = i.next();
            String id = s.substring(0, s.indexOf(','));

            buf.append("\tpublic static final int ");
            buf.append(id);
            buf.append(" = ");
            buf.append(offset);
            buf.append(";");
            buf.append(EOL);
        }

        buf.append(EOL);

        for (Iterator<String> i = tokens.iterator(); i.hasNext(); offset++) {
            String s = i.next();
            String id = s.substring(0, s.indexOf(','));
            String description = s.substring(s.indexOf(',')+1);

            buf.append("\tpublic static final Token TK_");
            buf.append(id);
            buf.append(" = new Token(");
            buf.append(id);
            buf.append(", \"");
            buf.append(id);
            buf.append("\", \"");
            buf.append(description);
            buf.append("\");");
            buf.append(EOL);
        }

        buf.append(EOL);

        System.out.println(buf);
    }
}
