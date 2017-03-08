package org.xhtmlrenderer.swing;

import junit.framework.TestCase;

public class NaiveUserAgentTest
        extends TestCase
{
    private static String resolve(String baseUri, String uri)
    {
        NaiveUserAgent userAgent=new NaiveUserAgent();
        userAgent.setBaseURL(baseUri);
        return userAgent.resolveURI(uri);
    }

    public void testBasicResolve()
    {
        // absolute uris should be unchanged
        assertEquals("http://www.example.com", resolve(null, "http://www.example.com"));
        assertEquals("http://www.example.com", resolve("ftp://www.example.com/other","http://www.example.com"));

        // by default relative uris resolves as file
        assertNotNull(resolve(null, "www.example.com"));
        assertTrue(resolve(null, "www.example.com").startsWith("file:"));

        // relative uris without slash
        assertEquals("ftp://www.example.com/test", resolve("ftp://www.example.com/other","test"));

        // relative uris with slash
        assertEquals("ftp://www.example.com/other/test", resolve("ftp://www.example.com/other/","test"));
        assertEquals("ftp://www.example.com/test", resolve("ftp://www.example.com/other/","/test"));
    }

    public void testCustomProtocolResolve()
    {
        // absolute uris should be unchanged
        assertEquals("custom://www.example.com", resolve(null, "custom://www.example.com"));
        assertEquals("custom://www.example.com", resolve("ftp://www.example.com/other","custom://www.example.com"));

        // relative uris without slash
        assertEquals("custom://www.example.com/test", resolve("custom://www.example.com/other","test"));

        // relative uris with slash
        assertEquals("custom://www.example.com/other/test", resolve("custom://www.example.com/other/","test"));
        assertEquals("custom://www.example.com/test", resolve("custom://www.example.com/other/","/test"));
    }


}
