package org.xhtmlrenderer.demo.browser.actions;

import org.xhtmlrenderer.demo.browser.BrowserStartup;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.swing.BasicPanel;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

public class CopySelectionAction extends AbstractAction {

    protected BrowserStartup root;

    public CopySelectionAction(BrowserStartup root) {
        super("Copy");
        this.root = root;
    }


    public void actionPerformed(ActionEvent evt) {
        // ... collection seleciton here
        Toolkit tk = Toolkit.getDefaultToolkit();
        Clipboard clip = tk.getSystemClipboard();
        clip.setContents(new StringSelection("..."), null);
    }
}

