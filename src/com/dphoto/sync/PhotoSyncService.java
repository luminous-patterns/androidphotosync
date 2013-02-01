package com.dphoto.sync;

import android.app.IntentService;
import android.content.Intent;

public class PhotoSyncService extends IntentService {

	public PhotoSyncService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

        // Do work here, based on the contents of dataString

    }
}