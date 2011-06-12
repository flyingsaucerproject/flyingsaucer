import com.lowagie.text.pdf.BaseFont;
import org.xhtmlrenderer.pdf.ITextFontResolver;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 */
public class ListFontFamilyNames {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Need path to font files (directory or file name)");
            System.exit(1);
        }
        File fod = new File(args[0]);
        List fontFiles = new ArrayList();
        if (fod.isDirectory()) {
            fontFiles.addAll(Arrays.asList(fod.listFiles(new FilenameFilter() {
                public boolean accept(File file, String s) {
                    return s.endsWith(".ttf");
                }
            })));
        } else {
            fontFiles.add(fod);
        }
        //System.out.println("font files " + fontFiles);
        List errors = new ArrayList();
        for (Iterator fit = fontFiles.iterator(); fit.hasNext();) {
            File f = (File) fit.next();
            Font awtf = null;
            try {
                awtf = Font.createFont(Font.TRUETYPE_FONT, f);
            } catch (FontFormatException e) {
                System.err.println("Trying to load font via AWT: " + e.getMessage());
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Trying to load font via AWT: " + e.getMessage());
                System.exit(1);
            }
            Set set;
            try {
                set = ITextFontResolver.getDistinctFontFamilyNames(f.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                System.out.println(
                        "Font located at " + f.getPath() + "\n" +
                                "  family name (reported by AWT): " + awtf.getFamily() + "\n" +
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
        if (errors.size() > 0) {
            if (args.length == 2 && args[1].equals("-e")) {
                System.err.println("Errors were reported on reading some font files.");
                for (Iterator eit = errors.iterator(); eit.hasNext();) {
                    System.err.println((String) eit.next());
                }
            } else {
                System.err.println("Errors were reported on reading some font files. Pass -e as argument to show them, and re-run.");
            }
        }
    }
}
