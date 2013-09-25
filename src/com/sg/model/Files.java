package com.sg.model;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;

public class Files implements Serializable{

	private static final long serialVersionUID = 45451L;
	
	private String fileName;
	private String dirPath;
	private int fileSize;
	private byte[] fileBuf;
	private int optionNum;
	private String userId;
	private String sessionKey;
	private String accessKey;
	private String masterId;

	public Files(){
	
	}
	
	public Files(String name, String path, int num, String id){
		fileName = name;
		dirPath = path;
		optionNum = num;		
		setUserId(id);
	}


	public Files(String name, String path, int num, byte[] buf, String id){
		fileName = name;
		optionNum = num;
		fileBuf = buf;
		dirPath = path;
		setUserId(id);
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getOptionNum() {
		return optionNum;
	}
	public void setOptionNum(int optionNum) {
		this.optionNum = optionNum;
	}
	
	public byte[] getFileBuf() {
		return fileBuf;
	}

	public void setFileBuf(byte[] fileBuf) {
		this.fileBuf = fileBuf;
	}

	public String getDirPath() {
		return dirPath;
	}


	public void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getmasterId() {
		return masterId;
	}

	public void setmasterId(String masterId) {
		this.masterId = masterId;
	}


}


