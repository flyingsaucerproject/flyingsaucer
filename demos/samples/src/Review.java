import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import jsyntaxpane.DefaultSyntaxKit;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

public class Review {
    private String lastRenderedHex;

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Review();
            }
        });
    }

    public Review() {
        JFrame f = new JFrame(Review.class.getName());
        final Container c = f.getContentPane();
        c.setLayout(new BorderLayout());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setOneTouchExpandable(true);
        split.setDividerLocation(300);

        c.add(split, BorderLayout.CENTER);

        DefaultSyntaxKit.initKit();

        final JEditorPane codeEditor = new JEditorPane();
        JScrollPane scrPane = new JScrollPane(codeEditor);
        split.setBottomComponent(scrPane);


        JTabbedPane tabs = new JTabbedPane();
        final XHTMLPanel xhtmlPanel = new XHTMLPanel();
        tabs.addTab("HTML", new FSScrollPane(xhtmlPanel));

        final FSPagePanel pdfPanel = new FSPagePanel();
        tabs.addTab("PDF", new JScrollPane(pdfPanel));

        JPanel top = new JPanel(new BorderLayout());
        JPanel controls = new JPanel();
        JButton render = new JButton(new AbstractAction("Render") {
            public void actionPerformed(ActionEvent e) {
                reRender(codeEditor, xhtmlPanel, pdfPanel);
            }
        });
        controls.add(render);
        top.add(controls, BorderLayout.NORTH);
        top.add(tabs, BorderLayout.CENTER);
        split.setTopComponent(top);
        c.doLayout();

        setContent(codeEditor);

        f.setSize(800, 600);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        /*EventQueue.invokeLater(new Runnable() {
            public void run() {
                reRender(codeEditor, xhtmlPanel, pdfPanel);

            }
        });*/

    }

    private void reRender(JEditorPane codeEditor, XHTMLPanel xhtmlPanel, FSPagePanel pdfPanel) {
        String content = codeEditor.getText();
        String currentHex = getDigestHex(content.getBytes());
        if (currentHex.equals(lastRenderedHex)) {
            XRLog.general("Already rendered, skipping");
            return;
        }

        try {
            xhtmlPanel.setDocumentFromString(content, null, new XhtmlNamespaceHandler());
            ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
            ITextRenderer renderer = new ITextRenderer();

            renderer.setDocumentFromString(content);
            renderer.layout();

            renderer.createPDF(os);
            os.flush();


            ByteBuffer buf = ByteBuffer.wrap(os.toByteArray());
            PDFFile pdffile = new PDFFile(buf);

            // show the first page
            XRLog.general("Page count: " + pdffile.getNumPages());
            PDFPage page = pdffile.getPage(0);
            pdfPanel.setZoom(-3.0);
            pdfPanel.showPage(page);
            lastRenderedHex = currentHex;

            XRLog.general("MD5 via BI: " + currentHex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDigestHex(byte[] buffer) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(buffer);
            return new BigInteger(1, md5.digest()).toString(16).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }


    private void setContent(JEditorPane codeEditor) {
        codeEditor.setContentType("text/xml");
        StringBuffer sb = new StringBuffer(256);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">\n");
        sb.append("<head>\n");
        sb.append("<style type=\"text/css\">\n");
        sb.append("</style>\n");
        sb.append("</head>\n\n");
        sb.append("<body>\n");
        sb.append("  <p>Hello World</p>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        codeEditor.setText(sb.toString());
    }

    public static class FSPagePanel extends JPanel implements ImageObserver, MouseListener, MouseMotionListener {

        Image currentImage;
        PDFPage currentPage;
        Dimension prevSize;
        Flag flag = new Flag();
        AffineTransform af;
        double zoomFactor = -1.0;
        double scale = 1.0;
        public final static double MAX_SCALE = 5;
        public final static double MIN_SCALE = 0.2;
        JViewport jvp = null;
        public final int SCROLLBAR_BORDER = 2;
        public final int SCROLLBAR_WIDTH = SCROLLBAR_BORDER + ((Integer) UIManager.get("ScrollBar.width")).intValue();

        public FSPagePanel() {
            super();
            setFocusable(true);
            addMouseListener(this);
            addMouseMotionListener(this);
            setBackground(Color.darkGray);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        /**
         * Stop the generation of any previous page, and draw the new one.
         *
         * @param page the PDFPage to draw.
         */
        public synchronized void showPage(PDFPage page) {
            // stop drawing the previous page
            if (currentPage != null && prevSize != null) {
                currentPage.stop(prevSize.width, prevSize.height, null);
            }

            // set up the new page
            currentPage = page;
            if (page == null) {
                // no page
                currentImage = null;
                af = null;
                repaint();
            } else {
                // start drawing -- clear the flag to indicate we're in progress.
                flag.clear();

                Dimension sz = getSize();

                if (this.getParent().getParent() instanceof JScrollPane) {
                    sz = ((JScrollPane) this.getParent().getParent()).getSize();
                    sz = new Dimension(sz.width - SCROLLBAR_BORDER, sz.height - SCROLLBAR_BORDER);
                }

                if (sz.width + sz.height <= 0) {
                    // no image to draw.
                    return;
                }

                Dimension pageSize = page.getUnstretchedSize(sz.width, sz.height, null);

                // get the new image
                int imw = pageSize.width;
                int imh = pageSize.height;

                int scrollbarWidth = 0;
                if (zoomFactor == -1.0) {//fit page
                    scale = Math.min(sz.getWidth() / imw, sz.getHeight() / imh);
                    imw *= scale;
                    imh *= scale;
                    setSize(sz.width, sz.height);
                    setPreferredSize(getSize());
                } else if (zoomFactor == -2.0) {//fit h
                    if ((sz.height * page.getAspectRatio()) > sz.width) {
                        scrollbarWidth = SCROLLBAR_WIDTH;
                    } else {
                        scrollbarWidth = 0;
                    }
                    scale = (sz.getHeight() - scrollbarWidth) / imh;
                    imh = sz.height - scrollbarWidth;
                    imw = (int) (imw * scale);
                    setSize(Math.max(imw, sz.width), imh);
                    setPreferredSize(getSize());
                } else if (zoomFactor == -3.0) {//fit w
                    if (sz.width > (sz.height * page.getAspectRatio())) {
                        scrollbarWidth = SCROLLBAR_WIDTH;
                    } else {
                        scrollbarWidth = 0;
                    }
                    scale = (sz.getWidth() - scrollbarWidth) / imw;
                    imw = sz.width - scrollbarWidth;
                    imh = (int) (imh * scale);
                    setSize(imw, Math.max(imh, sz.height));
                    setPreferredSize(getSize());
                } else if (zoomFactor == -4.0) {
                    imw = (int) page.getWidth();
                    imh = (int) page.getHeight();
                    scale = Math.min(sz.getWidth() / imw, sz.getHeight() / imh);
                    setSize(Math.max(imw, imh > sz.height ? sz.width - SCROLLBAR_WIDTH : sz.width), Math.max(imh, imw > sz.width ? sz.height - SCROLLBAR_WIDTH : sz.height));
                    setPreferredSize(getSize());
                } else if (zoomFactor > 0) {
                    scale = zoomFactor;
                    imw *= zoomFactor;
                    imh *= zoomFactor;
                    setSize(Math.max(imw, imh > sz.height ? sz.width - SCROLLBAR_WIDTH : sz.width), Math.max(imh, imw > sz.width ? sz.height - SCROLLBAR_WIDTH : sz.height));
                    setPreferredSize(getSize());
                }
                currentImage = page.getImage(imw, imh, null, this, true, true);


                prevSize = pageSize;


                repaint();

                if (this.getParent() instanceof JViewport) {
                    ((JViewport) this.getParent()).setViewPosition(new Point(0, 0));
                }
            }
        }

        public void paint(Graphics g1) {
            Graphics2D g2d = (Graphics2D) g1;
            try {
                g2d.setColor(getBackground());
                g2d.fillRect(0, 0, getWidth(), getHeight());
                if (currentImage == null) {
                    g2d.setColor(Color.black);
                } else {
                    // draw the image
                    int imw = currentImage.getWidth(null);
                    int imh = currentImage.getHeight(null);

                    // draw it centered within the panel
                    int offx = (getWidth() - imw) / 2;
                    int offy = (getHeight() - imh) / 2;
                    if (offx < 0) {
                        offx = 0;
                    }
                    if (offy < 0) {
                        offy = 0;
                    }
                    g2d.drawImage(currentImage, offx, offy, this);
                    af = null;
                }
            } finally {
                g2d.dispose();
            }
            requestFocus();
        }

        public synchronized void setZoom(double zf) {
            if (zf > 1) {
                this.zoomFactor = Math.min(zf, MAX_SCALE);
            } else if (zf < 1 && zf > 0) {
                this.zoomFactor = Math.max(zf, MIN_SCALE);
            } else if (zf == -4.0 || zf == -1.0 || zf == -2.0 || zf == -3.0) {
                this.zoomFactor = zf;
            }
            showPage(currentPage);
        }

        public double getScale() {
            return this.scale;
        }

        public synchronized PDFPage getPage() {
            return currentPage;
        }

        public Dimension getCurSize() {
            return prevSize;
        }

        public void waitForCurrentPage() {
            flag.waitForFlag();
        }

        /**
         * x location of the mouse-down event
         */
        int downx;
        /**
         * y location of the mouse-down event
         */
        int downy;

        /**
         * Handles a mousePressed event
         */
        public void mousePressed(MouseEvent evt) {
            downx = evt.getX();
            downy = evt.getY();
        }

        public void mouseReleased(MouseEvent evt) {
        }

        public void mouseClicked(MouseEvent evt) {
        }

        public void mouseEntered(MouseEvent evt) {
        }

        public void mouseExited(MouseEvent evt) {
        }

        public void mouseMoved(MouseEvent evt) {
        }

        public void mouseDragged(MouseEvent evt) {
            if (!(this.getParent() instanceof JViewport)) {
                return;
            }
            int x = evt.getX();
            int y = evt.getY();
            int dx = x - downx;
            int dy = y - downy;

            if (jvp == null) {
                jvp = (JViewport) this.getParent();
            }
            x = Math.max(jvp.getViewPosition().x - dx, 0);
            y = Math.max(jvp.getViewPosition().y - dy, 0);
            x = Math.min(x, getWidth() - jvp.getViewRect().width);
            y = Math.min(y, getHeight() - jvp.getViewRect().height);
            jvp.setViewPosition(new Point(x, y));
        }
    }

    public static class Flag {
        private boolean isSet;

        /**
         * Sets the flag.  Any pending waitForFlag calls will now return.
         */
        public synchronized void set() {
            isSet = true;
            notifyAll();
        }

        /**
         * Clears the flag.  Do this before calling waitForFlag.
         */
        public synchronized void clear() {
            isSet = false;
        }

        /**
         * Waits for the flag to be set, if it is not set already.
         * This method catches InterruptedExceptions, so if you want
         * notification of interruptions, use interruptibleWaitForFlag
         * instead.
         */
        public synchronized void waitForFlag() {
            while (!isSet) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                    // swallow
                }
            }
        }

        /**
         * Waits for the flag to be set, if it is not set already.
         */
        public synchronized void interruptibleWaitForFlag()
                throws InterruptedException {
            while (!isSet) {
                wait();
            }
        }
    }

}