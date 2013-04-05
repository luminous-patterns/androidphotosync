package com.dphoto.sync;

public class Config {
	public static final int    LOC_SLEEP       = 10000; //in milliseconds
	public static final int    USD_HEARTBEAT   = 10;
	
	// DPHOTO API Application credentials

	public static final String API_URL         = "https://api.dphoto.com/";
	public static final String APP_KEY     	   = "faa5307c52a04612d40bc27ea42c9a83";
	public static final String APP_SECRET      = "cce6c3f34af56905c6935288355164d4";
	
	public static final String API_UPLOAD      = "file/upload";
	public static final String API_GALLERY     = "gallery/getContents";
	public static final String API_ALBUM       = "album/getContents";

	
	
	public static final String SEPERATOR       = "[,+]";
}