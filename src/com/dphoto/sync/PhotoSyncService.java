/*
 * PhotoSync Service Sync the Phone Gallery images to Dphoto Album
 */

package com.dphoto.sync;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dphoto.sync.SyncIntentService.UploadTask;

import android.app.IntentService;
import android.app.Service;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

public class PhotoSyncService extends Service implements Runnable{
	
	private final String 		 TAG    = "PhotoSyncService";
	
	private Thread               thread;
	private boolean              running;
	
	public static final String   DATA           = "package com.dphoto.sync.PhotoSyncService.action.DATA";
    public static final String   ERROR          = "package com.dphoto.sync.PhotoSyncService.error";
    
    public   Map<String, Object> properties;
	public   HttpResponse        response;
	private  SortedSet<String>   propKeys;

	// DPHOTO API User credentials
	protected Integer 		     authUserId;
	protected String  		     authToken;
	private   String  		     authChecksum;
	protected Boolean     	     doUserCredentials = true;
	private   String  		     API;
	private   String             fileLocation;
	
	private   InputStream        bm;
	private   String             fileName;
	private   String             files;
	
	private   int 				 error 		  = 1;

 	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
	
	@Override
	public void onCreate(){
		Log. d(TAG, "<<<<<<<<<<<<<<<<<<<<< PhotoSyncService created   >>>>>>>>>>>>>>>>>>");
	}
	
	@Override
	public void onDestroy(){
		Log. d(TAG, "<<<<<<<<<<<<<<<<<<<<< PhotoSyncService destroyed   >>>>>>>>>>>>>>>>>>");
		stopThread();
	}

	@Override
	public void onStart(Intent intent, int startID) {
		Log. d(TAG, "<<<<<<<<<<<<<<<<<<<<< PhotoSyncService started   >>>>>>>>>>>>>>>>>>");

		/*
		 * DPhoto credentials 
		 */
		
		authUserId     = intent.getIntExtra("id", 0);
		authToken      = intent.getStringExtra("token");
		API            = intent.getStringExtra("api");	
	
		
		/*
		 * image file paths of phone gallery
		 */
		
		files          = intent.getStringExtra("filesList");
			
		Log.v(TAG, "Photo service:: FILES=" + files);
		
        properties = new HashMap<String, Object>();
        properties.put("app_key", Config.APP_KEY);
        properties.put("album_id", Config.ALBUM_ID);
              
        
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
			
			StringTokenizer st = new StringTokenizer(files, Config.SEPERATOR);		
			
			while (st.hasMoreElements()) {
					
				String location  = st.nextElement().toString();
				fileLocation =	location;
				
				Log.v("TAG", "FILE LOCATION " + fileLocation);
				File file   = new File(fileLocation);
				
				fileName    = file.getName();
				bm          = new FileInputStream(file);	
			
				String json  = sendPostRequest(Config.API_URL,API);
				Log.v(TAG," JSON " + json);
				JSONObject jsonResponse = new JSONObject(json);
				
				if (jsonResponse.has("status") && jsonResponse.getString("status") != "error") {
						error = 0;
				}else { error = 1; }
				
				intent. putExtra(ERROR,error);
				Thread. sleep(Config.LOC_SLEEP);
	
			}
		
		}
		catch(Exception e){ 		
			
			Log.v("FetchService", "Exception::" + e);
			e.printStackTrace();
			intent. putExtra(ERROR,1);	
		}
		
		sendBroadcast(intent);
		
	}
	/*
	 * sendPostRequest() sends request to DPoto server to upload the file.
	 */
	private String sendPostRequest(String url, String api ){
		
		try{
		
			HttpClient httpClient     = new DefaultHttpClient();
			HttpPost postRequest      = new HttpPost(url + api);
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			
			//to upload the file
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int bytesRead;
			while ((bytesRead = bm.read(b)) != -1) {
			   bos.write(b, 0, bytesRead);
			}
			byte[] bytes = bos.toByteArray();
			
			
			//add file to reqentity
			reqEntity.addPart("file", new ByteArrayBody(bytes, fileName));
			
			// Add other properties to reqentity
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
	
			Log.d("RESPONSE",jsonString);
		
			return jsonString;
		
		} catch (Exception e) {
			// handle exception here
			Log.e("*ERROR*",e.getClass().getName());
			e.printStackTrace();
		}
		return "";
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
}//PhotoSyncService