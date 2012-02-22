package org.xhtmlrenderer.pdf;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.xhtmlrenderer.util.XRLog;

public class ITextCookieUserAgent extends ITextUserAgent {

	private String cookie = "";
	private String fs_baseURL = null;

	public ITextCookieUserAgent(ITextOutputDevice outputDevice, String cookies, String baseURL) {
		super(outputDevice);
		this.cookie = cookies;
		this.fs_baseURL = baseURL;
		this.setBaseURL(baseURL);
	}

    //TOdO:implement this with nio.
    protected InputStream resolveAndOpenStream(String uri) {
        java.io.InputStream is = null;
        uri = resolveURI(uri);
        try {
        	URLConnection connection = new URL(uri).openConnection();
    		connection.setRequestProperty("Cookie", this.cookie);
    		is = connection.getInputStream();
        } catch (java.net.MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (java.io.FileNotFoundException e) {
            XRLog.exception("item at URI " + uri + " not found");
        } catch (java.io.IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }
        return is;
    }

	/**
	 * Resolves the URI; if absolute, leaves as is, if relative, returns an absolute URI based on the baseUrl for
	 * the agent.
	 *
	 * @param uri A URI, possibly relative.
	 *
	 * @return A URI as String, resolved, or null if there was an exception (for example if the URI is malformed).
	 */
	public String resolveURI(String uri) {
        if (uri == null) return null;

        if(this.getBaseURL() == null && fs_baseURL != null) {
        	this.setBaseURL(fs_baseURL);
        }

        // Call Super
        return super.resolveURI(uri);
    }

}