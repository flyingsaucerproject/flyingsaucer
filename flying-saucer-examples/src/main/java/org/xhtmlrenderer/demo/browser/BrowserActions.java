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

import com.google.errorprone.annotations.CheckReturnValue;
import org.xhtmlrenderer.demo.browser.actions.FontSizeAction;
import org.xhtmlrenderer.demo.browser.actions.FontSizeAction.FontSizeChange;
import org.xhtmlrenderer.demo.browser.actions.GenerateDiffAction;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.Uu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.util.Objects.requireNonNull;

public class BrowserActions {
    private static final Logger logger = Logger.getLogger("app.browser");

    final Action open_file;
    final Action export_pdf;
    final Action quit;
    final Action forward, backward, refresh, reload, load, print_preview, goHome;
    final Action generate_diff, usersManual, aboutPage;
    private final BrowserStartup root;
    final Action increase_font, decrease_font, reset_font;
    final Action goToPage;

    public BrowserActions(BrowserStartup root, String startPage) {
        this.root = root;

        open_file = action("Open File...", e -> openAndShowFile());

        setAccel(open_file, KeyEvent.VK_O);
        setMnemonic(open_file, KeyEvent.VK_O);


        export_pdf = action("Export PDF...", e -> exportToPdf());
        export_pdf.setEnabled(isOpenPdfInClasspath());

        quit = action("Quit", e -> System.exit(0));

        setAccel(quit, KeyEvent.VK_Q);
        setMnemonic(quit, KeyEvent.VK_Q);

        backward = new EmptyAction("Back", "Go back one page", imageIcon("images/go-previous.png"), e -> {
            root.panel.goBack();
            root.panel.view.repaint();
        });

        backward.setEnabled(false);
        backward.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(VK_LEFT, ALT_DOWN_MASK));


