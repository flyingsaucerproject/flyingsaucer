package org.xhtmlrenderer.util;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.logging.Level;

@ParametersAreNonnullByDefault
public class FontUtil {

    @CheckReturnValue
    public static boolean isEmbeddedBase64Font(@Nullable String uri) {
        return uri != null && uri.startsWith("data:font/");
    }

    @Nullable
    @CheckReturnValue
    public static InputStream getEmbeddedBase64Data(@Nullable String uri) {
        int b64Index = (uri!= null)? uri.indexOf("base64,") : -1;
        if (b64Index != -1) {
            String b64encoded = uri.substring(b64Index + "base64,".length());
            return new ByteArrayInputStream( Base64.getDecoder().decode( b64encoded));
        } else {
            XRLog.load(Level.SEVERE, "Embedded css fonts must be encoded in base 64.");
            return null;
        }
    }
}
