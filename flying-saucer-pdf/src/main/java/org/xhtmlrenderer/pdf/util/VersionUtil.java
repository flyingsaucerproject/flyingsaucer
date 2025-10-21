package org.xhtmlrenderer.pdf.util;

import org.openpdf.text.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class to get OpenPDF version information.
 */
public class VersionUtil {

    private static final String VERSION_PROPERTIES = "org/openpdf/text/version.properties";

    public static String getOpenPDFVersionNumber() {
        String releaseVersion = "UNKNOWN";
        try (InputStream input = Document.class.getClassLoader()
            .getResourceAsStream(VERSION_PROPERTIES)) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                releaseVersion = prop.getProperty("bundleVersion", releaseVersion);
            }
        } catch (IOException ignored) {
            // ignore this and leave the default
        }
        return releaseVersion;
    }

}
