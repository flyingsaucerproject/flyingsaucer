
/* 
 * {{{ header & license 
 * Copyright (c) 2004 Joshua Marinacci 
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; either version 2.1 
 * of the License, or (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 
 * }}} 
 */

package org.xhtmlrenderer.test;

import org.xhtmlrenderer.layout.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.css.*;
import org.xhtmlrenderer.swing.*;
import java.awt.*;
import javax.swing.*;
import org.joshy.u;

public class CustomBlockTest {
    public static void main(String[] args) throws Exception {
        HTMLPanel panel = new HTMLPanel();
        LayoutFactory.addCustomLayout("custom",new XLayout());
        panel.setDocument("demos/customblock.xhtml");
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setSize(200,200);
        frame.show();
    }
}
