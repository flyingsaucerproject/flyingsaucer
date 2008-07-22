/*
 * {{{ header & license
 * Copyright (c) 2008 Patrick Wright
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

import com.lowagie.text.DocumentException;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDEntity;
import com.wutka.dtd.DTDParser;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlCssOnlyNamespaceHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * This example launches a simple Swing app which allows you to test different font: property settings, which are
 * applied to generates a set of tables containing the standard XHTML entities (Latin-1, special, and symbols)
 * plus several general entity sets containing Unicode character ranges. The result is rendered in a Swing panel and
 * optionally dumped as a PDF. To run, specify the font-family you want to test, in quotes, from the command line, like
 * <code>EntityRender "<family>[,<family>,...]"</code>. The value you specify will be used as the default
 * font property for the rendered test. Use this sample to verify that a given font has a given glyph.
 *
 * @author Patrick Wright
 */
public class EntityRender {
    private static final int ENT_PER_ROW = 2;
    private static final int COLS_PER_ENT = 3;
    private String currentDoc;
    private JFileChooser fontFileChooser;
    private String fontSize = "10pt";

    public static void main(String[] args) throws Exception {
        String families;
        if (args.length == 0) {
            System.out.println("No font family specified.");
            families = "10pt serif";
        } else {
            families = args[0];
        }
        System.out.println("Rendering entities with font-family: " + families);
        new EntityRender().run(families);
    }

    private void run(final String fontFamilies) throws IOException, DocumentException {
        launchFrame(fontFamilies);
    }

    private void launchFrame(final String defaultFontProp) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final JFrame frame = new JFrame("Flying Saucer: Entity Rendering Tables");
                final XHTMLPanel panel = new XHTMLPanel();
                fontFileChooser = new JFileChooser();
                fontFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                JLabel lFon = new JLabel("CSS font property: ");
                final JTextField tFon = new JTextField(defaultFontProp, 40);
                JButton bFon = new JButton("Render");

                JLabel lFFile = new JLabel("Font File: ");
                final JTextField tFFile = new JTextField("", 40);
                tFFile.setEditable(false);

