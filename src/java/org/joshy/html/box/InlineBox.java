package org.joshy.html.box;



import org.w3c.dom.Node;

import java.awt.Font;



public class InlineBox extends Box{

    // if we are an inline block, then this is

    // the reference to the real block inside

    public BlockBox sub_block = null;

    public boolean replaced = false;

    

    // line breaking stuff

    public boolean break_after = false;

    public boolean break_before = false;

    

    // decoration stuff

    public boolean underline = false;

    public boolean strikethrough = false;

    public boolean overline = false;

    

    // vertical alignment stuff

    public int baseline;

    public int lineheight;

    public boolean vset = false;

    public boolean top_align = false;

    public boolean bottom_align = false;

    public boolean is_break = false;

    

    // text stuff

    public int start_index = -1;

    public int end_index = -1;

    public String text;

    

    public String getSubstring() {

        String txt = text.substring(start_index,end_index);

        return txt;

    }

    

    private Font font;

    public void setFont(Font font) {

        this.font = font;

    }

    public Font getFont() {

        return font;

    }



    public String getText() {

        return this.text;

    }

    public void setText(String text) {

        this.text = text;

    }

    

    public String toString() {

        return "InlineBox text = \"" + getText() +

            "\" bnds = " + x + "," + y + " - " + width + "x"+height +

            " start = " + this.start_index + " end = " + this.end_index +

            " baseline = " + this.baseline + " vset = " + this.vset +
            
             // CLN: (PWW 13/08/04)
             " color: " + color + " background-color: " + background_color + 
             " font: " + font;

    }

}



