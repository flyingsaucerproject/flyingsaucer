package org.xhtmlrenderer.render;

import org.xhtmlrenderer.layout.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.*;
import java.awt.font.LineMetrics;
import org.xhtmlrenderer.util.GraphicsUtil;
import org.xhtmlrenderer.util.u;

public class InlineRenderer extends BoxRenderer {

    /**
    * Description of the Method
    *
    * @param c    PARAM
    * @param box  PARAM
    */
    public void paintComponent( Context c, Box box ) {
        if ( box.isAnonymous() ) {
            paintInlineContext( c, box );
            return;
        }
        if ( BoxLayout.isBlockLayout( box.getElement(), c ) ) {
            super.paintComponent( c, box );
            return;
        }
        paintInlineContext( c, box );
    }

    /**
    * Description of the Method
    *
    * @param c    PARAM
    * @param box  PARAM
    */
    public void paintChildren( Context c, Box box ) {
        if ( box.isAnonymous() ) {
            return;
        }
        if ( BoxLayout.isBlockLayout( box.getElement(), c ) ) {
            super.paintChildren( c, box );
        }
    }

    /** Paint all of the inlines in this box. It recurses through
    * each line, and then each inline in each line, and paints them
    * individually.
    */
    private void paintInlineContext( Context c, Box box ) {
        BlockBox block = (BlockBox)box;
        // translate into local coords
        // account for the origin of the containing box
        c.translate( box.x, box.y );
        // for each line box
        //c.getGraphics().setColor( Color.black );
        for ( int i = 0; i < block.getChildCount(); i++ ) {
            // get the line box
            paintLine( c, (LineBox)block.getChild( i ) );
        }
        // translate back to parent coords
        c.translate( -box.x, -box.y );
    }

    /**  paint all of the inlines on the specified line
    */
    private void paintLine( Context c, LineBox line ) {
        // get x and y
        int lx = line.x;
        int ly = line.y + line.baseline;
        // for each inline box
        for ( int j = 0; j < line.getChildCount(); j++ ) {
            paintInline( c, (InlineBox)line.getChild( j ), lx, ly, line );
        }
        if ( c.debugDrawLineBoxes() ) {
            GraphicsUtil.drawBox( c.getGraphics(), line, Color.blue );
        }
    }

    // Inlines are drawn vertically relative to the baseline of the containing
    // line box, not relative to the origin of the line.
    // They *are* drawn horizontally (x) relative to the origin of the 
    // containing line box though

    private void paintInline( Context c, InlineBox inline, int lx, int ly, LineBox line ) {
        if ( InlineLayout.isReplaced( inline.node ) ) {
            paintReplaced(c,inline,line);
            debugInlines(c,inline,lx,ly);
            return;
        }

        if ( InlineLayout.isFloatedBlock( inline.node, c ) ) {
            paintFloat(c,inline,line);
            debugInlines(c,inline,lx,ly);
            return;
        }

        if ( inline.isBreak() ) { return; }

        handleRelativePre(c,inline);
        paintPadding(c,line,inline);
        c.updateSelection( inline );
        
        // calculate the x and y relative to the baseline of the line (ly) and the
        // left edge of the line (lx)
        int iy = ly + inline.y;
        int ix = lx + inline.x;
        // account for padding
        ix += inline.totalLeftPadding();
        
        paintSelection( c, inline, lx, ly );
        paintText(c, lx, ly, ix, iy, inline);
        debugInlines(c,inline,lx,ly);        
        handleRelativePost(c,inline);
    }
    
    private void handleRelativePre(Context c, InlineBox inline) {
        if ( inline.relative ) {
            c.translate( inline.left, inline.top );
        }
    }
    
    private void handleRelativePost(Context c, InlineBox inline) {
        if ( inline.relative ) {
            c.translate( -inline.left, -inline.top );
        }
    }
    
    private void debugInlines(Context c, InlineBox inline, int lx, int ly) {
        if ( c.debugDrawInlineBoxes() ) {
        GraphicsUtil.draw( c.getGraphics(), new Rectangle( lx + inline.x + 1, ly + inline.y + 1 - inline.height,
                inline.width - 2, inline.height - 2 ), Color.green );
        }
    }
    
    private void paintReplaced(Context c, InlineBox inline, LineBox line) {
        c.translate( line.x, line.y + ( line.baseline - inline.height ) );
        Renderer rend = LayoutFactory.getRenderer( inline.node );
        rend.paint( c, inline );
        c.translate( -line.x, -( line.y + ( line.baseline - inline.height ) ) );
    }
    
    private void paintFloat(Context c, InlineBox inline, LineBox line) {
        Rectangle oe = c.getExtents();
        c.setExtents( new Rectangle( oe.x, 0, oe.width, oe.height ) );
        int xoff = line.x + inline.x;
        int yoff = line.y + ( line.baseline - inline.height ) + inline.y;
        c.translate( xoff, yoff );
        Renderer rend = LayoutFactory.getRenderer( inline.node );
        rend.paint( c, inline.sub_block );
        c.translate( -xoff, -yoff );
        c.setExtents( oe );
    }

