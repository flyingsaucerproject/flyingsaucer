package org.xhtmlrenderer.demo.browser.actions;

import org.xhtmlrenderer.demo.browser.ScaleFactor;
import org.xhtmlrenderer.swing.ScalableXHTMLPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Implements zooming of the browser panel.
 */
public class ZoomAction extends AbstractAction {
    private static boolean needsWarning = true;

    private ScaleFactor scalingFactor;
    private final ScalableXHTMLPanel view;

    public ZoomAction(ScalableXHTMLPanel panel, ScaleFactor factor) {
        super(factor.getZoomLabel());
        this.scalingFactor = factor;
        this.view = panel;
    }

    public void actionPerformed(ActionEvent evt) {
        if (needsWarning) {
            String msg = "The \"zoom\" feature is experimental, and some pages\n" +
                    "will display incorrectly at certain zoom resolutions\n" +
                    "due to layout calculations not being zoom-aware.";
            JOptionPane.showMessageDialog(null, msg, "Zoom Panel", JOptionPane.WARNING_MESSAGE);
            needsWarning = false;
        }

        if (scalingFactor.getFactor() == ScaleFactor.PAGE_WIDTH) {
            view.setScalePolicy(ScalableXHTMLPanel.SCALE_POLICY_FIT_WIDTH);
            view.setDocument(view.getDocument());
        } else if (scalingFactor.getFactor() == ScaleFactor.PAGE_HEIGHT) {
            view.setScalePolicy(ScalableXHTMLPanel.SCALE_POLICY_FIT_HEIGHT);
            view.setDocument(view.getDocument());
        } else if (scalingFactor.getFactor() == ScaleFactor.PAGE_WHOLE) {
            view.setScalePolicy(ScalableXHTMLPanel.SCALE_POLICY_FIT_WHOLE);
            view.setDocument(view.getDocument());
        } else {
            view.setScale(scalingFactor.getFactor());
        }
    }
}