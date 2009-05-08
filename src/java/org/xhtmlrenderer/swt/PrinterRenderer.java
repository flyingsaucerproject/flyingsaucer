/*
 * {{{ header & license
 * Copyright (c) 2007 Vianney le Clément
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
package org.xhtmlrenderer.swt;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.extend.UserInterface;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.render.ViewportBox;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRLog;

/**
 * A renderer for a SWT Printer. Instances must be disposed with
 * {@link PrinterRenderer#dispose()}.
 * 
 * @author Vianney le Clément
 */
public class PrinterRenderer implements UserInterface {

    private final Printer _printer;
    private SharedContext _sharedContext;

    public PrinterRenderer(Printer printer) {
        this(printer, new NaiveUserAgent(printer));
    }

    public PrinterRenderer(Printer printer, UserAgentCallback uac) {
        _printer = printer;
        _sharedContext = new SharedContext(uac, new SWTFontResolver(printer),
            new SWTReplacedElementFactory(), new SWTTextRenderer(), printer
                .getDPI().y);
        _sharedContext.setPrint(true);
        _sharedContext.setInteractive(false);
    }

    /**
     * Dispose resources used by this {@link PrinterRenderer}. This does NOT
     * dispose the attached {@link Printer}.
     */
    public void dispose() {
        // dispose used fonts
        _sharedContext.flushFonts();
        // clean ReplacedElementFactory
        ((SWTReplacedElementFactory) _sharedContext.getReplacedElementFactory())
            .clean();
        // dispose images when using NaiveUserAgent
        UserAgentCallback uac = _sharedContext.getUac();
        if (uac instanceof NaiveUserAgent) {
            ((NaiveUserAgent) uac).disposeCache();
        }
    }

    /**
     * @return a new {@link LayoutContext}
     */
    protected LayoutContext newLayoutcontext(GC gc) {
        LayoutContext result = _sharedContext.newLayoutContextInstance();

        result.setFontContext(new SWTFontContext(gc));
        _sharedContext.getTextRenderer().setup(result.getFontContext());

        return result;
    }

    /**
     * @param gc
     * @return a new {@link RenderingContext}
     */
    protected RenderingContext newRenderingContext(GC gc) {
        RenderingContext result = _sharedContext.newRenderingContextInstance();

        result.setFontContext(new SWTFontContext(gc));
        result.setOutputDevice(new SWTOutputDevice(gc));

        _sharedContext.getTextRenderer().setup(result.getFontContext());

        return result;
    }

    public void print(Document doc, String url, NamespaceHandler nsh,
            String jobName, int startPage, int endPage) {
        // have to do this first ?
        if (Configuration.isTrue("xr.cache.stylesheets", true)) {
            _sharedContext.getCss().flushStyleSheets();
        } else {
            _sharedContext.getCss().flushAllStyleSheets();
        }

        _sharedContext.reset();

        _sharedContext.setBaseURL(url);
        _sharedContext.setNamespaceHandler(nsh);
        _sharedContext.getCss().setDocumentContext(_sharedContext,
            _sharedContext.getNamespaceHandler(), doc, this);

        if (!_printer.startJob(jobName)) {
            return;
        }
        GC gc = new GC(_printer);
        RenderingContext c = null;
        try {
            // LAYOUT
            LayoutContext layout = newLayoutcontext(gc);
            BlockBox rootBox;

            try {
                long start = System.currentTimeMillis();

                rootBox = BoxBuilder.createRootBox(layout, doc);

                PageBox first = Layer.createPageBox(layout, "first");
                rootBox.setContainingBlock(new ViewportBox(new Rectangle(0, 0,
                    first.getContentWidth(layout), first
                        .getContentHeight(layout))));
                rootBox.layout(layout);

                long end = System.currentTimeMillis();
                XRLog.layout(Level.INFO, "Layout took " + (end - start) + "ms");
            } catch (Throwable e) {
                XRLog.exception(e.getMessage(), e);
                return;
            }

            Layer root = rootBox.getLayer();
            Dimension intrinsic_size = root.getPaintingDimension(layout);
            root.trimEmptyPages(layout, intrinsic_size.height);
            root.assignPagePaintingPositions(layout, Layer.PAGED_MODE_PRINT);

            // RENDER
            c = newRenderingContext(gc);
            List pages = root.getPages();
            c.setPageCount(pages.size());
            if (startPage < 0) {
                startPage = 0;
            } else if (startPage >= pages.size()) {
                startPage = pages.size() - 1;
            }
            if (endPage < 0 || endPage >= pages.size()) {
                endPage = pages.size() - 1;
            }

            Shape working = c.getOutputDevice().getClip();

            for (int i = startPage; i <= endPage; i++) {
                PageBox page = (PageBox) pages.get(i);
                c.setPage(i, page);

                if (!_printer.startPage()) {
                    return;
                }
                
                page.paintBackground(c, 0, Layer.PAGED_MODE_PRINT);
                page.paintMarginAreas(c, 0, Layer.PAGED_MODE_PRINT);
                page.paintBorder(c, 0, Layer.PAGED_MODE_PRINT);

                Rectangle content = page.getPrintClippingBounds(c);
                c.getOutputDevice().clip(content);

                int top = -page.getPaintingTop()
                        + page.getMarginBorderPadding(c, CalculatedStyle.TOP);
                int left = page.getMarginBorderPadding(c, CalculatedStyle.LEFT);

                c.getOutputDevice().translate(left, top);
                root.paint(c);
                c.getOutputDevice().translate(-left, -top);

                c.getOutputDevice().setClip(working);

                _printer.endPage();
            }

        } finally {
            // cleanup
            if (c != null) {
                ((SWTOutputDevice) c.getOutputDevice()).clean();
            }
            gc.dispose();
            _printer.endJob();
        }
    }

