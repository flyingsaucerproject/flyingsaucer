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

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.isValidCodePoint;
import static java.lang.Integer.parseInt;

/**
 * Simple command-line program to test whether a given font has a glyph
 * available for a given Unicode codepoint, expressed as an integer.
 * <pre>
 * java VerifyGlyphExists 945 /path/to/fontfile
 * </pre>
 * <p>
 * If the glyph is available, output will look something like
 * "FOUND    &#945; for java.awt.Font[family=Kochi Gothic,name=Kochi Gothic,
 * style=plain,size=12] from
 * /usr/share/fonts/truetype/kochi/kochi-gothic.ttf"; if not available, will
 * look something like "NO GLYPH &#945; for
 * java.awt.Font[family=Lohit Bengali,name=Lohit Bengali,style=plain,size=12]
 * from /usr/share/fonts/truetype/ttf-bengali-fonts/lohit_bn.ttf".
 * </p>
 * <p>
 * Test for the glyph relies on java.awt.Font.canDisplay(codepoint).
 * </p>
 * <p>
 * Arguments: valid arguments are either code point and font-file path, or
 * code point and font-list path. Font-list
 * must be a text file with one line per font-file path. When providing a
 * font list, the program will loop over all
 * font files listed in the font-list, and check each one for support of the
 * glyph.
 * </p>
 */
public class VerifyGlyphExists {
    public static void main(String[] args) {
        if (args.length != 2) {
            error("Need two arguments: code point (int) and either a font file name, or a path to a text file listing font paths");
        }
        int codePoint = 0;
        try {
            codePoint = parseInt( args[ 0 ] );
        } catch (NumberFormatException e) {
            error("Value " + args[0] + " for codepoint is not an integer.");
        }
        final File file = new File(args[1]);
        if (file.exists()) {
            if (file.getName().endsWith("txt")) {
                final List<String> lines = readLines(file);
                for(final String path : lines ) {
                    testForGlyph( codePoint, new File( path ) );
                }
                System.out.println("TODO: read list of fonts");
            } else {
                testForGlyph(codePoint, file);
            }
        } else {
            error("Second argument must be a font file path, or a path to a text file listing font file paths");
        }
    }

    private static List<String> readLines(File file) {
        final List<String> l = new ArrayList<>();
        try( LineNumberReader r = new LineNumberReader(
            new BufferedReader( new FileReader( file ) ) ) ) {
          String path;
          while( (path = r.readLine()) != null ) {
            l.add( path );
          }
        } catch( IOException e ) {
          error( "Can't read list of font paths from " + file.getPath() );
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
                final String msg = "%s&#%d; for %s from %s";
                String found = "NO GLYPH ";

                if( !isValidCodePoint( codePoint ) ) {
                  found = "INVALID  ";
                }
                else if( font.canDisplay( (char) codePoint ) ) {
                  found = "FOUND    ";
                }

                System.out.printf( msg, found, codePoint, font, file.getPath() );
            }
        } catch (IOException e) {
            error("Can't load font at path " + file.getPath() + ", err: " + e.getMessage());
        }
    }

    private static Font loadFont( String fontPath) throws IOException {
        try( final InputStream s = new File( fontPath ).toURL().openStream() ) {
            final int format = Font.TRUETYPE_FONT;
            final Font font = Font.createFont(format,s );

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
