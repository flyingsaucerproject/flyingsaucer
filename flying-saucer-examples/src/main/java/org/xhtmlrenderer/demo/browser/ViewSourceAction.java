package org.xhtmlrenderer.demo.browser;

import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.xhtmlrenderer.simple.XHTMLPanel;

/**
 * @author pwright
 */
public class ViewSourceAction extends AbstractAction {
    private final XHTMLPanel panel;

    public ViewSourceAction(XHTMLPanel panel) {
        this.panel = panel;
    }

    public void actionPerformed(ActionEvent evt) {
        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer serializer;
        try {
            serializer = tfactory.newTransformer();
            //Setup indenting to "pretty print"
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            Element document = panel.getRootBox().getElement();
            DOMSource source = new DOMSource(document);
            StreamResult output = new StreamResult(System.out);
            serializer.transform(
                    source,
                    output
            );
        } catch (TransformerException ex) {
            // this is fatal, just dump the stack and throw a runtime exception
            ex.printStackTrace();

            throw new RuntimeException(ex);
        }
    }
}