    public static void paintSelection( Context c, InlineBox inline, int lx, int ly ) {
        if ( c.inSelection( inline ) ) {
            int dw = inline.width - 2;
            int xoff = 0;
            if ( c.getSelectionEnd() == inline ) {
                dw = c.getSelectionEndX();
            }
            if ( c.getSelectionStart() == inline ) {
                xoff = c.getSelectionStartX();
            }
            c.getGraphics().setColor( new Color( 200, 200, 255 ) );
            ( (Graphics2D)c.getGraphics() ).setPaint( new GradientPaint(
                    0, 0, new Color( 235, 235, 255 ),
                    0, inline.height / 2, new Color( 190, 190, 235 ),
                    true ) );
            //FontMetrics fm = c.getGraphics().getFontMetrics( inline.getFont() );
            //int top = ly + inline.y - fm.getAscent();
            //int height = fm.getAscent() + fm.getDescent();
            LineMetrics lm = c.getTextRenderer().getLineMetrics(c.getGraphics(), inline.getFont(), "Test");
            int top = ly + inline.y - (int)Math.ceil(lm.getAscent());
            int height = (int)Math.ceil(lm.getAscent() + lm.getDescent());
            c.getGraphics().fillRect(
                    lx + inline.x + xoff,
                    top,
                    dw - xoff,
                    height );
        }
    }
    
    
    public void paintText(Context c, int lx, int ly, int ix, int iy, InlineBox inline) {
        String text = inline.getSubstring();
        Graphics g = c.getGraphics();
        //adjust font for current settings
        Font oldfont = c.getGraphics().getFont();
        c.getGraphics().setFont( inline.getFont() );
        Color oldcolor = c.getGraphics().getColor();
        c.getGraphics().setColor( inline.color );
        Font cur_font = c.getGraphics().getFont();
        //LineMetrics lm = cur_font.getLineMetrics( text, ( (Graphics2D)c.getGraphics() ).getFontRenderContext() );
        LineMetrics lm = c.getTextRenderer().getLineMetrics(c.getGraphics(), cur_font, text);

        //u.p("lm descent = " + lm.getDescent());
        iy-= (int)lm.getDescent();
        
        //draw the line
        if ( text != null && text.length() > 0 ) {
            //c.getGraphics().drawString( text, ix, iy );
            c.getTextRenderer().drawString( c.getGraphics(), text, ix, iy );
        }
        c.getGraphics().setColor( oldcolor );
        //draw any text decoration
        int stringWidth = (int)Math.ceil(c.getTextRenderer().
            getLogicalBounds(c.getGraphics(),
             c.getGraphics().getFont(),
             text).getWidth());
 
        if ( inline.underline ) {
            float down = lm.getUnderlineOffset();
            float thick = lm.getUnderlineThickness();
            //g.fillRect( ix, iy + (int)down, g.getFontMetrics().stringWidth( text ), (int)thick );
            g.fillRect( ix, iy + (int)down, stringWidth, (int)thick );
        }

        if ( inline.strikethrough ) {
            float down = lm.getStrikethroughOffset();
            float thick = lm.getStrikethroughThickness();
            g.fillRect( ix, iy + (int)down, stringWidth, (int)thick );
            //g.fillRect( ix, iy + (int)down, g.getFontMetrics().stringWidth( text ), (int)thick );
        }

        if ( inline.overline ) {
            float down = lm.getAscent();
            float thick = lm.getUnderlineThickness();
            //g.fillRect( ix, iy - (int)down, g.getFontMetrics().stringWidth( text ), (int)thick );
            g.fillRect( ix, iy + (int)down, stringWidth, (int)thick );
        }

        if ( c.debugDrawFontMetrics() ) {
            g.setColor(Color.red);
            g.drawLine(ix,iy, ix+inline.width, iy);
            iy += (int)Math.ceil(lm.getDescent());
            //iy += lm.getDescent();
            g.drawLine(ix,iy, ix+inline.width, iy);
            //iy -= lm.getDescent();
            //iy -= lm.getAscent();
            iy -= (int)Math.ceil(lm.getDescent());
            iy -= (int)Math.ceil(lm.getAscent());
            g.drawLine(ix,iy, ix+inline.width, iy);
        }

        // restore the old font
        c.getGraphics().setFont( oldfont );
    }
    
    public void paintPadding(Context c, LineBox line, InlineBox inline) {
        // paint the background
        //u.p("painting the padding: " + inline);
        //int padding_xoff = inline.totalLeftPadding();
        int padding_xoff = 0;
        int padding_yoff = inline.totalTopPadding();
        //u.p("padding yoff = " + padding_yoff);
        c.translate(-padding_xoff, line.baseline-inline.y-inline.height - padding_yoff);
        //u.p("line = " + line);
        //u.p("inline = " + inline);
        int old_width = inline.width;
        int old_height = inline.height;
        //inline.width += inline.totalHorizontalPadding();
        inline.height += inline.totalVerticalPadding();
        paintBackground(c,inline);
        paintBorder(c,inline);
        inline.width = old_width;
        inline.height = old_height;
        c.translate(+padding_xoff, -(line.baseline-inline.y-inline.height) + padding_yoff);
    }
}



