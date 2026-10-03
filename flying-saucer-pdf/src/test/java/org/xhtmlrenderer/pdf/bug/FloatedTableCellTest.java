package org.xhtmlrenderer.pdf.bug;

import com.codeborne.pdftest.PDF;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;

import java.io.IOException;

import static com.codeborne.pdftest.assertj.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

/**
 * Reproducible example for <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/276">issue 276</a>
 */
class FloatedTableCellTest {
	private static final Logger log = LoggerFactory.getLogger(FloatedTableCellTest.class);

	@Test
	void tableWithFloatedCell() throws IOException {
		String page = """
			 <html>
				<body>
					<table>
						<tbody>
							<tr>
								<td id="cell-1" style="float:left;">first cell {float:left}</td>
								<td id="cell-2">second cell</td>
							</tr>
						</tbody>
					</table>
				</body>
			</html>""";

        ITextRenderer renderer = new ITextRenderer();
		byte[] result = renderer.createPDF(XMLResource.load(page).getDocument());
		PDF pdf = printFile(log, result, "table-with-floated-cell.pdf");
		assertThat(pdf).containsText("first cell", "second cell");
	}

	@Test
	void tableCell() throws IOException {
		String page = """
			<table><tr><td style="float:left;">first cell {float:left;}</td></tr></table>""";

        ITextRenderer renderer = new ITextRenderer();
		byte[] result = renderer.createPDF(XMLResource.load(page).getDocument());

		PDF pdf = printFile(log, result, "table-cell.pdf");
		assertThat(pdf).containsText("first cell");
	}
}
