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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.css.CSSBank;
import org.xhtmlrenderer.css.bridge.XRStyleReference;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.layout.LayoutFactory;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.u;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class HTMLTest extends JFrame {
    /** Description of the Field */
    private final HTMLPanel panel = new HTMLPanel();
    /** Description of the Field */
    public final static int text_width = 600;
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
        panel.setPreferredSize( new Dimension( text_width, text_width ) );
        JScrollPane scroll = new JScrollPane( panel );
        scroll.setVerticalScrollBarPolicy( scroll.VERTICAL_SCROLLBAR_ALWAYS );
        scroll.setHorizontalScrollBarPolicy( scroll.HORIZONTAL_SCROLLBAR_ALWAYS );
        scroll.setPreferredSize( new Dimension( text_width, text_width ) );
        panel.addMouseListener( new ClickMouseListener( panel ) );

        if ( args.length > 0 ) {
            // CLEAN
            // File file = new File(args[0]);
            // panel.setDocument(x.loadDocument(args[0]),file.toURL());
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

        JMenu debug = new JMenu( "Debug" );
        mb.add( debug );
        debug.setMnemonic( 'D' );

        JMenu debugShow = new JMenu( "Show" );
        debug.add( debugShow );
        debugShow.setMnemonic( 'S' );

        debugShow.add( new JCheckBoxMenuItem( new BoxOutlinesAction() ) );
        debugShow.add( new JCheckBoxMenuItem( new LineBoxOutlinesAction() ) );
        debugShow.add( new JCheckBoxMenuItem( new InlineBoxesAction() ) );

        debug.add( new ShowDOMInspectorAction() );

        debug.add(
                    new AbstractAction( "Print Box Tree" ) {
                        public void actionPerformed( ActionEvent evt ) {
                            panel.printTree();
                        }
                    } );

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

                                panel.setDocument( file );

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
        frame.setSize( text_width, 300 );
        frame.show();
        /*
         * new Thread(new Runnable() {
         * public void run() {
         * for(int i=0; i<100; i=i+1) {
         * u.sleep(100);
         * frame.resize(131-i,200);
         * System.out.println("blah = " + (131-i));
         * frame.validate();
         * frame.invalidate();
         * frame.repaint();
         * }
         * }
         * }).start();
         */
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
            panel.c.debug_draw_boxes = !panel.c.debug_draw_boxes;
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
            panel.c.debug_draw_line_boxes = !panel.c.debug_draw_line_boxes;
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
            panel.c.debug_draw_inline_boxes = !panel.c.debug_draw_inline_boxes;
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

                // CLEAN: this is more complicated than it needs to be
                // DOM Tree Inspector needs to work with either CSSBank
                // or XRStyleReference--implementations are not perfectly
                // so we have different constructors
                if ( panel.c.css instanceof CSSBank ) {
                    inspector = new DOMInspector( panel.doc );
                } else {
                    inspector = new DOMInspector( panel.doc, panel.c, panel.c.css );
                }

                inspectorFrame.getContentPane().add( inspector );

                inspectorFrame.pack();
                inspectorFrame.setSize( text_width, 600 );
                inspectorFrame.show();
            } else {
                if ( panel.c.css instanceof CSSBank ) {
                    inspector.setForDocument( panel.doc );
                } else {
                    inspector.setForDocument( panel.doc, panel.c, panel.c.css );
                }
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
class ClickMouseListener extends MouseAdapter {

    /** Description of the Field */
    HTMLPanel panel;

    /**
     * Constructor for the ClickMouseListener object
     *
     * @param panel  PARAM
     */
    public ClickMouseListener( HTMLPanel panel ) {
        this.panel = panel;
    }

    /**
     * Description of the Method
     *
     * @param evt  PARAM
     */
    public void mousePressed( MouseEvent evt ) {
        Box box = panel.findBox( evt.getX(), evt.getY() );
        if ( box == null ) {
            return;
        }
        u.p( "pressed " + box );
        if ( box.node != null ) {
            Node node = box.node;
            if ( node.getNodeType() == node.TEXT_NODE ) {
                node = node.getParentNode();
            }

            if ( LayoutFactory.isLink(node)) {
                u.p( "clicked on a link" );
                box.clicked = true;
                box.color = new Color( 255, 255, 0 );
                panel.repaint();
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param evt  PARAM
     */
    public void mouseReleased( MouseEvent evt ) {
        Box box = panel.findBox( evt.getX(), evt.getY() );
        if ( box == null ) {
            return;
        }
        u.p( "pressed " + box );
        if ( box.node != null ) {
            Node node = box.node;
            if ( node.getNodeType() == node.TEXT_NODE ) {
                node = node.getParentNode();
            }

            if ( LayoutFactory.isLink(node) ) {
                u.p( "clicked on a link" );
                box.clicked = true;
                box.color = new Color( 255, 0, 0 );
                panel.repaint();
                followLink( (Element)node );
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param elem  PARAM
     */
    private void followLink( final Element elem ) {
        try {
            if ( elem.hasAttribute( "href" ) ) {
                panel.setDocumentRelative( elem.getAttribute( "href" ) );
            }
        } catch ( Exception ex ) {
            u.p( ex );
        }
    }

}

/*
 * $Id$
 *
 * $Log$
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

