package org.joshy.html;

import org.joshy.html.box.*;
import org.joshy.u;
import org.w3c.dom.*;

public class AnonymousBoxLayout extends InlineLayout {
    public AnonymousBoxLayout() {
    }
    private Element parent;
    private Node text;

    // use the passed in 'text'  since that's what we are
    // really laying out instead of the 'node', which is really the
    // parent element.
    public Box createBox(Context c, Node node) {
        AnonymousBlockBox block = new AnonymousBlockBox(text,c);
        return block;
    }
    public void prepareBox(Box box, Context c) {

        box.border = new Border();
        box.padding = new Border();
        box.margin = new Border();

    }

    public Box layout(Context c, Element parent, Node text) {
        this.parent = parent;
        this.text = text;
        //Box box = new AnonymousBlockBox(text);
        Box box = super.layout(c,parent);
        //u.p("AnonymousBoxLayout.layout: returning: " + box);
        return box;
    }
    public Box layoutChildren(Context c, Box box) {
        //u.p("AnonymousBoxLayout.layoutChildren() noop" + box);
        return super.layoutChildren(c,box);
        //return box;
    }

}
