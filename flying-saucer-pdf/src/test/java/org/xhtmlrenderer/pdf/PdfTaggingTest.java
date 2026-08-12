package org.xhtmlrenderer.pdf;

import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class PdfTaggingTest {

    @Test
    void taggingIsDisabledByDefault() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setDocumentFromString("<html><body><h1>Title</h1><p>Body text</p></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> assertThat(catalog.getStructureTreeRoot()).isNull());
    }

    @Test
    void tagsHeadingsAndParagraphs() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><h1>Title</h1><p>Body text</p></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);
            assertThat(elements).extracting(PDStructureElement::getStructureType).contains("H1", "P");
        });
    }

    @Test
    void tagsImageAsFigureWithAlt() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><img src=\"classpath:flyingsaucer.png\" alt=\"A cat\" /></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);
            assertThat(elements)
                    .filteredOn(e -> "Figure".equals(e.getStructureType()))
                    .extracting(PDStructureElement::getAlternateDescription)
                    .containsExactly("A cat");
        });
    }

    @Test
    void decorativeImageWithEmptyAltIsNotTaggedAsFigure() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><img src=\"classpath:flyingsaucer.png\" alt=\"\" /></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);
            assertThat(elements).extracting(PDStructureElement::getStructureType).doesNotContain("Figure");
        });
    }

    private static void withCatalog(ITextRenderer renderer, Consumer<PDDocumentCatalog> assertions) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        renderer.createPDF(os);
        byte[] pdfBytes = os.toByteArray();

        try (RandomAccessRead buffer = new RandomAccessReadBuffer(pdfBytes);
             PDDocument document = new PDFParser(buffer).parse()) {
            assertions.accept(document.getDocumentCatalog());
        }
    }

    private static List<PDStructureElement> structureElementsOf(PDDocumentCatalog catalog) {
        PDStructureTreeRoot root = catalog.getStructureTreeRoot();
        assertThat(root).isNotNull();

        List<PDStructureElement> elements = new ArrayList<>();
        collectStructureElements(root, elements);
        return elements;
    }

    private static void collectStructureElements(PDStructureNode node, List<PDStructureElement> elements) {
        for (Object kid : node.getKids()) {
            if (kid instanceof PDStructureElement structureElement) {
                elements.add(structureElement);
                collectStructureElements(structureElement, elements);
            }
        }
    }
}
