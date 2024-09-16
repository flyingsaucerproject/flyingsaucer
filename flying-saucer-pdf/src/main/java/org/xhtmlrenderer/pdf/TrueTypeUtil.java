package org.xhtmlrenderer.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.util.XRRuntimeException;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * Uses code from iText's DefaultFontMapper and TrueTypeFont classes.  See
 * <a href="http://sourceforge.net/projects/itext/">http://sourceforge.net/projects/itext/</a> for license information.
 */
@ParametersAreNonnullByDefault
public class TrueTypeUtil {

    /**
     * Avoid using memory-mapped files for loading fonts.
     * On Windows, these memory-mapped files are not released from memory, causing memory leaks.
     * See <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/385#issuecomment-2352080728">github issue 385</a>.
     */
    private static final boolean AVOID_MEMORY_MAPPED_FILES = true;

    @Nonnull
    @CheckReturnValue
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

    @Nonnull
    @CheckReturnValue
    public static Collection<String> getFamilyNames(BaseFont font) {
        String[][] names = font.getFamilyFontName();

        if (names.length == 1) {
            return singletonList(names[0][3]);
        }

        List<String> result = new ArrayList<>();
        for (String[] name : names) {
            if ((name[0].equals("1") && name[1].equals("0")) || name[2].equals("1033")) {
                result.add(name[3]);
            }
        }

        return result;
    }

    @Nonnull
    @CheckReturnValue
    // HACK No accessor
    private static Map<String, int[]> extractTables(BaseFont font)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> current = font.getClass();

        while (current != null) {
            if (current.getName().endsWith(".TrueTypeFont")) {
                Field field = current.getDeclaredField("tables");
                field.setAccessible(AVOID_MEMORY_MAPPED_FILES);
                //noinspection unchecked
                return (Map<String, int[]>) field.get(font);
            }

            current = current.getSuperclass();
        }

        throw new NoSuchFieldException("Could not find tables field");
    }

    @Nonnull
    @CheckReturnValue
    private static String getTTCName(String name) {
        int index = name.toLowerCase().indexOf(".ttc,");
        return index < 0 ? name : name.substring(0, index + 4);
    }

    @Nonnull
    @CheckReturnValue
    public static FontDescription extractDescription(String path, BaseFont font) {
        try {
            FontDescription description = new FontDescription(font);
            TrueTypeUtil.populateDescription(path, font, description);
            return description;
        } catch (DocumentException | IOException | NoSuchFieldException | IllegalAccessException e) {
            throw new XRRuntimeException("Failed to read font description from %s".formatted(path), e);
        }
    }

    public static void populateDescription(String path, BaseFont font, FontDescription description)
            throws IOException, NoSuchFieldException, IllegalAccessException, DocumentException {

        try (RandomAccessFileOrArray rf = new RandomAccessFileOrArray(getTTCName(path), false, AVOID_MEMORY_MAPPED_FILES)) {
            populateDescription0(path, font, description, rf);
        }
    }

    @Nonnull
    @CheckReturnValue
    public static FontDescription extractDescription(String path, byte[] contents, BaseFont font) {
        try {
            FontDescription description = new FontDescription(font);
            populateDescription(path, contents, font, description);
            return description;
        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            throw new XRRuntimeException("Failed to read font description from %s".formatted(path), e);
        }
    }

    public static void populateDescription(String path, byte[] contents, BaseFont font, FontDescription description)
            throws IOException, NoSuchFieldException, IllegalAccessException, DocumentException {
        try (RandomAccessFileOrArray rf = new RandomAccessFileOrArray(contents)) {
            populateDescription0(path, font, description, rf);
        }
    }

    private static void populateDescription0(String path,
                                             BaseFont font, FontDescription description, RandomAccessFileOrArray rf)
            throws NoSuchFieldException, IllegalAccessException, DocumentException, IOException {
        Map<String, int[]> tables = extractTables(font);

        description.setStyle(guessStyle(font));

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

        description.setWeight(rf.readUnsignedShort());
        want = 20;
        got = rf.skip(want);
        if (got < want) {
            throw new DocumentException("Skip TT font strikeout, expect read " + want + " bytes, but only got " + got);
        }

        description.setYStrikeoutSize(rf.readShort());
        description.setYStrikeoutPosition(rf.readShort());

        location = tables.get("post");

        if (location != null) {
            rf.seek(location[0]);
            want = 8;
            got = rf.skip(want);
            if (got < want) {
                throw new DocumentException("Skip TT font underline, expect read " + want + " bytes, but only got " + got);
            }
            description.setUnderlinePosition(rf.readShort());
            description.setUnderlineThickness(rf.readShort());
        }
    }

}
