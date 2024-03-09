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
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.DOMInspector;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xhtmlrenderer.swing.ScalableXHTMLPanel;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.Uu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BrowserMenuBar extends JMenuBar {
    private final BrowserStartup root;
    private JMenu file;
    private JMenu edit;
    private JMenu view;
    private JMenu go;
    private JMenu debug;
    private JMenu demos;
    private String lastDemoOpened;

    private Map<String, String> allDemos;
    private JMenu help;

    public BrowserMenuBar(BrowserStartup root) {
        this.root = root;
    }

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

        JMenuItem view_source = new JMenuItem("Page Source");
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
        ScaleFactor[] factors = initializeScales();
        ButtonGroup zoomGroup = new ButtonGroup();
        for (ScaleFactor factor : factors) {
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
        allDemos = new LinkedHashMap<>();

        populateDemoList();

        for (Map.Entry<String, String> entry : allDemos.entrySet()) {
            demos.add(new LoadAction(entry.getKey(), entry.getValue()));
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
            @Override
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
                    close.addActionListener(evt1 -> root.validation_console.setVisible(false));

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
        List<String> demoList = new ArrayList<>();
        URL url = BrowserMenuBar.class.getResource("/demos/file-list.txt");
                
        if (url != null) {
            try (InputStream is = url.openStream()) {
                InputStreamReader reader = new InputStreamReader(is);
                try (LineNumberReader lnr = new LineNumberReader(reader)) {
                    String line;
                    while ((line = lnr.readLine()) != null) {
                        demoList.add(line);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (String s : demoList) {
                String[] s1 = s.split(",");
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


    public void createActions() {
        if (Configuration.isTrue("xr.use.listeners", true)) {
            List<FSMouseListener> l = root.panel.view.getMouseTrackingListeners();
            for (FSMouseListener listener : l) {
                if (listener instanceof LinkListener) {
                    root.panel.view.removeMouseTrackingListener(listener);
                }
            }

            root.panel.view.addMouseTrackingListener(new LinkListener() {
               @Override
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

    final class ShowDOMInspectorAction extends AbstractAction {
        private DOMInspector inspector;
        private JFrame inspectorFrame;

        ShowDOMInspectorAction() {
            super("DOM Tree Inspector");
            putValue(MNEMONIC_KEY, KeyEvent.VK_D);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (inspectorFrame == null) {
                inspectorFrame = new JFrame("DOM Tree Inspector");
            }
            if (inspector == null) {
                inspector = new DOMInspector(root.panel.view.getDocument(), root.panel.view.getSharedContext(), root.panel.view.getSharedContext().getCss());

                inspectorFrame.getContentPane().add(inspector);

                inspectorFrame.pack();
                inspectorFrame.setSize(500, 600);
                inspectorFrame.setVisible(true);
            } else {
                inspector.setForDocument(root.panel.view.getDocument(), root.panel.view.getSharedContext(), root.panel.view.getSharedContext().getCss());
            }
            inspectorFrame.setVisible(true);
        }
    }

    final class BoxOutlinesAction extends AbstractAction {
        BoxOutlinesAction() {
            super("Show Box Outlines");
            putValue(MNEMONIC_KEY, KeyEvent.VK_B);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            root.panel.view.getSharedContext().setDebug_draw_boxes(!root.panel.view.getSharedContext().debugDrawBoxes());
            root.panel.view.repaint();
        }
    }

    final class LineBoxOutlinesAction extends AbstractAction {
        LineBoxOutlinesAction() {
            super("Show Line Box Outlines");
            putValue(MNEMONIC_KEY, KeyEvent.VK_L);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            root.panel.view.getSharedContext().setDebug_draw_line_boxes(!root.panel.view.getSharedContext().debugDrawLineBoxes());
            root.panel.view.repaint();
        }
    }

    final class InlineBoxesAction extends AbstractAction {
        InlineBoxesAction() {
            super("Show Inline Boxes");
            putValue(MNEMONIC_KEY, KeyEvent.VK_I);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            root.panel.view.getSharedContext().setDebug_draw_inline_boxes(!root.panel.view.getSharedContext().debugDrawInlineBoxes());
            root.panel.view.repaint();
        }
    }

    final class FontMetricsAction extends AbstractAction {
        FontMetricsAction() {
            super("Show Font Metrics");
            putValue(MNEMONIC_KEY, KeyEvent.VK_F);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            root.panel.view.getSharedContext().setDebug_draw_font_metrics(!root.panel.view.getSharedContext().debugDrawFontMetrics());
            root.panel.view.repaint();
        }
    }

    final class NextDemoAction extends AbstractAction {

        NextDemoAction() {
            super("Next Demo Page");
            putValue(MNEMONIC_KEY, KeyEvent.VK_N);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        /**
         * Invoked when an action occurs.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            navigateToNextDemo();
        }
    }

    public void navigateToNextDemo() {
        String nextPage = null;
        for (Iterator<String> iter = allDemos.keySet().iterator(); iter.hasNext();) {
            String s = iter.next();
            if (s.equals(lastDemoOpened)) {
                if (iter.hasNext()) {
                    nextPage = iter.next();
                    break;
                }
            }
        }
        if (nextPage == null) {
            // go to first page
            Iterator<String> iter = allDemos.keySet().iterator();
            nextPage = iter.next();
        }

        try {
            root.panel.loadPage(allDemos.get(nextPage));
            lastDemoOpened = nextPage;
        } catch (Exception ex) {
            Uu.p(ex);
        }
    }

    final class PriorDemoAction extends AbstractAction {

        PriorDemoAction() {
            super("Prior Demo Page");
            putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        }

        /**
         * Invoked when an action occurs.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            navigateToPriorDemo();
        }
    }

    public void navigateToPriorDemo() {
        String priorPage = null;
        for (String s : allDemos.keySet()) {
            if (s.equals(lastDemoOpened)) {
                break;
            }
            priorPage = s;
        }
        if (priorPage == null) {
            // go to last page
            for (String s : allDemos.keySet()) {
                priorPage = s;
            }
        }

        try {
            root.panel.loadPage(allDemos.get(priorPage));
            lastDemoOpened = priorPage;
        } catch (Exception ex) {
            Uu.p(ex);
        }
    }

    class LoadAction extends AbstractAction {
        private final String url;
        private final String pageName;

        LoadAction(String name, String url) {
            super(name);
            pageName = name;
            this.url = url;
        }

        @Override
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
        private final int fontSizeThreshold;

        AntiAliasedAction(String text, int fontSizeThreshold) {
            super(text);
            this.fontSizeThreshold = fontSizeThreshold;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            root.panel.view.getSharedContext().getTextRenderer().setSmoothingThreshold(fontSizeThreshold);
            root.panel.view.repaint();
        }
    }

}


class EmptyAction extends AbstractAction {
    EmptyAction(String name, Icon icon) {
        this(name, "", icon);
    }

    EmptyAction(String name, String shortDesc, Icon icon) {
        super(name, icon);
        putValue(Action.SHORT_DESCRIPTION, shortDesc);
    }

    EmptyAction(String name, int accel) {
        this(name);
        putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(accel,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }

    EmptyAction(String name) {
        super(name);
    }

    @Override
    public final void putValue(String key, Object newValue) {
        super.putValue(key, newValue);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
    }
}
