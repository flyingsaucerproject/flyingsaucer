package org.xhtmlrenderer.pdf.borderradius;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.FSEntityResolver;

import junit.framework.TestCase;

public class BorderRadiusNonRegressionTest extends TestCase {
	
	/**
	 * This used to throw a ClassCastException (before this fix).
	 */
	public void testBorderRadiusWithBorderWidthZero() throws Exception {
		testNoException("borderRadiusWithBorderWidthZero.html");
	}
	
	private void testNoException(String htmlPath) throws Exception {
		URL htmlUrl = getClass().getResource(htmlPath);
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		builder.setEntityResolver(FSEntityResolver.instance());
		
		Document doc = builder.parse(htmlUrl.openStream());
		
		ITextRenderer renderer = new ITextRenderer();
		renderer.getSharedContext().setMedia("pdf");
		
		renderer.setDocument(doc, htmlUrl.toString());
		renderer.layout();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		renderer.createPDF(bos);
	}

}
