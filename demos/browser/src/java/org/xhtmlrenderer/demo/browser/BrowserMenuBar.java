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

import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.render.Java2DTextRenderer;
import org.xhtmlrenderer.render.MiniumTextRenderer;
import org.xhtmlrenderer.swing.DOMInspector;
import org.xhtmlrenderer.swing.HoverListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xhtmlrenderer.util.Uu;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

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
    public static Logger logger = Logger.getLogger("app.browser");
    private Map allDemos;

    /**
     * Constructor for the BrowserMenuBar object
     *
     * @param root PARAM
     */
    public BrowserMenuBar(BrowserStartup root) {
        this.root = root;
        logger.setLevel(Level.OFF);
    }

    /**
     * Description of the Method
     */
    public void init() {
        file = new JMenu("File");
        file.setMnemonic('F');

        debug = new JMenu("Debug");
        debug.setMnemonic('B');

        demos = new JMenu("Demos");
        demos.setMnemonic('D');

        edit = new JMenu("Edit");
        edit.setMnemonic('E');

        view = new JMenu("View");
        view.setMnemonic('V');

        view_source = new JMenuItem("Page Source");
        view_source.setEnabled(false);
        view.add(root.actions.stop);
        view.add(root.actions.refresh);
        view.add(root.actions.reload);
        view.add(new JSeparator());
        JMenu text_size = new JMenu("Text Size");
        text_size.setMnemonic('Z');
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
        file.add(root.actions.open_file);
        file.add(root.actions.print);
        file.add(new JSeparator());
        file.add(root.actions.quit);
        add(file);

        edit.add(root.actions.cut);
        edit.add(root.actions.copy);
        edit.add(root.actions.paste);
        add(edit);

        view.add(view_source);
        add(view);

        go.add(root.actions.forward);
        go.add(root.actions.backward);

        add(go);

        demos.add(new NextDemoAction());
        demos.add(new PriorDemoAction());
        demos.add(new JSeparator());
        allDemos = new LinkedHashMap();

        //allDemos.put("Simple Page", "demo:demos/single-line-page.xhtml");

        allDemos.put("Nested Float", "demo:demos/layout/multicol/glish/nested-float.xhtml");

        allDemos.put("Paragraph", "demo:demos/paragraph.xhtml");
        allDemos.put("Line Breaking", "demo:demos/breaking.xhtml");
        allDemos.put("Selectors", "demo:demos/selectors.xhtml");
        allDemos.put("Inheritance", "demo:demos/inherit.xhtml");
        allDemos.put("Borders", "demo:demos/border.xhtml");
        allDemos.put("Headers", "demo:demos/header.xhtml");
        allDemos.put("Lists", "demo:demos/list.xhtml");
        allDemos.put("Backgrounds", "demo:demos/background.xhtml");
        allDemos.put("Images", "demo:demos/image.xhtml");
        allDemos.put("Nested Divs", "demo:demos/nested.xhtml");
        allDemos.put("Rollovers with :hover ", "demo:demos/hover.xhtml");
        allDemos.put("Pseudo-elements ", "demo:demos/pseudo-elements.xhtml");
        allDemos.put("Forms", "demo:demos/forms.xhtml");
        // TODO: tables broken!
        // allDemos.put("Tables", "demo:demos/table.xhtml");
        allDemos.put("Link", "demo:demos/link.xhtml");
        allDemos.put("Game Screen", "demo:demos/game/index.xhtml");
        allDemos.put("Financial Report", "demo:demos/report.xhtml");
        allDemos.put("Alice", "demo:demos/alice/alice.xhtml");
        allDemos.put("Hamlet (the whole thing)", "demo:demos/hamlet.xhtml");

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
        ButtonGroup implementation = new ButtonGroup();
        JRadioButtonMenuItem java2d = new JRadioButtonMenuItem(new AbstractAction("Java2D Implementation") {
            public void actionPerformed(ActionEvent evt) {
                RenderingContext rc = root.panel.view.getRenderingContext();
                int level = rc.getTextRenderer().getSmoothingLevel();
                rc.setTextRenderer(new Java2DTextRenderer());
                rc.getTextRenderer().setSmoothingLevel(level);
                root.panel.view.repaint();
            }
        });
        java2d.setSelected(true);
        implementation.add(java2d);
        anti.add(java2d);

        JRadioButtonMenuItem minium = new JRadioButtonMenuItem(new AbstractAction("Minium Implementation") {
            public void actionPerformed(ActionEvent evt) {
                RenderingContext rc = root.panel.view.getRenderingContext();
                int level = rc.getTextRenderer().getSmoothingLevel();
                rc.setTextRenderer(new MiniumTextRenderer());
                rc.getTextRenderer().setSmoothingLevel(level);
                root.panel.view.repaint();
            }
        });
        implementation.add(minium);
        anti.add(minium);

        anti.add(new JSeparator());

        ButtonGroup anti_level = new ButtonGroup();
        addLevel(anti, anti_level, "None", TextRenderer.NONE);
        addLevel(anti, anti_level, "Low", TextRenderer.LOW).setSelected(true);
        addLevel(anti, anti_level, "Medium", TextRenderer.MEDIUM);
        addLevel(anti, anti_level, "High", TextRenderer.HIGH);
        debug.add(anti);


        debug.add(new ShowDOMInspectorAction());
        debug.add(new AbstractAction("Validation Console") {
            public void actionPerformed(ActionEvent evt) {
                if (root.validation_console == null) {
                    root.validation_console = new JFrame("Validation Console");
                    JFrame frame = root.validation_console;
                    JTextArea jta = new JTextArea();
                    root.error_handler.setTextArea(jta);

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
                    frame.setSize(200, 400);
                }
                root.validation_console.setVisible(true);
            }
        });

        debug.add(root.actions.generate_diff);
        add(debug);
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

        SelectionMouseListener ma = new SelectionMouseListener();
        root.panel.view.addMouseListener(ma);
        root.panel.view.addMouseMotionListener(ma);
        logger.info("added mouse selection listener: " + ma);

        HoverListener hl = new HoverListener(root.panel.view);
        root.panel.view.addMouseListener(hl);
        root.panel.view.addMouseMotionListener(hl);
        logger.info("added hover listener:" + hl);

        LinkListener ll = new LinkListener(root.panel.view);
        root.panel.view.addMouseListener(ll);
        root.panel.view.addMouseMotionListener(ll);
        logger.info("added link listener: " + ll);
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
                // inspectorFrame = new JFrame("DOM Tree Inspector");

                inspector = new DOMInspector(root.panel.view.getDocument(), root.panel.view.getContext(), root.panel.view.getContext().getCss());

                inspectorFrame.getContentPane().add(inspector);

                inspectorFrame.pack();
                inspectorFrame.setSize(500, 600);
                inspectorFrame.show();
            } else {
                inspector.setForDocument(root.panel.view.getDocument(), root.panel.view.getContext(), root.panel.view.getContext().getCss());
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
            root.panel.view.getContext().setDebug_draw_boxes(!root.panel.view.getContext().debugDrawBoxes());
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
            root.panel.view.getContext().setDebug_draw_line_boxes(!root.panel.view.getContext().debugDrawLineBoxes());
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
            root.panel.view.getContext().setDebug_draw_inline_boxes(!root.panel.view.getContext().debugDrawInlineBoxes());
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
            root.panel.view.getContext().setDebug_draw_font_metrics(!root.panel.view.getContext().debugDrawFontMetrics());
            root.panel.view.repaint();
        }
    }
    class NextDemoAction extends AbstractAction {

        public NextDemoAction() {
            super("Next Demo Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK));
        }
        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            String nextPage = null;
            for (Iterator iter = allDemos.keySet().iterator(); iter.hasNext();) {
                String s = (String) iter.next();
                if ( s.equals(lastDemoOpened)) {
                    if ( iter.hasNext()) {
                        nextPage = (String)iter.next();
                        break;
                    }
                }
            }
            if ( nextPage == null ) {
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

    }


    class PriorDemoAction extends AbstractAction {

        public PriorDemoAction() {
            super("Prior Demo Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.ALT_MASK));
        }
        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            String priorPage = null;
            for (Iterator iter = allDemos.keySet().iterator(); iter.hasNext();) {
                String s = (String) iter.next();
                if ( s.equals(lastDemoOpened)) {
                    break;
                }
                priorPage = s;
            }
            if ( priorPage == null ) {
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
        int hint;

        AntiAliasedAction(String text, int hint) {
            super(text);
            this.hint = hint;
        }

        public void actionPerformed(ActionEvent evt) {
            root.panel.view.getRenderingContext().getTextRenderer().setSmoothingLevel(hint);
            //root.panel.view.getRenderingContext().getTextRenderer().setSmoothingThreshold(20);
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
        super(name, icon);
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
 * Separated current state Context into ContextImpl and the rest into SharedContext.
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

