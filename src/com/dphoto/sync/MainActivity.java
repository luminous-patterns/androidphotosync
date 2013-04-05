package com.dphoto.sync;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Switch;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	
	private final String TAG = "MainActivity";
	Uri sourceUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	
	private OnSharedPreferenceChangeListener preferenceChangeListener;
	
	private Switch            mSyncSwitchView;
	
	private SharedPreferences appPreferences;
	private Integer           userId;
	private String            username;
	private String            password;
	private String            appToken;
	private Editor            editor;
	
	private Context           context;
	
	/*receivers for results */
	private BroadcastReceiver     httpReceiver  = null;
	private IntentFilter          httpFilter    = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		appPreferences = getSharedPreferences("com.dphoto.sync_preferences", MODE_PRIVATE);
		editor         = appPreferences.edit();
				
		userId         = appPreferences.getInt("dphoto_user_id",0);
		username       = appPreferences.getString("dphoto_username","");
		password       = appPreferences.getString("dphoto_password","");
		appToken 	   = appPreferences.getString("dphoto_auth_token", "");
		
		if (userId == 0 || userId == null || username.isEmpty()) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		}
		else {
			
			setContentView(R.layout.activity_main);
			
			mSyncSwitchView = (Switch) findViewById(R.id.switchSyncEnabled);
			mSyncSwitchView.setChecked(appPreferences.getBoolean("sync_enabled",false));
			
			preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
				public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
					mSyncSwitchView.setChecked(appPreferences.getBoolean("sync_enabled",false));
				}
			};

			appPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);			
		
		}
		
		httpFilter   = new IntentFilter(PhotoSyncService.DATA);
		httpReceiver = new BroadcastReceiver(){
			public void onReceive(Context context, Intent intent) {
                gotData(intent);
            }
        };
        
        registerReceiver(httpReceiver, httpFilter);
			
	}
	
	private void startServices(){
		
		Intent intent = new Intent(this,PhotoSyncService.class);
		
		/*
		 * send Dphoto credentials to PhotoSyncService
		 */
		intent.putExtra("id", userId);
		intent.putExtra("token",appToken);		
		intent.putExtra("galleryID", username + ".dphoto.com");
		startService(intent);
	}
	
	private void stopServices(){
		stopService(new Intent(this,PhotoSyncService.class));
	}
	
	private void gotData(Intent in){
		Log.d("MainActivity", "<<<<<<<<<<<<<<<<<<<< GOT DATA  >>>>>>>>>>>>>> ");
		stopServices();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		Log.v(TAG, "<<<<<<<<<<<<<< ON START >>>>>>>>>>>>>");
		try {
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void onSettingsClicked(View view) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	public void onUploadClicked(View view) {
		/*Intent intent = new Intent(this, UploadActivity.class);
		startActivity(intent);*/
	}
	
	public void onToggleClicked(View view) {
		
		boolean on = ((Switch) view).isChecked();
		
		if (on) {
			// enable sync
			editor.putBoolean("sync_enabled", true);
		}
		else {
			// disable sync
			editor.putBoolean("sync_enabled", false);
		}
		
		editor.commit();
		
	}

}
