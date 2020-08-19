package org.xhtmlrenderer.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * This class wraps an input stream and detects if it contains certain content using "magic numbers".
 * 
 * http://en.wikipedia.org/wiki/Magic_number_(programming)
 * 
 * currently only pdf detection is implemented
 * 
 * @author mwyraz
 */
public class ContentTypeDetectingInputStreamWrapper extends BufferedInputStream
{
    protected static final int MAX_MAGIC_BYTES=4;
    protected final byte[] MAGIC_BYTES;
    
    public ContentTypeDetectingInputStreamWrapper(InputStream source) throws IOException
    {
        super(source);
        byte[] MAGIC_BYTES=new byte[MAX_MAGIC_BYTES];
        mark(MAX_MAGIC_BYTES);
        
        try
        {
            int bytesRead=read(MAGIC_BYTES);
            if (bytesRead<MAX_MAGIC_BYTES) // Not enough data in stream
            {
                if (bytesRead<=0) MAGIC_BYTES=new byte[0]; // no data
                else MAGIC_BYTES=Arrays.copyOf(MAGIC_BYTES, bytesRead); // fewer bytes
            }
            this.MAGIC_BYTES=MAGIC_BYTES;
        }
        finally
        {
            reset();
        }
    }
    
    protected boolean streamStartsWithMagicBytes(byte[] bytes)
    {
        if (MAGIC_BYTES.length<bytes.length) return false;
        for (int i=0;i<bytes.length;i++)
        {
            if (MAGIC_BYTES[i]!=bytes[i]) return false;
        }
        return true;
    }
    
    protected final static byte[] MAGIC_BYTES_PDF="%PDF".getBytes();
    public boolean isPdf()
    {
        return streamStartsWithMagicBytes(MAGIC_BYTES_PDF);
    }
    
}
