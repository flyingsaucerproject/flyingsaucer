package org.xhtmlrenderer.demo.browser;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

class SelectOnFocus extends FocusAdapter {
    private final JTextField field;

    SelectOnFocus(JTextField field) {
        this.field = field;
    }

    @Override
    public void focusGained(FocusEvent e) {
        super.focusGained(e);
        field.selectAll();
    }

    @Override
    public void focusLost(FocusEvent e) {
        super.focusLost(e);
        field.select(0, 0);
    }
}
