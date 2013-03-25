package com.dphoto.sync;

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

	private OnSharedPreferenceChangeListener preferenceChangeListener;
	private Editor            editor;
	private Switch            mSyncSwitchView;
	private SharedPreferences appPreferences;
	private Integer           userId;
	private String            username;
	private String            password;
	private String            appToken;
	private Context           context;
	
	/*receivers for results */
	private BroadcastReceiver     httpReceiver  = null;
	private IntentFilter          httpFilter    = null;
	
	
//	private MediaStore        files;
	
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

			/*startActivity(new Intent(this,UploadActivity.class));
			files = MediaStore.Files.getContentUri(volumeName);*/
			
			
		}
		
		httpFilter   = new IntentFilter(FetchService.DATA);
		httpReceiver = new BroadcastReceiver(){
			public void onReceive(Context context, Intent intent) {
                gotData(intent);
            }
        };
			
	}
	
	private void startServices(){
		Intent intent = new Intent(this,FetchService.class);
		intent.putExtra("id", userId);
		intent.putExtra("token",appToken);
		intent.putExtra("api",Config.API);
		startService(intent);
	}
	
	private void stopServices(){
	
		stopService(new Intent(this,FetchService.class));
	}
	
	private void gotData(Intent in){
		Log.d("MainActivity", "<<<<<<<<<<<<<<<<<<<< GOT DATA  >>>>>>>>>>>>>> ");
		int error   = in.getIntExtra(FetchService.ERROR, 1);
		Log.d("MainActivity", "<<<<<<<<<<<<<<<<<<<< ERROR >>>>>>>>>>>>>> " +  error);
		if(error == 0 )  {	
			Utils.showToastNotification(this, "Downloaded Successfully");
			stopServices();
		}
		else{	
			Utils.showToastNotification(this, "Some Internet Problem"); 
		}	
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		startServices();
		registerReceiver(httpReceiver, httpFilter);
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
		Intent intent = new Intent(this, UploadActivity.class);
		startActivity(intent);
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
