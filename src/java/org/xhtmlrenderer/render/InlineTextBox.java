package org.xhtmlrenderer.render;

import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.css.constants.IdentValue;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jan-06
 * Time: 08:46:37
 * To change this template use File | Settings | File Templates.
 */
public class InlineTextBox extends InlineBox {

    public InlineBox copy() {
        InlineTextBox newBox = new InlineTextBox();
        InlineTextBox box = this;
        newBox.x = box.x;
        newBox.y = box.y;
        newBox.width = box.width;
        newBox.height = box.height;
        //border = box.border;
        //margin = box.margin;
        //padding = box.padding;
        //color = box.color;
        newBox.element = box.element;
        newBox.master = box.master;
        //newBox.sub_block = box.sub_block;
        //font = box.font;
        //newBox.underline = box.underline;
        //newBox.overline = box.overline;
        //newBox.strikethrough = box.strikethrough;
        newBox.pseudoElement = box.pseudoElement;
        return newBox;
    }

    public boolean isEndOfParentContent() {
        return end_index == master.length();
    }

    /**
     * Gets the substring attribute of the InlineBox object
     *
     * @return The substring value
     */
    public String getSubstring() {
        // new code for whitepsace handling
        if (getMasterText() != null) {
            if (start_index == -1 || end_index == -1) {
                throw new RuntimeException("negative index in InlineBox");
                //return getMasterText();
            }
            if (end_index < start_index) {
                throw new RuntimeException("end is less than start");
                //Uu.p("warning: end is less than start: " + end_index + " < " + start_index);
                //Uu.p("master = " + getMasterText());
                //return getMasterText();
            }
            return getMasterText().substring(start_index, end_index);
        } else {
            //if (content instanceof TextContent) {
            throw new RuntimeException("No master text set!");
            //XRLog.render(Level.WARNING, "No master text set!");
            //}
            //return "";
        }

    }

    public void setSubstring(int start, int end) {
        if (end < start) {
            Uu.p("setting substring to: " + start + " " + end);
            throw new RuntimeException("set substring length too long: " + this);
        } else if (end < 0 || start < 0) {
            throw new RuntimeException("Trying to set negative index to inline box");
        }
        start_index = start;
        end_index = end;
    }

    private String master;

    public void setMasterText(String master) {
        //Uu.p("set master text to: \"" + master + "\"");
        this.master = master;
    }

    public String getMasterText() {
        return master;
    }

    public IdentValue whitespace = IdentValue.NORMAL;

    public String pseudoElement;
}
