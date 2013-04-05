package com.dphoto.sync;

import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBAdapter {

	/*fields of locations table*/
	public 	static final String KEY_ID     = "id";
	public 	static final String KEY_NAME   = "name";
	public 	static final String KEY_SIZE   = "size";
	public 	static final String KEY_WIDTH  = "width";
	public 	static final String KEY_HEIGHT = "height";
	private static final String IMAGE_TBL  = "images_info";
	private static final String TAG = null;

	private Context        context;
	private SQLiteDatabase database;
	private DBHelper       dbHelper;


	public DBAdapter(Context context) {
            this.context = context;
	}

    /*open db*/
	public DBAdapter open() throws SQLException {
            dbHelper = new DBHelper(context);
            database = dbHelper.getWritableDatabase();
            return this;
	}
    
	/*close db*/
	public void close() {
        dbHelper.close();
	}
	
	public void dropTable(){
		database.execSQL("DROP TABLE IF EXISTS " + IMAGE_TBL);
		dbHelper.onCreate(database);
	}
	
	public boolean ifExist(String name){
		String query = "select * from " + IMAGE_TBL  +  " where name = '" + name + "'" ;
		int count    = getFileInfoCount(database.rawQuery(query, null));
        if(count > 0) {
        	Log.v(TAG," #########  File Already exists in Database ###### ");
        	return true;
        }
		return false;
	}
	
    /* inserts a new records in db and returns id of new record*/
	public long addImage(String name, String size, int width, int height) {
            ContentValues initialValues = createContentValues(name,size,width,height);
            
            String query = "select * from " + IMAGE_TBL  +  " where name = '" + name + "' AND size = '" + size + "' AND width = " + width + " AND height = " + height ;
	        int count    = getFileInfoCount(database.rawQuery(query, null));
	        
	        if(count > 0) {
	        	Log.v(TAG," #########  File Already exists in Database ###### ");
	        	return 0;
	        }
	        else{
	        	Log.v(TAG," #########  File inserted in Database ######## ");
	        	return database.insert(IMAGE_TBL, null, initialValues);
	        }
	}
	
	public int getCount(){
		//Cursor cur = database.query(IMAGE_TBL, new String[] { KEY_ID, KEY_NAME, KEY_SIZE, KEY_WIDTH, KEY_HEIGHT }, null, null, null, null, null);
		String query = "select * from " + IMAGE_TBL;
        return getFileInfoCount(database.rawQuery(query, null));
		
	}
	
	private int getFileInfoCount(Cursor cur) {
		 int count = 0;	 
		 while(cur.moveToNext()) {
			count++;
		 }		      
		 return count;
	}
	
    /* updates a record in db, and return true/false */
	public boolean updateLoc(long rowId, String name, String size, int width, int height) {
            ContentValues updateValues =  createContentValues(name,size,width,height);
            return database.update(IMAGE_TBL, updateValues, KEY_ID + "=" + rowId, null) > 0;
	}

	
    /* deletes a record in db, and return true/false */
	public boolean deleteLoc(long rowId) {
            return database.delete(IMAGE_TBL, KEY_ID + "=" + rowId, null) > 0;
	}

	
   /* del all locs */
   public void deleteAllLocs() {
	   database.delete(IMAGE_TBL, null, null);
   }

   /*return all locations info*/
	public Vector<FileInfo> getAllImages() {
		Cursor cur = database.query(IMAGE_TBL, new String[] { KEY_ID, KEY_NAME, KEY_SIZE, KEY_WIDTH, KEY_HEIGHT }, null, null, null, null, null);
		Vector<FileInfo> vctFileInfo = new Vector<FileInfo>();	 
		while(cur.moveToNext()) {	
			    String  fileName            = cur.getString(1);
				String  fileSize            = cur.getString(2);
				int     width 		        = cur.getInt(3);
				int     height 		        = cur.getInt(4);

				FileInfo voFileInfo = new FileInfo(fileName, fileSize, width, height);
				vctFileInfo.addElement(voFileInfo);
		  }		      
		 return vctFileInfo;

	}

	
    /*return location by id */
	public Cursor getImage(long rowId) throws SQLException {
            Cursor mCursor = database.query(true, IMAGE_TBL, new String[] {
                    KEY_ID, KEY_NAME, KEY_SIZE, KEY_WIDTH, KEY_HEIGHT},
                    KEY_ID + "=" + rowId, null, null, null, null, null);

            if (mCursor != null) { mCursor.moveToFirst(); }
            return mCursor;
	}


	private ContentValues createContentValues(String name, String size, int width, int height) {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME,  name);
            values.put(KEY_SIZE,  size);
            values.put(KEY_WIDTH,  width);
            values.put(KEY_HEIGHT,  height);
            return values;
	}
}//DbAdapter

