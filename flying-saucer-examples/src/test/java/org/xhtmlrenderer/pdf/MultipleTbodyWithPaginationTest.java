package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import com.lowagie.text.DocumentException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.resource.XMLResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;

public class MultipleTbodyWithPaginationTest {
    private static final Logger log = LoggerFactory.getLogger(MultipleTbodyWithPaginationTest.class);

    @Test
    public void simplePdf() throws DocumentException, IOException {
        String htmlContent = """
                <html>
                <head>
                    <style>
                        table{
                            -fs-table-paginate: paginate;
                            width: 100%;
                        }
                        tbody {
                           page-break-inside: avoid;
                        }
                        td {
                            border: 1px solid gray;
                        }
                    </style>
                </head>
                
                <body>
                    <table>
                        <tbody id="tbody-1">
                            <tr id="row-1.a">
                                <td rowspan="2" id="left-1">Left 1</td>
                                <td height="300px" id="right-1.a">Right 1.a</td>
                            </tr>
                            <tr id="row-1.b">
                                <td height="300px" id="right-1.b">Right 1.b</td>
                            </tr>
                        </tbody>
                        <tbody id="tbody-2">
                            <tr id="row-2.a">
                                <td rowspan="2" id="left-2">Left 2</td>
                                <td height="300px" id="right-2.a">Right 2.a</td>
                            </tr>
                            <tr id="row-2.b">
                                <td height="300px" id="right-2.b">Right 2.b</td>
                            </tr>
                        </tbody>
                    </table>
                </body>
                </html>
                """;

        File file = new File("target/multiple-tbody-with-pagination.pdf");
        try (FileOutputStream o = new FileOutputStream(file)) {
            ITextRenderer renderer = new ITextRenderer();
            Document source = XMLResource.load(new StringReader(htmlContent)).getDocument();
            renderer.createPDF(source, o);
        }
        log.info("Generated PDF: {}", file.getAbsolutePath());

        PDF pdf = new PDF(file);
        assertThat(pdf.text).isEqualTo("Left 1\nRight 1.a\nRight 1.b\nLeft 2\nRight 2.a\nRight 2.b");
    }
}