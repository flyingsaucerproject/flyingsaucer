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
package org.xhtmlrenderer.demo.browser;

import org.w3c.dom.Document;
import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.PDFCreationListener;
import org.xhtmlrenderer.pdf.util.XHtmlMetaToPdfInfoAdapter;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.swing.ImageResourceLoader;
import org.xhtmlrenderer.swing.ScalableXHTMLPanel;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;
import org.xhtmlrenderer.util.GeneralUtil;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRRuntimeException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Description of the Class
 *
 * @author empty
 */
public class BrowserPanel extends JPanel implements DocumentListener {
	private static final long serialVersionUID = 1L;

	/**
	 * Description of the Field
	 */
	JButton forward;
	/**
	 * Description of the Field
	 */
	JButton backward;
	/**
	 * Description of the Field
	 */
	JButton stop;
	/**
	 * Description of the Field
	 */
	JButton reload;
	/**
	 * Description of the Field
	 */
	JButton goHome;
	/**
	 * Description of the Field
	 */
	JButton font_inc;
	/**
	 * Description of the Field
	 */
	JButton font_rst;
	/**
	 * Description of the Field
	 */
	JButton font_dec;
	JButton print;
	/**
	 * Description of the Field
	 */
	JTextField url;
	/**
	 * Description of the Field
	 */
	BrowserStatus status;
	/**
	 * Description of the Field
	 */
	public ScalableXHTMLPanel view;
	/**
	 * Description of the Field
	 */
	JScrollPane scroll;
	/**
	 * Description of the Field
	 */
	BrowserStartup root;
	/**
	 * Description of the Field
	 */
	BrowserPanelListener listener;

	JButton print_preview;

	/**
	 * Description of the Field
	 */
	public static final Logger logger = Logger.getLogger("app.browser");

	private PanelManager manager;
	JButton goToPage;
	public JToolBar toolbar;

	/**
	 * Constructor for the BrowserPanel object
	 *
	 * @param root	 PARAM
	 * @param listener PARAM
	 */
	public BrowserPanel(BrowserStartup root, BrowserPanelListener listener) {
		super();
		this.root = root;
		this.listener = listener;
	}

	/**
	 * Description of the Method
	 */
	public void init() {
		forward = new JButton();
		backward = new JButton();
		stop = new JButton();
		reload = new JButton();
		goToPage = new JButton();
		goHome = new JButton();

		url = new JTextField();
		url.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				super.focusGained(e);
				url.selectAll();
			}

