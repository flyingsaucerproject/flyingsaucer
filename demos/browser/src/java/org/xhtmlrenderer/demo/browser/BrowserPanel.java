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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.*;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.swing.HTMLPanel;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.u;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class BrowserPanel extends JPanel implements DocumentListener {

    /** Description of the Field */
    JButton forward;
    /** Description of the Field */
    JButton backward;
    /** Description of the Field */
    JButton stop;
    /** Description of the Field */
    JButton reload;
    /** Description of the Field */
    JTextField url;
    /** Description of the Field */
    JLabel status;
    /** Description of the Field */
    XHTMLPanel view;
    /** Description of the Field */
    JScrollPane scroll;
    /** Description of the Field */
    BrowserStartup root;
    /** Description of the Field */
    BrowserPanelListener listener;

    /** Description of the Field */
    String current_url = null;
    /** Description of the Field */
    public static Logger logger = Logger.getLogger( "app.browser" );

    /**
     * Constructor for the BrowserPanel object
     *
     * @param root      PARAM
     * @param listener  PARAM
     */
    public BrowserPanel( BrowserStartup root, BrowserPanelListener listener ) {
        this.root = root;
        this.listener = listener;
    }


    /** Description of the Method */
    public void init() {
        forward = new JButton();
        backward = new JButton( "Back" );
        stop = new JButton( "Stop" );
        reload = new JButton( "Reload" );
        url = new JTextField();
        view = new XHTMLPanel();
        RenderingContext rc = view.getRenderingContext();
        try {
            rc.setFontMapping("Fuzz",Font.createFont(Font.TRUETYPE_FONT,
                new DemoMarker().getClass().getResourceAsStream("/demos/fonts/fuzz.ttf")));
        } catch (Exception ex) {
            u.p(ex);
        }
        view.setErrorHandler( root.error_handler );
        status = new JLabel( "Status" );

        int text_width = 200;
        view.setPreferredSize( new Dimension( text_width, text_width ) );
        scroll = new JScrollPane( view );
        scroll.setVerticalScrollBarPolicy( scroll.VERTICAL_SCROLLBAR_ALWAYS );
        scroll.setHorizontalScrollBarPolicy( scroll.HORIZONTAL_SCROLLBAR_ALWAYS );
        scroll.setPreferredSize( new Dimension( text_width, text_width ) );
        scroll.getVerticalScrollBar().setBlockIncrement( 100 );
        scroll.getVerticalScrollBar().setUnitIncrement( 15 );
    }

    /** Description of the Method */
    public void createLayout() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout( gbl );

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = c.weighty = 0.0;
        gbl.setConstraints( backward, c );
        add( backward );

        c.gridx++;
        gbl.setConstraints( forward, c );
        add( forward );

        c.gridx++;
        gbl.setConstraints( stop, c );
        add( stop );

        c.gridx++;
        gbl.setConstraints( reload, c );
        add( reload );

        c.gridx++;
        c.fill = c.HORIZONTAL;
        c.weightx = 10.0;
        gbl.setConstraints( url, c );
        add( url );

        c.gridx = 0;
        c.gridy++;
        c.fill = c.BOTH;
        c.gridwidth = 5;
        c.weightx = c.weighty = 10.0;
        gbl.setConstraints( scroll, c );
        add( scroll );

        c.gridx = 0;
        c.gridy++;
        c.fill = c.HORIZONTAL;
        c.weighty = 0.1;
        gbl.setConstraints( status, c );
        add( status );

    }

    /** Description of the Method */
    public void createActions() {
        backward.setAction( root.actions.backward );
        forward.setAction( root.actions.forward );
        reload.setAction( root.actions.reload );
        url.setAction( root.actions.load );
        updateButtons();
        view.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).
                put( KeyStroke.getKeyStroke( KeyEvent.VK_PAGE_DOWN, 0 ), "pagedown" );
        view.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).
                put( KeyStroke.getKeyStroke( KeyEvent.VK_PAGE_UP, 0 ), "pageup" );
        view.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).
                put( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 ), "down" );
        view.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).
                put( KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 ), "up" );

        view.getActionMap().put( "pagedown",
                    new AbstractAction() {
                        public void actionPerformed( ActionEvent evt ) {
                            JScrollBar sb = scroll.getVerticalScrollBar();
                            sb.getModel().setValue( sb.getModel().getValue() + sb.getBlockIncrement() );
                        }
                    } );
        view.getActionMap().put( "pageup",
                    new AbstractAction() {
                        public void actionPerformed( ActionEvent evt ) {
                            JScrollBar sb = scroll.getVerticalScrollBar();
                            sb.getModel().setValue( sb.getModel().getValue() - sb.getBlockIncrement() );
                        }
                    } );
        view.getActionMap().put( "down",
                    new AbstractAction() {
                        public void actionPerformed( ActionEvent evt ) {
                            JScrollBar sb = scroll.getVerticalScrollBar();
                            sb.getModel().setValue( sb.getModel().getValue() + sb.getUnitIncrement() );
                        }
                    } );
        view.getActionMap().put( "up",
                    new AbstractAction() {
                        public void actionPerformed( ActionEvent evt ) {
                            JScrollBar sb = scroll.getVerticalScrollBar();
                            sb.getModel().setValue( sb.getModel().getValue() - sb.getUnitIncrement() );
                        }
                    } );

    }


    /** Description of the Method */
    public void goForward() {
        root.history.goNext();
        view.setDocument( root.history.getCurrentDocument(), root.history.getCurrentURL() );
        //root.history.dumpHistory();
        updateButtons();
    }

    /**
     * Description of the Method
     *
     * @exception Exception  Throws
     */
    public void goBack()
        throws Exception {
        root.history.goPrevious();
        view.setDocument( root.history.getCurrentDocument(), root.history.getCurrentURL() );
        //root.history.dumpHistory();
        updateButtons();
    }

    /**
     * Description of the Method
     *
     * @exception Exception  Throws
     */
    public void reloadPage()
        throws Exception {
        logger.info( "Reloading Page: " );
        if ( current_url != null ) {
            loadPage( current_url );
        }
    }

    /**
     * Description of the Method
     *
     * @param doc            PARAM
     * @param url            PARAM
     * @exception Exception  Throws
     */
    public void loadPage( Document doc, URL url )
        throws Exception {
        view.setDocument( doc, url );
        view.addDocumentListener( this );
        root.history.goNewDocument( doc );
        updateButtons();
    }

    /**
     * Description of the Method
     *
     * @param url_text       PARAM
     * @exception Exception  Throws
     */
    public void loadPage( String url_text )
        throws Exception {
        logger.info( "Loading Page: " + url_text );
        current_url = url_text;

        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setValidating( true );
        DocumentBuilder builder = fact.newDocumentBuilder();
        builder.setErrorHandler( root.error_handler );
        Document doc = null;

        URL ref = null;

        if ( url_text.startsWith( "demo:" ) ) {
            DemoMarker marker = new DemoMarker();
            //u.p("marker = " + marker);
            String short_url = url_text.substring( 5 );
            //u.p("sub = " + short_url);
            if ( !short_url.startsWith( "/" ) ) {
                short_url = "/" + short_url;
            }
            doc = builder.parse( marker.getClass().getResourceAsStream( short_url ) );
            ref = marker.getClass().getResource( short_url );
        } else if ( url_text.startsWith( "http" ) ) {
            doc = builder.parse( url_text );
            ref = new File( url_text ).toURL();
        } else if ( url_text.startsWith( "file://" ) ) {
            File file = new File( new URI( url_text ) );
            if ( file.isDirectory() ) {
                doc = new DirectoryLister().list( file );
                ref = file.toURL();
            } else {
                doc = builder.parse( file );
                ref = file.toURL();
            }

        } else {
            doc = builder.parse( url_text );
            ref = new File( url_text ).toURL();
        }
        loadPage( doc, ref );

        setStatus( "Successfully loaded: " + url_text );
        if ( listener != null ) {
            listener.pageLoadSuccess( url_text, view.getDocumentTitle() );
        }
    }


    /** Description of the Method */
    public void documentLoaded() {
        //u.p("got a document loaded event");
        setupSubmitActions();
    }


    /**
     * Sets the status attribute of the BrowserPanel object
     *
     * @param txt  The new status value
     */
    public void setStatus( String txt ) {
        status.setText( txt );
    }

    /** Description of the Method */
    public void setupSubmitActions() {
        //u.p("setup submit actions");
        Context cx = view.getContext();
        Map forms = cx.getForms();
        //u.p("forms = " + forms);
        Iterator form_it = forms.keySet().iterator();
        while ( form_it.hasNext() ) {
            final String form_name = (String)form_it.next();
            Map form = (Map)forms.get( form_name );
            //u.p("got form: " + form_name);
            Iterator fields = form.keySet().iterator();
            while ( fields.hasNext() ) {
                String field_name = (String)fields.next();
                List field_list = (List)form.get( field_name );
                //u.p("got field set: " + field_name);

                ButtonGroup bg = new ButtonGroup();
                for ( int i = 0; i < field_list.size(); i++ ) {
                    Context.FormComponent comp = (Context.FormComponent)field_list.get( i );
                    //u.p("got component: " + comp);

                    // bind radio buttons together
                    if ( comp.component instanceof JRadioButton ) {
                        bg.add( (JRadioButton)comp.component );
                    }

                    // add reset action listeners
                    if ( comp.component instanceof JButton ) {
                        //u.p("it's a jbutton");
                        if ( comp.element.getAttribute( "type" ).equals( "reset" ) ) {
                            ( (JButton)comp.component ).addActionListener(
                                        new ActionListener() {
                                            public void actionPerformed( ActionEvent evt ) {
                                                u.p( "reset button hit" );

                                                Context ctx = view.getContext();
                                                Iterator fields = ctx.getInputFieldComponents( form_name );
                                                while ( fields.hasNext() ) {
                                                    List field_list = (List)fields.next();
                                                    for ( int i = 0; i < field_list.size(); i++ ) {
                                                        Context.FormComponent comp = (Context.FormComponent)field_list.get( i );
                                                        comp.reset();
                                                    }
                                                }

                                            }
                                        } );
                        }
                        if ( comp.element.getAttribute( "type" ).equals( "submit" ) ) {
                            ( (JButton)comp.component ).addActionListener(
                                        new ActionListener() {
                                            public void actionPerformed( ActionEvent evt ) {
                                                u.p( "submit button hit" );
                                                StringBuffer query = new StringBuffer();
                                                query.append( "?" );
                                                Context ctx = view.getContext();
                                                Iterator fields = ctx.getInputFieldComponents( form_name );
                                                while ( fields.hasNext() ) {
                                                    List field = (List)fields.next();
                                                    for ( int i = 0; i < field.size(); i++ ) {
                                                        Context.FormComponent comp = (Context.FormComponent)field.get( i );
                                                        if ( comp.element.hasAttribute( "value" ) ) {
                                                            query.append( comp.element.getAttribute( "name" ) );
                                                            query.append( "=" );
                                                            query.append( comp.element.getAttribute( "value" ) );
                                                            query.append( "&" );
                                                        }
                                                    }
                                                }
                                                String url = ctx.getFormAction( form_name ) + query.toString();
                                                u.p( "going to load: " + url );
                                                try {
                                                    loadPage( url );
                                                } catch ( Exception ex ) {
                                                    u.p( ex );
                                                }
                                            }
                                        } );
                        }
                    }
                }
            }
        }
    }


    /** Description of the Method */
    protected void updateButtons() {
        if ( root.history.hasPrevious() ) {
            root.actions.backward.setEnabled( true );
        } else {
            root.actions.backward.setEnabled( false );
        }
        if ( root.history.hasNext() ) {
            root.actions.forward.setEnabled( true );
        } else {
            root.actions.forward.setEnabled( false );
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.6  2004/11/12 20:43:29  joshy
 * added demo of custom font
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/12 02:23:56  joshy
 * added new APIs for rendering context, xhtmlpanel, and graphics2drenderer.
 * initial support for font mapping additions
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/23 14:38:58  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

