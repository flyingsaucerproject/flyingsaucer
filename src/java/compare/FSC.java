/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Patrick Wright
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
package compare;

import org.jdesktop.jdic.browser.WebBrowser;
import org.jdesktop.jdic.browser.WebBrowserEvent;
import org.jdesktop.jdic.browser.WebBrowserListener;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * CBrowser is a mini-application to test the Flying Saucer renderer across a set of
 * XML/CSS files, by comparing it with an embedded Gecko (Firefox) browser component
 * using JDIC. The Gecko renderer renders to an AWT component, and FS is used to render
 * to an image, which is overlaid on the Gecko component.
 *
 * @author Who?
 */
public class FSC {
    WebBrowser geckoBrowser;

    /**
     * Constructor for the Eeze object
     */
    private FSC() {
    }

    /**
     * Main processing method for the Eeze object
     *
     * @param args PARAM
     */
    private void run(String args[]) {
        buildFrame();
        directory = args[0];
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    File fontFile = new File(directory + "/support/AHEM____.TTF");
                    if (fontFile.exists()) {
                        html.getSharedContext().setFontMapping("Ahem",
                                Font.createFont(Font.TRUETYPE_FONT, fontFile.toURL().openStream()));
                    }
                } catch (FontFormatException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
        testFiles = buildFileList();
        try {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //switchPage( (File)testFiles.get( 0 ) );
                    showHelpPage();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    private List buildFileList() {
        List fileList = null;
        try {
            File dir = new File(directory);
            if (!dir.isDirectory()) {
                showUsage("Please enter a directory name (not a file name).");
                System.exit(-1);
            }
            File list[] = dir.listFiles(HTML_FILE_FILTER);
            fileList = Arrays.asList(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return fileList;
    }

    /**
     * Description of the Method
     */
    private void buildFrame() {
        try {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
            }

            cbFrame = new JFrame("FS ComparaBrowser");
            final JFrame frame = cbFrame;
            frame.setExtendedState(JFrame.NORMAL);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    geckoBrowser = new WebBrowser();
                    geckoBrowser.setSize(new Dimension(0, 0));
                    geckoBrowser.addWebBrowserListener(
                            new WebBrowserListener() {
                                boolean isFirstPage = true;

                                public void downloadStarted(WebBrowserEvent event) {
                                    XRLog.load("JDIC: browser loading " + event.getData());
                                }

                                public void downloadCompleted(WebBrowserEvent event) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            cbFrame.requestFocus();
                                            System.out.println("Focus requested");
                                        }
                                    });
                                }

                                public void downloadProgress(WebBrowserEvent event) {
                                    ;
                                }

                                public void downloadError(WebBrowserEvent event) {
                                    ;
                                }

                                public void documentCompleted(WebBrowserEvent event) {
                                }

                                public void titleChange(WebBrowserEvent event) {
                                    frame.setTitle(event.getData());
                                }

                                public void statusTextChange(WebBrowserEvent event) {
                                    ;
                                }
                            });


                    final JScrollPane gscroll = new JScrollPane(geckoBrowser);
                    gscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    //gscroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                    gscroll.addComponentListener(new ComponentAdapter() {
                        public void componentResized(ComponentEvent e) {
                            geckoBrowser.setSize(gscroll.getSize());
                        }
                    });
                    JPanel gPanel = new JPanel();
                    gPanel.setLayout(new BorderLayout());
                    gPanel.setPreferredSize(new Dimension(0, 0));
                    gPanel.add(gscroll, BorderLayout.CENTER);

                    html = new XHTMLPanel();
                    html.setLayoutInProgressMsg("");
                    html.setAntiAliased(false);
                    FSScrollPane fspane = new FSScrollPane(html);
                    fspane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

                    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fspane, gPanel);
                    splitPane.setDividerLocation(0.5D);
                    splitPane.setDividerSize(5);
                    splitPane.setResizeWeight(.5);
                    splitPane.setContinuousLayout(false);
                    splitPane.setOpaque(true);

                    frame.getContentPane().add(splitPane);
                    frame.pack();
                    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    frame.setVisible(true);

                    frame.addComponentListener(new ComponentAdapter() {
                        public void componentResized(ComponentEvent e) {
                            html.relayout();
                        }
                    });

                    nextDemoAction = new NextDemoAction();
                    reloadPageAction = new ReloadPageAction();
                    chooseDemoAction = new ChooseDemoAction();
                    growAction = new GrowAction();
                    shrinkAction = new ShrinkAction();

                    increase_font = new FontSizeAction(FontSizeAction.INCREMENT, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
                    reset_font = new FontSizeAction(FontSizeAction.RESET, KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK));
                    decrease_font = new FontSizeAction(FontSizeAction.DECREMENT, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));

                    reloadFileList = new ReloadFileListAction();
                    showGrid = new ShowGridAction();
                    showHelp = new ShowHelpAction();

                    frame.setJMenuBar(new JMenuBar());
                    JMenu doMenu = new JMenu("Do");
                    doMenu.add(new JMenuItem(reloadPageAction));
                    doMenu.add(new JMenuItem(nextDemoAction));
                    doMenu.add(new JMenuItem(chooseDemoAction));
                    doMenu.add(new JMenuItem(growAction));
                    doMenu.add(new JMenuItem(shrinkAction));
                    doMenu.add(new JMenuItem(increase_font));
                    doMenu.add(new JMenuItem(reset_font));
                    doMenu.add(new JMenuItem(decrease_font));
                    doMenu.add(new JMenuItem(showGrid));
                    doMenu.add(new JMenuItem(reloadFileList));
                    doMenu.add(new JMenuItem(showHelp));
                    doMenu.setVisible(true);
                    frame.getJMenuBar().add(doMenu);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @param file PARAM
     */
    private void switchPage(File file, boolean reload) {
        cbFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            if (reload) {
                XRLog.load("Reloading " + currentDisplayed);
                html.reloadDocument(file.toURL().toExternalForm());
                geckoBrowser.setURL(file.toURI().toURL());
            } else {
                XRLog.load("Loading " + currentDisplayed);
                html.setDocument(file.toURL().toExternalForm());
                geckoBrowser.setURL(file.toURI().toURL());
            }
            XRLog.load("LOADED " + file.toURI().toURL());
            currentDisplayed = file;
            changeTitle(file.toURL().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            cbFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Description of the Method
     */
    private void showHelpPage() {
        cbFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            URL help = helpUrl();
            html.setDocument(help.openStream(), help.toString());
            geckoBrowser.setURL(help);
            changeTitle(html.getDocumentTitle());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            cbFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Description of the Method
     *
     * @param hdelta PARAM
     * @param vdelta PARAM
     */
    private void resizeFrame(float hdelta, float vdelta) {
        Dimension d = cbFrame.getSize();
        cbFrame.setSize((int) (d.getWidth() * hdelta),
                (int) (d.getHeight() * vdelta));
    }

    /**
     * Description of the Method
     *
     * @param newPage PARAM
     */
    private void changeTitle(String newPage) {
        cbFrame.setTitle("ComparaBrowser:  " + html.getDocumentTitle() + "  (" + newPage + ")");
    }

    /**
     * Description of the Method
     *
     * @return Returns
     */
    private URL helpUrl() {
        return this.getClass().getClassLoader().getResource("compare/fsc_help.html");
    }

    /**
     * Description of the Method
     *
     * @param args PARAM
     */
    public static void main(String args[]) {
        try {
            if (args.length == 0) {
                showUsage("ComparaBrowser needs some information to work.");
                System.exit(-1);
            }
            new FSC().run(args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @param error PARAM
     */
    private static void showUsage(String error) {
        StringBuffer sb = new StringBuffer();

        sb.append("Oops! " + error + " \n")
                .append(" \n")
                .append("ComparaBrowser (CBrowser) \n")
                .append("  A tool to compare Gecko with Flying Saucer \n")
                .append(" \n")
                .append(" Usage: \n")
                .append("    java compare.CBrowser {directory}\n")
                .append(" \n")
                .append(" where {directory} is a directory containing XHTML/XML files.\n")
                .append(" jdic.jar must be on your classpath, and JDIC libraries must be\n")
                .append(" on your java.library.path\n")
                .append(" \n")
                .append(" All files ending in .*htm* are loaded in a list, in alphabetical \n")
                .append(" order. The first is rendered. Use Alt-h to show keyboard navigation \n")
                .append(" shortcuts.\n")
                .append(" \n");
        System.out.println(sb.toString());
    }

    /**
     * Description of the Class
     *
     * @author Who?
     */
    static class GridGlassPane extends JPanel {

        /**
         * Description of the Field
         */
        private final Color mainUltraLightColor = new Color(128, 192, 255);
        /**
         * Description of the Field
         */
        private final Color mainLightColor = new Color(0, 128, 255);
        /**
         * Description of the Field
         */
        private final Color mainMidColor = new Color(0, 64, 196);
        /**
         * Description of the Field
         */
        private final Color mainDarkColor = new Color(0, 0, 128);

        /**
         * Constructor for the GridGlassPane object
         */
        public GridGlassPane() {
            // intercept mouse and keyboard events and do nothing
            this.addMouseListener(new MouseAdapter() {
            });
            this.addMouseMotionListener(new MouseMotionAdapter() {
            });
            this.addKeyListener(new KeyAdapter() {
            });
            this.setOpaque(false);
        }

        /**
         * Description of the Method
         *
         * @param g PARAM
         */
        protected void paintComponent(Graphics g) {
            Graphics2D graphics = (Graphics2D) g;
            BufferedImage oddLine = createGradientLine(this.getWidth(), mainLightColor,
                    mainDarkColor, 0.6);
            BufferedImage evenLine = createGradientLine(this
                    .getWidth(), mainUltraLightColor,
                    mainMidColor, 0.6);

            int width = this.getWidth();
            int height = this.getHeight();
            for (int row = 0; row < height; row = row + 10) {
                if ((row % 2) == 0) {
                    graphics.drawImage(evenLine, 0, row, null);
                } else {
                    graphics.drawImage(oddLine, 0, row, null);
                }
            }
        }


        /**
         * Description of the Method
         *
         * @param width      PARAM
         * @param leftColor  PARAM
         * @param rightColor PARAM
         * @param opacity    PARAM
         * @return Returns
         */
        public BufferedImage createGradientLine(int width, Color leftColor,
                                                Color rightColor, double opacity) {
            BufferedImage image = new BufferedImage(width, 1,
                    BufferedImage.TYPE_INT_ARGB);
            int iOpacity = (int) (255 * opacity);

            for (int col = 0; col < width; col++) {
                double coef = (double) col / (double) width;
                int r = (int) (leftColor.getRed() + coef
                        * (rightColor.getRed() - leftColor.getRed()));
                int g = (int) (leftColor.getGreen() + coef
                        * (rightColor.getGreen() - leftColor.getGreen()));
                int b = (int) (leftColor.getBlue() + coef
                        * (rightColor.getBlue() - leftColor.getBlue()));

                int color = (iOpacity << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(col, 0, color);
            }
            return image;
        }
    }

    /**
     * Action to trigger frame to grow in size.
     *
     * @author Who?
     */
    class GrowAction extends AbstractAction {
        /**
         * Description of the Field
         */
        private float increment = 1.1F;

        /**
         * Constructor for the GrowAction object
         */
        public GrowAction() {
            super("Grow Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_G));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.ALT_MASK));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(ActionEvent e) {
            resizeFrame(increment, increment);
        }
    }

    /**
     * Action to show a grid over the current page
     *
     * @author Who?
     */
    class ShowGridAction extends AbstractAction {
        private boolean on;
        private Component originalGlassPane;
        private GridGlassPane gridGlassPane;

        /**
         * Constructor for the ShowGridAction object
         */
        public ShowGridAction() {
            super("Show Grid");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_G));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.ALT_MASK));
            gridGlassPane = new GridGlassPane();
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(ActionEvent e) {
            if (on) {
                cbFrame.setGlassPane(originalGlassPane);
                gridGlassPane.setVisible(false);
            } else {
                originalGlassPane = cbFrame.getGlassPane();
                cbFrame.setGlassPane(gridGlassPane);
                gridGlassPane.setVisible(true);
            }
            on = !on;
        }
    }

    /**
     * Action to trigger frame to shrink in size.
     *
     * @author Who?
     */
    class ShrinkAction extends AbstractAction {
        /**
         * Description of the Field
         */
        private float increment = 1 / 1.1F;

        /**
         * Constructor for the ShrinkAction object
         */
        public ShrinkAction() {
            super("Shrink Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_MASK));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(ActionEvent e) {
            resizeFrame(increment, increment);
        }
    }

    /**
     * Description of the Class
     *
     * @author Who?
     */
    class ShowHelpAction extends AbstractAction {

        /**
         * Constructor for the ShowHelpAction object
         */
        public ShowHelpAction() {
            super("Show Help Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.ALT_MASK));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(ActionEvent e) {
            showHelpPage();
        }
    }

    /**
     * Description of the Class
     *
     * @author Who?
     */
    class NextDemoAction extends AbstractAction {

        /**
         * Constructor for the ReloadPageAction object
         */
        public NextDemoAction() {
            super("Next Demo Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(ActionEvent e) {
            File nextPage = null;
            for (Iterator iter = testFiles.iterator(); iter.hasNext();) {
                File f = (File) iter.next();
                if (f.equals(currentDisplayed)) {
                    if (iter.hasNext()) {
                        nextPage = (File) iter.next();
                        break;
                    }
                }
            }
            if (nextPage == null) {
                // go to first page
                Iterator iter = testFiles.iterator();
                nextPage = (File) iter.next();
            }

            try {
                switchPage(nextPage, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Description of the Class
     *
     * @author Who?
     */
    class ReloadPageAction extends AbstractAction {

        /**
         * Constructor for the ReloadPageAction object
         */
        public ReloadPageAction() {
            super("Reload Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_R));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_MASK));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(ActionEvent e) {
            try {
                switchPage(currentDisplayed, true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Description of the Class
     *
     * @author Who?
     */
    class ChooseDemoAction extends AbstractAction {

        /**
         * Constructor for the ReloadPageAction object
         */
        public ChooseDemoAction() {
            super("Choose Demo Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_MASK));
        }

        /**
         * Invoked when an action occurs.
         *
         * @param e PARAM
         */
        public void actionPerformed(ActionEvent e) {
            File nextPage = (File) JOptionPane.showInputDialog(cbFrame,
                    "Choose a demo file",
                    "Choose Demo",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    testFiles.toArray(),
                    currentDisplayed);

            try {
                switchPage(nextPage, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    class ReloadFileListAction extends AbstractAction {
        public ReloadFileListAction() {
            super("Reload File List Page");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_F));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_MASK));
        }

        public void actionPerformed(ActionEvent e) {
            testFiles = buildFileList();
            currentDisplayed = (File) testFiles.get(0);
            reloadPageAction.actionPerformed(null);
        }
    }

    /**
     * Description of the Class
     *
     * @author Who?
     */
    class FontSizeAction extends AbstractAction {
        /**
         * Description of the Field
         */
        private int whichDirection;

        /**
         * Description of the Field
         */
        final static int DECREMENT = 0;
        /**
         * Description of the Field
         */
        final static int INCREMENT = 1;
        /**
         * Description of the Field
         */
        final static int RESET = 2;

        /**
         * Constructor for the FontSizeAction object
         *
         * @param which PARAM
         * @param ks    PARAM
         */
        public FontSizeAction(int which, KeyStroke ks) {
            super("FontSize");
            this.whichDirection = which;
            this.putValue(Action.ACCELERATOR_KEY, ks);
        }

        /**
         * Constructor for the FontSizeAction object
         *
         * @param scale PARAM
         * @param which PARAM
         * @param ks    PARAM
         */
        public FontSizeAction(float scale, int which, KeyStroke ks) {
            this(which, ks);
            html.setFontScalingFactor(scale);
        }

        /**
         * Description of the Method
         *
         * @param evt PARAM
         */
        public void actionPerformed(ActionEvent evt) {
            switch (whichDirection) {
                case INCREMENT:
                    html.incrementFontSize();
                    break;
                case RESET:
                    html.resetFontSize();
                    break;
                case DECREMENT:
                    html.decrementFontSize();
                    break;
            }
        }
    }

    /**
     * Description of the Field
     */
    List testFiles;
    /**
     * Description of the Field
     */
    JFrame cbFrame;
    /**
     * Description of the Field
     */
    File currentDisplayed;
    /**
     * Description of the Field
     */
    Action growAction;
    /**
     * Description of the Field
     */
    Action shrinkAction;
    /**
     * Description of the Field
     */
    Action nextDemoAction;

    Action chooseDemoAction;

    /**
     * Description of the Field
     */
    Action increase_font, reset_font, decrease_font, showHelp, showGrid;

    /**
     * Description of the Field
     */
    private XHTMLPanel html;

    /**
     * Description of the Field
     */
    private String directory;

    /**
     * Description of the Field
     */
    private final static FileFilter HTML_FILE_FILTER =
            new FileFilter() {
                public boolean accept(File f) {
                    return f.getName().endsWith(".html") ||
                            f.getName().endsWith(".htm") ||
                            f.getName().endsWith(".xhtml") ||
                            f.getName().endsWith(".xml");
                }
            };
    private ReloadPageAction reloadPageAction;
    private ReloadFileListAction reloadFileList;
}// end class

