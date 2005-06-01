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
package eeze;

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
 * Eeze is a mini-application to test the Flying Saucer renderer across a set of
 * XML/CSS files.
 *
 * @author Who?
 */
public class Eeze {
    /**
     * Description of the Field
     */
    List testFiles;
    /**
     * Description of the Field
     */
    JFrame eezeFrame;
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
                    return f.getName().endsWith("html") ||
                            f.getName().endsWith("htm") ||
                            f.getName().endsWith("xml");
                }
            };

    /**
     * Constructor for the Eeze object
     */
    private Eeze() {
    }

    /**
     * Main processing method for the Eeze object
     *
     * @param args PARAM
     */
    private void run(String args[]) {
        buildFrame();
        directory = args[0];
        testFiles = buildFileList();
        try {
            //switchPage( (File)testFiles.get( 0 ) );
            showHelpPage();
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
            eezeFrame = new JFrame("FS Eeze");
            final JFrame frame = eezeFrame;
            frame.setExtendedState(JFrame.NORMAL);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    html = new XHTMLPanel();
                    frame.getContentPane().add(new FSScrollPane(html));
                    frame.pack();
                    frame.setSize(1024, 768);
                    frame.setVisible(true);

                    frame.addComponentListener(new ComponentAdapter() {
                        public void componentResized(ComponentEvent e) {
                            html.relayout();
                        }
                    });

                    nextDemoAction = new NextDemoAction();
                    growAction = new GrowAction();
                    shrinkAction = new ShrinkAction();

                    increase_font = new FontSizeAction(FontSizeAction.INCREMENT, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
                    reset_font = new FontSizeAction(FontSizeAction.RESET, KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK));
                    decrease_font = new FontSizeAction(FontSizeAction.DECREMENT, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));

                    showGrid = new ShowGridAction();
                    showHelp = new ShowHelpAction();

                    frame.setJMenuBar(new JMenuBar());
                    JMenu doMenu = new JMenu("Do");
                    doMenu.add(nextDemoAction);
                    doMenu.add(growAction);
                    doMenu.add(shrinkAction);
                    doMenu.add(increase_font);
                    doMenu.add(reset_font);
                    doMenu.add(decrease_font);
                    doMenu.add(showGrid);
                    doMenu.add(showHelp);
                    doMenu.setVisible(false);
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
    private void switchPage(File file) {
        XRLog.load("Loading " + currentDisplayed);
        eezeFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            html.setDocument(file.toURL());
            currentDisplayed = file;
            changeTitle(file.toURL().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            eezeFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Description of the Method
     */
    private void showHelpPage() {
        eezeFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            URL help = eezeHelp();
            html.setDocument(help.openStream(), help.toString());
            changeTitle(html.getDocumentTitle());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            eezeFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Description of the Method
     *
     * @param hdelta PARAM
     * @param vdelta PARAM
     */
    private void resizeFrame(float hdelta, float vdelta) {
        Dimension d = eezeFrame.getSize();
        eezeFrame.setSize((int) (d.getWidth() * hdelta),
                (int) (d.getHeight() * vdelta));
    }

    /**
     * Description of the Method
     *
     * @param newPage PARAM
     */
    private void changeTitle(String newPage) {
        eezeFrame.setTitle("Eeze:  " + html.getDocumentTitle() + "  (" + newPage + ")");
    }

    /**
     * Description of the Method
     *
     * @return Returns
     * @throws IOException Throws
     */
    private URL eezeHelp()
            throws IOException {
        return this.getClass().getClassLoader().getResource("eeze/eeze_help.html");
    }

    /**
     * Description of the Method
     *
     * @param args PARAM
     */
    public static void main(String args[]) {
        try {
            if (args.length == 0) {
                showUsage("Eeze needs some information to work.");
                System.exit(-1);
            }
            new Eeze().run(args);
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
                .append("Eeze \n")
                .append("  A frame to walk through a set of XHTML/XML pages with Flying Saucer \n")
                .append(" \n")
                .append(" Usage: \n")
                .append("    java eeze.Eeze {directory}\n")
                .append(" \n")
                .append(" where {directory} is a directory containing XHTML/XML files.\n")
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
    class GridGlassPane extends JPanel {

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
                eezeFrame.setGlassPane(originalGlassPane);
                gridGlassPane.setVisible(false);
            } else {
                originalGlassPane = eezeFrame.getGlassPane();
                eezeFrame.setGlassPane(gridGlassPane);
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
         * Constructor for the NextDemoAction object
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
                switchPage(nextPage);
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
}// end class

