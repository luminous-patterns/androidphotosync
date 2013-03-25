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
/*
*
*/
public class FetchService extends Service implements Runnable{

	private static final       String   TAG = "FetchService";
	private Thread             thread;
	private boolean            running;
	
	public static final String DATA   = "package com.dphoto.sync.FetchService.action.DATA";
    public static final String ERROR  = "package com.dphoto.sync.FetchService.error";
    
    public   Map<String, Object> properties;
	public   HttpResponse        response;
	private  SortedSet<String>   propKeys;

	private Bitmap               bmpFile;
	// DPHOTO API User credentials
	protected Integer 		  authUserId;
	protected String  		  authToken;
	private   String  		  authChecksum;
	protected Boolean     	  doUserCredentials = true;
	private   String  		  API;

	


 	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
	
	@Override
	public void onCreate(){
		Log. d(TAG, "<<<<<<<<<<<<<<<<<<<<< FetchService created   >>>>>>>>>>>>>>>>>>");
	}
	
	@Override
	public void onDestroy(){
		Log. d(TAG, "<<<<<<<<<<<<<<<<<<<<< FetchService destroyed   >>>>>>>>>>>>>>>>>>");
		stopThread();
	}
	
	@Override
	public void onStart(Intent intent, int startid){
		Log. d(TAG, "<<<<<<<<<<<<<<<<<<<<< FetchService started   >>>>>>>>>>>>>>>>>>");

		authUserId = intent.getIntExtra("id", 0);
		authToken  = intent.getStringExtra("token");
		API        = intent.getStringExtra("api");
        
        properties = new HashMap<String, Object>();
        properties.put("app_key", Config.APP_KEY);
        properties.put("album_id",Config.ALBUM_ID);
        properties.put("file_id",Config.FILE_ID);
        properties.put("tag",    Config.FILE_TAG);
     
        
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
	
	private int getJson(String strJson){ 
		try {
			JSONObject jsonResponse = new JSONObject(strJson);
			Album album;
			if (jsonResponse.has("status") && jsonResponse.getString("status") != "error") {
					
				JSONArray results =  jsonResponse.getJSONArray("result");
				for(int i = 0; i < results.length(); i++){
				        
				    JSONObject c = results.getJSONObject(i);
				    String id         = c.getString("album_id");
				    String fileName   = c.getString("file_title");
				    String fileURL    = c.getString("file_url");
				    String fileHref   = c.getString("file_href");
				    String fileID     = c.getString("file_id");
				    
				    Log.e(TAG," <<<<<<< FILE_URL >>>>>>>>>> "  + fileURL);
				    Log.e(TAG," <<<<<<< FILE_HREF >>>>>>>>>> " + fileHref);
				    Log.e(TAG," <<<<<<< FILE_ID >>>>>>>>>> " + fileID);
				    
				    /*To download image from server*/
					/*bmpFile  = NetUtils.downloadImage(fileURL);    
					savePhoto(bmpFile, fileName);
					Log.e(TAG," <<<<<<< AFTER DOWNLOAD BITMAP >>>>>>>>>> ");*/
					
				    album = new Album(id, fileName, fileURL, fileHref, fileID);
				    Globals.vctAlbum.add(album);
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
	
	public void savePhoto(Bitmap bmp, String fileName)
	{
		String root = Environment.getExternalStorageDirectory().toString();
	    File myDir  = new File(root + "/saved_images");    
	    myDir.mkdirs();
	    Random generator = new Random();
	    
	    int n = 10000;
	    n = generator.nextInt(n);
	    String fname = "Image-"+ n +".jpg";
	    File file = new File (myDir, fname);
	    if (file.exists ()) file.delete (); 
	    
	    
	    try {
	           FileOutputStream out = new FileOutputStream(file);
	           bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
	           out.flush();
	           out.close();

	    } catch (Exception e) {
	    	Log.e(TAG,"EXception in Saving Photo");
	           e.printStackTrace();
	    }
	    
	}


	public String fromInt(int val)
	{
		return String.valueOf(val);
	}
		
}//FetchService