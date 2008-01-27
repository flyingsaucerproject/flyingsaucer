/*
 * {{{ header & license
 * Copyright (c) 2008 Patrick Wright
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

import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.XHTMLPrintable;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;


/**
 * Example of how to print an XHTMLPanel; the heart of the code is in the
 * {@link #printPanel(org.xhtmlrenderer.simple.XHTMLPanel)} method.
 *
 * @author Patrick Wright
 */
public class SimplePrintable {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Need file path");
            System.exit(-1);
        }

        if (args[0].startsWith("http:")) {
            new SimplePrintable().printURL(args[0]);
        } else {
            new SimplePrintable().printFile(new File(args[0]));
        }
    }

    private void printPanel(XHTMLPanel panel) {
        final PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(new XHTMLPrintable(panel));

        if (printJob.printDialog()) {
            try {

                printJob.print();

            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void printURL(String url) {
        XHTMLPanel panel = new XHTMLPanel();

        try {
            panel.setDocument(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        printPanel(panel);
    }

    public void printFile(File file) {
        printURL(file.toURI().toString());
    }
}
