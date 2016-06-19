package org.xhtmlrenderer.pdf;

import java.util.HashMap;
import java.util.Map;

import org.xhtmlrenderer.util.XRLog;

public class ITextFontResolverAccessible {
	
    public static Map<String, String> getFileNameAndExtension(String uri){
		String fileName = "";
		String extension = "";
		try {
			int posFileName = uri.lastIndexOf("/");
			if (posFileName != -1) {
				fileName = uri.substring(posFileName + 1, uri.length());
				if(fileName.indexOf("?") != -1){
					fileName = fileName.substring(0, fileName.indexOf("?"));
				}
				int pos = fileName.indexOf(".");
				if (pos != -1) {
					extension = fileName.substring(pos, fileName.length());
				}
			}
		} catch (Exception e) {
			XRLog.exception("Error calculating filename and extension from uri:" + uri, e);
		}
		Map<String, String> ret = new HashMap<String, String>();
		ret.put(fileName, extension);
		return ret;
	}
    
    public static String getSupportedFontUri(String oriUri, String extension){
    	String uri = oriUri;
    	if(extension.contains(".eot")){
    		uri = uri.replace(".eot", ".ttf");
    	}else if(extension.contains(".woff")){
    		uri = uri.replace(".woff", ".ttf");
    	}else if(extension.contains(".svg")){
    		uri =uri.replace(".svg", ".ttf");
    	}
		if(uri.indexOf("?") != -1){
			uri = uri.substring(0, uri.indexOf("?"));
		}
    	return uri;
    }

}