			public void focusLost(FocusEvent e) {
				super.focusLost(e);
				url.select(0, 0);
			}
		});


		manager = new PanelManager();
        view = new ScalableXHTMLPanel(manager);
        manager.setRepaintListener(view);
        ImageResourceLoader irl = new ImageResourceLoader();
        irl.setRepaintListener(view);
        manager.setImageResourceLoader(irl);
        view.getSharedContext().setReplacedElementFactory(new SwingReplacedElementFactory(view, irl));
        view.addDocumentListener(manager);
        view.setCenteredPagedView(true);
        view.setBackground(Color.LIGHT_GRAY);
        scroll = new FSScrollPane(view);
		print_preview = new JButton();
		print = new JButton();

		loadCustomFonts();

		status = new BrowserStatus();
		status.init();

		initToolbar();

		int text_width = 200;
		view.setPreferredSize(new Dimension(text_width, text_width));

		setLayout(new BorderLayout());
		this.add(scroll, BorderLayout.CENTER);
	}

	private void initToolbar() {
		toolbar = new JToolBar();
		toolbar.setRollover(true);
		toolbar.add(backward);
		toolbar.add(forward);
		toolbar.add(reload);
		toolbar.add(goHome);
		toolbar.add(url);
		toolbar.add(goToPage);
		// disabled for R6
		// toolbar.add(print);
        toolbar.setFloatable(false);
    }

	private void loadCustomFonts() {
		SharedContext rc = view.getSharedContext();
		try {
			rc.setFontMapping("Fuzz", Font.createFont(Font.TRUETYPE_FONT,
					new DemoMarker().getClass().getResourceAsStream("/demos/fonts/fuzz.ttf")));
		} catch (Exception ex) {
			Uu.p(ex);
		}
	}

	/**
	 * Description of the Method
	 */
	public void createLayout() {
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gbl);

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(toolbar, c);
		add(toolbar);

		//c.gridx = 0;
		c.gridx++;
		c.gridy++;
		c.weightx = c.weighty = 0.0;
		c.insets = new Insets(5, 0, 5, 5);
		gbl.setConstraints(backward, c);
		add(backward);

		c.gridx++;
		gbl.setConstraints(forward, c);
		add(forward);

		c.gridx++;
		gbl.setConstraints(reload, c);
		add(reload);

		c.gridx++;
		c.fill = GridBagConstraints.NONE;
		c.weightx = c.weighty = 0.0;
		gbl.setConstraints(print_preview, c);
		add(print_preview);

		c.gridx++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 5;
		c.ipady = 5;
		c.weightx = 10.0;
		c.insets = new Insets(5, 0, 5, 0);
		gbl.setConstraints(url, c);
		url.setBorder(BorderFactory.createLoweredBevelBorder());
		add(url);

		c.gridx++;
		c.fill = GridBagConstraints.NONE;
		c.weightx = c.weighty = 0.0;
		c.insets = new Insets(0, 5, 0, 0);
		gbl.setConstraints(goToPage, c);
		add(goToPage);

		c.gridx = 0;
		c.gridy++;
		c.ipadx = 0;
		c.ipady = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 7;
		c.weightx = c.weighty = 10.0;
		gbl.setConstraints(scroll, c);
		add(scroll);

		c.gridx = 0;
		c.gridy++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0.1;
		gbl.setConstraints(status, c);
		add(status);

	}

	/**
	 * Description of the Method
	 */
	public void createActions() {
		// set text to "" to avoid showing action text in button--
		// we only want it in menu items
		backward.setAction(root.actions.backward);
		backward.setText("");
		forward.setAction(root.actions.forward);
		forward.setText("");
		reload.setAction(root.actions.reload);
		reload.setText("");
		goHome.setAction(root.actions.goHome);
		goHome.setText("");
		print_preview.setAction(root.actions.print_preview);
		print_preview.setText("");

		url.setAction(root.actions.load);
		goToPage.setAction(root.actions.goToPage);
		updateButtons();
	}


	/**
	 * Description of the Method
	 */
	public void goForward() {
		String uri = manager.getForward();
		view.setDocument(uri);
		updateButtons();
	}

	/**
	 * Description of the Method
	 */
	public void goBack() {
		String uri = manager.getBack();
		view.setDocument(uri);
		updateButtons();
	}

	/**
	 * Description of the Method
	 */
	public void reloadPage() {
		logger.info("Reloading Page: ");
		if (manager.getBaseURL() != null) {
			loadPage(manager.getBaseURL());
		}
	}

	/**
	 * Description of the Method
	 *
	 * @param url_text PARAM
	 */
	//TODO: make this part of an implementation of UserAgentCallback instead
	public void loadPage(final String url_text) {
		try {
			logger.info("Loading Page: " + url_text);
			view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			view.setDocument(url_text);
			view.addDocumentListener(BrowserPanel.this);

			updateButtons();

			setStatus("Successfully loaded: " + url_text);

			if (listener != null) {
				listener.pageLoadSuccess(url_text, view.getDocumentTitle());
			}
		} catch (XRRuntimeException ex) {
			XRLog.general(Level.SEVERE, "Runtime exception", ex);
            setStatus("Can't load document");
            handlePageLoadFailed(url_text, ex);
        } catch (Exception ex) {
			XRLog.general(Level.SEVERE, "Could not load page for display.", ex);
			ex.printStackTrace();
		}
	}
	
	public void exportToPdf( String path )
	{
       if (manager.getBaseURL() != null) {
           setStatus( "Exporting to " + path + "..." );
           OutputStream os = null;
           try {
               os = new FileOutputStream(path);
               try {
               ITextRenderer renderer = new ITextRenderer();

               DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
               DocumentBuilder db = dbf.newDocumentBuilder();
               Document doc =  db.parse(manager.getBaseURL());

               PDFCreationListener pdfCreationListener = new XHtmlMetaToPdfInfoAdapter( doc );
               renderer.setListener( pdfCreationListener );
                              
               renderer.setDocument(manager.getBaseURL());
               renderer.layout();

               renderer.createPDF(os);
               setStatus( "Done export." );
            } catch (Exception e) {
                XRLog.general(Level.SEVERE, "Could not export PDF.", e);
                e.printStackTrace();
                setStatus( "Error exporting to PDF." );
               } finally {
                   try {
                       os.close();
                   } catch (IOException e) {
                       // swallow
            }
        }
           } catch (Exception e) {
               e.printStackTrace();
	}
       }
	}

    private void handlePageLoadFailed(String url_text, XRRuntimeException ex) {
        final XMLResource xr;
        final String rootCause = getRootCause(ex);
        final String msg = GeneralUtil.escapeHTML(addLineBreaks(rootCause, 80));
        String notFound =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE html PUBLIC \" -//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
                        "<body>\n" +
                        "<h1>Document can't be loaded</h1>\n" +
                        "<p>Could not load the page at \n" +
                        "<pre>" + GeneralUtil.escapeHTML(url_text) + "</pre>\n" +
                        "</p>\n" +
                        "<p>The page failed to load; the error was </p>\n" +
                        "<pre>" + msg + "</pre>\n" +
                        "</body>\n" +
                        "</html>";

        xr = XMLResource.load(new StringReader(notFound));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                root.panel.view.setDocument(xr.getDocument(), null);
            }
        });
   }

    private String addLineBreaks(String _text, int maxLineLength) {
        StringBuffer broken = new StringBuffer(_text.length() + 10);
        boolean needBreak = false;
        for (int i = 0; i < _text.length(); i++) {
            if (i > 0 && i % maxLineLength == 0) needBreak = true;

            final char c = _text.charAt(i);
            if (needBreak && Character.isWhitespace(c)) {
                System.out.println("Breaking: " + broken.toString());
                needBreak = false;
                broken.append('\n');
            } else {
                broken.append(c);
            }
        }
        System.out.println("Broken! " + broken.toString());
        return broken.toString();  
    }

    private String getRootCause(Exception ex) {
        // FIXME
        Throwable cause = ex;
        while (cause != null) {
            cause = cause.getCause();
        }

        return cause == null ? ex.getMessage() : cause.getMessage();
    }

    public void documentStarted() {
		// TODO...
	}

	/**
	 * Description of the Method
	 */
	public void documentLoaded() {
		view.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Sets the status attribute of the BrowserPanel object
	 *
	 * @param txt The new status value
	 */
	public void setStatus(String txt) {
		status.text.setText(txt);
	}

	/**
	 * Description of the Method
	 */
	protected void updateButtons() {
		if (manager.hasBack()) {
			root.actions.backward.setEnabled(true);
		} else {
			root.actions.backward.setEnabled(false);
		}
		if (manager.hasForward()) {
			root.actions.forward.setEnabled(true);
		} else {
			root.actions.forward.setEnabled(false);
		}

		url.setText(manager.getBaseURL());
	}


	public void onLayoutException(Throwable t) {
        // TODO: clean
        t.printStackTrace();
	}

	public void onRenderException(Throwable t) {
        // TODO: clean
		t.printStackTrace();
	}
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.41  2009/08/03 19:36:29  pdoubleya
 * Add new listener for PDF generation which automatically parses HTML header information and adds it as PDF properties (e.g. title, subject). Patch submitted by Tim Telcik in email. Thanks!
 *
 * Revision 1.38  2008/09/06 18:44:29  peterbrant
 * Add PDF export to browser (patch from Mykola Gurov)
 *
 * Revision 1.37  2008/05/30 13:25:00  pdoubleya
 * Remove commented code blocks, add error handling if can't load page.
 *
 * Revision 1.36  2007/07/14 12:56:40  pdoubleya
 * Browser toolbar should not be movable
 *
 * Revision 1.35  2007/07/13 13:32:31  pdoubleya
 * Add webstart entry point for browser with no URL or File/open option. Move Zoom to menu entry, add warning on first zoom. Move preview to menu entry. Reorganize launch method a little to allow for multiple entry points.
 *
 * Revision 1.34  2007/06/19 21:28:41  pdoubleya
 * Support for document scaling, from Christophe Marchand via email. Browser demo panel is now scalable as well.
 *
 * Revision 1.33  2007/04/03 13:18:15  peterbrant
 * Implement new DocumentListener API
 *
 * Revision 1.32  2006/08/06 21:27:00  pdoubleya
 * Removed printing for R6.
 *
 * Revision 1.31  2006/08/03 14:15:01  pdoubleya
 * Print action
 *
 * Revision 1.30  2006/07/31 14:20:54  pdoubleya
 * Bunch of cleanups and fixes. Now using a toolbar for actions, added Home button, next/prev navigation actions to facilitate demo file browsing, loading demo pages from a list, about dlg and link to user's manual.
 *
 * Revision 1.29  2005/10/27 00:08:50  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.28  2005/10/08 17:40:17  tobega
 * Patch from Peter Brant
 *
 * Revision 1.27  2005/06/19 23:32:46  joshy
 * cursor stuff
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.26  2005/06/16 07:24:43  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.25  2005/06/15 13:35:27  tobega
 * Fixed history
 *
 * Revision 1.24  2005/06/15 10:56:13  tobega
 * cleaned up a bit of URL mess, centralizing URI-resolution and loading to UserAgentCallback
 *
 * Revision 1.23  2005/06/01 21:36:34  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.22  2005/03/28 20:02:01  pdoubleya
 * Removed commented code, cleaned menu bar.
 *
 * Revision 1.21  2005/02/05 11:35:59  pdoubleya
 * Load pages directly from XMLResource.
 *
 * Revision 1.20  2005/02/03 23:19:43  pdoubleya
 * Uses ResourceProvider for loading files.
 *
 * Revision 1.19  2005/01/30 10:21:49  pdoubleya
 * Extracted keyboard actions to FSScrollPane.
 *
 * Revision 1.18  2005/01/29 20:17:42  pdoubleya
 * Updated panels to support page up/down properly, and formatted/cleaned.
 *
 * Revision 1.17  2005/01/13 00:48:47  tobega
 * Added preparation of values for a form submission
 *
 * Revision 1.16  2004/12/29 10:39:38  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
 * Revision 1.15  2004/12/12 16:11:04  tobega
 * Fixed bug concerning order of inline content. Added a demo for pseudo-elements.
 *
 * Revision 1.14  2004/12/12 03:33:07  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.13  2004/12/12 02:54:11  tobega
 * Making progress
 *
 * Revision 1.12  2004/12/01 00:13:34  tobega
 * Fixed incorrect handling of http urls.
 *
 * Revision 1.11  2004/11/27 15:46:37  joshy
 * lots of cleanup to make the code clearer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/17 14:58:17  joshy
 * added actions for font resizing
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/17 00:44:54  joshy
 * fixed bug in the history manager
 * added cursor support to the link listener
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/16 07:25:20  tobega
 * Renamed HTMLPanel to BasicPanel
 *
 * Revision 1.7  2004/11/16 03:43:25  joshy
 * first pass at printing support
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/12 20:43:29  joshy
 * added demo of custom font
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/12 02:23:56  joshy
 * added new APIs for rendering context, xhtmlpanel, and graphics2drenderer.
 * initial support for font mapping additions
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/23 14:38:58  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

