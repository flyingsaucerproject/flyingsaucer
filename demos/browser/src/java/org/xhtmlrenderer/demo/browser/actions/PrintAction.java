package org.xhtmlrenderer.demo.browser.actions;

import java.awt.event.*;
import javax.swing.*;
import org.xhtmlrenderer.demo.browser.*;
import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xhtmlrenderer.simple.XHTMLPrintable;
import java.awt.Dimension;
import java.awt.print.*;
import java.awt.*;

public class PrintAction extends AbstractAction {
    protected BrowserStartup root;
    
    public PrintAction(BrowserStartup root) {
        super("Print");
        this.root = root;
    }
    
    public void actionPerformed(ActionEvent evt) {
        u.p("printing");
        final PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(new XHTMLPrintable(root.panel.view));
        
        if(printJob.printDialog()) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        u.p("starting printing");
                        printJob.print();
                        u.p("done printing");
                    } catch(PrinterException ex) {
                        u.p(ex);
                    }
                }
            }).start();
        }
        
    }
}

