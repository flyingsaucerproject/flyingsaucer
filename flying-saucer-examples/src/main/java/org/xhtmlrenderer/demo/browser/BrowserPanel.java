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

import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.PDFCreationListener;
import org.xhtmlrenderer.pdf.util.XHtmlMetaToPdfInfoAdapter;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.swing.ImageResourceLoader;
import org.xhtmlrenderer.swing.ScalableXHTMLPanel;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

public final class BrowserPanel extends JPanel implements DocumentListener {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BrowserPanel.class);
    private static final Logger logger = Logger.getLogger("app.browser");

    private static final int maxLineLength = 80;

    private final JButton forward;
    private final JButton backward;
    private final JButton reload;
    private final JButton goHome;
    final JTextField url;
    final BrowserStatus status;
    public final ScalableXHTMLPanel view;
    private final BrowserStartup root;
    private final BrowserPanelListener listener;
    private final JButton print_preview;
    private final PanelManager manager;
    private final JButton goToPage;
    final JToolBar toolbar;

    public BrowserPanel(BrowserStartup root, BrowserPanelListener listener) {
        this.root = root;
        this.listener = listener;

        forward = new JButton();
        backward = new JButton();
        reload = new JButton();
        goToPage = new JButton();
        goHome = new JButton();

        url = new JTextField();
        url.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                url.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                url.select(0, 0);
            }
        });


        manager = new PanelManager();
        view = new ScalableXHTMLPanel(manager);
        manager.setRepaintListener(view);
        ImageResourceLoader irl = new ImageResourceLoader(view);
        manager.setImageResourceLoader(irl);
        view.getSharedContext().setReplacedElementFactory(new SwingReplacedElementFactory(view, irl));
        view.addDocumentListener(manager);
        view.setCenteredPagedView(true);
        view.setBackground(Color.LIGHT_GRAY);
        JScrollPane scroll = new FSScrollPane(view);
        print_preview = new JButton();

        loadCustomFonts();

        status = new BrowserStatus();

        toolbar = initToolbar();

        int text_width = 200;
        view.setPreferredSize(new Dimension(text_width, text_width));

        setLayout(new BorderLayout());
        this.add(scroll, BorderLayout.CENTER);

        createActions();
    }

    private JToolBar initToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setRollover(true);
        toolbar.add(backward);
        toolbar.add(forward);
        toolbar.add(reload);
        toolbar.add(goHome);
        toolbar.add(url);
        toolbar.add(goToPage);
        // disabled for R6
        // toolbar.add(print);
        toolbar.setFloatable(false);
        return toolbar;
    }

    private void loadCustomFonts() {
        SharedContext rc = view.getSharedContext();
        try {
            rc.setFontMapping("Fuzz", Font.createFont(Font.TRUETYPE_FONT,
                    requireNonNull(DemoMarker.class.getResourceAsStream("/demos/fonts/fuzz.ttf"))));
        } catch (Exception ex) {
            Uu.p(ex);
        }
    }

    private void createActions() {
        // set text to "" to avoid showing action text in button--
        // we only want it in menu items
        backward.setAction(root.actions.backward);
        backward.setText("");
        forward.setAction(root.actions.forward);
        forward.setText("");
        reload.setAction(root.actions.reload);
        reload.setText("");
        goHome.setAction(root.actions.goHome);
        goHome.setText("");
        print_preview.setAction(root.actions.print_preview);
        print_preview.setText("");

        url.setAction(root.actions.load);
        goToPage.setAction(root.actions.goToPage);
        updateButtons();
    }


    public void goForward() {
        String uri = manager.getForward();
        view.setDocument(uri);
        updateButtons();
    }

    public void goBack() {
        String uri = manager.getBack();
        view.setDocument(uri);
        updateButtons();
    }

    public void reloadPage() {
        logger.info("Reloading Page: ");
        if (manager.getBaseURL() != null) {
            loadPage(manager.getBaseURL());
        }
    }

    //TODO: make this part of an implementation of UserAgentCallback instead
    public void loadPage(final String url_text) {
        try {
            logger.info("Loading Page: " + url_text);
            view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            view.setDocument(url_text);
            view.addDocumentListener(this);

            updateButtons();

            setStatus("Successfully loaded: " + url_text);

            if (listener != null) {
                listener.pageLoadSuccess(url_text, view.getDocumentTitle());
            }
        } catch (XRRuntimeException ex) {
            XRLog.general(Level.SEVERE, "Runtime exception", ex);
            setStatus("Can't load document");
            handlePageLoadFailed(url_text, ex);
        } catch (Exception ex) {
            XRLog.general(Level.SEVERE, "Could not load page for display.", ex);
            log.error("Failed to load page for display", ex);
        }
    }

    public void exportToPdf(String path) throws IOException, ParserConfigurationException, SAXException {
        if (manager.getBaseURL() != null) {
            setStatus("Exporting to " + path + "...");
            try (OutputStream os = Files.newOutputStream(Paths.get(path))) {
                ITextRenderer renderer = new ITextRenderer();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(manager.getBaseURL());

                PDFCreationListener pdfCreationListener = new XHtmlMetaToPdfInfoAdapter(doc);
                renderer.setListener(pdfCreationListener);

                renderer.createPDF(doc, os);
                setStatus("Done export.");
            }
        }
    }

    private void handlePageLoadFailed(String url_text, XRRuntimeException ex) {
        final XMLResource xr;
        final String rootCause = getRootCause(ex);
        final String msg = GeneralUtil.escapeHTML(addLineBreaks(rootCause));
        String notFound =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE html PUBLIC \" -//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
                        "<body>\n" +
                        "<h1>Document can't be loaded</h1>\n" +
                        "<p>Could not load the page at \n" +
                        "<pre>" + GeneralUtil.escapeHTML(url_text) + "</pre>\n" +
                        "</p>\n" +
                        "<p>The page failed to load; the error was </p>\n" +
                        "<pre>" + msg + "</pre>\n" +
                        "</body>\n" +
                        "</html>";

        xr = XMLResource.load(new StringReader(notFound));
        SwingUtilities.invokeLater(() -> root.panel.view.setDocument(xr.getDocument(), null));
   }

   private String addLineBreaks(String _text) {
        StringBuilder broken = new StringBuilder(_text.length() + 10);
        boolean needBreak = false;
        for (int i = 0; i < _text.length(); i++) {
            if (i > 0 && i % maxLineLength == 0) needBreak = true;

            final char c = _text.charAt(i);
            if (needBreak && Character.isWhitespace(c)) {
                System.out.println("Breaking: " + broken);
                needBreak = false;
                broken.append('\n');
            } else {
                broken.append(c);
            }
        }
        System.out.println("Broken! " + broken);
        return broken.toString();
    }

    private String getRootCause(Exception ex) {
        // FIXME
        Throwable cause = ex;
        while (cause != null) {
            cause = cause.getCause();
        }

        return cause == null ? ex.getMessage() : cause.getMessage();
    }

    @Override
    public void documentStarted() {
        // TODO...
    }

    @Override
    public void documentLoaded() {
        view.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Sets the status attribute of the BrowserPanel object
     *
     * @param txt The new status value
     */
    public void setStatus(String txt) {
        status.text.setText(txt);
    }

    protected void updateButtons() {
        root.actions.backward.setEnabled(manager.hasBack());
        root.actions.forward.setEnabled(manager.hasForward());
        url.setText(manager.getBaseURL());
    }


    @Override
    public void onLayoutException(Throwable t) {
        // TODO: clean
        log.error(t.toString(), t);
    }

    @Override
    public void onRenderException(Throwable t) {
        // TODO: clean
        log.error(t.toString(), t);
    }
}
