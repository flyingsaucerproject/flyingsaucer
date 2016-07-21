package eeze;

import junit.framework.TestCase;

import org.xhtmlrenderer.swing.NaiveUserAgent;

public class TestNaiveUserAgentUrlResolver extends TestCase
{
    protected String resolve(String uri)
    {
        return resolve(null,uri);
    }
    protected String resolve(String baseUri, String uri)
    {
        NaiveUserAgent userAgent=new NaiveUserAgent();
        userAgent.setBaseURL(baseUri);
        return userAgent.resolveURI(uri);
    }
    
    public void testBasicResolve()
    {
        // absolut uris shold be unchanged
        assertEquals("http://www.example.com", resolve("http://www.example.com"));
        assertEquals("http://www.example.com", resolve("ftp://www.example.com/other","http://www.example.com"));
        
        // by default relative uris resolves as file 
        assertNotNull(resolve("www.example.com"));
        assertTrue(resolve("www.example.com").startsWith("file:"));
        
        // relative uris without slash
        assertEquals("ftp://www.example.com/test", resolve("ftp://www.example.com/other","test"));
        
        // relative uris with slash
        assertEquals("ftp://www.example.com/other/test", resolve("ftp://www.example.com/other/","test"));
        assertEquals("ftp://www.example.com/test", resolve("ftp://www.example.com/other/","/test"));
    }
    
    public void testCustomProtocolResolve()
    {
        // absolut uris shold be unchanged
        assertEquals("custom://www.example.com", resolve("custom://www.example.com"));
        assertEquals("custom://www.example.com", resolve("ftp://www.example.com/other","custom://www.example.com"));
        
        // relative uris without slash
        assertEquals("custom://www.example.com/test", resolve("custom://www.example.com/other","test"));
        
        // relative uris with slash
        assertEquals("custom://www.example.com/other/test", resolve("custom://www.example.com/other/","test"));
        assertEquals("custom://www.example.com/test", resolve("custom://www.example.com/other/","/test"));
    }
    

}
