/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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

import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;


/**
 * Description of the Class
 *
 * @author empty
 */
public class HTMLTest extends JFrame {
    private static final long serialVersionUID = 1L;

    /**
     * Description of the Field
     */
    private final XHTMLPanel panel;
    /** Description of the Field */
    //public final static int text_width = 600;
    /**
     * Description of the Field
     */
    private final static String BASE_TITLE = "Flying Saucer";

    /**
     * Constructor for the HTMLTest object
     *
     * @param args PARAM
     */
    public HTMLTest(String[] args) {
        super(BASE_TITLE);
        panel = new XHTMLPanel();
        int width = 360;
        int height = 500;
        panel.setPreferredSize(new Dimension(width, height));
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new Dimension(width, height));

        panel.addMouseTrackingListener(new LinkListener());

        if (args.length > 0) {
            loadDocument(args[0]);
        }

        getContentPane().add("Center", scroll);

        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        mb.add(file);
        file.setMnemonic('F');
        file.add(new QuitAction());

        JMenu view = new JMenu("View");
        mb.add(view);
        view.setMnemonic('V');
        view.add(new RefreshPageAction());
        view.add(new ReloadPageAction());


        JMenu debug = new JMenu("Debug");
        mb.add(debug);
        debug.setMnemonic('D');

        JMenu debugShow = new JMenu("Show");
        debug.add(debugShow);
        debugShow.setMnemonic('S');

        debugShow.add(new JCheckBoxMenuItem(new BoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new LineBoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new InlineBoxesAction()));
        debugShow.add(new JCheckBoxMenuItem(new FontMetricsAction()));


        JMenu anti = new JMenu("Anti Aliasing");
        anti.add(new JCheckBoxMenuItem(new AntiAliasedAction("None", -1)));
        anti.add(new JCheckBoxMenuItem(new AntiAliasedAction("Low (Default)", 25)));
        anti.add(new JCheckBoxMenuItem(new AntiAliasedAction("Medium", 12)));
        anti.add(new JCheckBoxMenuItem(new AntiAliasedAction("Highest", 0)));
        debug.add(anti);

