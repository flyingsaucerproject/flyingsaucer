/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import org.w3c.dom.Element;
import java.util.Map;
import org.w3c.dom.Node;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.simple.*;
import org.xhtmlrenderer.extend.*;
import org.xhtmlrenderer.layout.LayoutFactory;
import org.xhtmlrenderer.layout.Layout;
import org.xhtmlrenderer.layout.InlineLayout;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.u;
import java.io.*;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class HTMLTest extends JFrame {
    /** Description of the Field */
    private final XHTMLPanel panel;
    /** Description of the Field */
    //public final static int text_width = 600;
    /** Description of the Field */
    private final static String BASE_TITLE = "Flying Saucer";

    /**
     * Constructor for the HTMLTest object
     *
     * @param args           PARAM
     * @exception Exception  Throws
     */
    public HTMLTest( String[] args )
        throws Exception {
        super( BASE_TITLE );
        panel = new XHTMLPanel();
        int width = 360;
        int height = 500;
        panel.setPreferredSize( new Dimension( width, height ) );
        JScrollPane scroll = new JScrollPane( panel );
        scroll.setVerticalScrollBarPolicy( scroll.VERTICAL_SCROLLBAR_ALWAYS );
        scroll.setHorizontalScrollBarPolicy( scroll.HORIZONTAL_SCROLLBAR_ALWAYS );
        scroll.setPreferredSize( new Dimension( width, height ) );
        panel.addMouseListener( new LinkListener( panel ) );
        HoverListener hov = new HoverListener(panel);
        panel.addMouseListener(hov);
        panel.addMouseMotionListener(hov);

        if ( args.length > 0 ) {
            loadDocument( args[0] );
        }

        getContentPane().add( "Center", scroll );

        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu( "File" );
        mb.add( file );
        file.setMnemonic( 'F' );
        file.add( new QuitAction() );

        JMenu view = new JMenu( "View" );
        mb.add( view );
        view.setMnemonic( 'V' );
        view.add( new RefreshPageAction() );
        view.add( new ReloadPageAction() );

        /*
        JMenu test = new JMenu( "Test" );
        mb.add( test );
        test.setMnemonic( 'T' );

        String demoRootDir = "demos/browser/xhtml";
        addFileLoadAction( test, "One Liner", demoRootDir + "/one-line.xhtml" );
        addFileLoadAction( test, "Background Colors/Images", demoRootDir + "/background.xhtml" );
        addFileLoadAction( test, "Borders", demoRootDir + "/border.xhtml" );
        addFileLoadAction( test, "Box Sizing", demoRootDir + "/box-sizing.xhtml" );
        addFileLoadAction( test, "Mixed Test (1)", demoRootDir + "/content.xhtml" );
        addFileLoadAction( test, "Line Breaking", demoRootDir + "/breaking.xhtml" );
        addFileLoadAction( test, "Headers", demoRootDir + "/header.xhtml" );
        addFileLoadAction( test, "Inline Image", demoRootDir + "/image.xhtml" );
        addFileLoadAction( test, "List ", demoRootDir + "/list.xhtml" );
        addFileLoadAction( test, "Nesting", demoRootDir + "/nested.xhtml" );
        addFileLoadAction( test, "General Styled Text", demoRootDir + "/paragraph.xhtml" );
        addFileLoadAction( test, "CSS Selectors", demoRootDir + "/selectors.xhtml" );
        addFileLoadAction( test, "Table", demoRootDir + "/table.xhtml" );
        addFileLoadAction( test, "Text Alignment", demoRootDir + "/text-alignment.xhtml" );
        addFileLoadAction( test, "Whitespace Handling", demoRootDir + "/whitespace.xhtml" );
        addFileLoadAction( test, "iTunes Email", demoRootDir + "/itunes/itunes1.xhtml" );
        addFileLoadAction( test, "Follow Links", demoRootDir + "/link.xhtml" );
        addFileLoadAction( test, "Hamlet (slow!)", demoRootDir + "/hamlet.xhtml" );
        addFileLoadAction( test, "extended", demoRootDir + "/extended.xhtml" );
        addFileLoadAction( test, "XML-like", demoRootDir + "/xml.xhtml" );
        addFileLoadAction( test, "XML", demoRootDir + "/xml.xml" );
        addFileLoadAction( test, "pseudo-element", "/home/tobe/Projekt/xhtmlrenderer/test.xhtml" );
        */
        JMenu debug = new JMenu( "Debug" );
        mb.add( debug );
        debug.setMnemonic( 'D' );

        JMenu debugShow = new JMenu( "Show" );
        debug.add( debugShow );
        debugShow.setMnemonic( 'S' );

        debugShow.add( new JCheckBoxMenuItem( new BoxOutlinesAction() ) );
        debugShow.add( new JCheckBoxMenuItem( new LineBoxOutlinesAction() ) );
        debugShow.add( new JCheckBoxMenuItem( new InlineBoxesAction() ) );
        debugShow.add( new JCheckBoxMenuItem( new FontMetricsAction() ) );
        
        /*
        JMenu anti = new JMenu("Anti Aliasing");
        anti.add( new JCheckBoxMenuItem( new AntiAliasedAction("None", TextRenderer.NONE) ) );
        anti.add( new JCheckBoxMenuItem( new AntiAliasedAction("Low (Default)", TextRenderer.LOW) ) );
        anti.add( new JCheckBoxMenuItem( new AntiAliasedAction("Medium", TextRenderer.MEDIUM) ) );
        anti.add( new JCheckBoxMenuItem( new AntiAliasedAction("Highest", TextRenderer.HIGH) ) );
        debug.add( anti );
        */
        debug.add( new ShowDOMInspectorAction() );
/*
        debug.add(
                    new AbstractAction( "Print Box Tree" ) {
                        public void actionPerformed( ActionEvent evt ) {
                            panel.printTree();
                        }
                    } );
*/
        setJMenuBar( mb );
    }

    /**
     * Adds a feature to the FileLoadAction attribute of the HTMLTest object
     *
     * @param menu     The feature to be added to the FileLoadAction attribute
     * @param display  The feature to be added to the FileLoadAction attribute
     * @param file     The feature to be added to the FileLoadAction attribute
     */
    public void addFileLoadAction( JMenu menu, String display, final String file ) {
        menu.add(
                    new AbstractAction( display ) {
                        public void actionPerformed( ActionEvent evt ) {
                            loadDocument( file );
                        }
                    } );
    }

    /**
     * Description of the Method
     *
     * @param file  PARAM
     */
    private void loadDocument( final String file ) {
        SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            try {
                                long st = System.currentTimeMillis();

                                panel.setDocument( new File(file).toURL() );

                                long el = System.currentTimeMillis() - st;
                                XRLog.general( "loadDocument(" + file + ") in " + el + "ms, render may take longer" );
                                HTMLTest.this.setTitle( BASE_TITLE + "-  " +
                                        panel.getDocumentTitle() + "  " +
                                        "(" + file + ")" );
                            } catch ( Exception ex ) {
                                u.p( ex );
                            }
                            panel.repaint();
                        }
                    } );
    }

    /**
     * The main program for the HTMLTest class
     *
     * @param args           The command line arguments
     * @exception Exception  Throws
     */
    public static void main( String[] args )
        throws Exception {

        final JFrame frame = new HTMLTest( args );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.pack();
        //frame.setSize( text_width, 300 );
        frame.show();
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    class QuitAction extends AbstractAction {
        /** Constructor for the QuitAction object */
        QuitAction() {
            super( "Quit" );
            putValue( MNEMONIC_KEY, new Integer( KeyEvent.VK_Q ) );
        }

        /**
         * Description of the Method
         *
         * @param evt  PARAM
         */
        public void actionPerformed( ActionEvent evt ) {
            System.exit( 0 );
        }
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    class BoxOutlinesAction extends AbstractAction {
        /** Constructor for the BoxOutlinesAction object */
        BoxOutlinesAction() {
            super( "Show Box Outlines" );
            putValue( MNEMONIC_KEY, new Integer( KeyEvent.VK_B ) );
        }

        /**
         * Description of the Method
         *
         * @param evt  PARAM
         */
        public void actionPerformed( ActionEvent evt ) {
            panel.getRenderingContext().getContext().debug_draw_boxes = !panel.getRenderingContext().getContext().debug_draw_boxes;
            panel.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    class LineBoxOutlinesAction extends AbstractAction {
        /** Constructor for the LineBoxOutlinesAction object */
        LineBoxOutlinesAction() {
            super( "Show Line Box Outlines" );
            putValue( MNEMONIC_KEY, new Integer( KeyEvent.VK_L ) );
        }

        /**
         * Description of the Method
         *
         * @param evt  PARAM
         */
        public void actionPerformed( ActionEvent evt ) {
            panel.getRenderingContext().getContext().debug_draw_line_boxes = !panel.getRenderingContext().getContext().debug_draw_line_boxes;
            panel.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    class InlineBoxesAction extends AbstractAction {
        /** Constructor for the InlineBoxesAction object */
        InlineBoxesAction() {
            super( "Show Inline Boxes" );
            putValue( MNEMONIC_KEY, new Integer( KeyEvent.VK_I ) );
        }

        /**
         * Description of the Method
         *
         * @param evt  PARAM
         */
        public void actionPerformed( ActionEvent evt ) {
            panel.getRenderingContext().getContext().debug_draw_inline_boxes = !panel.getRenderingContext().getContext().debug_draw_inline_boxes;
            panel.repaint();
        }
    }

    class FontMetricsAction extends AbstractAction {
        /** Constructor for the InlineBoxesAction object */
        FontMetricsAction() {
            super( "Show Font Metrics" );
            putValue( MNEMONIC_KEY, new Integer( KeyEvent.VK_F ) );
        }

        /**
         * Description of the Method
         *
         * @param evt  PARAM
         */
        public void actionPerformed( ActionEvent evt ) {
            panel.getRenderingContext().getContext().debug_draw_font_metrics = !panel.getRenderingContext().getContext().debug_draw_font_metrics;
            panel.repaint();
        }
    }
    
    class AntiAliasedAction extends AbstractAction {
        int hint;
        AntiAliasedAction(String text, int hint) {
            super( text );
            this.hint = hint;
        }

        public void actionPerformed( ActionEvent evt ) {
            panel.getRenderingContext().getTextRenderer().setSmoothingLevel(hint);
            panel.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    class ShowDOMInspectorAction extends AbstractAction {
        /** Description of the Field */
        private DOMInspector inspector;
        /** Description of the Field */
        private JFrame inspectorFrame;

        /** Constructor for the ShowDOMInspectorAction object */
        ShowDOMInspectorAction() {
            super( "DOM Tree Inspector" );
            putValue( MNEMONIC_KEY, new Integer( KeyEvent.VK_D ) );
        }

        /**
         * Description of the Method
         *
         * @param evt  PARAM
         */
        public void actionPerformed( ActionEvent evt ) {
            if ( inspectorFrame == null ) {
                inspectorFrame = new JFrame( "DOM Tree Inspector" );
            }
            if ( inspector == null ) {
                // inspectorFrame = new JFrame("DOM Tree Inspector");

                inspector = new DOMInspector( panel.doc, panel.getRenderingContext().getContext(), panel.getRenderingContext().getContext().css );

                inspectorFrame.getContentPane().add( inspector );

                inspectorFrame.pack();
                inspectorFrame.setSize( 400, 600 );
                inspectorFrame.show();
            } else {
                inspector.setForDocument( panel.doc, panel.getRenderingContext().getContext(), panel.getRenderingContext().getContext().css );
            }
            inspectorFrame.show();
        }
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    class RefreshPageAction extends AbstractAction {
        /** Constructor for the RefreshPageAction object */
        RefreshPageAction() {
            super( "Refresh Page" );
            putValue( MNEMONIC_KEY, new Integer( KeyEvent.VK_R ) );
            putValue( ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke( "F5" ) );
        }

        /**
         * Description of the Method
         *
         * @param evt  PARAM
         */
        public void actionPerformed( ActionEvent evt ) {
            // TODO
            System.out.println( "Refresh Page triggered" );
        }
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    class ReloadPageAction extends AbstractAction {
        /** Constructor for the ReloadPageAction object */
        ReloadPageAction() {
            super( "Reload Page" );
            putValue( MNEMONIC_KEY, new Integer( KeyEvent.VK_P ) );
            putValue( ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke( KeyEvent.VK_F5,
                    ActionEvent.CTRL_MASK ) );
        }

        /**
         * Description of the Method
         *
         * @param evt  PARAM
         */
        public void actionPerformed( ActionEvent evt ) {
            // TODO
            System.out.println( "Reload Page triggered" );
        }
    }
}


/**
 * Description of the Class
 *
 * @author   empty
 */

/*
 * $Id$
 *
 * $Log$
 * Revision 1.19  2004/11/15 14:33:10  joshy
 * fixed line breaking bug with certain kinds of unbreakable lines
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.18  2004/11/14 21:33:49  joshy
 * new font rendering interface support
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.17  2004/11/12 02:23:59  joshy
 * added new APIs for rendering context, xhtmlpanel, and graphics2drenderer.
 * initial support for font mapping additions
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/11/10 17:28:55  joshy
 * initial support for anti-aliased text w/ minium
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/11/10 04:46:12  tobega
 * no message
 *
 * Revision 1.14  2004/11/09 16:16:08  joshy
 * moved listeners into their own classes
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/11/09 15:53:51  joshy
 * initial support for hover (currently disabled)
 * moved justification code into it's own class in a new subpackage for inline
 * layout (because it's so blooming complicated)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2004/11/09 00:36:09  joshy
 * fixed more text alignment
 * added menu item to show font metrics
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/08 08:22:17  tobega
 * Added support for pseudo-elements
 *
 * Revision 1.10  2004/11/07 01:17:56  tobega
 * DOMInspector now works with any StyleReference
 *
 * Revision 1.9  2004/11/06 22:15:15  tobega
 * Quick fix to run old DOMInspector
 *
 * Revision 1.8  2004/10/28 13:46:33  joshy
 * removed dead code
 * moved code about specific elements to the layout factory (link and br)
 * fixed form rendering bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/10/23 13:51:54  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

