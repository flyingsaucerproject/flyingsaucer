/*
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
 * Copyright (c) 2006 Wisconsin Court System
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
 *
 */
package org.xhtmlrenderer.layout;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.render.InlineBox;

/**
 * @author Torbjoern Gannholm
 */
public class WhitespaceStripper {
    public final static String SPACE = " ";
    public final static String EOL = "\n";
    public final static char EOLC = '\n';
    
    public final static Pattern linefeed_space_collapse = Pattern.compile("\\s+\\n\\s+");//Pattern is thread-safe
    public final static Pattern linefeed_to_space = Pattern.compile("\\n");
    public final static Pattern tab_to_space = Pattern.compile("\\t");
    public final static Pattern space_collapse = Pattern.compile("(?: )+");
    public final static Pattern space_before_linefeed_collapse = Pattern.compile("[\\s&&[^\\n]]\\n");
    
    /**
     * Strips whitespace early in inline content generation. This can be done
     * because "whitespage" does not ally to :first-line and :first-letter. For
     * dynamic pseudo-classes we are allowed to choose which properties apply.
     * 
     * <b>NOTE:</b> The <code>inlineContent</code> parameter may be modified
     *
     * @param c
     * @param inlineContent
     */
    public static void stripInlineContent(List inlineContent) {
        boolean collapse = false;
        boolean allWhitespace = true;

        for (Iterator i = inlineContent.iterator(); i.hasNext();) {
            Styleable node = (Styleable)i.next();

            if (node.getStyle().isInline()) {
                InlineBox iB = (InlineBox)node;
                boolean collapseNext = stripWhitespace(iB, collapse);
                if (! iB.isRemovableWhitespace()) {
                    allWhitespace = false;
                }
                
                collapse = collapseNext;
            } else {
                if (! canCollapseThrough(node)) {
                    allWhitespace = false;
                    collapse = false;
                }
            }
        }

        if (allWhitespace) {
            stripTextContent(inlineContent);
        }
    }
    
    private static boolean canCollapseThrough(Styleable styleable) {
        CalculatedStyle style = styleable.getStyle();
        return style.isFloated() || style.isAbsolute() || style.isFixed() || style.isRunning();
    }

    private static void stripTextContent(List stripped) {
        boolean onlyAnonymous = true;
        for (Iterator i = stripped.iterator(); i.hasNext(); ) {
            Styleable node = (Styleable)i.next();
            if (node.getStyle().isInline()) {
                InlineBox iB = (InlineBox)node;
                if (iB.getElement() != null) {
                    onlyAnonymous = false;
                }
                
                iB.truncateText();
            }
        }
        
        if (onlyAnonymous) {
            for (Iterator i = stripped.iterator(); i.hasNext(); ) {
                Styleable node = (Styleable)i.next();
                if (node.getStyle().isInline()) {
                    i.remove();
                }
            }
        }
    }

    /**
     * this function strips all whitespace from the text according to the CSS
     * 2.1 spec on whitespace handling. It accounts for the different whitespace
     * settings like normal, nowrap, pre, etc
     *
     * @param style
     * @param collapseLeading
     * @param tc              the TextContent to strip. The text in it is
     *                        modified.
     * @return whether the next leading space should collapse or
     *         not.
     */
    private static boolean stripWhitespace(InlineBox iB, boolean collapseLeading) {

        IdentValue whitespace = iB.getStyle().getIdent(CSSName.WHITE_SPACE);
        
        String text = iB.getText();

        text = collapseWhitespace(iB, whitespace, text, collapseLeading);

        boolean collapseNext = (text.endsWith(SPACE) &&
                (whitespace == IdentValue.NORMAL || whitespace == IdentValue.NOWRAP || whitespace == IdentValue.PRE));

        iB.setText(text);
        if (text.trim().equals("")) {
            if (whitespace == IdentValue.NORMAL || whitespace == IdentValue.NOWRAP) {
                iB.setRemovableWhitespace(true);
            } else if (whitespace == IdentValue.PRE) {
                iB.setRemovableWhitespace(false);//actually unnecessary, is set to this by default
            } else if (text.indexOf(EOL) < 0) {//and whitespace.equals("pre-line"), the only one left
                iB.setRemovableWhitespace(true);
            }
        }
        return text.equals("") ? collapseLeading : collapseNext;
    }

    private static String collapseWhitespace(InlineBox iB, IdentValue whitespace, String text, boolean collapseLeading) {
        if (whitespace == IdentValue.NORMAL || whitespace == IdentValue.NOWRAP) {
            text = linefeed_space_collapse.matcher(text).replaceAll(EOL);
        } else if (whitespace == IdentValue.PRE) {
            text = space_before_linefeed_collapse.matcher(text).replaceAll(EOL);
        }

        if (whitespace == IdentValue.NORMAL || whitespace == IdentValue.NOWRAP) {
            text = linefeed_to_space.matcher(text).replaceAll(SPACE);
            text = tab_to_space.matcher(text).replaceAll(SPACE);
            text = space_collapse.matcher(text).replaceAll(SPACE);
        } else if (whitespace == IdentValue.PRE || whitespace == IdentValue.PRE_WRAP) {
            int tabSize = (int) iB.getStyle().asFloat(CSSName.TAB_SIZE);
            char[] tabs = new char[tabSize];
            Arrays.fill(tabs, ' ');
            text = tab_to_space.matcher(text).replaceAll(new String(tabs));
        } else if (whitespace == IdentValue.PRE_LINE) {
            text = tab_to_space.matcher(text).replaceAll(SPACE);
            text = space_collapse.matcher(text).replaceAll(SPACE);
        }

        if (whitespace == IdentValue.NORMAL || whitespace == IdentValue.NOWRAP) {
            // collapse first space against prev inline
            if (text.startsWith(SPACE) &&
                    collapseLeading) {
                text = text.substring(1, text.length());
            }
        }

        return text;
    }
}


