/*
 * {{{ header & license
 * Copyright (c) 2008 Patrick Wright
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

import java.io.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;


/**
 * Simple command-line program to test whether a given font has a glyph available for a given Unicode codepoint, expressed
 * as an integer.
 * <pre>
 * java VerifyGlyphExists 945 /path/to/fontfile
 * </pre>
 * If the glyph is available, output will look something like
 * "FOUND    &#945; for java.awt.Font[family=Kochi Gothic,name=Kochi Gothic,style=plain,size=12] from
 * /usr/share/fonts/truetype/kochi/kochi-gothic.ttf"; if not available, will look something like "NO GLYPH &#945; for
 * java.awt.Font[family=Lohit Bengali,name=Lohit Bengali,style=plain,size=12] from
 * /usr/share/fonts/truetype/ttf-bengali-fonts/lohit_bn.ttf".
 * Test for the glyph relies on java.awt.Font.canDisplay(codepoint).
 * <p>Arguments: valid arguments are either code point and font-file path, or code point and font-list path. Font-list
 * must be a text file with one line per font-file path. When providing a font list, the program will loop over all
 * font files listed in the font-list, and check each one for support of the glyph.
 */
public class VerifyGlyphExists {
    public static void main(String[] args) {
        if (args.length != 2) {
            error("Need two arguments: code point (int) and either a font file name, or a path to a text file listing font paths");
        }
        int codePoint = 0;
        try {
            codePoint = Integer.valueOf(args[0]).intValue();
        } catch (NumberFormatException e) {
            error("Value " + args[0] + " for codepoint is not an integer.");
        }
        File file = new File(args[1]);
        if (file.exists()) {
            if (file.getName().endsWith("txt")) {
                List lines = readLines(file);
                for (Iterator it = lines.iterator(); it.hasNext();) {
                    String path = (String) it.next();
                    testForGlyph(codePoint, new File(path));
                }
                System.out.println("TODO: read list of fonts");
            } else {
                testForGlyph(codePoint, file);
            }
        } else {
            error("Second argument must be a font file path, or a path to a text file listing font file paths");
        }
    }

    private static List readLines(File file) {
        List l = new ArrayList();
        LineNumberReader r = null;
        try {
            r = new LineNumberReader(new BufferedReader(new FileReader(file)));
            String path;
            while ((path = r.readLine()) != null) {
                l.add(path);
            }
        } catch (IOException e) {
            error("Can't read list of font paths from " + file.getPath());
        } finally {
            try {
                if (r != null) r.close();
            } catch (IOException e) {
                // swallow
            }
        }
        return l;
    }

    private static void testForGlyph(int codePoint, File file) {
        Font font;
        try {
            font = loadFont(file.getCanonicalPath());
            if (font == null) {
                error("Could not load font at path: " + file.getPath());
            } else {
                if (font.canDisplay((char) 0)) {  // FIXME: Character.codePoint(codePoint) not in 1.4
                    System.out.println("FOUND    &#" + codePoint + "; for " + font + " from " + file.getPath());
                } else {
                    System.out.println("NO GLYPH &#" + codePoint + "; for " + font + " from " + file.getPath());
                }
            }
        } catch (IOException e) {
            error("Can't load font at path " + file.getPath() + ", err: " + e.getMessage());
        }
    }

    private static Font loadFont(String fontPath) throws IOException {
        try {
            // FIXME: only TTF supported in 1.4
            // int format = fontPath.endsWith(".ttf") ? Font.TRUETYPE_FONT : Font.TYPE1_FONT;
            int format = Font.TRUETYPE_FONT;
            Font font = Font.createFont(format, new File(fontPath).toURL().openStream());
            return font.deriveFont(Font.PLAIN, 12);
        } catch (FontFormatException e) {
            System.err.println(fontPath + " INVALID FONT FORMAT " + e.getMessage());
            return null;
        }
    }

    private static void error(String msg) {
        System.err.println(msg);
        System.exit(-1);
    }
}
