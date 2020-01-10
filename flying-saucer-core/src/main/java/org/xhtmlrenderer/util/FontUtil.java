package org.xhtmlrenderer.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.logging.Level;

public class FontUtil {

    public static Boolean isEmbeddedBase64Font(String uri) {
        return uri != null && uri.startsWith("data:font/");
    }

    public static InputStream getEmbeddedBase64Data(String uri) {
        int b64Index = (uri!= null)? uri.indexOf("base64,") : -1;
        if (b64Index != -1) {
            String b64encoded = uri.substring(b64Index + "base64,".length());
            return new ByteArrayInputStream(Base64.getDecoder().decode(b64encoded));
        } else {
            XRLog.load(Level.SEVERE, "Embedded css fonts must be encoded in base 64.");
            return null;
        }
    }
}
