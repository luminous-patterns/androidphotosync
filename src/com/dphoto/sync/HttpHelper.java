package com.dphoto.sync;

import java.net.*;
import java.io.*;
import android.util.Log;

public class HttpHelper {
	/* data can be "uid=1&p=100" */
    public static String sendPostRequest(String apiUrl, String data) throws Exception{
				//Log.d("HTTP HELPER","------------- "+data+" ----------------");
        /* send the request */
        URL           url  = new URL(apiUrl);
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            
        /* write parameters */
        writer.write(data);
        writer.flush();
            
        /* get the response */
        StringBuffer    resp   = new StringBuffer();
        BufferedReader  reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        	
		String line;
        while ((line = reader.readLine()) != null) {
		    String str = new String(line.getBytes(),"UTF-8");
            //resp.append(line);
            resp.append(str);
        }
        
		writer.close();
        reader.close();
		
        return resp.toString();
    }
	
	public static String sendGetRequest(String url) throws Exception{
		URL getUrl = new URL(url);
		BufferedReader in = new BufferedReader(new InputStreamReader(getUrl.openStream()));
		
		String inputLine;
		String ret="";
		while ((inputLine = in.readLine()) != null) { ret += inputLine; }
		in.close();
		return ret;
	}
 
}/*HttpHelper*/