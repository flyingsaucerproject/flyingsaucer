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


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.SelectionHighlighter;
import org.xhtmlrenderer.swing.SelectionHighlighter.CopyAction;

/**
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
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.add(new JLabel("hi"), BorderLayout.NORTH);
        jp.setMinimumSize(new Dimension(300,300));
        jp.setPreferredSize(new Dimension(700,500));
        XHTMLPanel p = new XHTMLPanel();
        try {
           p.setDocument("http://www.w3.org/MarkUp/");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SelectionHighlighter caret = new SelectionHighlighter();
        caret.install(p);
        FSScrollPane fs = new FSScrollPane(p);
        jp.add(fs,BorderLayout.CENTER);
        CopyAction copyAction = new SelectionHighlighter.CopyAction();
        copyAction.install(caret);
        jp.add(new JButton(copyAction), BorderLayout.SOUTH);
        add(jp);
        
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
