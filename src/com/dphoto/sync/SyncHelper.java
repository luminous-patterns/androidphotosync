package com.dphoto.sync;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class SyncHelper {
	private static final String TAG = "SyncHelper";
	
	/*
	 * Function to get album id from given json string
	 */
	public static String getAlbumID(String strJson){ 
		try {
			JSONObject jsonResponse = new JSONObject(strJson);	
			if (jsonResponse.has("status") && jsonResponse.getString("status") != "error") {
					
				JSONArray results =  jsonResponse.getJSONArray("result");
				for(int i = 0; i < results.length(); i++){
				  
				    JSONObject c 	  = results.getJSONObject(i);
				    String id         = c.getString("album_id");
				    
				    /*
				     * There may be multiple album id's. But at the moment we are just considering first album id
				     */
				    return id; 
				}
			}
		}catch(Exception e){
				Log.d(TAG, "Exception in JSON" + e);
				return "";
		}
		return "";
	}
	
	/*
	 * 
	 */
	public static boolean parseAlbumJson(String strJson){ 
		/*
		 * Clear the previous elements in vector
		 */
		Globals.vctFileInfo.clear();
		
		try {
			FileInfo fileInfo;
			JSONObject jsonResponse = new JSONObject(strJson);	
			if (jsonResponse.has("status") && jsonResponse.getString("status") != "error") {
					
				JSONArray results =  jsonResponse.getJSONArray("result");
				for(int i = 0; i < results.length(); i++){
				   
				    JSONObject c 	  	= results.getJSONObject(i);
				    String fileName   	= c.getString("file_title");
				    String fileSize   	= c.getString("file_largest_size");
				    int width   		= Integer.parseInt(c.getString("file_original_width"));
				    int height   	    = Integer.parseInt(c.getString("file_original_height"));
				    
				    fileInfo = new FileInfo(fileName, fileSize, width, height);
				    Globals.vctFileInfo.add(fileInfo);
    
				}
				return true;
			}
		}catch(Exception e){
				Log.d(TAG, "Exception in JSON" + e);
				return false;
				
		}
		return false;
	}
}
