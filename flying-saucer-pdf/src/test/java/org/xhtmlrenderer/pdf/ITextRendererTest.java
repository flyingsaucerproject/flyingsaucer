package org.xhtmlrenderer.pdf;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.FSEntityResolver;

import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfXConformanceException;

import junit.framework.TestCase;

public class ITextRendererTest extends TestCase {
	
	private static final String TEST_HTML_FILE = "simplePdfTemplate.html";
	private static final String TEST_COLOUR_PROFILE = "/testColourProfile.icm";
	private static final String COLOUR_ERROR_MSG = "Colour space profile has not been set";
	
	private ITextRenderer renderer;
	
	public void setUp() throws UnsupportedEncodingException {
		renderer = new ITextRenderer();
	}
	
	public void testCreateNonPdfAConformance() throws Exception {
		createPdf(TEST_HTML_FILE);
	}
	
	public void testCreatePdfAConformanceNoFontsEmbedded() throws Exception {
		try {
			renderer.setPDFXConformance(PdfWriter.PDFA1A);
			renderer.setColourSpaceProfile(TEST_COLOUR_PROFILE);
			createPdf(TEST_HTML_FILE);
			fail();
		} catch(Exception e) {
			assertEquals(PdfXConformanceException.class, e.getClass());
		}
	}

	public void testCreatePdfAConformanceNoColourProfileSet() {
		try {
			renderer.setPDFXConformance(PdfWriter.PDFA1A);
			createPdf(TEST_HTML_FILE);
			fail();
		} catch (Exception e) {
			assertEquals(NullPointerException.class, e.getClass());
			assertEquals(COLOUR_ERROR_MSG, e.getMessage());
		}
	}
	
	private void createPdf(String htmlPath) throws Exception {
		URL htmlUrl = getClass().getResource(htmlPath);
		
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		builder.setEntityResolver(FSEntityResolver.instance());
		Document doc = builder.parse(htmlUrl.openStream());
		
		renderer.getSharedContext().setMedia("pdf");
		renderer.setDocument(doc, htmlUrl.toString());
		renderer.layout();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		renderer.createPDF(bos);
	}
}
