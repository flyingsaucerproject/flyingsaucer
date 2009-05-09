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

import org.xhtmlrenderer.demo.browser.actions.ZoomAction;
import org.xhtmlrenderer.swing.*;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.Uu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Description of the Class
 *
 * @author empty
 */
public class BrowserMenuBar extends JMenuBar {
    /**
     * Description of the Field
     */
    BrowserStartup root;

    /**
     * Description of the Field
     */
    JMenu file;
    /**
     * Description of the Field
     */
    JMenu edit;
    /**
     * Description of the Field
     */
    JMenu view;
    /**
     * Description of the Field
     */
    JMenu go;
    /**
     * Description of the Field
     */
    JMenuItem view_source;
    /**
     * Description of the Field
     */
    JMenu debug;
    /**
     * Description of the Field
     */
    JMenu demos;
    /**
     *
     */
    private String lastDemoOpened;

    /**
     * Description of the Field
     */
    private Map allDemos;
    private JMenu help;

    /**
     * Constructor for the BrowserMenuBar object
     *
     * @param root PARAM
     */
    public BrowserMenuBar(BrowserStartup root) {
        this.root = root;
    }

    /**
     * Description of the Method
     */
    public void init() {
        file = new JMenu("Browser");
        file.setMnemonic('B');

        debug = new JMenu("Debug");
        debug.setMnemonic('U');

        demos = new JMenu("Demos");
        demos.setMnemonic('D');

        view = new JMenu("View");
        view.setMnemonic('V');

        help = new JMenu("Help");
        help.setMnemonic('H');

        view_source = new JMenuItem("Page Source");
        view_source.setEnabled(false);
        view.add(root.actions.stop);
        view.add(root.actions.refresh);
        view.add(root.actions.reload);
        view.add(new JSeparator());
        JMenu text_size = new JMenu("Text Size");
        text_size.setMnemonic('T');
        text_size.add(root.actions.increase_font);
        text_size.add(root.actions.decrease_font);
        text_size.add(new JSeparator());
        text_size.add(root.actions.reset_font);
        view.add(text_size);

        go = new JMenu("Go");
        go.setMnemonic('G');
    }


