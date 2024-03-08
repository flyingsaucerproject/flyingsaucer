package org.xhtmlrenderer.demo.browser.actions;

import org.xhtmlrenderer.demo.browser.ScaleFactor;
import org.xhtmlrenderer.swing.ScalableXHTMLPanel;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.swing.*;
import java.awt.event.ActionEvent;

import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

/**
 * Implements zooming of the browser panel.
 */
@ParametersAreNonnullByDefault
public class ZoomAction extends AbstractAction {
    private static boolean needsWarning = true;

    private final ScaleFactor scalingFactor;
    private final ScalableXHTMLPanel view;

    public ZoomAction(ScalableXHTMLPanel panel, ScaleFactor factor) {
        super(factor.zoomLabel());
        scalingFactor = factor;
        view = panel;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (needsWarning) {
            String msg = """
                    The "zoom" feature is experimental, and some pages
                    will display incorrectly at certain zoom resolutions
                    due to layout calculations not being zoom-aware.""";
            showMessageDialog(null, msg, "Zoom Panel", WARNING_MESSAGE);
            needsWarning = false;
        }

        if (scalingFactor.factor() == ScaleFactor.PAGE_WIDTH) {
            view.setScalePolicy(ScalableXHTMLPanel.SCALE_POLICY_FIT_WIDTH);
            view.setDocument(view.getDocument());
        } else if (scalingFactor.factor() == ScaleFactor.PAGE_HEIGHT) {
            view.setScalePolicy(ScalableXHTMLPanel.SCALE_POLICY_FIT_HEIGHT);
            view.setDocument(view.getDocument());
        } else if (scalingFactor.factor() == ScaleFactor.PAGE_WHOLE) {
            view.setScalePolicy(ScalableXHTMLPanel.SCALE_POLICY_FIT_WHOLE);
            view.setDocument(view.getDocument());
        } else {
            view.setScale(scalingFactor.factor());
        }
    }
}