package com.dphoto.sync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Service;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.content.Intent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONArray;
import android.graphics.Bitmap;

/***********************************************************************************
*FetchAlbum service send request to Dphoto server to get gallery contents.
*From the Json response we fetch the album id and in future we can also fetch 
*other album information such as file_title, file_url _file_id etc.     
************************************************************************************/
public class FetchAlbum extends Service implements Runnable{

	private static final         String   TAG = "FetchAlbum";
	private Thread               thread;
	private boolean              running;
	
	public static final String   DATA   = "package com.dphoto.sync.FetchAlbum.action.DATA";
    public static final String   ERROR  = "package com.dphoto.sync.FetchAlbum.error";
    
    public   Map<String, Object> properties;
	public   HttpResponse        response;
	private  SortedSet<String>   propKeys;

	private Bitmap               bmpFile;
	
	// DPHOTO API User credentials
	protected Integer 		     authUserId;
	protected String  		     authToken;
	private   String  		     authChecksum;
	protected Boolean     	     doUserCredentials = true;
	private   String  		     API;
	private   String  		     galleryID;

 	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
	
	@Override
	public void onCreate(){
		Log. d(TAG, "<<<<<<<<<<<<<<<<<<<<< FetchAlbum created   >>>>>>>>>>>>>>>>>>");
	}
	
	@Override
	public void onDestroy(){
		Log. d(TAG, "<<<<<<<<<<<<<<<<<<<<< FetchAlbum destroyed   >>>>>>>>>>>>>>>>>>");
		stopThread();
	}
	
	@Override
	public void onStart(Intent intent, int startid){
		Log. d(TAG, "<<<<<<<<<<<<<<<<<<<<< FetchAlbum started   >>>>>>>>>>>>>>>>>>");

		authUserId = intent.getIntExtra("id", 0);
		authToken  = intent.getStringExtra("token");
		API        = intent.getStringExtra("api");
		galleryID  = intent.getStringExtra("galleryID");
        properties = new HashMap<String, Object>();
        properties.put("app_key", Config.APP_KEY);
        properties.put("gallery_id",galleryID);
       
        if (doUserCredentials) {
            properties.put("auth_token", authToken);
            properties.put("user_id", authUserId);
        }
        
		startThread();
	}
	
	public void setRunning(boolean running) {
        this.running = running;
    }
	
	private void startThread(){
		thread = new Thread(this);
		setRunning(true);
		thread.start();
	}
	
	private void stopThread(){
		setRunning(false);
	}
	
	public void run(){	
		propKeys = new TreeSet<String>(properties.keySet());
		makeChecksum();
		
		Intent intent   = new Intent(DATA);	  		
		
		try{
			
			String json  = sendPostRequest(Config.API_URL,API);
			int error    = getJson(json);
			intent. putExtra(ERROR,error);	
			Thread. sleep(Config.LOC_SLEEP);
		
		}
		catch(Exception e){ 		
			
			Log.v("FetchService", "Exception::" + e);
			e.printStackTrace();
			intent. putExtra(ERROR,1);	
		}
		
		sendBroadcast(intent);
		
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
    	
    	strBuf.append(Config.APP_SECRET);
    	
    	// Set checksum
    	authChecksum = md5(strBuf.toString());
    	Log.e("***",strBuf.toString());
    	Log.e("***",authChecksum);
    	
    	// Return true on success
    	return true;
    	
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
	
	private String sendPostRequest(String url, String api ){
		try{
		
			HttpClient httpClient     = new DefaultHttpClient();
			HttpPost postRequest      = new HttpPost(url + api);
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
			// Add other properties
			for (String key : propKeys) { 
				
				Object val = properties.get(key);
				if (val instanceof String || val instanceof Integer) {
					reqEntity.addPart(key, new StringBody(val.toString()));
				}
			}
			
			reqEntity.addPart("checksum", new StringBody(authChecksum));

			// Do request
			postRequest.setEntity(reqEntity);
			response              = httpClient.execute(postRequest);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		
			String sResponse;
			StringBuilder s = new StringBuilder();

			while ((sResponse = reader.readLine()) != null) {
				s = s.append(sResponse);
			}
		
			String jsonString = s.toString();
		
			//Log.d("***","***");
			Log.d("RESPONSE",jsonString);
		
			return jsonString;
		
		} catch (Exception e) {
			// handle exception here
			Log.e("*ERROR*",e.getClass().getName());
			e.printStackTrace();
		}
		return "";
	}
	
	/*
	 * The file_title that we get from Json is simple file name without its extension information.
	 */
	
	private int getJson(String strJson){ 
		try {
			JSONObject jsonResponse = new JSONObject(strJson);
			Album album;
			if (jsonResponse.has("status") && jsonResponse.getString("status") != "error") {
					
				JSONArray results =  jsonResponse.getJSONArray("result");
				for(int i = 0; i < results.length(); i++){
				        
				    JSONObject c = results.getJSONObject(i);
				    String id         = c.getString("album_id");
				    //String fileName   = c.getString("file_title"); 
				    //album             = new Album(id, fileName);
				   
				    Globals.vctAlbumID.add(id);
				}
			}
			else {
					return 1;
			}
		}catch(Exception e){
				Log.d(TAG, "Exception in JSON" + e);
				return 1;
		}
		return 0;
	}

	public String fromInt(int val)
	{
		return String.valueOf(val);
	}
		
}//FetchAlbum