import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.XMLUtil;

import javax.swing.*;

import static javax.swing.SwingUtilities.invokeLater;


public class QuotingExample extends JFrame {
    //currently we cannot display different quotes based on depth
    private static final String DOCUMENT = 
        "<html>\n" +
        "  <head>\n" +
        "    <style type='text/css'><![CDATA[\n" +
        "      * { quotes: '\"' '\"' \"'\" \"'\" }\n" +
        "      q:before { content: open-quote }\n" +
        "      q:after { content: close-quote }\n" +
        "      blockquote p:before     { content: open-quote }\n" +
        "      blockquote p:after      { content: no-close-quote }\n" + 
        "      blockquote p.last:after { content: close-quote }\n" +
        "    ]]></style>\n" +
        "  </head>\n" +
        "  <body>\n" +
        "    <blockquote>\n" +
        "      <p>This is just a test of the emergency <q>quoting</q> system.</p>\n" +
        "      <p>This is only a test.</p>\n" +
        "      <p class='last'>Thank you for your cooperation during this <q>test.</q></p>\n" +
        "    </blockquote>\n" +
        "  </body>\n" +
        "</html>\n";
    
    protected void frameInit() {
        super.frameInit();
        
        setTitle("CSS Quoting Example");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        XHTMLPanel xr = new XHTMLPanel();
        try {
            xr.setDocument(XMLUtil.documentFromString(DOCUMENT));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        
        add(xr);
        
        setSize(500, 300);
    }
    
    public static void main(String[] args) {
        invokeLater(() -> new QuotingExample().setVisible(true));
    }
}
