package com.dphoto.sync;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ListAdapter;

public class OAuthTask extends AsyncTask<Void, Void, Boolean> {

	private Context context;
	protected Boolean doUserCredentials = true;
	protected InputStream bitmap;
	protected Boolean sendFile = false;

	public Map<String, Object> properties;
	public HttpResponse response;
	private SortedSet<String> propKeys;

	// Application preferences
	private SharedPreferences appPreferences;
	
	// DPHOTO API User credentials
	protected Integer authUserId;
	protected String authToken;
	private String authChecksum;
	
	// DPHOTO API Application credentials
	private String apiGateway = "https://api.dphoto.com/";
	protected String apiCall = "file/upload";
	private String appKey = "faa5307c52a04612d40bc27ea42c9a83";
	private String appSecret = "cce6c3f34af56905c6935288355164d4";
	
	public String jsonString;
	public JSONObject jsonResponse;
	
    public OAuthTask(Context mContext) {
    	
        context = mContext;

		appPreferences = context.getSharedPreferences("com.dphoto.sync_preferences", Context.MODE_PRIVATE);
		
		authUserId = appPreferences.getInt("dphoto_user_id", 0);
		authToken = appPreferences.getString("dphoto_auth_token", "");
        
        properties = new HashMap<String, Object>();
        properties.put("app_key", appKey);
        
        if (doUserCredentials) {
            properties.put("auth_token", authToken);
            properties.put("user_id", authUserId);
        }
        
    }
	
	@Override
	protected Boolean doInBackground(Void... params) {
    	
    	propKeys = new TreeSet<String>(properties.keySet());
		
		makeChecksum();
		
		try {
			
			// HTTP client
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(apiGateway + apiCall);

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			
			// Do some stuff with the file
			if (sendFile) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
//				bitmap.compress(CompressFormat.JPEG, 100, bos);
//				byte[] data = bos.toByteArray();
				byte[] b = new byte[1024];
				int bytesRead;
				while ((bytesRead = bitmap.read(b)) != -1) {
				   bos.write(b, 0, bytesRead);
				}
				byte[] bytes = bos.toByteArray();
				reqEntity.addPart("file", new ByteArrayBody(bytes, "upload.jpg"));
			}
			
			// Add other properties
	    	for (String key : propKeys) { 
	    		
	    		Object val = properties.get(key);
	    		
	    		if (val instanceof String || val instanceof Integer) {
	    			reqEntity.addPart(key, new StringBody(val.toString()));
	    		}
	    		
	    		Log.e(key,val.toString());
	    		
	    	}
    		reqEntity.addPart("checksum", new StringBody(authChecksum));

			// Do request
			postRequest.setEntity(reqEntity);
			response = httpClient.execute(postRequest);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			
			String sResponse;
			StringBuilder s = new StringBuilder();

			while ((sResponse = reader.readLine()) != null) {
				s = s.append(sResponse);
			}
			
			jsonString = s.toString();
			jsonResponse = new JSONObject(jsonString);
			
			Log.e("***","***");
			Log.e("RESPONSE",s.toString());
		} catch (Exception e) {
			// handle exception here
			Log.e("*ERROR*",e.getClass().getName());
			e.printStackTrace();
		}
		
		return true;
		
	}
    
    private Boolean makeChecksum() {
    	
    	// Build checksum
    	StringBuffer strBuf = new StringBuffer();
    	for (String key : propKeys) { 
    		
    		if (key == "file") {
    			continue;
    		}
    		
    		Object entry = properties.get(key);
    		
    		if (entry instanceof String || entry instanceof Integer) {
    			strBuf.append(key);
    			strBuf.append("=");
    			strBuf.append(entry.toString());
    		}
    		
    	}
    	
    	strBuf.append(appSecret);
    	
    	// Set checksum
    	authChecksum = md5(strBuf.toString());
    	Log.e("***",strBuf.toString());
    	Log.e("***",authChecksum);
    	
    	// Return true on success
    	return true;
    	
    }

	@Override
	protected void onPostExecute(final Boolean success) {

	}

	@Override
	protected void onCancelled() {

	}
	
	public String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i=0; i<messageDigest.length; i++) {
				
				String hex = Integer.toHexString(0xFF & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append('0');
				
				hexString.append(hex);
				
			}
			
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	
}