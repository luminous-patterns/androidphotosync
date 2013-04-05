package com.dphoto.sync;

public class FileInfo {
	
	private String 	fileSize;
	private String  fileName;
	private int     width;
	private int     height;
	
	public FileInfo(String fileName, String fileSize, int width, int height){
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.width    = width;
		this.height   = height;
	}
	
	public String 	getFileSize(){ 	return fileSize;	}
	public String	getFileName(){ 	return fileName;	}
	public int 		getWidth()	 { 	return width;		}
	public int		getHeight()	 { 	return height;		}
	
	
	public void 	setFileSize(String fileSize)	{ this.fileSize = fileSize;	}
	public void		setFileName(String fileName)	{ this.fileName = fileName;	}
	public void 	setWidth(int width)				{ this.width = width;	}
	public void		setHeight(int height)			{ this.height = height;	}

}/*FileInfo*/
