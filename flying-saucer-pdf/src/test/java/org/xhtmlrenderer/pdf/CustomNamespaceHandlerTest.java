package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.NoNamespaceHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CustomNamespaceHandlerTest {
    @Test
    void usingCustomNamespaceHandler() throws IOException {
        Document doc = XMLResource.load("""
            <html>
                <body><h1>Hello, world!</h1></body>
            </html>
            """).getDocument();
        NamespaceHandler handler = spy(new NoNamespaceHandler());
        final PDF pdf;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocument(doc, null, handler);
            renderer.createPDF(doc, os);
            os.flush();
            pdf = new PDF(os.toByteArray());
        }

        assertThat(pdf).containsText("Hello, world!");
        verify(handler).getDefaultStylesheet();
        verify(handler).getStylesheets(doc);
        verifyNoMoreInteractions(handler);
    }
}
