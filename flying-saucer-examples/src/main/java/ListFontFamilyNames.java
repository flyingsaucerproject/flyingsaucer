import com.lowagie.text.pdf.BaseFont;
import org.xhtmlrenderer.pdf.ITextFontResolver;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public class ListFontFamilyNames {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Need path to font files (directory or file name)");
            System.exit(1);
        }
        File fod = new File(args[0]);
        List<File> fontFiles = new ArrayList<>();
        if (fod.isDirectory()) {
            fontFiles.addAll(asList(requireNonNull(fod.listFiles((file, s) -> s.endsWith(".ttf")))));
        } else {
            fontFiles.add(fod);
        }
        
        List<String> errors = new ArrayList<>();
        for (File f : fontFiles) {
            final Font awtFont;
            try {
                awtFont = Font.createFont(Font.TRUETYPE_FONT, f);
            } catch (FontFormatException | IOException e) {
                throw new IllegalStateException("Failed to load font from " + f + ": " + e.getMessage(), e);
            }
            Set<String> set;
            try {
                set = ITextFontResolver.getDistinctFontFamilyNames(f.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                System.out.println(
                        "Font located at " + f.getPath() + "\n" +
                                "  family name (reported by AWT): " + awtFont.getFamily() + "\n" +
                                "  family name (reported by iText): " + set.iterator().next() + "\n"
                );
            } catch (RuntimeException e) {
                if (e.getMessage().contains("not a valid TTF or OTF file.")) {
                    errors.add(e.getMessage());
                } else if (e.getMessage().contains("Table 'OS/2' does not exist")) {
                    errors.add(e.getMessage());
                } else if (e.getMessage().contains("licensing restrictions.")) {
                    errors.add(e.getMessage());
                } else {
                    throw e;
                }
            }
        }
        if (!errors.isEmpty()) {
            if (args.length == 2 && args[1].equals("-e")) {
                System.err.println("Errors were reported on reading some font files.");
                for (String error : errors) {
                    System.err.println(error);
                }
            } else {
                System.err.println("Errors were reported on reading some font files. Pass -e as argument to show them, and re-run.");
            }
        }
    }
}
