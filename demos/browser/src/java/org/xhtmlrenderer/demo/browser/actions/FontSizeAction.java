package org.xhtmlrenderer.demo.browser.actions;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.demo.browser.*;
import org.xhtmlrenderer.util.u;

public class FontSizeAction extends AbstractAction {

    protected BrowserStartup root;
    protected float scale;
    public FontSizeAction(BrowserStartup root, float scale) {
        super("FontSize");
        this.root = root;
        this.scale = scale;
    }
    
    public void actionPerformed(ActionEvent evt) {
        RenderingContext rc = root.panel.view.getRenderingContext();
        rc.getTextRenderer().setFontScale(rc.getTextRenderer().getFontScale()*scale);
        u.p("new font scale = " + rc.getTextRenderer().getFontScale());
        root.panel.view.relayout();
        root.panel.view.repaint();
    }

}
