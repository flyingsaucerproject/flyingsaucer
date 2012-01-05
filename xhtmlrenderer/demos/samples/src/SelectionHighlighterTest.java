/*
 * {{{ header & license
 * Copyright (c) 2007 Nick Reddel
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */


import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;

import javax.swing.*;

import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.SelectionHighlighter;
import org.xhtmlrenderer.swing.SelectionHighlighter.CopyAction;

/**
 * Sample for text selection in a rendered document; allows you to select text in the document
 * and copy to the clipboard.
 *
 * @author Nick Reddel
 */
public class SelectionHighlighterTest extends JFrame {

    /**
     * @param args
     */
    public static void main(String[] args) {
        new SelectionHighlighterTest().setVisible(true);
    }

    public SelectionHighlighterTest() {
        // create the panel--standard setup
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(new JLabel("Use mouse to select/highlight text"), BorderLayout.NORTH);
        mainPanel.setMinimumSize(new Dimension(300,300));
        mainPanel.setPreferredSize(new Dimension(700,500));
        XHTMLPanel xhtmlPanel = new XHTMLPanel();

        // selection highlight in FS uses the textArea.selectionForeground and textArea.selectionBackground properties in
        // the UIManager
        //UIManager.put("TextArea.selectionBackground", Color.BLACK);
        //UIManager.put("TextArea.selectionForeground", Color.GREEN);
        try {
           xhtmlPanel.setDocument("http://www.w3.org/MarkUp/");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // install a selection highlighter no the panel
        final SelectionHighlighter caret = new SelectionHighlighter();
        caret.install(xhtmlPanel);
        caret.selectAll();

        FSScrollPane fs = new FSScrollPane(xhtmlPanel);

        mainPanel.add(fs,BorderLayout.CENTER);

        //
        // actions
        //
        JPanel actionPanel = new JPanel(new FlowLayout());
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        // Select all text
        Action selectAll = new AbstractAction("Select All") {
            public void actionPerformed(ActionEvent event) {
                caret.selectAll();
            }
        };
        actionPanel.add(new JButton(selectAll));



        // Copy selection
        // install an action to copy selected test; must be "installed" around
        // the selection highlighter (caret) we just created
        CopyAction copyAction = new SelectionHighlighter.CopyAction();
        copyAction.install(caret);

        actionPanel.add(new JButton(copyAction), BorderLayout.SOUTH);
        add(mainPanel);
        
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