    public void print(String url, NamespaceHandler nsh, String jobName,
            int startPage, int endPage) {
        print(loadDocument(url), url, nsh, jobName, startPage, endPage);
    }

    protected Document loadDocument(final String uri) {
        XMLResource xmlResource = _sharedContext.getUac().getXMLResource(uri);
        if (xmlResource == null) {
            return null;
        }
        return xmlResource.getDocument();
    }

    public boolean isActive(Element e) {
        return false;
    }

    public boolean isFocus(Element e) {
        return false;
    }

    public boolean isHover(Element e) {
        return false;
    }

    // static helpers
    public static void print(Printer printer, Document doc, String url,
            NamespaceHandler nsh, String jobName, int startPage, int endPage) {
        PrinterRenderer render = new PrinterRenderer(printer);
        render.print(doc, url, nsh, jobName, startPage, endPage);
        render.dispose();
    }

    public static void print(Printer printer, String url, NamespaceHandler nsh,
            String jobName, int startPage, int endPage) {
        PrinterRenderer render = new PrinterRenderer(printer);
        render.print(url, nsh, jobName, startPage, endPage);
        render.dispose();
    }

    public static void print(PrinterData printerData, Document doc, String url,
            NamespaceHandler nsh, String jobName) {
        Printer printer = new Printer(printerData);
        int startPage = -1, endPage = -1;
        if (printerData.scope == PrinterData.PAGE_RANGE) {
            startPage = printerData.startPage - 1;
            endPage = printerData.endPage - 1;
        }
        print(printer, doc, url, nsh, jobName, startPage, endPage);
        printer.dispose();
    }

    public static void print(PrinterData printerData, String url,
            NamespaceHandler nsh, String jobName) {
        Printer printer = new Printer(printerData);
        int startPage = -1, endPage = -1;
        if (printerData.scope == PrinterData.PAGE_RANGE) {
            startPage = printerData.startPage - 1;
            endPage = printerData.endPage - 1;
        }
        print(printer, url, nsh, jobName, startPage, endPage);
        printer.dispose();
    }

    public static void printDialog(Shell shell, Document doc, String url,
            NamespaceHandler nsh, String jobName) {
        PrintDialog dlg = new PrintDialog(shell);
        dlg.setText(jobName);
        PrinterData data = dlg.open();
        if (data != null) {
            print(data, doc, url, nsh, jobName);
        }
    }

    public static void printDialog(Shell shell, String url,
            NamespaceHandler nsh, String jobName) {
        PrintDialog dlg = new PrintDialog(shell);
        dlg.setText(jobName);
        PrinterData data = dlg.open();
        if (data != null) {
            print(data, url, nsh, jobName);
        }
    }

}
