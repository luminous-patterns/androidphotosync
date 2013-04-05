package com.dphoto.sync;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME 		= "dphoto.db";
	private static final int    DATABASE_VERSION 	=  2;
	private static final String TAG 				= "DBHelper";

	private static final String TBL_CREATE = "CREATE TABLE images_info (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, size TEXT NOT NULL, width INTEGER NOT NULL, height INTEGER NOT NULL);";
	
	DBHelper(Context context){
		super(context,DATABASE_NAME,null,DATABASE_VERSION);
		Log.v(TAG, "creating Db_______");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.v(TAG, " table created....");
		db.execSQL(TBL_CREATE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.w(DBHelper.class.getName(), "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS images_info");
		onCreate(db);
	}
}
