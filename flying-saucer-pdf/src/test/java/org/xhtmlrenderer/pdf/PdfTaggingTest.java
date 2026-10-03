package org.xhtmlrenderer.pdf;

import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDAttributeObject;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureNode;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDTableAttributeObject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class PdfTaggingTest {

    @Test
    void paragraphInsideTableCellIsAssociatedWithTheCellNotTheDocument() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><table><tr><td><p>Alice</p></td></tr></table></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);
            // The <p>'s text is marked against the TD's NonStruct wrapper (proving it's associated with the
            // required table hierarchy) rather than producing a separate P tag hanging flatly off Document.
            assertThat(elements).extracting(PDStructureElement::getStructureType).doesNotContain("P");
            assertThat(byType(elements, "TD")).hasSize(1);
            PDStructureElement td = byType(elements, "TD").get(0);
            assertThat(parentType(td)).isEqualTo("TR");
            assertThat(byType(elements, "NonStruct")).singleElement().extracting(PdfTaggingTest::parentType).isEqualTo("TD");
        });
    }

    @Test
    void tagsSecondDocumentCorrectlyWhenRendererIsReused() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);

        renderer.setDocumentFromString("<html><body><h1>First document</h1></body></html>");
        renderer.layout();
        renderer.createPDF(new ByteArrayOutputStream());

        renderer.setDocumentFromString("<html><body><p>Second document</p></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);
            assertThat(elements)
                    .extracting(PDStructureElement::getStructureType)
                    .contains("P")
                    .doesNotContain("H1");
        });
    }

    @Test
    void tagsListItemBodiesFreshOnEachCreatePdfCallForTheSameDocument() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><ul><li>An item</li></ul></body></html>");
        renderer.layout();

        renderer.createPDF(new ByteArrayOutputStream());

        ByteArrayOutputStream secondOutput = new ByteArrayOutputStream();
        renderer.createPDF(secondOutput);

        try (RandomAccessRead buffer = new RandomAccessReadBuffer(secondOutput.toByteArray());
             PDDocument document = new PDFParser(buffer).parse()) {
            List<PDStructureElement> elements = structureElementsOf(document.getDocumentCatalog());
            assertThat(elements).extracting(PDStructureElement::getStructureType).contains("L", "LI", "LBody");
        }
    }

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

    @Test
    void tagsTableSectionsRowsAndCells() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("""
            <html><body><table>
                <thead><tr><th>Name</th><th>Age</th></tr></thead>
                <tbody><tr><td>Alice</td><td>30</td></tr></tbody>
            </table></body></html>
            """);
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);

            assertThat(elements).extracting(PDStructureElement::getStructureType).contains("Table");
            assertThat(byType(elements, "TH")).allMatch(th -> "TR".equals(parentType(th)));
            assertThat(byType(elements, "TD")).allMatch(td -> "TR".equals(parentType(td)));
            assertThat(byType(elements, "TR"))
                    .extracting(PdfTaggingTest::parentType)
                    .containsExactlyInAnyOrder("THead", "TBody");
            assertThat(byType(elements, "THead")).allMatch(section -> "Table".equals(parentType(section)));
            assertThat(byType(elements, "TBody")).allMatch(section -> "Table".equals(parentType(section)));
        });
    }

    @Test
    void tagsImplicitTbodyForBareRows() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><table><tr><td>A</td></tr></table></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);

            PDStructureElement td = byType(elements, "TD").get(0);
            PDStructureElement tr = (PDStructureElement) td.getParent();
            PDStructureElement tbody = (PDStructureElement) tr.getParent();

            assertThat(tr.getStructureType()).isEqualTo("TR");
            assertThat(tbody.getStructureType()).isEqualTo("TBody");
            assertThat(parentType(tbody)).isEqualTo("Table");
        });
    }

    @Test
    void tagsCellSpanAttributes() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("""
            <html><body><table>
                <tr><td colspan="2">Wide</td></tr>
                <tr><td>A</td><td>B</td></tr>
            </table></body></html>
            """);
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);

            PDStructureElement wideCell = byType(elements, "TD").stream()
                    .filter(td -> td.getAttributes().size() > 0)
                    .findFirst()
                    .orElseThrow();
            PDAttributeObject attribute = wideCell.getAttributes().getObject(0);

            assertThat(attribute).isInstanceOf(PDTableAttributeObject.class);
            assertThat(((PDTableAttributeObject) attribute).getColSpan()).isEqualTo(2);
        });
    }

    @Test
    void doesNotTagUnspannedCells() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><table><tr><td>A</td></tr></table></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);

            PDStructureElement td = byType(elements, "TD").get(0);
            assertThat(td.getAttributes().size()).isZero();
        });
    }

    @Test
    void tableCellWithTextAndImageDoesNotCrash() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString(
                "<html><body><table><tr><td>Some text <img src=\"classpath:flyingsaucer.png\" alt=\"A cat\" /></td></tr></table></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);
            PDStructureElement figure = byType(elements, "Figure").get(0);
            assertThat(parentType(figure)).isEqualTo("TD");
        });
    }

    @Test
    void nestsImageInsideTableCell() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString(
                "<html><body><table><tr><td><img src=\"classpath:flyingsaucer.png\" alt=\"A cat\" /></td></tr></table></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);

            PDStructureElement figure = byType(elements, "Figure").get(0);
            assertThat(parentType(figure)).isEqualTo("TD");
        });
    }

    @Test
    void tagsListsAndListItems() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><ul><li>First</li><li>Second</li></ul></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);

            assertThat(elements).extracting(PDStructureElement::getStructureType).contains("L");
            List<PDStructureElement> items = byType(elements, "LI");
            assertThat(items).hasSize(2);
            assertThat(items).allMatch(li -> "L".equals(parentType(li)));
        });
    }

    @Test
    void nestsNestedLists() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><ul><li>Item<ul><li>Nested</li></ul></li></ul></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);

            List<PDStructureElement> lists = byType(elements, "L");
            assertThat(lists).hasSize(2);

            PDStructureElement innerList = lists.stream()
                    .filter(l -> "LI".equals(parentType(l)))
                    .findFirst()
                    .orElseThrow();
            PDStructureElement outerItem = (PDStructureElement) innerList.getParent();
            PDStructureElement outerList = (PDStructureElement) outerItem.getParent();

            assertThat(outerItem.getStructureType()).isEqualTo("LI");
            assertThat(outerList.getStructureType()).isEqualTo("L");
        });
    }

    @Test
    void tagsLinkText() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><a href=\"https://example.com\">Click here</a></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);
            assertThat(elements).extracting(PDStructureElement::getStructureType).contains("Link");
        });
    }

    @Test
    void doesNotTagAnchorWithoutHref() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><a name=\"anchor\">Text</a></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);
            assertThat(elements).extracting(PDStructureElement::getStructureType).doesNotContain("Link");
        });
    }

    @Test
    void setsDocumentLanguageWhenTagged() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html lang=\"en\"><body><p>Hello</p></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> assertThat(catalog.getLanguage()).isEqualTo("en"));
    }

    @Test
    void doesNotSetLanguageWhenAbsent() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><p>Hello</p></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> assertThat(catalog.getLanguage()).isNull());
    }

    @Test
    void setsDisplayDocTitleViewerPreferenceWhenTagged() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("<html><body><p>Hello</p></body></html>");
        renderer.layout();

        withCatalog(renderer, catalog -> assertThat(catalog.getViewerPreferences().displayDocTitle()).isTrue());
    }

    @Test
    void taggedTableWithBackgroundsAndBordersStillRendersCorrectStructure() throws IOException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getSharedContext().setMedia("pdf");
        renderer.setTagged(true);
        renderer.setDocumentFromString("""
            <html><body>
                <table style="border: 1px solid black; background: #eee;">
                    <tr><td style="border: 1px solid black; background: #ccc;">A</td></tr>
                </table>
            </body></html>
            """);
        renderer.layout();

        withCatalog(renderer, catalog -> {
            List<PDStructureElement> elements = structureElementsOf(catalog);

            PDStructureElement td = byType(elements, "TD").get(0);
            PDStructureElement tr = (PDStructureElement) td.getParent();
            PDStructureElement tbody = (PDStructureElement) tr.getParent();

            assertThat(tr.getStructureType()).isEqualTo("TR");
            assertThat(tbody.getStructureType()).isEqualTo("TBody");
            assertThat(parentType(tbody)).isEqualTo("Table");
        });
    }

    private static List<PDStructureElement> byType(List<PDStructureElement> elements, String type) {
        return elements.stream().filter(e -> type.equals(e.getStructureType())).toList();
    }

    private static String parentType(PDStructureElement element) {
        return ((PDStructureElement) element.getParent()).getStructureType();
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