                JButton bFFile = new JButton("...");
                bFFile.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                int rc = fontFileChooser.showOpenDialog(frame);
                                if (rc == JFileChooser.APPROVE_OPTION) {
                                    File file = fontFileChooser.getSelectedFile();
                                    boolean succeed = true;
                                    String err = "";
                                    try {
                                        Font font = Font.createFont(Font.TRUETYPE_FONT, file);
                                        tFFile.setText(file.getAbsolutePath());
                                        tFon.setText("\"" + font.getFontName() + "\"");
                                    } catch (Exception e1) {
                                        succeed = false;
                                        err = e1.getMessage();
                                    }
                                    if (!succeed) {
                                        JOptionPane.showMessageDialog(
                                                frame,
                                                "Could not load Font file--is it a TTF font? Err: " + err,
                                                "Can't load Font",
                                                JOptionPane.ERROR_MESSAGE
                                        );
                                        return;
                                    }
                                }
                            }
                        });

                bFon.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                String fontSpec = tFon.getText();
                                String fontFileName = tFFile.getText();
                                if (fontSpec.length() == 0) {
                                    tFon.setText(defaultFontProp);
                                    tFon.requestFocus();
                                } else {
                                    currentDoc = buildDocument(fontSpec, fontFileName);
                                    SwingUtilities.invokeLater(
                                            new Runnable() {
                                                public void run() {
                                                    try {
                                                        setPanelDocument(panel, currentDoc);
                                                    } catch (Exception e1) {
                                                        JOptionPane.showMessageDialog(frame,
                                                                e1.getMessage(),
                                                                "Oops! Render to Panel Failed",
                                                                JOptionPane.ERROR_MESSAGE
                                                        );
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                JButton bPdf = new JButton("PDF");
                bPdf.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            String loc = createPDF(currentDoc);
                            JOptionPane.showMessageDialog(frame,
                                    "Rendered as PDF to " + loc,
                                    "PDF Render Complete",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception e1) {
                            JOptionPane.showMessageDialog(frame,
                                    e1.getMessage(),
                                    "Oops! Render PDF Failed",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                JPanel control1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JPanel control2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                control1.add(lFon);
                control1.add(tFon);
                control1.add(bFon);
                control1.add(bPdf);
                control2.add(lFFile);
                control2.add(tFFile);
                control2.add(bFFile);
                JPanel control = new JPanel(new GridLayout(2, 1));
                control.add(control1);
                control.add(control2);

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(BorderLayout.NORTH, control);
                frame.getContentPane().add(BorderLayout.CENTER, new FSScrollPane(panel));
                frame.pack();
                frame.setSize(1024, 768);
                frame.setVisible(true);
            }
        });
    }

    private void setPanelDocument(XHTMLPanel panel, String doc) {
        try {
            panel.setDocumentFromString(doc, null, new XhtmlCssOnlyNamespaceHandler());
        } catch (Exception e) {
            throw new RuntimeException("Failed to render document: " + e.getMessage());
        }
    }

    private String createPDF(String docContents) throws IOException, DocumentException {
        OutputStream os = null;
        try {
            File pdfFile = File.createTempFile("xhtmlrenderer-entities.", ".pdf");
            os = new FileOutputStream(pdfFile);

            ITextRenderer renderer = new ITextRenderer();
            Document doc = XMLResource.load(new StringReader(docContents)).getDocument();

            renderer.setDocument(doc, null, new XhtmlCssOnlyNamespaceHandler());
            renderer.layout();
            renderer.createPDF(os);
            return pdfFile.getAbsolutePath();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private String buildDocument(String fontFamily, String fontFileName) {
        List esets = prepareEntitySets();
        StringBuffer sbPage = new StringBuffer();
        startPage(sbPage, fontFamily, fontFileName);

        Iterator eit = esets.iterator();
        while (eit.hasNext()) {
            addTable(sbPage, (EntitySet) eit.next());
            addSeparator(sbPage);
        }

        endPage(sbPage);
        String doc = sbPage.toString();
        // DEBUG
        // System.out.println(doc);
        return doc;
    }

    private List prepareEntitySets() {
        List esets = new ArrayList();
        // NOTE: HTML 4.01 entity files not parsing correctly with DTDParser, not sure why
        // however, should be approx the same set as XHTML 1.0 entities
        //esets.add(new EntitySet("HTML 4.01 Latin-1", "/resources/schema/html-4.01/entity/html-lat1.ent"));
        //esets.add(new EntitySet("HTML 4.01 Special", "/resources/schema/html-4.01/entity/html-special.ent"));
        //esets.add(new EntitySet("HTML 4.01 Special", "/resources/schema/html-4.01/entity/html-symbol.ent"));

        esets.add(new EntitySet("XHTML Latin-1", "/resources/schema/xhtml/entity/xhtml-lat1.ent"));
        esets.add(new EntitySet("XHTML Special", "/resources/schema/xhtml/entity/xhtml-special.ent"));
        esets.add(new EntitySet("XHTML Symbols", "/resources/schema/xhtml/entity/xhtml-symbol.ent"));
        esets.add(EntitySet.generalEntities());
        esets.add(EntitySet.generalPunctuation());
        esets.add(EntitySet.greek());
        esets.add(EntitySet.math());
        return esets;
    }

    private void addSeparator(StringBuffer sbPage) {
        sbPage.append("    <br /><br />\n");
    }

    private void startPage(StringBuffer sb, String fontSpec, String fontFileName) {
        sb.append("<html>\n");
        sb.append("<head>\n");
        sb.append("<style type=\"text/css\">\n");
        sb.append("@page { ");
        sb.append("  @top-left { content: \"Flying Saucer: Entities Tables\"} ");
        sb.append("  @top-right { content: 'Using font: " + fontSize + " " + fontSpec + "'; text-align: right; }");
        sb.append("}\n");
        sb.append("* { font: " + fontSpec + "; }\n");
        sb.append("table { width: 100%; border: 1px dotted black; border-collapse: collapse; -fs-table-paginate: paginate; }\n");
        sb.append("td { border: 1px solid black; border-collapse: collapse; padding: 5px; font: " + fontSize + " " + fontSpec + "; }\n");
        if (fontFileName != null && fontFileName.length() > 0) {
            sb.append("@font-face { src: url(" + fontFileName + "); -fs-pdf-font-embed: embed; }\n");
        }
        sb.append("</style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        // DEBUG
        //System.out.println(sb.toString());
    }

    private void endPage(StringBuffer sb) {
        sb.append("</body>\n");
        sb.append("</html>\n");
    }

    private void addTable(StringBuffer sb, EntitySet set) {
        int totalCols = ENT_PER_ROW * COLS_PER_ENT;
        sb.append("  <table>\n");
        sb.append("  <thead><tr><td style=\"-fs-table-cell-colspan: " + totalCols + "\">").append(set.eName).append("</td></tr></thead>\n");
        sb.append("  <tbody>\n");
        buildRows(sb, set, ENT_PER_ROW);
        sb.append("  </tbody>\n");
        sb.append("  </table>\n");
    }

    private void buildRows(StringBuffer sb, EntitySet entitySet, int colCount) {
        Iterator eit = entitySet.entities.iterator();
        int extra = entitySet.entities.size() % colCount;
        int cnt = 0;
        sb.append("    <tr>\n");
        while (eit.hasNext()) {
            EntityTuple etup = (EntityTuple) eit.next();
            appendEntitiesTD(sb, etup);
            if (++cnt % colCount == 0) {
                sb.append("    </tr>\n");
                if (eit.hasNext()) {
                    sb.append("    <tr>\n");
                }
            }
        }
        if (extra > 0) {
            int fill = (colCount * COLS_PER_ENT) - (extra * COLS_PER_ENT);
            for (int i = 0; i < fill; i++) {
                sb.append("      <td/>");
            }
            sb.append("    </tr>\n");
        }
    }

    private void appendEntitiesTD(StringBuffer sb, EntityTuple etup) {
        if (etup.hasSymbol) {
            sb.append("      <td>").append("&amp;&#35;").append(etup.symbol).append("&#59;").append("</td>\n");
        } else {
            sb.append("      <td>").append("</td>\n");
        }
        sb.append("      <td>").append("&amp;&#35;").append(etup.code).append("&#59;").append("</td>\n");
        sb.append("      <td>").append("&#").append(etup.code).append(";").append("</td>\n");
    }

    private static class EntitySet implements Comparable {
        String eName;
        URL eUrl;
        List entities;

        private EntitySet(String name) {
            eName = name;
            entities = new ArrayList();
        }

        public EntitySet(String name, String uri) {
            this(name);
            eUrl = EntityRender.class.getResource(uri);
            if (eUrl == null) {
                throw new IllegalArgumentException("Invalid entity doc URI: " + uri);
            }
            loadFromURI(this);
        }

        private void loadFromURI(EntitySet set) {
            try {
                DTD dtd = new DTDParser(set.eUrl, false).parse();
                Enumeration een = dtd.entities.elements();
                while (een.hasMoreElements()) {
                    DTDEntity entity = (DTDEntity) een.nextElement();
                    set.entities.add(new EntityTuple(entity.name, entity.value));
                }
                Collections.sort(set.entities);
            } catch (IOException e) {
                throw new RuntimeException("Can't parse DTD, IO Exception: " + e.getMessage());
            }
        }

        public int compareTo(Object o) {
            if (o == null) throw new NullPointerException();
            if (!(o instanceof EntitySet)) return 0;
            return eName.compareTo(((EntitySet) o).eName);
        }

        public String toString() {
            return eName;
        }

        public static EntitySet generalEntities() {
            EntitySet es = new EntitySet("General Entities; Unicode &#33; - &#476; (see http://www.sweeting.org/mark/html/entity_chars.php)");
            addEntitiesInRange(es, 33, 476);
            return es;
        }

        public static EntitySet generalPunctuation() {
            EntitySet es = new EntitySet("General Entities; Unicode &#8194; - &#8365; (see http://www.sweeting.org/mark/html/entity_punct.php)");
            addEntitiesInRange(es, 8194, 8365);
            return es;
        }

        public static EntitySet greek() {
            EntitySet es = new EntitySet("Greek Letters; Unicode &#913; - &#984; (see http://www.sweeting.org/mark/html/entity_greek.php)");
            addEntitiesInRange(es, 913, 984);
            return es;
        }

        public static EntitySet math() {
            EntitySet es = new EntitySet("Math Entities; Unicode &#8704; - &#8903; (see http://www.sweeting.org/mark/html/entity_math.php)");
            addEntitiesInRange(es, 8704, 8903);
            return es;
        }

        private static void addEntitiesInRange(EntitySet es, int startChar, int endChar) {
            for (int i = startChar; i < endChar; i++) {
                es.entities.add(EntityTuple.forUnicodeChar(i));
            }
        }
    }

    private static class EntityTuple implements Comparable {

        String symbol;

        int code;

        boolean hasSymbol;

        public EntityTuple(String name) {
            symbol = name;
            hasSymbol = true;
        }

        public EntityTuple(String name, String value) {
            this(name);
            code = Integer.valueOf(value.substring(2, value.indexOf(";"))).intValue();
        }

        public int compareTo(Object o) {
            if (o == null) throw new NullPointerException();
            if (!(o instanceof EntityTuple)) return 0;
            EntityTuple other = (EntityTuple) o;
            return code == other.code ? 0 : code < other.code ? -1 : 1;
        }

        public String toString() {
            return symbol + "= &#" + code + ";";
        }

        public static EntityTuple forUnicodeChar(int ichar) {
            EntityTuple et = new EntityTuple("UC " + ichar);
            et.code = ichar;
            et.hasSymbol = false;
            return et;
        }
    }
}