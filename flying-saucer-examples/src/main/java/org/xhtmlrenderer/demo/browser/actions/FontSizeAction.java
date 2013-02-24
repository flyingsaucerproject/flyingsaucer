package org.xhtmlrenderer.demo.browser.actions;

import org.xhtmlrenderer.demo.browser.BrowserStartup;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FontSizeAction extends AbstractAction {

    protected BrowserStartup root;
    public static final int DECREMENT = 0;
    public static final int INCREMENT = 1;
    public static final int RESET = 2;
    private int whichDirection;

    public FontSizeAction(BrowserStartup root, int which) {
        super("FontSize");
        this.root = root;
        this.whichDirection = which;
    }

    public FontSizeAction(BrowserStartup root, float scale, int which) {
        this(root, which);
        this.root.panel.view.setFontScalingFactor(scale);
    }

    public void actionPerformed(ActionEvent evt) {
        switch (whichDirection) {
            case INCREMENT:
                root.panel.view.incrementFontSize();
                break;
            case RESET:
                root.panel.view.resetFontSize();
                break;
            case DECREMENT:
                root.panel.view.decrementFontSize();
                break;
        }
    }
}