    /**
     * Description of the Method
     */
    public void createLayout() {
        final ScalableXHTMLPanel panel = root.panel.view;

        file.add(root.actions.open_file);
        file.add(new JSeparator());
        file.add(root.actions.export_pdf);
        file.add(new JSeparator());
        file.add(root.actions.quit);
        add(file);

        /*
        // TODO: we can get the document and format it, but need syntax highlighting
        // and a tab or separate window, dialog, etc.
        view_source.setAction(new ViewSourceAction(panel));
        view.add(view_source);
        */

        JMenu zoom = new JMenu("Zoom");
        zoom.setMnemonic('Z');
        ScaleFactor[] factors = this.initializeScales();
        ButtonGroup zoomGroup = new ButtonGroup();
        for (int i = 0; i < factors.length; i++) {
            ScaleFactor factor = factors[i];
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(new ZoomAction(panel, factor));

            if (factor.isNotZoomed()) item.setSelected(true);

            zoomGroup.add(item);
            zoom.add(item);
        }
        view.add(new JSeparator());
        view.add(zoom);
        view.add(new JSeparator());
        view.add(new JCheckBoxMenuItem(root.actions.print_preview));
        add(view);

        go.add(root.actions.forward);
        go.add(root.actions.backward);

        add(go);

        demos.add(new NextDemoAction());
        demos.add(new PriorDemoAction());
        demos.add(new JSeparator());
        allDemos = new LinkedHashMap();

        populateDemoList();

        for (Iterator iter = allDemos.keySet().iterator(); iter.hasNext();) {
            String s = (String) iter.next();
            demos.add(new LoadAction(s, (String) allDemos.get(s)));
        }

        add(demos);

        JMenu debugShow = new JMenu("Show");
        debug.add(debugShow);
        debugShow.setMnemonic('S');

        debugShow.add(new JCheckBoxMenuItem(new BoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new LineBoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new InlineBoxesAction()));
        debugShow.add(new JCheckBoxMenuItem(new FontMetricsAction()));

        JMenu anti = new JMenu("Anti Aliasing");
        ButtonGroup anti_level = new ButtonGroup();
        addLevel(anti, anti_level, "None", -1);
        addLevel(anti, anti_level, "Low", 25).setSelected(true);
        addLevel(anti, anti_level, "Medium", 12);
        addLevel(anti, anti_level, "High", 0);
        debug.add(anti);


        debug.add(new ShowDOMInspectorAction());
        debug.add(new AbstractAction("Validation Console") {
            public void actionPerformed(ActionEvent evt) {
                if (root.validation_console == null) {
                    root.validation_console = new JFrame("Validation Console");
                    JFrame frame = root.validation_console;
                    JTextArea jta = new JTextArea();

                    root.error_handler.setTextArea(jta);

                    jta.setEditable(false);
                    jta.setLineWrap(true);
                    jta.setText("Validation Console: XML Parsing Error Messages");

                    frame.getContentPane().setLayout(new BorderLayout());
                    frame.getContentPane().add(new JScrollPane(jta), "Center");
                    JButton close = new JButton("Close");
                    frame.getContentPane().add(close, "South");
                    close.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            root.validation_console.setVisible(false);
                        }
                    });

                    frame.pack();
                    frame.setSize(400, 300);
                }
                root.validation_console.setVisible(true);
            }
        });

        debug.add(root.actions.generate_diff);
        add(debug);

        help.add(root.actions.usersManual);
        help.add(new JSeparator());
        help.add(root.actions.aboutPage);
        add(help);
    }

    private void populateDemoList() {
        List demoList = new ArrayList();
        URL url = BrowserMenuBar.class.getResource("/demos/file-list.txt");
        InputStream is = null;
        LineNumberReader lnr = null;
        if (url != null) {
            try {
                is = url.openStream();
                InputStreamReader reader = new InputStreamReader(is);
                lnr = new LineNumberReader(reader);
                try {
                    String line;
                    while ((line = lnr.readLine()) != null) {
                        demoList.add(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        lnr.close();
                    } catch (IOException e) {
                        // swallow
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // swallow
                    }
                }
            }

            for (Iterator itr = demoList.iterator(); itr.hasNext();) {
                String s = (String) itr.next();
                String s1[] = s.split(",");
                allDemos.put(s1[0], s1[1]);
            }
        }
    }

    private JRadioButtonMenuItem addLevel(JMenu menu, ButtonGroup group, String title, int level) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(new AntiAliasedAction(title, level));
        group.add(item);
        menu.add(item);
        return item;
    }


    /**
     * Description of the Method
     */
    public void createActions() {
        if (Configuration.isTrue("xr.use.listeners", true)) {
            List l = root.panel.view.getMouseTrackingListeners();
            for (Iterator i = l.iterator(); i.hasNext(); ) {
                FSMouseListener listener = (FSMouseListener)i.next();
                if ( listener instanceof LinkListener ) {
                    root.panel.view.removeMouseTrackingListener(listener);
                }
            }

            root.panel.view.addMouseTrackingListener(new LinkListener() {
               public void linkClicked(BasicPanel panel, String uri) {
                   if (uri.startsWith("demoNav")) {
                       String pg = uri.split(":")[1];
                       if (pg.equals("back")) {
                           navigateToPriorDemo();
                       } else {
                           navigateToNextDemo();
                       }
                   } else {
                       super.linkClicked(panel, uri);
                   }
               } 
            });
        }
    }

    private ScaleFactor[] initializeScales() {
        ScaleFactor[] scales = new ScaleFactor[11];
        int i = 0;
        scales[i++] = new ScaleFactor(1.0d, "Normal (100%)");
        scales[i++] = new ScaleFactor(2.0d, "200%");
        scales[i++] = new ScaleFactor(1.5d, "150%");
        scales[i++] = new ScaleFactor(0.85d, "85%");
        scales[i++] = new ScaleFactor(0.75d, "75%");
        scales[i++] = new ScaleFactor(0.5d, "50%");
        scales[i++] = new ScaleFactor(0.33d, "33%");
        scales[i++] = new ScaleFactor(0.25d, "25%");
        scales[i++] = new ScaleFactor(ScaleFactor.PAGE_WIDTH, "Page width");
        scales[i++] = new ScaleFactor(ScaleFactor.PAGE_HEIGHT, "Page height");
        scales[i++] = new ScaleFactor(ScaleFactor.PAGE_WHOLE, "Whole page");
        return scales;
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class ShowDOMInspectorAction extends AbstractAction {
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
                inspector = new DOMInspector(root.panel.view.getDocument(), root.panel.view.getSharedContext(), root.panel.view.getSharedContext().getCss());

                inspectorFrame.getContentPane().add(inspector);

                inspectorFrame.pack();
                inspectorFrame.setSize(500, 600);
                inspectorFrame.show();
            } else {
                inspector.setForDocument(root.panel.view.getDocument(), root.panel.view.getSharedContext(), root.panel.view.getSharedContext().getCss());
            }
            inspectorFrame.show();
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class BoxOutlinesAction extends AbstractAction {
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
            root.panel.view.getSharedContext().setDebug_draw_boxes(!root.panel.view.getSharedContext().debugDrawBoxes());
            root.panel.view.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class LineBoxOutlinesAction extends AbstractAction {
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
            root.panel.view.getSharedContext().setDebug_draw_line_boxes(!root.panel.view.getSharedContext().debugDrawLineBoxes());
            root.panel.view.repaint();
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class InlineBoxesAction extends AbstractAction {
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
            root.panel.view.getSharedContext().setDebug_draw_inline_boxes(!root.panel.view.getSharedContext().debugDrawInlineBoxes());
            root.panel.view.repaint();
        }
    }

    class FontMetricsAction extends AbstractAction {
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
            root.panel.view.getSharedContext().setDebug_draw_font_metrics(!root.panel.view.getSharedContext().debugDrawFontMetrics());
            root.panel.view.repaint();
        }
    }

    class NextDemoAction extends AbstractAction {

        public NextDemoAction() {
            super("Next Demo Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            navigateToNextDemo();
        }
    }

    public void navigateToNextDemo() {
        String nextPage = null;
        for (Iterator iter = allDemos.keySet().iterator(); iter.hasNext();) {
            String s = (String) iter.next();
            if (s.equals(lastDemoOpened)) {
                if (iter.hasNext()) {
                    nextPage = (String) iter.next();
                    break;
                }
            }
        }
        if (nextPage == null) {
            // go to first page
            Iterator iter = allDemos.keySet().iterator();
            nextPage = (String) iter.next();
        }

        try {
            root.panel.loadPage((String) allDemos.get(nextPage));
            lastDemoOpened = nextPage;
        } catch (Exception ex) {
            Uu.p(ex);
        }
    }

    class PriorDemoAction extends AbstractAction {

        public PriorDemoAction() {
            super("Prior Demo Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            navigateToPriorDemo();
        }
    }

    public void navigateToPriorDemo() {
        String priorPage = null;
        for (Iterator iter = allDemos.keySet().iterator(); iter.hasNext();) {
            String s = (String) iter.next();
            if (s.equals(lastDemoOpened)) {
                break;
            }
            priorPage = s;
        }
        if (priorPage == null) {
            // go to last page
            Iterator iter = allDemos.keySet().iterator();
            while (iter.hasNext()) {
                priorPage = (String) iter.next();
            }
        }

        try {
            root.panel.loadPage((String) allDemos.get(priorPage));
            lastDemoOpened = priorPage;
        } catch (Exception ex) {
            Uu.p(ex);
        }
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    class LoadAction extends AbstractAction {
        /**
         * Description of the Field
         */
        protected String url;

        private String pageName;

        /**
         * Constructor for the LoadAction object
         *
         * @param name PARAM
         * @param url  PARAM
         */
        public LoadAction(String name, String url) {
            super(name);
            pageName = name;
            this.url = url;
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            try {
                root.panel.loadPage(url);
                lastDemoOpened = pageName;
            } catch (Exception ex) {
                Uu.p(ex);
            }
        }

    }

    class AntiAliasedAction extends AbstractAction {
        int fontSizeThreshold;

        AntiAliasedAction(String text, int fontSizeThreshold) {
            super(text);
            this.fontSizeThreshold = fontSizeThreshold;
        }

        public void actionPerformed(ActionEvent evt) {
            root.panel.view.getSharedContext().getTextRenderer().setSmoothingThreshold(fontSizeThreshold);
            root.panel.view.repaint();
        }
    }

}


/**
 * Description of the Class
 *
 * @author empty
 */
class EmptyAction extends AbstractAction {
    public EmptyAction(String name, Icon icon) {
        this(name, "", icon);
    }

    public EmptyAction(String name, String shortDesc, Icon icon) {
        super(name, icon);
        putValue(Action.SHORT_DESCRIPTION, shortDesc);
    }

    /**
     * Constructor for the EmptyAction object
     *
     * @param name  PARAM
     * @param accel PARAM
     */
    public EmptyAction(String name, int accel) {
        this(name);
        putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(accel,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    /**
     * Constructor for the EmptyAction object
     *
     * @param name PARAM
     */
    public EmptyAction(String name) {
        super(name);
    }

    /**
     * Description of the Method
     *
     * @param evt PARAM
     */
    public void actionPerformed(ActionEvent evt) {
    }
}

/*
* $Id$
*
* $Log$
* Revision 1.51  2009/05/09 15:16:43  pdoubleya
* FindBugs: proper disposal of IO resources
*
* Revision 1.50  2009/03/22 15:13:24  pdoubleya
* Follow up for removing Minium AA: font "smoothing level" now deprecated. Changed to use font smoothing threshold alone. Remove corresponding property from configuration file.
*
* Revision 1.49  2009/03/22 12:27:38  pdoubleya
* Remove Minium anti-aliasing library as sources are not available. Removed jar and all references to it. For R8 release.
*
* Revision 1.48  2009/02/15 19:57:49  pdoubleya
* Remove references to "r7", and move browser demos to top-level xhtml directory.
*
* Revision 1.47  2008/09/06 18:44:29  peterbrant
* Add PDF export to browser (patch from Mykola Gurov)
*
* Revision 1.46  2007/11/01 00:18:31  peterbrant
* Adapt to R7 mouse tracking API
*
* Revision 1.45  2007/07/14 17:38:17  pdoubleya
* fix menu accelerator assignments to be cross-platform compatible (esp. with OS X)
*
* Revision 1.44  2007/07/13 13:32:31  pdoubleya
* Add webstart entry point for browser with no URL or File/open option. Move Zoom to menu entry, add warning on first zoom. Move preview to menu entry. Reorganize launch method a little to allow for multiple entry points.
*
* Revision 1.43  2007/05/24 13:22:39  peterbrant
* Optimize and clean up hover and link listeners
*
* Patch from Sean Bright
*
* Revision 1.42  2007/04/12 12:39:25  peterbrant
* Fix NPE if demo list is not found
*
* Revision 1.41  2007/04/11 21:07:02  pdoubleya
* Prepare to point to R7 versions of files
*
* Revision 1.40  2007/02/07 16:33:38  peterbrant
* Initial commit of rewritten table support and associated refactorings
*
* Revision 1.39  2006/08/06 21:27:00  pdoubleya
* Removed printing for R6.
*
* Revision 1.38  2006/07/31 14:20:54  pdoubleya
* Bunch of cleanups and fixes. Now using a toolbar for actions, added Home button, next/prev navigation actions to facilitate demo file browsing, loading demo pages from a list, about dlg and link to user's manual.
*
* Revision 1.37  2006/01/09 23:24:53  peterbrant
* Provide config key to not use link and hover listeners (one of which currently leaks memory horribly)
*
* Revision 1.36  2005/10/27 00:08:50  tobega
* Sorted out Context into RenderingContext and LayoutContext
*
* Revision 1.35  2005/10/20 20:31:04  pdoubleya
* Cleaned imports.
*
* Revision 1.34  2005/08/16 22:46:27  joshy
* added new demos, streamlined downloads
*
* Revision 1.33  2005/07/31 01:12:29  joshy
* updated browser demos, about box demos, and added pack200 to the distro
*
* Revision 1.32  2005/07/21 21:51:07  joshy
* added new demos to browser
*
* Revision 1.31  2005/07/13 22:49:14  joshy
* updates to get the jnlp to work without being signed
*
* Revision 1.30  2005/03/28 20:03:14  pdoubleya
* Icon/menu bar assignments.
*
* Revision 1.29  2005/03/28 19:04:17  pdoubleya
* Moved text size controls on menu, cleaned list of pages.
*
* Revision 1.28  2005/01/29 12:24:57  pdoubleya
* .
*
* Revision 1.27  2005/01/25 11:51:39  pdoubleya
* Added next and prior page; refactored demos into Map for manipulation.
*
* Revision 1.26  2004/12/29 10:39:38  tobega
* Separated current state Context into LayoutContext and the rest into SharedContext.
*
* Revision 1.25  2004/12/29 07:35:40  tobega
* Prepared for cloned Context instances by encapsulating fields
*
* Revision 1.24  2004/12/12 16:11:04  tobega
* Fixed bug concerning order of inline content. Added a demo for pseudo-elements.
*
* Revision 1.23  2004/12/12 03:33:06  tobega
* Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
*
* Revision 1.22  2004/12/12 02:53:49  tobega
* Making progress
*
* Revision 1.21  2004/12/09 18:03:11  joshy
* added game screen to browser
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.20  2004/11/17 00:45:58  joshy
* added link demo
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.18  2004/11/16 03:43:25  joshy
* first pass at printing support
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.17  2004/11/15 14:50:45  joshy
* removed text code
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.16  2004/11/15 14:50:26  joshy
* font threshold support
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.15  2004/11/14 21:33:46  joshy
* new font rendering interface support
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.14  2004/11/12 20:25:16  joshy
* added hover support to the browser
* created hover demo
* fixed bug with inline borders
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.13  2004/11/10 17:28:53  joshy
* initial support for anti-aliased text w/ minium
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.12  2004/11/10 04:53:59  tobega
* cleaned up
*
* Revision 1.11  2004/11/09 15:53:47  joshy
* initial support for hover (currently disabled)
* moved justification code into it's own class in a new subpackage for inline
* layout (because it's so blooming complicated)
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.10  2004/11/09 03:52:25  joshy
* added financial report demo
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.9  2004/11/09 00:36:07  joshy
* fixed more text alignment
* added menu item to show font metrics
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.8  2004/11/07 23:24:19  joshy
* added menu item to generate diffs
* added diffs for multi-colored borders and inline borders
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.7  2004/11/05 18:48:42  joshy
* added alice demo to the browser
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.6  2004/11/03 23:54:32  joshy
* added hamlet and tables to the browser
* more support for absolute layout
* added absolute layout unit tests
* removed more dead code and moved code into layout factory
*
*
* Issue number:
* Obtained from:
* Submitted by:
* Reviewed by:
*
* Revision 1.5  2004/10/28 14:18:22  joshy
* cleaned up the htmlpanel and made more of the variables protected
* fixed the bug where the body is too small for the viewport
* fixed the bug where the screen isn't re-laid out when the window is resized
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

