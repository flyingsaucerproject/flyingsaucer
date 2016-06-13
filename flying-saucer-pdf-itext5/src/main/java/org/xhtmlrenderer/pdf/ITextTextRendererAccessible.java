package org.xhtmlrenderer.pdf;

import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.JustificationInfo;

/**
 * Contains accessibility PDF/A generation methods.
 * Rewriten class delegates on this class 
 *
 */
public class ITextTextRendererAccessible {
	
	
	/**
	 * Overwrite ITextTextRenderer drawString method including a new InlineText input param
	 * @param outputDevice
	 * @param inlineText
	 * @param string
	 * @param x
	 * @param y
	 */
    public static void drawStringAccessible(OutputDevice outputDevice, InlineText inlineText, String string, float x, float y) {
        ((ITextOutputDevice)outputDevice).drawStringAccessible(inlineText, string, x, y, null);
    }
    
    /**
     * Overwrite ITextTextRenderer drawString method including a new InlineText input param
     * @param outputDevice
     * @param inlineText
     * @param string
     * @param x
     * @param y
     * @param info
     */
    public static void drawStringAccessible(
            OutputDevice outputDevice, InlineText inlineText, String string, float x, float y, JustificationInfo info) {
        ((ITextOutputDevice)outputDevice).drawStringAccessible(inlineText, string, x, y, info);
    }

}
