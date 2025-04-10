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

import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

public class BrowserStartup {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BrowserStartup.class);
    private static final Logger logger = Logger.getLogger("app.browser");

    public final BrowserPanel panel;
    final JFrame frame;
    @Nullable
    JFrame validation_console = null;
    final BrowserActions actions;
    private final String startPage;
    final ValidationHandler error_handler = new ValidationHandler();

    public BrowserStartup() {
        this("/demos/splash/splash.html");
    }

    public BrowserStartup(String startPage) {
        logger.info("starting up");
        URL url = requireNonNull(getClass().getResource(startPage),
                () -> "Start page not found in classpath: " + startPage);
        this.startPage = url.toExternalForm();

        setLookAndFeel();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame = frame;
        logger.info("creating UI");
        actions = new BrowserActions(this, startPage);

        panel = new BrowserPanel(this, new FrameBrowserPanelListener());

        frame.setJMenuBar(new BrowserMenuBar(this));

        frame.getContentPane().add(panel.toolbar, BorderLayout.PAGE_START);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        frame.getContentPane().add(panel.status, BorderLayout.PAGE_END);
        frame.pack();
        frame.setSize(1024, 768);
    }

    /**
     * The main program for the BrowserStartup class
     *
     * @param args The command line arguments
     */
    public static void main(final String[] args) {
        EventQueue.invokeLater(() -> {
            BrowserStartup bs = new BrowserStartup();
            bs.launch();
        });
    }

    /**
     * Loads the first page (specified in the constructor) and shows the frame.
     */
    public void launch() {
        try {
            panel.loadPage(startPage);

            frame.setVisible(true);
        } catch (Exception ex) {
            XRLog.general(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private static void setLookAndFeel() {
        if (GeneralUtil.isMacOSX()) {
            setLookAndFeel_mac();
        } else {
            setLookAndFeel_nonMac();
        }
    }

    private static void setLookAndFeel_mac() {
        try {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "FS Browser");
        } catch (Exception e) {
            try {
                logger.log(Level.SEVERE, "error initializing the mac properties", e);
            } catch (Exception ex2) {
                log.error("error writing to the log file", ex2);
            }
        }
    }

    private static void setLookAndFeel_nonMac() {
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        } catch (Throwable e) {
            log.debug("Failed to use JGoodies look and feel", e);

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable ex) {
                log.error("Failed to use system look and feel", ex);
            }
        }
    }

    class FrameBrowserPanelListener implements BrowserPanelListener {
        @Override
        public void pageLoadSuccess(String url, String title) {
            frame.setTitle(title + (!title.isEmpty() ? " - " : "") + "Flying Saucer");
        }
    }

}
