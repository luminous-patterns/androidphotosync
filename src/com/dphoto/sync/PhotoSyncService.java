/*
 * PhotoSync Service Sync the Phone Gallery images to Dphoto Album
 */

package com.dphoto.sync;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

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
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

public class PhotoSyncService extends Service implements Runnable{
	
	private final String 		 TAG           = "PhotoSyncService";
	
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
	private   String  		     galleryID;
	private   String  		     albumID;
	
	private  Boolean             isFileUpload      	= false;
	private  InputStream         bm;
	private  int 				 error 		  		= 1;
	
	/*
	 * Name of file to be uploaded to Dphoto server.
	 */
	private   String             fileName;
	
	
	/*
	 * Database handle
	 */
	private DBAdapter            dbAdapter;
	
	/*
	 *path  for gallery
	 */
	private Uri sourceUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

 	@Override
	public IBinder onBind(Intent intent){
		return null;
	}
	
	@Override
	public void onCreate(){
		Log. d(TAG, "<<<<<<<<<<<<<<<<<<<<< PhotoSyncService created   >>>>>>>>>>>>>>>>>>");
		
		/*
		 * create database object 
		 */
		dbAdapter  = new DBAdapter(this);
		dbAdapter.open();
	}
	
	@Override
	public void onDestroy(){
		Log. d(TAG, "<<<<<<<<<<<<<<<<<<<<< PhotoSyncService destroyed   >>>>>>>>>>>>>>>>>>");
		stopThread();
		
		/*
		 *close the database; 
		 */
		dbAdapter.close();
	}

	@Override
	public void onStart(Intent intent, int startID) {
		Log. d(TAG, "<<<<<<<<<<<<<<<<<<<<< PhotoSyncService started   >>>>>>>>>>>>>>>>>>");
		
		/*
		 * DPhoto credentials 
		 */
		authUserId     = intent.getIntExtra("id", 0);
		authToken      = intent.getStringExtra("token");	
		galleryID      = intent.getStringExtra("galleryID");
		
        properties = new HashMap<String, Object>();
        properties.put("auth_token", authToken);
        properties.put("user_id", authUserId);  
        properties.put("app_key", Config.APP_KEY);
	    properties.put("gallery_id",galleryID);
	    
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
		
		Intent intent   = new Intent(DATA);	  		
		String json     = "";
		
		isFileUpload = false;
			
		try{	
		
			/*
			 * Send gallery/getContents() service to DPhoto server.
			 */
			
			propKeys     =   new TreeSet<String>(properties.keySet());
			makeChecksum();	
				
			json         = sendPostRequest(Config.API_URL,Config.API_GALLERY);
				
			/*
			 * Album ID used to fetch album files
			 */
			albumID   =  SyncHelper.getAlbumID(json);
				
			Log.v(TAG,"ALBUM ID :::: " + albumID);
					
			/*
			 * Sync the db
			 */
			updateSyncDB();
			
			/*
			 * fetch all the files from phone gallery and upload 
			 */
			isFileUpload = true;
			int count = 0;
			
			while(true){
				
				Log.v(TAG," +++++++++++ LOOP STARTS ++++++++++++++++++++");
				
				//if(count % Config.USD_HEARTBEAT == 0 || dbAdapter.getCount() == 0){
				if(count % Config.USD_HEARTBEAT == 0 ){
					count = 0;
					isFileUpload = false;
					updateSyncDB();
					isFileUpload = true;
				}

				String[] filePathColumn = {MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(sourceUri,filePathColumn, null, null, null);
	            cursor.moveToPosition(-1);
	            
				//fetch all the images from phone gallery and upload to DPhoto server.
				
				while (cursor.moveToNext()) {
					
					
					int columnIndex     = cursor.getColumnIndex(filePathColumn[0]);
					
					String fileLocation = cursor.getString(columnIndex);
					File file   = new File(fileLocation);
					fileName    = file.getName();
					
					String fileNameNoExt = fileName.substring(0,fileName.indexOf("."));
					
					/*
					 * if file exist in database then do not upload that file
					 */
					if(dbAdapter.ifExist(fileNameNoExt)){
						Log.v(TAG,fileNameNoExt + "  already synced");
						continue;
					}
					
					isFileUpload = true;
					Log.v(TAG," Upoading file " + fileNameNoExt);
					bm         = new FileInputStream(file);	
					json       = sendPostRequest(Config.API_URL,Config.API_UPLOAD);
					
					Log.v(TAG," JSON " + json);
					JSONObject jsonResponse = new JSONObject(json);
					
					if (jsonResponse.has("status") && jsonResponse.getString("status") != "error") {
						/*
						 * Inserting file in database after uploading it.
						 */
						Log.e(TAG,"Inserting file into database after uploading it");
						dbAdapter.addImage(fileNameNoExt, "", 0, 0);
						error = 0;
					}
					else { 
						error = 1;
						intent. putExtra(ERROR,error);
						break;
					}
					
					intent. putExtra(ERROR,error);			
				
				}
	        
				cursor.close();
				
				count++;
				Thread.sleep(Config.LOC_SLEEP);
				
			}
		}catch(Exception e){ 		
				Log.v(TAG, "Exception::" + e);
				e.printStackTrace();
				intent. putExtra(ERROR,1);	
		}
		
		sendBroadcast(intent);
		
	}
	
	private void updateSyncDB(){
		Log.v(TAG," +++++++++++ SYNC DB ++++++++++++++++++++");
		isFileUpload = false;
	
		/*
		 * Send album/getContents() service to DPhoto server.
		 */
		properties.put("album_id", albumID);
		propKeys = new TreeSet<String>(properties.keySet());
		makeChecksum();
		
		/* 
		 * Drop the table and recreate it.
		 */
		dbAdapter.dropTable();
		
		String json      = sendPostRequest(Config.API_URL,Config.API_ALBUM);
		
		/*
		 * parse json and insert image information into database
		 */
		boolean result   = parseAlbumJson(json);	
	}
	
	/*
	 * Function parses json and insert image information into database
	 */
	public boolean parseAlbumJson(String strJson){ 		
		try {
			JSONObject jsonResponse = new JSONObject(strJson);	
			if (jsonResponse.has("status") && jsonResponse.getString("status") != "error") {
					
				JSONArray results =  jsonResponse.getJSONArray("result");
				for(int i = 0; i < results.length(); i++){
				   
				    JSONObject c 	  	= results.getJSONObject(i);
				    String fileName   	= c.getString("file_title");
				    String fileSize   	= c.getString("file_largest_size");
				    int width   		= Integer.parseInt(c.getString("file_original_width"));
				    int height   	    = Integer.parseInt(c.getString("file_original_height"));
				    
				    Log.v(TAG,"FILENAME FROM JSON :::" + fileName);
				    dbAdapter.addImage(fileName, fileSize, width, height);
				}
				return true;
			}
		}catch(Exception e){
				Log.d(TAG, "Exception in JSON" + e);
				return false;
				
		}
		return false;
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
			if(isFileUpload){
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] b = new byte[1024];
				int bytesRead;
				while ((bytesRead = bm.read(b)) != -1) {
					bos.write(b, 0, bytesRead);
				}
				byte[] bytes = bos.toByteArray();
			
			
				//add file to reqentity
				reqEntity.addPart("file", new ByteArrayBody(bytes, fileName));
			}
				
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
    	//Log.e("***",strBuf.toString());
    	//Log.e("***",authChecksum);
    	
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