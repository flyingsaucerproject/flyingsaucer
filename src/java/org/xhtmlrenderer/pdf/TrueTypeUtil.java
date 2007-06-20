package org.xhtmlrenderer.pdf;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.pdf.ITextFontResolver.FontDescription;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.RandomAccessFileOrArray;

/**
 * Uses code from iText's DefaultFontMapper and TrueTypeFont classes.  See
 * <a href="http://sourceforge.net/projects/itext/">http://sourceforge.net/projects/itext/</a> for license information.
 */
public class TrueTypeUtil {
    private static IdentValue guessStyle(BaseFont font) {
        String[][] names = font.getFullFontName();
        for (int i = 0; i < names.length; i++) {
            String name[] = names[i];
            String lower = name[3].toLowerCase();
            if (lower.indexOf("italic") != -1) {
                return IdentValue.ITALIC;
            } else if (lower.indexOf("oblique") != -1) {
                return IdentValue.OBLIQUE;
            }
        }
        
        return IdentValue.NORMAL;
    }
    
    public static String getFamilyName(BaseFont font) {
        String names[][] = font.getFamilyFontName();
        if (names.length == 1) {
            return names[0][3];
        }
        
        String name10 = null;
        String name3x = null;
        for (int k = 0; k < names.length; ++k) {
            String name[] = names[k];
            if (name[0].equals("1") && name[1].equals("0"))
                name10 = name[3];
            else if (name[2].equals("1033")) {
                name3x = name[3];
                break;
            }
        }
        String finalName = name3x;
        if (finalName == null) {
            finalName = name10;
        }
        if (finalName == null) {
            finalName = names[0][3];
        }
        
        return finalName;
    }    
    
    // HACK No accessor
    private static Map extractTables(BaseFont font) 
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, 
                    IllegalAccessException {
        Class current = font.getClass();
        while (current != null) {
            if (current.getName().endsWith(".TrueTypeFont")) {
                Field f = current.getDeclaredField("tables");
                f.setAccessible(true);
                return (Map)f.get(font);
            }
            
            current = current.getSuperclass();
        }
        
        throw new NoSuchFieldException("Could not find tables field");
    }
    
    public static void populateDescription(String path, BaseFont font, FontDescription descr) 
            throws IOException, NoSuchFieldException, IllegalAccessException, DocumentException {
        RandomAccessFileOrArray rf = null;
        try {
            rf = new RandomAccessFileOrArray(path);
            
            Map tables = extractTables(font);
            
            descr.setStyle(guessStyle(font));
            
            int[] location = (int[])tables.get("OS/2");
            if (location == null) {
                throw new DocumentException("Table 'OS/2' does not exist in " + path);
            }
            rf.seek(location[0]);
            rf.skip(4);
            descr.setWeight(rf.readUnsignedShort());
            rf.skip(20);
            descr.setYStrikeoutSize(rf.readShort());
            descr.setYStrikeoutPosition(rf.readShort());
            
            location = (int[])tables.get("post");
            
            if (location != null) {
                rf.seek(location[0]);
                rf.skip(8);
                descr.setUnderlinePosition(rf.readShort());
                descr.setUnderlineThickness(rf.readShort());
            }
            
            rf.close();
            rf = null;
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
}
