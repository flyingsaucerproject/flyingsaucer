/*
 * HTMLNamespaceHandler.java
 *
 * Created on den 15 oktober 2004, 20:20
 */

package net.homelinux.tobe.browser;
import net.homelinux.tobe.XhtmlNamespaceHandler;

import org.apache.xpath.XPathAPI;

/**
 *
 * @author  Torbjörn Gannholm
 *
 * A quick and dirty proof-of-concept. There are more difficulties in HTML
 */
public class HTMLNamespaceHandler extends XhtmlNamespaceHandler {
    
    public String getElementStyling(org.w3c.dom.Element e) {
        StringBuffer sb = new StringBuffer();
        String s = e.getAttribute("style");
        if(!s.equals("")) {
            sb.append(s);
            if(!s.endsWith(";")) sb.append(";");
        }
        s = e.getAttribute("align");
        if(s.equals("left") || s.equals("right")) {
            sb.append("float: ").append(s).append(";");
        } else if(!s.equals("")) {
            sb.append("vertical-align: ").append(s).append(";");
        }
        s = e.getAttribute("bgcolor");
        if(!s.equals("")) {
            sb.append("background-color: ").append(s).append(";");
        }
        s = e.getAttribute("border");
        if(!s.equals("")) {
            sb.append("border-width: ").append(s).append(";");
        }
        /*s = e.getAttribute("height");
        if(!s.equals("")) {
            sb.append("height: ").append(s).append(";");
        }*/
        s = e.getAttribute("valign");
        if(!s.equals("")) {
            sb.append("vertical-align: ").append(s).append(";");
        }
        /*s = e.getAttribute("width");
        if(!s.equals("")) {
            sb.append("width: ").append(s).append(";");
        }*/
        //a special one
        if(e.getTagName().equals("font")) {
            s = e.getAttribute("size");
            if(!s.equals("")) {
                sb.append("font-size: ");
                if(s.startsWith("+")) {
                    sb.append("larger").append(";");
                } else if(s.startsWith("-")) {
                    sb.append("smaller").append(";");
                } else if(s.equals("1")) {
                    sb.append("xx-small").append(";");
                } else if(s.equals("2")) {//x-small has no equivalent
                    sb.append("small").append(";");
                } else if(s.equals("3")) {
                    sb.append("medium").append(";");
                } else if(s.equals("4")) {
                    sb.append("large").append(";");
                } else if(s.equals("5")) {
                    sb.append("x-large").append(";");
                } else if(s.equals("6")) {
                    sb.append("xx-large").append(";");
                } else if(s.equals("7")) {//7 has no equivalent now
                    sb.append("xx-large").append(";");
                }
            }
            s = e.getAttribute("color");
            if(!s.equals("")) {
                sb.append("color: ").append(s).append(";");
            }
            s = e.getAttribute("face");
            if(!s.equals("")) {
                sb.append("font-family: ").append(s).append(";");
            }
        }
        return sb.toString();
    }

    public String getDocumentTitle(org.w3c.dom.Document doc) {
        String title = "";
        try {
            org.apache.xpath.objects.XObject xo = XPathAPI.eval(doc.getDocumentElement(), "//head/title/text()");
            org.w3c.dom.NodeList nl = xo.nodelist();
            if ( nl.getLength() == 0 ) { 
                System.err.println("Apparently no title element for this document.");
                title = "TITLE UNKNOWN";
            } else {
                title = nl.item(0).getNodeValue();
            }
        } catch ( Exception ex ) {
            System.err.println("Error retrieving document title. " + ex.getMessage());
            title = "";
        }
        return title;
    }    
    
}
