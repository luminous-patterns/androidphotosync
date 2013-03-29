/*
 * All the image files exists in phone gallery are getting uploaded to Dphoto account even 
 * if the image file with same name already exists in Dphoto gallery. There is probability 
 * that the same file will get uploaded again.
 */

package com.dphoto.sync;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class UploadActivity extends Activity {
	private final String TAG = "UploadActivity";

	Uri sourceUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	
	private SharedPreferences  	appPreferences;
	private Integer            	userId;
	private String            	username;
	private String            	password;
	private String            	appToken;
	private Editor            	editor;
	private String              files = "";

	/*receivers for results */
	private BroadcastReceiver     httpReceiver  = null;
	private IntentFilter          httpFilter    = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		
		appPreferences = getSharedPreferences("com.dphoto.sync_preferences", MODE_PRIVATE);
		//editor         = appPreferences.edit();
		
		//fetch the Dphoto credentials from SharedPreferences 
		userId         = appPreferences.getInt("dphoto_user_id",0);
		username       = appPreferences.getString("dphoto_username","");
		password       = appPreferences.getString("dphoto_password","");
		appToken 	   = appPreferences.getString("dphoto_auth_token", "");
	
		httpFilter   = new IntentFilter(PhotoSyncService.DATA);
		httpReceiver = new BroadcastReceiver(){
			public void onReceive(Context context, Intent intent) {
                gotData(intent);
            }
        };
        
        registerReceiver(httpReceiver, httpFilter);
       	
	}
		
	@Override
	public void onStart() {
		super.onStart();
		Log.v(TAG, "<<<<<<<<<<<<<< ON START >>>>>>>>>>>>>");
		try {
		
			CursorLoader cursorLoader = new CursorLoader( this, sourceUri, null, null, null, MediaStore.Images.Media.TITLE);
	        Cursor cursor             = cursorLoader.loadInBackground();     
	       	        
            Boolean loop      = true;
	        cursor.moveToFirst();
         
        	//fetch all the file paths from phone gallery and concat to a string.
	        while (loop) {
	        	String fileLocation = cursor.getString(1);
	        	files = files + fileLocation + Config.SEPERATOR;
				if (!cursor.moveToNext()) {
					loop = false;
				}
	        }
        	
        	Log.v(TAG, files);
        	startServices();
	        
		} catch (Exception e) {
			Log.e(e.getClass().getName(), e.getMessage());
		}
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		unregisterReceiver(httpReceiver);
		stopServices();
	}
	
	private void startServices(){
		Intent intent = new Intent(this,PhotoSyncService.class);
		
		/*
		 * send Dphoto credentials to PhotoSyncService
		 */
		intent.putExtra("id", userId);
		intent.putExtra("token",appToken);
		intent.putExtra("api",Config.API);
		intent.putExtra("filesList", files); //file paths of phone gallery
		startService(intent);
	}
	
	private void stopServices(){
		stopService(new Intent(this,PhotoSyncService.class));
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	public void onBackClick(View view) {
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}
	
	private void gotData(Intent intent){
		TextView result  = (TextView) findViewById(R.id.notificationArea);
		result.setText("Files have been synced successfully");
	}
	
}
