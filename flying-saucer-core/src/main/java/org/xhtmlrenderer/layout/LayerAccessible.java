package org.xhtmlrenderer.layout;

import java.util.List;
import java.util.Vector;

import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.render.RenderingContext;

public class LayerAccessible {

	public static void paintInlineContentAccessible(RenderingContext c, List lines, BoxRangeLists rangeLists) {
		BoxRangeHelper helper = new BoxRangeHelper(c.getOutputDevice(), rangeLists.getInline());
		//PDF/UA: List of processed element, we need a Vector for use it by reference inside recursion methods
		Vector<Object> processedElements = new Vector<Object>();

		for (int i = 0; i < lines.size(); i++) {
			helper.popClipRegions(c, i);
			helper.pushClipRegion(c, i);

			InlinePaintable paintable = (InlinePaintable) lines.get(i);
			paintChildrenInline(c, paintable, processedElements);
		}

		helper.popClipRegions(c, lines.size());
	}

	private static void paintChildrenInline(RenderingContext c, InlinePaintable paintable,
			Vector<Object> processedElements) {
		 if (paintable instanceof BlockBox) {
			BlockBox blockBox = (BlockBox) paintable;
			List children = blockBox.getChildren();
			for (Object child : children) {
				paintChildrenInline(c, (InlinePaintable) child, processedElements);
			}
		} else if (paintable instanceof LineBox) {
			LineBox lineBox = (LineBox) paintable;
			List children = lineBox.getChildren();
			for (Object child : children) {
				paintChildrenInline(c, (InlinePaintable) child, processedElements);
			}
		} else if (paintable instanceof InlineLayoutBox) {
			InlineLayoutBox inlineLB = (InlineLayoutBox) paintable;
			List children = inlineLB.getInlineChildren();
			for (Object child : children) {
				if (child instanceof InlineLayoutBox) {
					paintChildrenInline(c, (InlinePaintable) child, processedElements);
				} else if (child instanceof InlineText) {
					if (!processedElements.contains(paintable)) {
						processedElements.addElement(inlineLB);
						paintable.paintInline(c);
					}
				}
			}
		}
	}
}
