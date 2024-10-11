package org.xhtmlrenderer.demo.browser.actions;

import org.xhtmlrenderer.demo.browser.BrowserStartup;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class FontSizeAction extends AbstractAction {

    private final BrowserStartup root;
    public enum FontSizeChange {
        DECREMENT,
        INCREMENT,
        RESET
    }
    private final FontSizeChange whichDirection;

    public FontSizeAction(BrowserStartup root, FontSizeChange which) {
        super("FontSize");
        this.root = root;
        this.whichDirection = which;
    }

    public FontSizeAction(BrowserStartup root, float scale, FontSizeChange which) {
        this(root, which);
        this.root.panel.view.setFontScalingFactor(scale);
    }

    public void actionPerformed(ActionEvent evt) {
        switch (whichDirection) {
            case INCREMENT ->
                root.panel.view.incrementFontSize();
            case RESET ->
                root.panel.view.resetFontSize();
            case DECREMENT ->
                root.panel.view.decrementFontSize();
        }
    }
}
