package com.dphoto.sync;

import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.util.Log;

public class AlbumTask  extends OAuthTask {
	
	public AlbumTask(Context mContext) {
		super(mContext);
		sendFile = false;
		apiCall  = "album/getContents";
		properties.put("album_id",Config.ALBUM_ID);
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		
		try {
			
			Album album;
			if (jsonResponse.has("status") && jsonResponse.getString("status") != "error") {
				
				JSONArray results =  jsonResponse.getJSONArray("result");
			    for(int i = 0; i < results.length(); i++){
			        
			    	JSONObject c = results.getJSONObject(i);
			        String id      = c.getString("album_id");
			        String fileName   = c.getString("file_title");
			        String fileURL   = c.getString("file_url");
			        String fileHref   = c.getString("file_href");
			        String fileID     = c.getString("file_id");
			        album = new Album(id, fileName, fileURL, fileHref,fileID);
			        
			        synchronized(Globals.vctAlbum){
			        	Globals.vctAlbum.add(album);
			        }
			    }
			
			}
			else {
				Log.e(AlbumTask.class.toString(), "API Error " + jsonResponse.getString("message"));
			}
			
		}
		catch (Exception e) {
			// handle exception here
			Log.e("*ERROR*",e.getClass().getName());
			e.printStackTrace();
		}
	}

	@Override
	protected void onCancelled() {

	}

}
