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
package org.xhtmlrenderer.demo.browser;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import org.xhtmlrenderer.css.CSSBank;
import org.xhtmlrenderer.css.bridge.XRStyleReference;
import org.xhtmlrenderer.swing.DOMInspector;
import org.xhtmlrenderer.util.u;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class BrowserMenuBar extends JMenuBar {
    /** Description of the Field */
    BrowserStartup root;

    /** Description of the Field */
    JMenu file;
    /** Description of the Field */
    JMenu edit;
    /** Description of the Field */
    JMenu view;
    /** Description of the Field */
    JMenu go;
    /** Description of the Field */
    JMenuItem view_source;
    /** Description of the Field */
    JMenu debug;
    /** Description of the Field */
    JMenu demos;
    /** Description of the Field */
    public static Logger logger = Logger.getLogger( "app.browser" );

    /**
     * Constructor for the BrowserMenuBar object
     *
     * @param root  PARAM
     */
    public BrowserMenuBar( BrowserStartup root ) {
        this.root = root;
        logger.setLevel( Level.OFF );
    }

    /** Description of the Method */
    public void init() {
        file = new JMenu( "File" );
        file.setMnemonic( 'F' );

        debug = new JMenu( "Debug" );
        debug.setMnemonic( 'B' );

        demos = new JMenu( "Demos" );
        demos.setMnemonic( 'D' );

        edit = new JMenu( "Edit" );
        edit.setMnemonic( 'E' );

        view = new JMenu( "View" );
        view.setMnemonic( 'V' );

        view_source = new JMenuItem( "Page Source" );
        view_source.setEnabled( false );
        view.add( root.actions.stop );
        view.add( root.actions.refresh );
        view.add( root.actions.reload );

        go = new JMenu( "Go" );
        go.setMnemonic( 'G' );
    }


    /** Description of the Method */
    public void createLayout() {
        file.add( root.actions.open_file );
        file.add( root.actions.quit );
        add( file );

        edit.add( root.actions.cut );
        edit.add( root.actions.copy );
        edit.add( root.actions.paste );
        add( edit );

        view.add( view_source );
        add( view );

        go.add( root.actions.forward );
        go.add( root.actions.backward );

        add( go );

        // CLEAN
        demos.add( new LoadAction( "Inheritance", "demo:demos/inherit.xhtml" ) );
        demos.add( new LoadAction( "Borders", "demo:demos/border.xhtml" ) );
        demos.add( new LoadAction( "Backgrounds", "demo:demos/background.xhtml" ) );
        demos.add( new LoadAction( "Paragraph", "demo:demos/paragraph.xhtml" ) );
        demos.add( new LoadAction( "Line Breaking", "demo:demos/breaking.xhtml" ) );
        demos.add( new LoadAction( "Forms", "demo:demos/forms.xhtml" ) );
        demos.add( new LoadAction( "Headers", "demo:demos/header.xhtml" ) );
        demos.add( new LoadAction( "Nested Divs", "demo:demos/nested.xhtml" ) );
        demos.add( new LoadAction( "Selectors", "demo:demos/selectors.xhtml" ) );
        demos.add( new LoadAction( "Images", "demo:demos/image.xhtml" ) );
        demos.add( new LoadAction( "Lists", "demo:demos/list.xhtml" ) );
        //demos.add(new LoadAction("Tables","demo:demos/table.xhtml"));
        try {
            //demos.add(new LoadAction("File Listing (Win)","file:///c:"));
            //demos.add(new LoadAction("File Listing (Unix)","file:///"));
        } catch ( Exception ex ) {
            u.p( ex );
        }

        add( demos );

        JMenu debugShow = new JMenu( "Show" );
        debug.add( debugShow );
        debugShow.setMnemonic( 'S' );

        debugShow.add( new JCheckBoxMenuItem( new BoxOutlinesAction() ) );
        debugShow.add( new JCheckBoxMenuItem( new LineBoxOutlinesAction() ) );
        debugShow.add( new JCheckBoxMenuItem( new InlineBoxesAction() ) );

        debug.add( new ShowDOMInspectorAction() );
        debug.add(
                    new AbstractAction( "Validation Console" ) {
                        public void actionPerformed( ActionEvent evt ) {
                            if ( root.validation_console == null ) {
                                root.validation_console = new JFrame( "Validation Console" );
                                JFrame frame = root.validation_console;
                                JTextArea jta = new JTextArea();
                                root.error_handler.setTextArea( jta );

                                frame.getContentPane().setLayout( new BorderLayout() );
                                frame.getContentPane().add( new JScrollPane( jta ), "Center" );
                                JButton close = new JButton( "Close" );
                                frame.getContentPane().add( close, "South" );
                                close.addActionListener(
                                            new ActionListener() {
                                                public void actionPerformed( ActionEvent evt ) {
                                                    root.validation_console.setVisible( false );
                                                }
                                            } );

                                frame.pack();
                                frame.setSize( 200, 400 );
                            }
                            root.validation_console.setVisible( true );
                        }
                    } );

        add( debug );
    }

    /** Description of the Method */
    public void createActions() {
        SelectionMouseListener ma = new SelectionMouseListener();
        root.panel.view.addMouseListener( ma );
        root.panel.view.addMouseMotionListener( ma );
        logger.info( "added a mouse motion listener: " + ma );
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
                if ( root.panel.view.c.css instanceof CSSBank ) {
                    inspector = new DOMInspector( root.panel.view.doc );
                } else {
                    inspector = new DOMInspector( root.panel.view.doc, root.panel.view.c, (XRStyleReference)root.panel.view.c.css );
                }

                inspectorFrame.getContentPane().add( inspector );

                inspectorFrame.pack();
                inspectorFrame.setSize( 500, 600 );
                inspectorFrame.show();
            } else {
                if ( root.panel.view.c.css instanceof CSSBank ) {
                    inspector.setForDocument( root.panel.view.doc );
                } else {
                    inspector.setForDocument( root.panel.view.doc, root.panel.view.c, (XRStyleReference)root.panel.view.c.css );
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
            root.panel.view.c.debug_draw_boxes = !root.panel.view.c.debug_draw_boxes;
            root.panel.view.repaint();
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
            root.panel.view.c.debug_draw_line_boxes = !root.panel.view.c.debug_draw_line_boxes;
            root.panel.view.repaint();
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
            root.panel.view.c.debug_draw_inline_boxes = !root.panel.view.c.debug_draw_inline_boxes;
            root.panel.view.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    class LoadAction extends AbstractAction {
        /** Description of the Field */
        protected String url;

        /**
         * Constructor for the LoadAction object
         *
         * @param name  PARAM
         * @param url   PARAM
         */
        public LoadAction( String name, String url ) {
            super( name );
            this.url = url;
        }

        /**
         * Description of the Method
         *
         * @param evt  PARAM
         */
        public void actionPerformed( ActionEvent evt ) {
            try {
                root.panel.loadPage( url );
            } catch ( Exception ex ) {
                u.p( ex );
            }
        }

    }

}


/**
 * Description of the Class
 *
 * @author   empty
 */
class EmptyAction extends AbstractAction {

    /**
     * Constructor for the EmptyAction object
     *
     * @param name   PARAM
     * @param accel  PARAM
     */
    public EmptyAction( String name, int accel ) {
        this( name );
        putValue( Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke( accel,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ) );
    }

    /**
     * Constructor for the EmptyAction object
     *
     * @param name  PARAM
     */
    public EmptyAction( String name ) {
        super( name );
    }

    /**
     * Description of the Method
     *
     * @param evt  PARAM
     */
    public void actionPerformed( ActionEvent evt ) { }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2004/10/23 14:38:58  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

