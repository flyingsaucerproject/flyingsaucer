package org.joshy.html.box;

import org.w3c.dom.Node;
//import java.util.ArrayList;
//import java.util.List;

public class BlockBox extends Box {
    public boolean auto_width = true;
    public boolean auto_height = true;
    //public boolean inline = false;
    
    public boolean display_block = true;
    public boolean display_inline_block = false;
    /*
    public boolean isInline() {
        return inline;
    }
    public boolean isBlock() {
        return !inline;
    }
    */
    
    public BlockBox() {
        super();
    }
    public BlockBox(int x, int y, int w, int h) {
        super(x,y,w,h);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("BlockBox:");
        sb.append(super.toString());
        if(this.fixed) {
            sb.append(" position: fixed");
        }
        if(this.right_set) {
            sb.append(" right = " + this.right);
        }
        //+ " right = " + this.right;
        // + " width = " + auto_width + " height = " + auto_height;
        return sb.toString();
    }

}
