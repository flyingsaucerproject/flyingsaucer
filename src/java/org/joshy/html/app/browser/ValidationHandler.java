package org.joshy.html.app.browser;

import org.xml.sax.*;
import java.util.logging.*;
import javax.swing.JTextArea;

public class ValidationHandler implements ErrorHandler {
    public void setTextArea(JTextArea jta) {
        this.jta = jta;
    }
    public static Logger logger = Logger.getLogger("app.browser");
    protected JTextArea jta;
    protected void print(String str) {
        if(jta != null) {
            jta.append(str);
        }
    }
    
    public void error(SAXParseException ex) {
        print("error: " + print(ex));
    }
    
    public void fatalError(SAXParseException ex) {
        print("fatal error: " + print(ex));
    }
    
    public void warning(SAXParseException ex) {
        print("warning: " + print(ex));
    }
    
    public String print(SAXParseException ex) {
        StringBuffer sb = new StringBuffer();
        sb.append("Exception: " + ex.getMessage());
        sb.append("failed at column : " + ex.getColumnNumber() +
        " on line " + ex.getLineNumber());
        sb.append("entity:\n" + ex.getPublicId() + "\n" + ex.getSystemId());
        return sb.toString();
    }
        
}
