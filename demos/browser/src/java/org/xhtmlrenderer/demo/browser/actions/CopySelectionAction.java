package org.xhtmlrenderer.demo.browser.actions;

import org.xhtmlrenderer.demo.browser.BrowserStartup;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.swing.BasicPanel;

import javax.swing.*;
import java.awt.*;
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
        BasicPanel panel = root.panel.view;
        Box start = panel.getContext().getSelectionStart();
        Box end = panel.getContext().getSelectionEnd();
        StringBuffer sb = new StringBuffer();
        collectSelection(panel.getContext(), panel.getRootBox(), start, end, sb, false);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Clipboard clip = tk.getSystemClipboard();
        clip.setContents(new StringSelection(sb.toString()), null);
    }

    public boolean collectSelection(SharedContext ctx, Box root, Box current, Box last, StringBuffer sb, boolean in_selection) {

        if (root == current) {
            in_selection = true;
        }
        if (in_selection) {
            if (root instanceof LineBox) {
                sb.append("\n");
            }
            if (root instanceof InlineBox) {
                InlineBox ib = (InlineBox) root;
                int start = 0;
                int end = ib.getSubstring().length();
                if (ib == current) {
                    //TODO: find a way to do this
                    //start = ib.getTextIndex(ctx.getSelectionStartX(), ctx.getGraphics());
                }
                if (ib == last) {
                    //TODO: find a way to do this
                    //end = ib.getTextIndex(ctx.getSelectionEndX(), ctx.getGraphics());
                }
                String st = ib.getSubstring().substring(Math.max(0, start - 1), end);
                sb.append(st);
            }
        }
        if (root == last) {
            in_selection = false;
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            Box child = root.getChild(i);
            in_selection = collectSelection(ctx, child, current, last, sb, in_selection);
        }

        return in_selection;
    }
}

