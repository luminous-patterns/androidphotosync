package com.dphoto.sync;

public class Album {
	
	private String albumID;
	private String fileName;
	private String fileURL;
	private String fileHref;
	private String fileID;
	
	public Album(String albumID,String fileName, String fileURL, String fileHref, String fileID){
		this.albumID   = albumID;
		this.fileName  = fileName;
		this.fileURL   = fileURL;
		this.fileHref  = fileHref;
		this.fileID    = fileID;

	}
	
	
	public void setAlbumID( String albumID)  {	this.albumID  = albumID; 	}
	public void setFileName(String fileName) {	this.fileName = fileName;	}
	public void setFileURL( String fileURL)  {	this.fileURL  = fileURL;	}
	public void setFileHref(String fileHref) {	this.fileHref = fileHref;	}
	public void setFileID(  String fileID)   {	this.fileID   = fileID;	}
	
	public String getAlbumID() 				 {	return albumID;		}
	public String getFileName()				 {	return fileName;	}
	public String getFileURL() 				 {	return fileURL;		}
	public String getFileHref()				 {	return fileHref;	}
	public String getFileID()				 {	return fileID;		}

}/*Album*/
