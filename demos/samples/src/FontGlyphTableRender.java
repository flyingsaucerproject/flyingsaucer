/**
 *
 */

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xhtmlrenderer.event.DefaultDocumentListener;
import org.xml.sax.InputSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Opens a frame and displays, for a selected font, the glyphs for a range of Unicode code points. Can be used to
 * identify which glyphs are supported by a font. Can export to PDF.
 *
 * @author Patrick Wright
 */
public class FontGlyphTableRender {
    private static final int TO_SWING = 1;
    private static final int TO_PDF = 2;
    private static final int ENT_PER_PAGE = 199;

    private int curFrom;
    private Font currentFont;

    private JFrame frame;
    private XHTMLPanel xpanel;
    private JTextField fontFamilyPathField;
    private JTextField familyNameField;
    private JButton prevBtn;
    private JButton nextBtn;


    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new FontGlyphTableRender().run();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void run() throws Exception {
        frame = new JFrame("Flying Saucer: Show Font Glyphs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JPanel optionsPanel = new JPanel(new BorderLayout());

        fontFamilyPathField = new JTextField("");
        fontFamilyPathField.setColumns(40);

        familyNameField = new JTextField();
        familyNameField.setEnabled(true);
        familyNameField.setEditable(false);
        familyNameField.setColumns(40);
        JPanel top1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top1.add(new JLabel("Enter font path: "));
        top1.add(fontFamilyPathField);
        JButton chooseFontFileBtn = new JButton("...");
        chooseFontFileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String filename = File.separator + "tmp";
                String famPath = fontFamilyPathField.getText();
                if (currentFont != null && famPath.length() > 0) {
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
                    fontFamilyPathField.setText(selFile.getPath());
                    familyNameField.setText(font.getFamily());
                }

            }
        });
        top1.add(chooseFontFileBtn);
        JPanel top2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top2.add(new JLabel("Family: "));
        top2.add(familyNameField);
        JPanel top = new JPanel(new BorderLayout());
        top.add(top1, BorderLayout.NORTH);
        top.add(top2, BorderLayout.CENTER);

        JPanel mid = new JPanel(new FlowLayout(FlowLayout.LEFT));
        prevBtn = new JButton("Prev");
        nextBtn = new JButton("Next");
        JButton pdfBtn = new JButton("PDF");
        JButton renderBtn = new JButton("Render");
        prevBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                deferredChangePage(curFrom - ENT_PER_PAGE);
            }
        });
        nextBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                deferredChangePage(curFrom + ENT_PER_PAGE);
            }
        });
        renderBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                deferredChangePage(curFrom);
            }
        });
        pdfBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (resolveFamilyName().length() == 0) {
                    JOptionPane.showMessageDialog(frame, "Need a valid font file path");
                    fontFamilyPathField.requestFocus();
                    return;
                }
                deferredLoadAndRender(curFrom, TO_PDF);
            }
        });
        mid.add(prevBtn);
        mid.add(nextBtn);
        mid.add(renderBtn);
        mid.add(pdfBtn);
        optionsPanel.add(top, BorderLayout.NORTH);
        optionsPanel.add(mid, BorderLayout.CENTER);
        fontFamilyPathField.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                deferredChangePage(curFrom);
            }
        });

        // Create a JPanel subclass to render the page
        xpanel = new XHTMLPanel();
        xpanel.addDocumentListener(new DefaultDocumentListener(){
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
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel cont = new JPanel(new BorderLayout());
        cont.add(optionsPanel, BorderLayout.NORTH);
        cont.add(scroll, BorderLayout.CENTER);
        frame.getContentPane().add(cont);
        frame.pack();
        frame.setSize(1024, 730);
        enableButtons();
        frame.setVisible(true);
    }

    private Font loadFont(String fontPath) throws IOException {
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath));
        } catch (FontFormatException e) {
            try {
                font = Font.createFont(Font.TYPE1_FONT, new File(fontPath));
            } catch (FontFormatException e1) {
                System.err.println(fontPath + " INVALID FONT FORMAT " + e.getMessage());
                return null;
            }
        }
        return font.deriveFont(Font.PLAIN, 12);
    }

    private void resetMouseListeners() {
        List l = xpanel.getMouseTrackingListeners();
        for (Iterator i = l.iterator(); i.hasNext();) {
            FSMouseListener listener = (FSMouseListener) i.next();
            if (listener instanceof LinkListener) {
                xpanel.removeMouseTrackingListener(listener);
            }
        }
    }

    private void deferredChangePage(final int startAt) {
        deferredLoadAndRender(startAt, TO_SWING);
    }

    private void deferredLoadAndRender(final int startAt, final int renderTo) {
        if (resolveFamilyName().length() == 0) {
            JOptionPane.showMessageDialog(frame, "Can't load font--check font file path.");
            fontFamilyPathField.requestFocus();
            return;
        }
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new Thread(new Runnable() {
            public void run() {
                final Document doc = loadDocument(startAt);
                deferredRender(doc, startAt, renderTo);
            }
        }).start();
    }

    private void deferredRender(final Document doc, final int startAt, final int renderTo) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (renderTo == TO_SWING) {
                    try {
                        curFrom = startAt;
                        xpanel.setDocument(doc, null, new XhtmlNamespaceHandler());
                        xpanel.getSharedContext().getCss().getCascadedStyle(null, false);
                    } catch (Throwable e) {
                        JOptionPane.showMessageDialog(frame, "Can't load document (table of glyphs). Err: " + e.getMessage());
                    }
                    enableButtons();
                } else {
                    File f;
                    try {
                        f = File.createTempFile("flying-saucer-glyph-test", ".pdf");
                    } catch (IOException e) {
                        //
                        JOptionPane.showMessageDialog(frame, "Can't create temp file for PDF output, err: " + e.getMessage());
                        return;
                    }
                    final ITextRenderer renderer = new ITextRenderer();
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(f);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        renderer.setDocument(doc, null, new XhtmlNamespaceHandler());
                        ITextFontResolver resolver = renderer.getFontResolver();
                        // TODO: encoding is hard-coded as IDENTITY_H; maybe give user option to override
                        resolver.addFont(
                                fontFamilyPathField.getText(),
                                BaseFont.IDENTITY_H,
                                BaseFont.EMBEDDED
                        );
                        renderer.layout();
                        renderer.createPDF(bos);
                        JOptionPane.showMessageDialog(frame, "Rendered PDF: " + f.getCanonicalPath());
                    } catch (FileNotFoundException e) {
                        JOptionPane.showMessageDialog(frame, "Can't create PDF, err: " + e.getMessage());
                    } catch (DocumentException e) {
                        JOptionPane.showMessageDialog(frame, "Can't create PDF, err: " + e.getMessage());
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(frame, "Can't create PDF, err: " + e.getMessage());
                    } finally {
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                // swallow
                            }
                        }
                    }
                }
            }
        });
    }

    private Document loadDocument(int startAt) {
        curFrom = startAt;
        String table = buildTable(startAt, startAt + ENT_PER_PAGE);
        // DEBUG
        // System.out.println(table);
        InputSource is = new InputSource(new BufferedReader(new StringReader(table)));
        return XMLResource.load(is).getDocument();
    }

    private String resolveFamilyName() {
        String path = fontFamilyPathField.getText();
        if (path.length() == 0) {
            return "";
        }
        try {
            currentFont = loadFont(path);
            if (currentFont != null) {
                familyNameField.setText(currentFont.getFamily());
                return currentFont.getFamily();
            } else {
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void enableButtons() {
        prevBtn.setEnabled(curFrom > 0);
        nextBtn.setEnabled(Math.pow(2, 16) - curFrom != 0);
    }

    private String buildTable(int from, int to) {
        Table table = new Table(16);
        for (int j = from; j <= to; j++) {
            if (isLegalInXml(j)) {
                if (currentFont.canDisplay(j)) {
                    table.addColumn("&amp;#" + j + ";");
                    table.addColumn("&#" + j + ";");
                } else {
                    table.addColumn("&amp;#" + j + ";");
                    table.addColumn("&nbsp;");
                }
            } else {
                table.addColumn("&amp;#" + j + ";");
                table.addColumn("(ill)");
            }
        }
        return table.toHtml(getFontFamily(), curFrom);
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

    private String getFontFamily() {
        return currentFont.getFamily();
    }

    private static class Table {
        private int colCnt;

        private List cols = new ArrayList();

        public Table(int colCnt) {
            this.colCnt = colCnt;
        }

        public String toHtml(String fontFamily, int curFrom) {
            StringBuilder sb = new StringBuilder();
            sb.append(getHeadDecl(getStyleDecl(fontFamily)));
            sb.append("<body>\n");
            sb.append("Table of Unicode Characters<br />\n");
            sb.append("Using font: ").append(fontFamily).append(", Unicode code points starting with ").append(curFrom).append("<br />\n");
            sb.append("Empty cell means no glyph available; ill means codepoint not allowed in XML, per spec.<br />\n");

            sb.append("<table>\n");
            int cnt = 0;
            sb.append("<tr>\n");
            for (Iterator it = cols.iterator(); it.hasNext();) {
                String content = (String) it.next();
                sb.append("<td>").append(content).append("</td>");
                if (++cnt % colCnt == 0 && it.hasNext()) {
                    sb.append("\n</tr>\n");
                    sb.append("<tr>\n");
                }
            }
            sb.append("\n</tr>\n");
            sb.append("</table>\n");
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
                            "table {width: 100%; border: 1px solid black; border-collapse: collapse;} " +
                            "td {border: 1px solid black; min-width: 10pt; text-align: center;} ";

            return "<style type=\"text/css\">\n" + css + "\n</style>\n";
        }

        public void addColumn(String content) {
            cols.add(content);
        }
    }
}