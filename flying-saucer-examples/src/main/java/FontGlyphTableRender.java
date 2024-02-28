/*
 * Copyright (c) 2008 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */


import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import org.w3c.dom.Document;
import org.xhtmlrenderer.event.DefaultDocumentListener;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.FSEntityResolver;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

/**
 * Opens a frame and displays, for a selected font, the glyphs for a range of Unicode code points. Can be used to
 * identify which glyphs are supported by a font. Can export to PDF. Requires core-renderer and OpenPDF on classpath.
 *
 * @author Patrick Wright
 */
public class FontGlyphTableRender {
    private static final int TO_SWING = 1;
    private static final int TO_PDF = 2;
    private static final String OUTPUT_ENTITIES = "entities";
    private static final String OUTPUT_CODEPOINTS = "codepoints";
    private static final int ENT_PER_PAGE = 399;  // based on number we can fit on one printed (PDF) part

    private int curFrom;

    private String outputType = OUTPUT_CODEPOINTS;
    private Font currentFont;
    private JFrame frame;
    private XHTMLPanel xpanel;
    private JTextField fontPathTF;
    private JTextField familyNameFieldAwt;
    private JTextField familyNameFieldIText;
    private JButton prevBtn;
    private JButton nextBtn;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FontGlyphTableRender().run());
    }

    private void run() {
        frame = new JFrame("Flying Saucer: Show Font Glyphs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JPanel optionsPanel = new JPanel(new BorderLayout());

        // TODO: don't know a good way to determine path where fonts are stored, per-os
        // otherwise we could display a drop-down of installed fonts on the machine
        // "/usr/share/fonts/truetype/freefont/FreeMono.ttf"
        fontPathTF = new JTextField();
        fontPathTF.setColumns(40);

        familyNameFieldAwt = new JTextField();
        familyNameFieldAwt.setEnabled(true);
        familyNameFieldAwt.setEditable(false);
        familyNameFieldAwt.setColumns(20);
        familyNameFieldIText = new JTextField();
        familyNameFieldIText.setEnabled(true);
        familyNameFieldIText.setEditable(false);
        familyNameFieldIText.setColumns(20);

        JPanel top1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top1.add(new JLabel("Enter font path: "));
        top1.add(fontPathTF);
        JButton chooseFontFileBtn = new JButton("...");
        chooseFontFileBtn.addActionListener(e -> {
            String filename = File.separator + "tmp";
            String famPath = fontPathTF.getText();
            if (currentFont != null && !famPath.isEmpty()) {
                filename = new File(famPath).getParent();
            }
            JFileChooser fc = new JFileChooser(new File(filename));
            fc.showOpenDialog(frame);
            File selFile = fc.getSelectedFile();
            Font font = null;
            String msg = "";
            try {
                font = loadFont(selFile.getPath());
            } catch (IOException e1) {
                // swallow, just allow font to be null
                msg = e1.getMessage();
            }
            if (font == null) {
                JOptionPane.showMessageDialog(frame, "Can't load file--is it a valid Font file? " + msg);
            } else {
                fontPathTF.setText(selFile.getPath());
                familyNameFieldAwt.setText(font.getFamily());
                familyNameFieldIText.setText(getITextFontFamilyName(selFile));
            }
        });
        top1.add(chooseFontFileBtn);
        ActionListener outputSelection = e -> {
            outputType = e.getActionCommand();
            enableButtons();
            if (currentFont != null) {
                deferredChangePage(curFrom);
            }
        };
        JRadioButton jrbCodePoint = new JRadioButton("Codepoints");
        jrbCodePoint.setActionCommand(OUTPUT_CODEPOINTS);
        jrbCodePoint.addActionListener(outputSelection);
        jrbCodePoint.setSelected(true);
        JRadioButton jrbEntities = new JRadioButton("Entities");
        jrbEntities.setActionCommand(OUTPUT_ENTITIES);
        jrbEntities.addActionListener(outputSelection);
        ButtonGroup bg = new ButtonGroup();
        bg.add(jrbCodePoint);
        bg.add(jrbEntities);
        top1.add(jrbCodePoint);
        top1.add(jrbEntities);

        JPanel top2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top2.add(new JLabel("Family (AWT): "));
        top2.add(familyNameFieldAwt);
        top2.add(new JLabel("Family (iText): "));
        top2.add(familyNameFieldIText);

        JPanel top = new JPanel(new BorderLayout());
        top.add(top1, BorderLayout.NORTH);
        top.add(top2, BorderLayout.CENTER);

        JPanel mid = new JPanel(new FlowLayout(FlowLayout.LEFT));
        prevBtn = new JButton("Prev");
        nextBtn = new JButton("Next");
        JButton pdfBtn = new JButton("PDF");
        JButton renderBtn = new JButton("Render");
        prevBtn.addActionListener(actionEvent -> deferredChangePage(curFrom - ENT_PER_PAGE));
        nextBtn.addActionListener(actionEvent -> deferredChangePage(curFrom + ENT_PER_PAGE));
        renderBtn.addActionListener(actionEvent -> deferredChangePage(curFrom));
        pdfBtn.addActionListener(actionEvent -> {
            resolveCurrentFont();
            if (currentFont == null) {
                JOptionPane.showMessageDialog(frame, "Need a valid font file path");
                fontPathTF.requestFocus();
                return;
            }
            deferredLoadAndRender(curFrom, TO_PDF);
        });
        mid.add(prevBtn);
        mid.add(nextBtn);
        mid.add(renderBtn);
        mid.add(pdfBtn);
        optionsPanel.add(top, BorderLayout.NORTH);
        optionsPanel.add(mid, BorderLayout.CENTER);
        fontPathTF.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                deferredChangePage(curFrom);
            }
        });

        // Create a JPanel subclass to render the page
        xpanel = new XHTMLPanel();
        xpanel.addDocumentListener(new DefaultDocumentListener() {
            @Override
            public void documentLoaded() {
                frame.setCursor(Cursor.getDefaultCursor());
            }
        });

        resetMouseListeners();

        // Put our xpanel in a scrolling pane. You can use
        // a regular JScrollPane here, or our FSScrollPane.
        // FSScrollPane is already set up to move the correct
        // amount when scrolling 1 line or 1 page
        FSScrollPane scroll = new FSScrollPane(xpanel);
        scroll.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel cont = new JPanel(new BorderLayout());
        cont.add(optionsPanel, BorderLayout.NORTH);
        cont.add(scroll, BorderLayout.CENTER);
        frame.getContentPane().add(cont);
        frame.pack();
        frame.setSize(1024, 730);
        enableButtons();
        frame.setVisible(true);
    }

    private String getITextFontFamilyName(File selFile) {
        Set<String> set = ITextFontResolver.getDistinctFontFamilyNames(
                selFile.getPath(),
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED);
        System.out.println("All family names reported by iText for " + selFile.getPath() + ": " + set);
        return set.iterator().next();
    }

    private void resetMouseListeners() {
        List<FSMouseListener> l = xpanel.getMouseTrackingListeners();
        for (FSMouseListener listener : l) {
            if (listener instanceof LinkListener) {
                xpanel.removeMouseTrackingListener(listener);
            }
        }
    }

    private void enableButtons() {
        prevBtn.setEnabled(outputType.equals(OUTPUT_CODEPOINTS) && curFrom > 0);
        nextBtn.setEnabled(outputType.equals(OUTPUT_CODEPOINTS) && Math.pow(2, 16) - curFrom != 0);
    }

    private Font loadFont(String fontPath) throws IOException {
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath).toURI().toURL().openStream());
            return font.deriveFont(Font.PLAIN, 12);
        } catch (FontFormatException e) {
            System.err.println(fontPath + " INVALID FONT FORMAT " + e.getMessage());
            return null;
        }
    }

    private void deferredChangePage(final int startAt) {
        deferredLoadAndRender(startAt, TO_SWING);
    }

    private void deferredLoadAndRender(final int startAt, final int renderTo) {
        resolveCurrentFont();
        if (currentFont == null) {
            JOptionPane.showMessageDialog(frame, "Can't load font--check font file path.");
            fontPathTF.requestFocus();
            return;
        }
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(() -> {
            final Document doc = loadDocument(startAt, renderTo);
            if (renderTo == TO_SWING) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        curFrom = startAt;
                        xpanel.setDocument(doc, null, new XhtmlNamespaceHandler());
                        xpanel.getSharedContext().getCss().getCascadedStyle(null, false);
                    } catch (Throwable e) {
                        JOptionPane.showMessageDialog(frame, "Can't load document (table of glyphs). Err: " + e.getMessage());
                    }
                    enableButtons();
                });
            } else {
                frame.setCursor(Cursor.getDefaultCursor());
                renderPDF(doc);
            }
        }).start();
    }

    private Document loadDocument(int startAt, int renderTo) {
        curFrom = startAt;
        String page;
        String fontFamily = getFontFamily(renderTo);
        if (outputType.equals(OUTPUT_CODEPOINTS)) {
            Table table = buildGlyphTable(startAt, startAt + ENT_PER_PAGE);
            page = new Page().toHtml(table.toHtml(fontFamily, curFrom), fontFamily);
        } else {
            page = parseEnt(new Page().toHtml("", fontFamily));
        }
        // DEBUG
        //System.out.println(page);
        InputSource is = new InputSource(new BufferedReader(new StringReader(page)));
        return XMLResource.load(is).getDocument();
    }

    private Table buildGlyphTable(int from, int to) {
        Table table = new Table(16);
        for (int j = from; j <= to; j++) {
            if (isLegalInXml(j)) {
                if (currentFont.canDisplay((char)j)) {
                    table.addColumn("&amp;#" + j + ";");
                    table.addGlyph("&#" + j + ";");
                } else {
                    table.addColumn("&amp;#" + j + ";");
                    table.addGlyph("&nbsp;");
                }
            } else {
                table.addColumn("&amp;#" + j + ";");
                table.addGlyph("!");
            }
        }
        return table;
    }

    private void renderPDF(Document doc) {
        File f;
        try {
            f = File.createTempFile("flying-saucer-glyph-test", ".pdf");
        } catch (IOException e) {
            //
            JOptionPane.showMessageDialog(frame, "Can't create temp file for PDF output, err: " + e.getMessage());
            return;
        }
        final ITextRenderer renderer = new ITextRenderer();
        String msgToUser;
        try (FileOutputStream fos = new FileOutputStream(f)) {
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            ITextFontResolver resolver = renderer.getFontResolver();
            // TODO: encoding is hard-coded as IDENTITY_H; maybe give user option to override
            resolver.addFont(
                    fontPathTF.getText(),
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED
            );
            renderer.getSharedContext().setNamespaceHandler(new XhtmlNamespaceHandler());
            renderer.createPDF(doc, bos);

            msgToUser = "Rendered PDF: " + f.getCanonicalPath();
        } catch (DocumentException | IOException e) {
            msgToUser = "Can't create PDF, err: " + e.getMessage();
        }
        final String finalMsg = msgToUser;
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, finalMsg));
    }

    // attempts to load Font given font path; if succeeds, currentFont will have the reference, otherwise it will
    // be set to null
    private void resolveCurrentFont() {
        String path = fontPathTF.getText();
        if (path.isEmpty()) {
            currentFont = null;
            return;
        }
        try {
            currentFont = loadFont(path);
            if (currentFont != null) {
                familyNameFieldAwt.setText(currentFont.getFamily());
                familyNameFieldIText.setText(getITextFontFamilyName(new File(fontPathTF.getText())));
            }
        } catch (IOException e) {
            e.printStackTrace();
            currentFont = null;
        }
    }

    private String parseEnt(String html) {
        try {
            final Table table = new Table(15);
            XMLReader parser = XMLReaderFactory.createXMLReader();
            InputSource is = new InputSource(new BufferedReader(new StringReader(html)));
            try {
                parser.setFeature("http://xml.org/sax/features/validation", true);
            } catch (SAXException e) {
                System.err.println("Cannot activate validation.");
            }
            parser.setEntityResolver(FSEntityResolver.instance());
            DefaultHandler2 dh2 = new DefaultHandler2() {
                boolean isEnt;

                // we'll get a callback for each DTD loaded; we're interested in the entity includes
                @Override
                public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
                    super.externalEntityDecl(name, publicId, systemId);
                    isEnt = systemId.endsWith(".ent");
                }

                // we'll get a callback for each entity; those starting with % are elements, can skip
                @Override
                public void internalEntityDecl(String name, String value) throws SAXException {
                    super.internalEntityDecl(name, value);
                    if (isEnt) {
                        if (name.startsWith("%")) return;
                        int codePoint = value.charAt(0);
                        // FIXME: codePointAt not available in 1.4
                        //  Character.codePointAt(value.toCharArray(), 0);
                        table.addColumn("&amp;" + name + ";");
                        table.addColumn("&amp;#" + codePoint + ";");
                        table.addGlyph("&#" + codePoint + ";");
                    }
                }
            };
            parser.setProperty("http://xml.org/sax/properties/declaration-handler", dh2);
            parser.parse(is);
            return new Page().toHtml(table.toHtml(getFontFamily(TO_SWING), 0), getFontFamily(TO_SWING));
        }
        catch (SAXException | IOException e) {
            e.printStackTrace();
            return "";
        }

    }

    private boolean isLegalInXml(int uccp) {
        // see http://www.xml.com/2000/05/10/conformance/reports/report-xerces-nv.html
        // and http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html
        return ((uccp == 0x9) ||
                (uccp == 0xA) ||
                (uccp == 0xD) ||
                ((uccp >= 0x20) && (uccp <= 0xD7FF)) ||
                ((uccp >= 0xE000) && (uccp <= 0xFFFD)) ||
                ((uccp >= 0x10000) && (uccp <= 0x10FFFF))) &&
                uccp != 0x0DDD;  // causes VM crash on ubuntu...

    }

    private String getFontFamily(int renderTo) {
        if (renderTo == TO_SWING) {
            return currentFont.getFamily();
        } else {
            return getITextFontFamilyName(new File(fontPathTF.getText()));
        }
    }

    private static class Page {
        public String toHtml(String bodyContent, String fontFamily) {
            StringBuilder sb = new StringBuilder();
            sb.append(getHeadDecl(getStyleDecl(fontFamily)));
            sb.append("<body>\n");
            sb.append(bodyContent);
            sb.append("</body>\n");
            sb.append("</html>\n");
            return sb.toString();
        }

        private String getHeadDecl(String style) {
            return "<?xml version='1.0' encoding='utf-8'?>\n" +
                    "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>\n" +
                    "<html xmlns='http://www.w3.org/1999/xhtml'>\n" +
                    "<head>" +
                    "<title>Full Entity Chart</title>\n" +
                    style +
                    "</head>\n";
        }

        private String getStyleDecl(String fontFamily) {
            String css = "* {font-size: 8pt; font-family: \"" + fontFamily + "\" } " +
                    "table {table-layout: fixed; width: 100%; border-collapse: collapse; border: 1px solid black;} " +
                    "col {} " +
                    ".glyph {width: 1.35em; border-right-width: 2px;} " +
                    "td {border: 1px solid black; }" +
                    "td .glyph {}";

            return "<style type=\"text/css\" media=\"all\">\n" + css + "\n</style>\n";
        }
    }


    private static class Table {
        private final int colCnt;

        private final List<Col> cols = new ArrayList<>();
        private final List<String> headerLines = new ArrayList<>();

        private Table(int colCnt) {
            this.colCnt = colCnt;
        }

        public String toHtml(String fontFamily, int curFrom) {
            StringBuilder sb = new StringBuilder();
            for (String line : headerLines) {
                sb.append("<p>").append(line).append("</p>\n");

            }
            sb.append("<p>Table of Unicode Characters</p>\n");
            sb.append("<p>Using font: ").append(fontFamily).append(", Unicode code points starting with ").append(curFrom).append("</p>\n");
            sb.append("<p>Empty cell means no glyph available; ! means codepoint not allowed in XML, per spec.</p>\n");

            sb.append("<table>\n");
            for (int i = 0; i < colCnt; i++) {
                sb.append("<col class=\"").append(cols.get(i).cssClass).append("\"/>\n");
            }
            sb.append("<tr>\n");
            int cnt = 0;
            for (Iterator<Col> it = cols.iterator(); it.hasNext();) {
                Col col = it.next();
                sb.append("<td class=\"").append(cols.get(cnt).cssClass).append("\">").append(col.content).append("</td>");
                if (++cnt % colCnt == 0 && it.hasNext()) {
                    sb.append("\n</tr>\n");
                    sb.append("<tr>\n");
                }
            }
            sb.append("\n</tr>\n");
            sb.append("</table>\n");
            return sb.toString();
        }

        public void addColumn(String content) {
            cols.add(new Col("", content));

        }
        public void addGlyph(String content) {
            cols.add(new Col("glyph", content));
        }

        public void addHeaderLine(String text) {
            headerLines.add(text);
        }

        static class Col {
            private final String cssClass;
            private final String content;

            Col(String cssClass, String content) {
                this.cssClass = cssClass;
                this.content = content;
            }
        }

    }
}