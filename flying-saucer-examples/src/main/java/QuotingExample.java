import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.XMLUtil;

import javax.swing.*;

import static javax.swing.SwingUtilities.invokeLater;


public class QuotingExample extends JFrame {
    //currently we cannot display different quotes based on depth
    private static final String DOCUMENT =
            """
                    <html>
                      <head>
                        <style type='text/css'><![CDATA[
                          * { quotes: '"' '"' "'" "'" }
                          q:before { content: open-quote }
                          q:after { content: close-quote }
                          blockquote p:before     { content: open-quote }
                          blockquote p:after      { content: no-close-quote }
                          blockquote p.last:after { content: close-quote }
                        ]]></style>
                      </head>
                      <body>
                        <blockquote>
                          <p>This is just a test of the emergency <q>quoting</q> system.</p>
                          <p>This is only a test.</p>
                          <p class='last'>Thank you for your cooperation during this <q>test.</q></p>
                        </blockquote>
                      </body>
                    </html>
                    """;
    
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
