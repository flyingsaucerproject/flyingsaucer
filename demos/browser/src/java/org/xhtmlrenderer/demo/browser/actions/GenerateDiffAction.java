package org.xhtmlrenderer.demo.browser.actions;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.xhtmlrenderer.demo.browser.*;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.test.DocumentDiffTest;

/**
 *  Action to generate a unit test diff file of the current document
 *  in the browser
 *
 * @author     jmarinacci
 * @created    November 17, 2004
 */
public class GenerateDiffAction extends AbstractAction {

    protected BrowserStartup root;
    public GenerateDiffAction(BrowserStartup root) {
        super("Generate Diff");
        this.root = root;
    }

    /**
     *  Description of the Method
     *
     * @param  evt  Description of the Parameter
     */
    public void actionPerformed(ActionEvent evt) {
        try {

            URL url = root.panel.view.getURL();
            if (url != null) {
                if (url.toString().startsWith("file:")) {
                    String str = url.toString();
                    str = str.substring(6, str.length() - 6);
                    if (new File(str + ".diff").exists()) {
                        int n = JOptionPane.showConfirmDialog(root.panel.view,
                                "Diff already exists. Overwrite?",
                                "Warning",
                                JOptionPane.OK_CANCEL_OPTION);
                        if (n != JOptionPane.OK_OPTION) {
                            return;
                        }
                    }
                    DocumentDiffTest.generateTestFile(str + ".xhtml",
                            str + ".diff",
                            500, 500);
                    u.p("wrote out: " + str + ".diff");
                }
            }
        } catch (Exception ex) {
            u.p(ex);
        }
    }
}


