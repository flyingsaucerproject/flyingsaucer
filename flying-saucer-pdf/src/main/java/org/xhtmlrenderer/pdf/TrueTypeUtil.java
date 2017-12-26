package org.xhtmlrenderer.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.pdf.ITextFontResolver.FontDescription;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Uses code from iText's DefaultFontMapper and TrueTypeFont classes.  See
 * <a href="http://sourceforge.net/projects/itext/">http://sourceforge.net/projects/itext/</a> for license information.
 */
public class TrueTypeUtil {

    private static IdentValue guessStyle(BaseFont font) {
        String[][] names = font.getFullFontName();

        for (String[] name : names) {
            String lower = name[3].toLowerCase();
            if (lower.contains("italic")) {
                return IdentValue.ITALIC;
            } else if (lower.contains("oblique")) {
                return IdentValue.OBLIQUE;
            }
        }

        return IdentValue.NORMAL;
    }

    public static String[] getFamilyNames(BaseFont font) {
        String[][] names = font.getFamilyFontName();

        if (names.length == 1) {
            return new String[]{names[0][3]};
        }

        List<String> result = new ArrayList<String>();
        for (String[] name : names) {
            if ((name[0].equals("1") && name[1].equals("0")) || name[2].equals("1033")) {
                result.add(name[3]);
            }
        }

        return result.toArray(new String[result.size()]);
    }

    // HACK No accessor
    private static Map<String, int[]> extractTables(BaseFont font)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class current = font.getClass();

        while (current != null) {
            if (current.getName().endsWith(".TrueTypeFont")) {
                Field field = current.getDeclaredField("tables");
                field.setAccessible(true);
                return (Map) field.get(font);
            }

            current = current.getSuperclass();
        }

        throw new NoSuchFieldException("Could not find tables field");
    }

    private static String getTTCName(String name) {
        int index = name.toLowerCase().indexOf(".ttc,");

        return index < 0 ? name : name.substring(0, index + 4);
    }

    public static void populateDescription(String path, BaseFont font, FontDescription descr)
            throws IOException, NoSuchFieldException, IllegalAccessException, DocumentException {
        RandomAccessFileOrArray rf = null;
        try {
            rf = new RandomAccessFileOrArray(getTTCName(path));

            rf = populateDescription0(path, font, descr, rf);
        } finally {
            if (rf != null) {
                try {
                    rf.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static void populateDescription(String path, byte[] contents, BaseFont font, FontDescription descr)
            throws IOException, NoSuchFieldException, IllegalAccessException, DocumentException {
        RandomAccessFileOrArray rf = null;
        try {
            rf = new RandomAccessFileOrArray(contents);

            rf = populateDescription0(path, font, descr, rf);
        } finally {
            if (rf != null) {
                try {
                    rf.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private static RandomAccessFileOrArray populateDescription0(String path,
                                                                BaseFont font, FontDescription descr, RandomAccessFileOrArray rf)
            throws NoSuchFieldException, IllegalAccessException, DocumentException, IOException {
        Map<String, int[]> tables = extractTables(font);

        descr.setStyle(guessStyle(font));

        int[] location = tables.get("OS/2");
        if (location == null) {
            throw new DocumentException("Table 'OS/2' does not exist in " + path);
        }

        rf.seek(location[0]);
        int want = 4;
        long got = rf.skip(want);
        if (got < want) {
            throw new DocumentException("Skip TT font weight, expect read " + want + " bytes, but only got " + got);
        }

        descr.setWeight(rf.readUnsignedShort());
        want = 20;
        got = rf.skip(want);
        if (got < want) {
            throw new DocumentException("Skip TT font strikeout, expect read " + want + " bytes, but only got " + got);
        }

        descr.setYStrikeoutSize(rf.readShort());
        descr.setYStrikeoutPosition(rf.readShort());

        location = tables.get("post");

        if (location != null) {
            rf.seek(location[0]);
            want = 8;
            got = rf.skip(want);
            if (got < want) {
                throw new DocumentException("Skip TT font underline, expect read " + want + " bytes, but only got " + got);
            }
            descr.setUnderlinePosition(rf.readShort());
            descr.setUnderlineThickness(rf.readShort());
        }

        rf.close();
        rf = null;
        return rf;
    }

}
