package com.dphoto.sync;

import java.util.Map;
import java.util.Set;

import com.dphoto.sync.LoginActivity.UserLoginTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.TextView;

public class UploadActivity extends Activity {

	//private Context context;
	
	private ResponseReceiver receiver;
	private SharedPreferences appPreferences;
	
	Uri sourceUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		
		appPreferences = getSharedPreferences("com.dphoto.sync_preferences", MODE_PRIVATE);
       		
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		try {
			
	        //String[] from = {MediaStore.MediaColumns.TITLE};
	        //int[] to      = {android.R.id.text1};
			//ListAdapter adapter       = new SimpleCursorAdapter( this, android.R.layout.simple_list_item_1, cursor, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
	        
			
			CursorLoader cursorLoader = new CursorLoader( this, sourceUri, null, null, null, MediaStore.Images.Media.TITLE);
	        Cursor cursor             = cursorLoader.loadInBackground();     
	        
	        String lastFileLocation   = appPreferences.getString("last_file_location","");
	        
            Boolean loop      = true;
            //Boolean sendFile  = true;
	        cursor.moveToFirst();
        	   
	        while (loop) {
	        	
	        	String fileLocation = cursor.getString(1);
	        		        	
	        	if (!lastFileLocation.isEmpty()) {
		        	
	        		if (fileLocation == lastFileLocation) {
		        		//sendFile  = false;
						loop      = false;
		        		continue;
		        	}
	        	}
	        	
	        	//Log.d("sync", "Looping: " + cursor.getString(1));
	    		Intent syncIntent = new Intent(this, SyncIntentService.class);
	    		syncIntent.putExtra("PARAM_FILE_LOCATION", fileLocation);
	    		
	    		startService(syncIntent);
	    		
				if (!cursor.moveToNext()) {
					loop = false;
				}

	        }
	        
		} catch (Exception e) {
			Log.e(e.getClass().getName(), e.getMessage());
		}
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	
	public void onBackClick(View view) {
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}
	
	public class ResponseReceiver extends BroadcastReceiver {
	   public static final String ACTION_RESP =
	      "com.dphoto.intent.action.MESSAGE_PROCESSED";
	   @Override
	    public void onReceive(Context context, Intent intent) {
	       TextView result  = (TextView) findViewById(R.id.notificationArea);
	       String text      = intent.getStringExtra(SyncIntentService.PARAM_OUT_MSG);
	       result.setText(text);
	    }
	}

}
