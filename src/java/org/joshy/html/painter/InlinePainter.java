package org.joshy.html.painter;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.*;

import org.joshy.html.*;
import org.joshy.html.box.*;
import org.joshy.html.util.*;
import org.joshy.u;

/**
 *  Description of the Class
 *
 * @author     jmarinacci
 * @created    August 30, 2004
 */
public class InlinePainter {

    /**
     *  Description of the Method
     *
     * @param  c    Description of the Parameter
     * @param  box  Description of the Parameter
     */
    public static void paintInlineContext(Context c, Box box) {
        BlockBox block = (BlockBox) box;

        // translate into local coords
        // account for the origin of the containing box
        c.getGraphics().translate(box.x, box.y);

        // for each line box
        c.getGraphics().setColor(Color.black);
        //u.p("line count = " + block.boxes.size());
        for (int i = 0; i < block.getChildCount(); i++) {
            // get the line box
            //u.p("line = " + block.boxes.get(i));
            paintLine(c, (LineBox) block.getChild(i));
        }

        // translate back to parent coords
        //c.getGraphics().translate(-c.getExtents().x,-c.getExtents().y);
        c.getGraphics().translate(-box.x, -box.y);
        //c.getGraphics().translate(inset_left,inset_top);
    }


    /**
     *  Description of the Method
     *
     * @param  c     Description of the Parameter
     * @param  line  Description of the Parameter
     */
    private static void paintLine(Context c, LineBox line) {
        //u.p("painting line = " + line);
        // get x and y
        int lx = line.x;
        int ly = line.y + line.baseline;

        // for each inline box
        for (int j = 0; j < line.getChildCount(); j++) {
            paintInline(c, (InlineBox) line.getChild(j), lx, ly, line);
        }
        if (c.debugDrawLineBoxes()) {
            GraphicsUtil.drawBox(c.getGraphics(), line, Color.blue);
        }
    }


    // inlines are drawn vertically relative to the baseline of the line,
    // not relative to the origin of the line.
    // they *are* drawn horizontally (x) relative to the origin of the line
    // though

    /**
     *  Description of the Method
     *
     * @param  c       Description of the Parameter
     * @param  inline  Description of the Parameter
     * @param  lx      Description of the Parameter
     * @param  ly      Description of the Parameter
     * @param  line    Description of the Parameter
     */
    private static void paintInline(Context c, InlineBox inline, int lx, int ly, LineBox line) {

        if (InlineLayout.isReplaced(inline.node)) {
            //u.p("painting a replaced block: " + inline);
            c.getGraphics().translate(line.x, line.y + (line.baseline - inline.height));
            Layout layout = (Layout) LayoutFactory.getLayout(inline.node);
            //u.p("inline node = " + inline.node);
            //u.p("got the layout: " + layout);
            layout.paint(c, inline);
            c.getGraphics().translate(-line.x, -(line.y + (line.baseline - inline.height)));
            if (c.debugDrawInlineBoxes()) {
                GraphicsUtil.draw(c.getGraphics(), new Rectangle(lx + inline.x + 1, ly + inline.y + 1 - inline.height,
                        inline.width - 2, inline.height - 2), Color.green);
            }
            return;
        }

        if (InlineLayout.isFloatedBlock(inline.node, c)) {
            //u.p("painting a floated block: " + inline);
            //u.p("line = " + line);
            Rectangle oe = c.getExtents();
            //inline.x = 0;
            //u.p("extents = " + c.getExtents());
            c.setExtents(new Rectangle(oe.x, 0, oe.width, oe.height));

            int xoff = line.x + inline.x;
            int yoff = line.y + (line.baseline - inline.height) + inline.y;
            c.getGraphics().translate(xoff, yoff);
            Layout layout = (Layout) LayoutFactory.getLayout(inline.node);
            layout.paint(c, inline.sub_block);
            c.getGraphics().translate(-xoff, -yoff);

            c.setExtents(oe);
            return;
        }

        if (inline.is_break) {
            return;
        }

        Graphics g = c.getGraphics();

        // handle relative
        if (inline.relative) {
            g.translate(inline.left, inline.top);
        }

        c.updateSelection(inline);

        // calculate the x and y relative to the baseline of the line (ly) and the
        // left edge of the line (lx)
        //String text = inline.text.substring(inline.start_index,inline.end_index);
        String text = inline.getSubstring();
        int iy = ly + inline.y;
        int ix = lx + inline.x;

        // draw a selection rectangle
        paintSelection(c, inline, lx, ly);

        //adjust font for current settings
        Font oldfont = c.getGraphics().getFont();
        c.getGraphics().setFont(inline.getFont());
        Color oldcolor = c.getGraphics().getColor();
        c.getGraphics().setColor(inline.color);


        //draw the line
        //u.p("drawing: " + text + " at " + x + "," + iy);
        if (text != null && text.length() > 0) {
            c.getGraphics().drawString(text, ix, iy);
        }
        c.getGraphics().setColor(oldcolor);

        //draw any text decoration
        Font cur_font = c.getGraphics().getFont();

        LineMetrics lm = cur_font.getLineMetrics(text, ((Graphics2D) c.getGraphics()).getFontRenderContext());
        if (inline.underline) {
            float down = lm.getUnderlineOffset();
            float thick = lm.getUnderlineThickness();
            g.fillRect(ix, iy + (int) down, g.getFontMetrics().stringWidth(text), (int) thick);
        }
        if (inline.strikethrough) {
            float down = lm.getStrikethroughOffset();
            float thick = lm.getStrikethroughThickness();
            g.fillRect(ix, iy + (int) down, g.getFontMetrics().stringWidth(text), (int) thick);
        }
        if (inline.overline) {
            float down = lm.getAscent();
            float thick = lm.getUnderlineThickness();
            g.fillRect(ix, iy - (int) down, g.getFontMetrics().stringWidth(text), (int) thick);
        }

        if (c.debugDrawInlineBoxes()) {
            GraphicsUtil.draw(c.getGraphics(), new Rectangle(lx + inline.x + 1, ly + inline.y + 1 - inline.height,
                    inline.width - 2, inline.height - 2), Color.green);
        }

        // restore the old font
        c.getGraphics().setFont(oldfont);

        // handle relative
        if (inline.relative) {
            g.translate(-inline.left, -inline.top);
        }
    }


    /**
     *  Description of the Method
     *
     * @param  c       Description of the Parameter
     * @param  inline  Description of the Parameter
     * @param  lx      Description of the Parameter
     * @param  ly      Description of the Parameter
     */
    public static void paintSelection(Context c, InlineBox inline, int lx, int ly) {
        if (c.inSelection(inline)) {
            int dw = inline.width - 2;
            int xoff = 0;
            if (c.getSelectionEnd() == inline) {
                dw = c.getSelectionEndX();
            }
            if (c.getSelectionStart() == inline) {
                xoff = c.getSelectionStartX();
            }
            c.getGraphics().setColor(new Color(200, 200, 255));

            ((Graphics2D) c.getGraphics()).setPaint(new GradientPaint(
                    0, 0, new Color(235, 235, 255),
                    0, inline.height / 2, new Color(190, 190, 235),
                    true));
            c.getGraphics().fillRect(
                    lx + inline.x + xoff, ly + inline.y - inline.height,
                    dw - xoff, inline.height);
        }
    }

}