        debug.add(new ShowDOMInspectorAction());
/*
        debug.add(
                    new AbstractAction( "Print Box Tree" ) {
                        public void actionPerformed( ActionEvent evt ) {
                            panel.printTree();
                        }
                    } );
*/
        setJMenuBar(mb);
    }

    /**
     * Adds a feature to the FileLoadAction attribute of the HTMLTest object
     *
     * @param menu    The feature to be added to the FileLoadAction attribute
     * @param display The feature to be added to the FileLoadAction attribute
     * @param file    The feature to be added to the FileLoadAction attribute
     */
    public void addFileLoadAction(JMenu menu, String display, final String file) {
        menu.add(new AbstractAction(display) {
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent evt) {
                loadDocument(file);
            }
        });
    }

    /**
     * Description of the Method
     *
     * @param uri taken to be a file, if not beginning with http://
     */
    private void loadDocument(final String uri) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    long st = System.currentTimeMillis();

                    URL url = null;
                    if (uri.startsWith("http://"))
                        url = new URL(uri);
                    else
                        url = new File(uri).toURL();

                    System.err.println("loading " + url.toString() + "!");
                    panel.setDocument(url.toExternalForm());

                    long el = System.currentTimeMillis() - st;
                    XRLog.general("loadDocument(" + url.toString() + ") in " + el + "ms, render may take longer");
                    HTMLTest.this.setTitle(BASE_TITLE + "-  " +
                            panel.getDocumentTitle() + "  " +
                            "(" + url.toString() + ")");
                } catch (Exception ex) {
                    Uu.p(ex);
                }
                panel.repaint();
            }
        });
    }

    /**
     * The main program for the HTMLTest class
     *
     * @param args The command line arguments
     * @throws Exception Throws
     */
    public static void main(String[] args)
            throws Exception {


        final JFrame frame = new HTMLTest(args);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        //frame.setSize( text_width, 300 );
        frame.setVisible(true);
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    static class QuitAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for the QuitAction object
         */
        QuitAction() {
            super("Quit");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(final ActionEvent evt) {
            System.exit(0);
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class BoxOutlinesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for the BoxOutlinesAction object
         */
        BoxOutlinesAction() {
            super("Show Box Outlines");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            panel.getSharedContext().setDebug_draw_boxes(!panel.getSharedContext().debugDrawBoxes());
            panel.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class LineBoxOutlinesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for the LineBoxOutlinesAction object
         */
        LineBoxOutlinesAction() {
            super("Show Line Box Outlines");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            panel.getSharedContext().setDebug_draw_line_boxes(!panel.getSharedContext().debugDrawLineBoxes());
            panel.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class InlineBoxesAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for the InlineBoxesAction object
         */
        InlineBoxesAction() {
            super("Show Inline Boxes");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            panel.getSharedContext().setDebug_draw_inline_boxes(!panel.getSharedContext().debugDrawInlineBoxes());
            panel.repaint();
        }
    }

    class FontMetricsAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for the InlineBoxesAction object
         */
        FontMetricsAction() {
            super("Show Font Metrics");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            panel.getSharedContext().setDebug_draw_font_metrics(!panel.getSharedContext().debugDrawFontMetrics());
            panel.repaint();
        }
    }

    class AntiAliasedAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        int fontSizeThreshold;

        AntiAliasedAction(String text, int fontSizeThreshold) {
            super(text);
            this.fontSizeThreshold = fontSizeThreshold;
        }

        public void actionPerformed(ActionEvent evt) {
            panel.getSharedContext().getTextRenderer().setSmoothingThreshold(fontSizeThreshold);
            panel.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class ShowDOMInspectorAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Description of the Field
         */
        private DOMInspector inspector;
        /**
         * Description of the Field
         */
        private JFrame inspectorFrame;

        /**
         * Constructor for the ShowDOMInspectorAction object
         */
        ShowDOMInspectorAction() {
            super("DOM Tree Inspector");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            if (inspectorFrame == null) {
                inspectorFrame = new JFrame("DOM Tree Inspector");
            }
            if (inspector == null) {
                // inspectorFrame = new JFrame("DOM Tree Inspector");

                inspector = new DOMInspector(panel.doc, panel.getSharedContext(), panel.getSharedContext().getCss());

                inspectorFrame.getContentPane().add(inspector);

                inspectorFrame.pack();
                inspectorFrame.setSize(400, 600);
                inspectorFrame.setVisible(true);
            } else {
                inspector.setForDocument(panel.doc, panel.getSharedContext(), panel.getSharedContext().getCss());
            }
            inspectorFrame.setVisible(true);
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    static class RefreshPageAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for the RefreshPageAction object
         */
        RefreshPageAction() {
            super("Refresh Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("F5"));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            // TODO
            System.out.println("Refresh Page triggered");
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    static class ReloadPageAction extends AbstractAction {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor for the ReloadPageAction object
         */
        ReloadPageAction() {
            super("Reload Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_F5,
                            ActionEvent.CTRL_MASK));
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            // TODO
            System.out.println("Reload Page triggered");
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
 * Revision 1.39  2009/05/09 14:15:14  pdoubleya
 * FindBugs: inner class could be static
 *
 * Revision 1.38  2009/03/22 15:13:24  pdoubleya
 * Follow up for removing Minium AA: font "smoothing level" now deprecated. Changed to use font smoothing threshold alone. Remove corresponding property from configuration file.
 *
 * Revision 1.37  2009/03/22 12:27:36  pdoubleya
 * Remove Minium anti-aliasing library as sources are not available. Removed jar and all references to it. For R8 release.
 *
 * Revision 1.36  2007/05/24 13:22:38  peterbrant
 * Optimize and clean up hover and link listeners
 *
 * Patch from Sean Bright
 *
 * Revision 1.35  2007/05/20 23:25:33  peterbrant
 * Various code cleanups (e.g. remove unused imports)
 *
 * Patch from Sean Bright
 *
 * Revision 1.34  2005/10/27 00:09:08  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.33  2005/07/15 23:39:49  joshy
 * updates to try to fix the resize issue
 *
 * Revision 1.32  2005/07/02 07:27:00  joshy
 * better support for jumping to anchor tags
 * also some testing for the resize issue
 * need to investigate making the history remember document position.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.31  2005/06/16 07:24:53  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.30  2005/06/15 10:56:15  tobega
 * cleaned up a bit of URL mess, centralizing URI-resolution and loading to UserAgentCallback
 *
 * Revision 1.29  2005/06/09 22:34:57  joshy
 * This makes the hover listener be added to the xhtml panel by default.
 * Also improves the box searching code by testing if the parent of the deepest
 * box is hoverable in the case where the deepest box is not.
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.28  2005/01/29 20:21:09  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.27  2005/01/08 11:55:18  tobega
 * Started massaging the extension interfaces
 *
 * Revision 1.26  2004/12/29 10:39:35  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
 * Revision 1.25  2004/12/29 07:35:39  tobega
 * Prepared for cloned Context instances by encapsulating fields
 *
 * Revision 1.24  2004/12/12 03:33:02  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.23  2004/12/01 14:02:53  joshy
 * modified media to use the value from the rendering context
 * added the inline-block box
 * - j
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.22  2004/11/16 03:44:15  joshy
 * removed printing from html test
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.21  2004/11/16 03:43:27  joshy
 * first pass at printing support
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.20  2004/11/15 23:02:35  tobega
 * Modified so that a http url may be given on command-line
 *
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

