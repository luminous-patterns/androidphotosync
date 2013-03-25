package com.dphoto.sync;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class PhotoSyncService extends IntentService {
	
	private final String TAG = "PhotoSyncService";
	AlbumTask    albumTask = null;
		
	public PhotoSyncService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();
        Album album;
        try{
        	albumTask = new AlbumTask(this);
        	albumTask.execute((Void) null);
        	synchronized(Globals.vctAlbum){
        	
        		for(int i = 0 ; i <Globals.vctAlbum.size(); i++) {
        			album = Globals.vctAlbum.get(i);
        			Log.v(TAG, "Album HREF " + album.getFileHref());
        		}
        	}
        	
        	
        }catch (Exception e) {
        	e.printStackTrace();
        }	
   
    }
}