package com.dphoto.sync;

public class Config {
	public static final int    LOC_SLEEP       = 50; //in milliseconds
	
	// DPHOTO API Application credentials

	public static final String GALLERY_ID      = "testweavebytes.dphoto.com";
	public static final String ALBUM_ID        = "zgmwnm";
	
	public static final String API_URL         = "https://api.dphoto.com/";
	public static final String API             = "file/upload";
	public static final String API_ALBUM       = "album/getContents";

	public static final String APP_KEY     	   = "faa5307c52a04612d40bc27ea42c9a83";
	public static final String APP_SECRET      = "cce6c3f34af56905c6935288355164d4";
	
	// we are combining the list of photo paths with separator in activity
	// and tokenizing them in service
	public static final String    SEPERATOR    = "[,+]";
}