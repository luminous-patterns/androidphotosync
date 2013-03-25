package com.dphoto.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.json.JSONArray;

import com.dphoto.sync.LoginActivity.UserLoginTask;
import com.dphoto.sync.UploadActivity.ResponseReceiver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;

public class SyncIntentService extends IntentService {
	
	InputStream     bm;
	String[]        projection;
	TextView        countView;
	String          fileName;
	String          fileLocation;
	
	private Context      context;
	private final String TAG      = "SyncIntentService";
	
	UploadTask     mUploadTask = null;
	Uri            sourceUri   = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";	
    
    private Editor            editor;
	private SharedPreferences appPreferences;
    
    public SyncIntentService() {
        super("SyncIntentService");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
		
    	appPreferences = getSharedPreferences("com.dphoto.sync_preferences", MODE_PRIVATE);
		editor         = appPreferences.edit();
    	fileLocation   = intent.getStringExtra("PARAM_FILE_LOCATION");
    
    	Log.d("sync", "run instance:" + fileLocation);
    	
       /*String msg = intent.getStringExtra(PARAM_IN_MSG);
        SystemClock.sleep(5000); // 30 seconds
        String resultTxt = msg + " "
        + DateFormat.format("MM/dd/yy h:mmaa", System.currentTimeMillis());*/
        
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_MSG, fileLocation);
        sendBroadcast(broadcastIntent);
        
		try {
			
			File file   = new File(fileLocation);
			fileName    = file.getName();
			bm          = new FileInputStream(file);
		
			mUploadTask = new UploadTask(this);
			mUploadTask.execute((Void) null);
			mUploadTask.get();

			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
//		mUploadTask.doInBackground((Void) null);
        
    }
    
	public class UploadTask extends OAuthTask {

		public UploadTask(Context mContext) {
			
			super(mContext);
	    	Log.d(TAG, "sending file...");
			sendFile    = true;
			bitmap      = bm;
			OA_fileName = fileName;
			properties.put("album_id","zgmwnm");
			
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			
			Log.d(TAG,"Saving last file location: " + fileLocation);
			editor.putString("last_file_location", fileLocation);
			editor.commit();

		}

		@Override
		protected void onCancelled() {

		}
		
	}
    
}