package org.xhtmlrenderer.layout;

import java.util.List;
import java.util.Vector;

import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.render.RenderingContext;

public class LayerAccessible {

	public static void paintInlineContentAccessible(RenderingContext c, List lines, BoxRangeLists rangeLists) {
		BoxRangeHelper helper = new BoxRangeHelper(c.getOutputDevice(), rangeLists.getInline());
		// PDF/UA: List of processed element, we need a Vector for use it by
		// reference inside recursion methods
		Vector<Object> processedElements = new Vector<Object>();

		for (int i = 0; i < lines.size(); i++) {
			helper.popClipRegions(c, i);
			helper.pushClipRegion(c, i);

			InlinePaintable paintable = (InlinePaintable) lines.get(i);
			paintChildrenInline(c, paintable, processedElements);
		}
		// PDF/UA close open tags after inlinecontent painted
		c.getOutputDevice().closeOpenTags();
		helper.popClipRegions(c, lines.size());
	}

	private static void paintChildrenInline(RenderingContext c, InlinePaintable paintable,
			Vector<Object> processedElements) {
		if (!processedElements.contains(paintable)) {
			if (paintable instanceof InlineLayoutBox) {
				InlineLayoutBox inlineLB = (InlineLayoutBox) paintable;
				List children = inlineLB.getInlineChildren();
				for (int i = 0; i < children.size(); i++) {
					if (children.get(i) instanceof InlineLayoutBox) {
						// TODO revisar si es necesario usar esta recursividad
						// en lugar de presuponer que el hijo es un inlineText
						// paintChildrenInline(c, (InlinePaintable)
						// children.get(i), processedElements);
						// LA siguiente burrada que no es recursiva se hace para
						// mantener el order de los inline text, ya que la
						// recursividad
						// hace que el orden de pintado dependa de la
						// profuncidad que tiene el texto.
						InlineLayoutBox inlineLB1 = (InlineLayoutBox) children.get(i);
						if (!processedElements.contains(inlineLB1)) {
							processedElements.addElement(inlineLB1);
							inlineLB1.paintInline(c);
						}
					} else if (!processedElements.contains(inlineLB)) {
						processedElements.addElement(inlineLB);
						inlineLB.paintInline(c);
					}
				}
			} else if (paintable instanceof BlockBox) {
				BlockBox blockBox = (BlockBox) paintable;
				if (!processedElements.contains(blockBox)) {
					processedElements.addElement(blockBox);
					paintable.paintInline(c);
				}
				List children = blockBox.getChildren();
				for (int i = 0; i < children.size(); i++) {
					paintChildrenInline(c, (InlinePaintable) children.get(i), processedElements);
				}
			} else if (paintable instanceof LineBox) {
				LineBox lineBox = (LineBox) paintable;
				List children = lineBox.getChildren();
				for (int i = 0; i < children.size(); i++) {
					paintChildrenInline(c, (InlinePaintable) children.get(i), processedElements);
				}
			}
		}
	}
}