        forward = new EmptyAction("Forward", "Go forward one page", imageIcon("images/go-next.png"), e -> {
            root.panel.goForward();
            root.panel.view.repaint();
        });
        forward.setEnabled(false);
        forward.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ALT_DOWN_MASK));

        refresh = new EmptyAction("Refresh", "Refresh page", imageIcon("images/view-refresh.png"), e -> {
            root.panel.view.invalidate();
            root.panel.view.repaint();
        });
        refresh.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("F5"));

        reload = new EmptyAction("Reload", "Reload page", imageIcon("images/view-refresh.png"), e -> {
            root.panel.reloadPage();
            root.panel.view.repaint();
        });
        reload.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, SHIFT_DOWN_MASK));
        reload.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);

        print_preview = new EmptyAction("Print Preview", "Print preview mode", null, e -> togglePrintPreview());
        print_preview.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);

        load = action("Load", e -> {
            String url_text = root.panel.url.getText();
            root.panel.loadPage(url_text);
            root.panel.view.repaint();
        });
        goToPage = new EmptyAction("Go", "Go to URL in address bar", imageIcon("images/media-playback-start_16x16.png"), e -> {
            String url_text = root.panel.url.getText();
            root.panel.loadPage(url_text);
            root.panel.view.repaint();
        });

        goHome = new EmptyAction("Go Home", "Browser homepage", imageIcon("images/go-home.png"), e -> {
            root.panel.loadPage(startPage);
            root.panel.view.repaint();
        });

        usersManual = new EmptyAction("FS User's Guide", "Flying Saucer User's Guide", null, e -> {
            root.panel.loadPage("/users-guide-r8.html");
            root.panel.view.repaint();
        });

        aboutPage = new EmptyAction("About", "About the Browser Demo", null, e -> showAboutDialog());

        generate_diff = new GenerateDiffAction(root);

        increase_font = new FontSizeAction("Increase", root, FontSizeChange.INCREMENT);
        increase_font.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        increase_font.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);

        reset_font = new FontSizeAction("Normal", root, FontSizeChange.RESET);
        reset_font.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_0,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        reset_font.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);

        decrease_font = new FontSizeAction("Decrease", root, FontSizeChange.DECREMENT);
        decrease_font.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        decrease_font.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
    }

    @CheckReturnValue
    private boolean isOpenPdfInClasspath() {
        try {
            Class.forName("org.openpdf.text.DocumentException");
            return true;
        } catch (ClassNotFoundException ignore) {
            return false;
        }
    }

    private void showAboutDialog() {
        final JDialog aboutDlg = new JDialog(root.frame);
        aboutDlg.setSize(new Dimension(500, 450));

        PanelManager uac = new PanelManager();
        XHTMLPanel panel = new XHTMLPanel(uac);
        uac.setRepaintListener(panel);
        panel.setOpaque(false);

        panel.setDocument("demo:/demos/about.xhtml");

        JPanel outer = new JPanel(new BorderLayout());
        outer.add(panel, BorderLayout.CENTER);
        final JButton btn = new JButton(new AbstractAction("OK") {
            @Override
            public void actionPerformed(ActionEvent e) {
                aboutDlg.dispose();
            }
        });
        SwingUtilities.invokeLater(btn::requestFocusInWindow);
        JPanel control = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        control.add(btn);
        outer.add(control, BorderLayout.SOUTH);

        aboutDlg.getContentPane().setLayout(new BorderLayout());
        aboutDlg.getContentPane().add(outer, BorderLayout.CENTER);

        aboutDlg.setTitle("About the Browser Demo");

        int xx = (root.frame.getWidth() - aboutDlg.getWidth()) / 2;
        int yy = (root.frame.getHeight() - aboutDlg.getHeight()) / 2;
        aboutDlg.setLocation(xx, yy);
        aboutDlg.setModal(true);
        aboutDlg.setVisible(true);
    }

    private void togglePrintPreview() {
        try {
            SharedContext sharedContext = root.panel.view.getSharedContext();

            // flip status--either we are in "print" mode (print media) or non-print (screen media)
            if (sharedContext.isPrint()) {
                sharedContext.setPrint(false);
                sharedContext.setInteractive(true);
            } else {
                sharedContext.setPrint(true);
                sharedContext.setInteractive(false);
            }
            print_preview.putValue(Action.SHORT_DESCRIPTION,
                    !sharedContext.isPrint() ? "Print preview" : "Normal view");
            root.panel.reloadPage();
            root.panel.view.repaint();
        } catch (Exception ex) {
            Uu.p(ex);
        }
    }

    private void openAndShowFile() {
        try {
            FileDialog fd = new FileDialog(root.frame, "Open a local file", FileDialog.LOAD);
            fd.setVisible(true);
            if (fd.getFile() != null) {
                final String url = new File(fd.getDirectory(), fd.getFile()).toURI().toURL().toString();
                root.panel.loadPage(url);
            }
        } catch (Exception ex) {
            logger.info("error:" + ex);
        }
    }

    private void exportToPdf() {
        try {
            FileDialog fd = new FileDialog(root.frame, "Save as PDF", FileDialog.SAVE);
            fd.setVisible(true);
            if (fd.getFile() != null) {
                File outTarget = new File(fd.getDirectory(), fd.getFile());
                root.panel.exportToPdf(outTarget.getAbsolutePath());
            }
        } catch (Exception ex) {
            logger.info("error:" + ex);
        }
    }

    /**
     * Sets the accel attribute of the BrowserActions object
     */
    public static void setAccel(Action act, int key) {
        act.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(key,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    }

    /**
     * Sets the mnemonic attribute of the BrowserActions object
     */
    public static void setMnemonic(Action action, int mnemonicValue) {
        action.putValue(Action.MNEMONIC_KEY, mnemonicValue);
    }

    @CheckReturnValue
    private static ImageIcon imageIcon(String url) {
        URL imageUrl = requireNonNull(BrowserActions.class.getClassLoader().getResource(url),
                () -> "Resource not found in classpath: " + url);
        return new ImageIcon(imageUrl);
    }

    @CheckReturnValue
    private Action action(String name, Consumer<ActionEvent> handler) {
        return new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent event) {
                try {
                    handler.accept(event);
                } catch (Exception ex) {
                    Uu.p(ex);
                }
            }
        };
    }
}
