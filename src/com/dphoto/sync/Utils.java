package com.dphoto.sync;


import android.util.Log;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/*******************************************************************************
 * Generic convenience functions for the application.              *
 ******************************************************************************/
public class Utils {
    private static final String TAG = "Utils::";

    /*
     * convenience function to show popup msg
     */
    public static void showToastNotification(Activity activity, CharSequence msg) {
        Context      context   = activity.getApplicationContext();
        int          duration  = Toast.LENGTH_SHORT;
        Toast        toast     = Toast.makeText(context, msg, duration);
        toast.show();
    }
	
	/*
	*Function call() will make call to phone.
	*/
	public static void call(Context context, String tel){
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:" + tel ));
		context.startActivity(callIntent);
	}
	
	/**
     * function will check if the phone is connected to internet
     **/
	 
	public static boolean isInternetConnected(Context context){
		try{
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			return activeNetworkInfo != null;

		}catch(Exception e){	Log.d("NetUtils", "##################################   Exception occured");		}
		return false;
	}
	
}/*Utils*/
