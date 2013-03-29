package com.dphoto.sync;

/*
 * Value Object for Album 
 */
public class Album {
	
	private String albumID;
	private String fileName;
	
	public Album(String albumID,String fileName){
		this.albumID   = albumID;
		this.fileName  = fileName;
	}
	
	public void setAlbumID( String albumID)  {	this.albumID  = albumID; 	}
	public void setFileName(String fileName) {	this.fileName = fileName;	}
	
	public String getAlbumID() 				 {	return albumID;		}
	public String getFileName()				 {	return fileName;	}
	
}/*Album*/
