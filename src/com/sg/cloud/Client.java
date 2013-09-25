package com.sg.cloud;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.sg.dao.mysqlIF;
import com.sg.model.Files;

public class Client extends Thread {

	/* member variable */
	private Socket socket;
	private Files recievFile = null;
	private boolean runable;

	/* Stream */
	private ObjectInputStream objInput;
	private ObjectOutputStream objOutput;
	private OutputStream writer;
	private InputStream reader;

	/* Hadoop Stream */
	private Configuration config;
	private FileSystem dfs;
	private FSDataInputStream fis;
	private FSDataOutputStream fos;

	/*myslq interface*/
	private mysqlIF dao;
	
	public int reCount = 0;

	public Client(Socket clientSocket, mysqlIF dao) {
		/* 리소스 초기화 */
		this.socket = clientSocket;
		this.runable = true;
		
		this.dao = dao;
		
		/* 스트림 열기 - 파일 송수신 목적 */
		try {
			System.out.println("스트림 설정 ");
			writer = socket.getOutputStream();
			reader = socket.getInputStream();
			objOutput = new ObjectOutputStream(writer);
			objInput = new ObjectInputStream(reader);
			System.out.println("스트림 정상 생성");
			// new ObjectInputStream(sock.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public boolean receiveRequest() throws IOException {
		boolean isRequest = true; 
		
		try {
			recievFile = (Files) objInput.readObject();
			/* 첫 로그인시 session key 전송, 회원 가입시 access key 전송*/
			if (recievFile.getOptionNum() == 4
					|| recievFile.getOptionNum() == 0) {
				System.out.println("key 발급");
			} else {
				isRequest = checkSessionKey(recievFile.getSessionKey(), recievFile.getUserId());
				if (isRequest)
					System.out.println("sessionKey 일치");
				else
					System.out.println("sessionKey 불일치");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			isRequest = false;
		} 
		System.out.println("\n");
		
		return isRequest;
	}

	public void run() {
		/* 도착한 패킷에 따라 업-다운로드를 분류하기 위 */
		int authResult = 0;
		try {
			if (!receiveRequest()) {
				System.out.println("connection error. check your accessKey");
				sendSessionError();
				return;
			}
		} catch (SocketException e1) {

		} catch (EOFException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("리퀘스트 수신 성공");

		try {

			switch (recievFile.getOptionNum()) {
			case 0: {
				authResult = this.authAccessKey(recievFile.getAccessKey(), recievFile.getUserId());
				this.sendSessionKey(recievFile.getUserId(), authResult);
				
				break;
			}
			case 1: {
				System.out.println("select upload");
				this.upload();

				break;
			}
			case 2: {

				System.out.println("select download");
				this.download();

				break;
			}
			case 3: {
				System.out.println("select delete");
				this.delete();
				break;
			}
			case 4: {
				System.out.println("access Key 저장");
				saveAccessKey(recievFile.getAccessKey(), recievFile.getUserId());
				break;
			}

			default: {

			}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void sendSessionError() throws IOException{
		Files sendingFile = new Files();
		int result = 2;
		
		sendingFile.setOptionNum(result);
		objOutput.writeObject(sendingFile);
		objOutput.flush();
		
	}
	
	private int authAccessKey(String key, String uId) throws IOException {
		Files sendingFile = new Files();
		int result;
		if (!checkAccessKey(key,uId))
			return 2;
		else
			return 0;		
	}
	
	private int download() throws IOException {
		String fileName = recievFile.getFileName();
		String directory = recievFile.getDirPath();
		String userId = recievFile.getUserId();

		Files sendingFile = null;
		byte[] buf = new byte[10240];

		// isExist();
		// connect HDFS
		Configuration config = new Configuration();
		config.set("fs.default.name", "hdfs://127.0.0.1:9000/");
		dfs = FileSystem.get(config);
		// download HDFS
		Path filepath;
		if (recievFile.getmasterId() != null) {
			filepath = new Path(dfs.getWorkingDirectory() + "/"
					+ "secretgarden" + recievFile.getmasterId() + "/"
					+ directory + fileName);
		} else {
			filepath = new Path(dfs.getWorkingDirectory() + "/"
					+ "secretgarden" + userId + "/" + directory + fileName);
		}
		System.out.println(filepath);
		
		
		// 파일 보내
		int totalBytesRead = 0;
		int bytesRead = 0;
		/************************************************************************/
		try {
			fis = dfs.open(filepath);
			
			sendingFile = new Files(fileName, directory, 0, buf, userId);
			objOutput.writeObject(sendingFile);
			objOutput.flush();
			System.out.println("파일이 존재함");
			
			while (-1 != (bytesRead = fis.read(buf, 0, buf.length))) {
				
				totalBytesRead += bytesRead;
				System.out.println("sending bytes : " + totalBytesRead);
				writer.write(buf, 0, bytesRead);
			}		
			
		} catch (FileNotFoundException nf) {
			int optionNum = -1;
			sendingFile = new Files(fileName, directory, optionNum, buf, userId);
			System.out.println("그런 파일 없음");
			objOutput = new ObjectOutputStream(writer);
		}		
		
		writer.close();
		System.out.println("다운로드 완료");

		return 0;
	}

	
	private void upload() throws IOException {

		byte[] buf = new byte[10240];
		int bytesRead = 0;

		/* hdfs upload*/
		Configuration config = new Configuration();
		config.set("fs.default.name", "hdfs://127.0.0.1:9000/");
		dfs = FileSystem.get(config);

		System.out.println(recievFile.getDirPath() + recievFile.getFileName());

		/*결과 보내기*/
		Files sendingFile = new Files();
		sendingFile.setOptionNum(0);
		objOutput.writeObject(sendingFile);
		objOutput.flush();
		
		Path directoryPath = new Path(dfs.getWorkingDirectory() + "/"
				+ "secretgarden" + recievFile.getUserId() + "/"
				+ recievFile.getDirPath());
		dfs.mkdirs(directoryPath);

		Path filepath = new Path(dfs.getWorkingDirectory() + "/"
				+ "secretgarden" + recievFile.getUserId() + "/"
				+ recievFile.getDirPath() + recievFile.getFileName());

		fos = dfs.create(filepath);
		//test
		int recvBytes = 0;
		while(-1 != (bytesRead = reader.read(buf, 0, buf.length))) {
			recvBytes += bytesRead;
			System.out.println("recv length :"+recvBytes);
			fos.write(buf, 0, bytesRead);
			fos.flush();
		}
		System.out.println("upload 완료");
		System.out.println(recievFile.getFileName()+"\n");
		
		
		dfs.close();
		fos.close();
	}
	
	
	
	private void delete() throws IOException {
		String fileName = recievFile.getFileName();
		String directory = recievFile.getDirPath();
		String userId = recievFile.getUserId();
		
		int response = 0;

		boolean isSuccess = false;
		Files sendingFile = null;
		byte[] buf = new byte[10240];

		// isExist();
		// connect HDFS
		Configuration config = new Configuration();
		config.set("fs.default.name", "hdfs://127.0.0.1:9000/");
		try {
			dfs = FileSystem.get(config);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// download HDFS
		//String fileDir = getFileDir(fileName);
		
		Path filepath = new Path(dfs.getWorkingDirectory() + "/"
				+ "secretgarden" + userId + "/" + directory + fileName); 
		System.out.println(filepath);
		try {
			isSuccess = dfs.delete(filepath, true);
			if (!isSuccess) {
				response = 2;
				sendingFile = new Files(fileName, directory, response, buf, userId);
				System.out.println("파일 삭제 실패");
				objOutput = new ObjectOutputStream(writer);
			}else {
				response = 0;
				sendingFile = new Files(fileName, directory, response, buf, userId);
				objOutput.writeObject(sendingFile);
				objOutput.flush();
			}
		} catch (FileNotFoundException nf) {
			response = 1;
			sendingFile = new Files(fileName, directory, response, buf, userId);
			System.out.println("그런 파일 없음");
			objOutput = new ObjectOutputStream(writer);
		} 
		return;	
	}
	
	
	
	private void sendSessionKey(String uId, int authResult) throws IOException {
		
		Files session = new Files();

		if (authResult == 2) {
			session.setOptionNum(authResult);
			objOutput.writeObject(session);
			objOutput.flush();
		} else {

			int result = 0;
			String id = "user_id = \'" + uId + "\'";
			String randomKey = generateSessionKey();
			String sessionKey = "session_key = \'" + randomKey + "\'";
			// DB에 저장 필요
			dao.updateUserSessionKey("user_info", sessionKey, id);
			
			session.setOptionNum(result);
			session.setSessionKey(randomKey);

			System.out.println("Session key : " + session.getSessionKey());
			objOutput.writeObject(session);
			objOutput.flush();
		}
	}
	
	
	private String getFileDir(String fileName){
		String fileDir;		
		fileDir = fileName.substring(0, fileName.lastIndexOf("/")+1);
		return fileDir;
	}
	
	
	private boolean saveAccessKey(String key, String uId) throws IOException {
		System.out.println("save access key");
		
		String user_id = "\'" + uId + "\'";
		String accessKey = "\'" + key + "\'";
		/*DB에 accessKey 저장*/
		if (!dao.insertUserInDB("user_info", user_id, accessKey))
			return false;
//		/*확인차 보냄*/
//		int result = 0;
//		Files confirmAccess = new Files();
//		confirmAccess.setOptionNum(result);
//		confirmAccess.setSessionKey(key);
		
		return true;
	}
	
	
	private boolean checkSessionKey(String key, String uId) {
		/*디비에서 불러와야 함*/
//		String sessionKey = "aa";
		String user_id = "user_id = \'" + uId + "\'";
		String sessionKey = dao.getKeyFromDB("session_key", "user_info", user_id);
		System.out.println("session key 인증 : " + key);
		if(sessionKey.equals(key))
			return true;
		return false;
	}
	
	
	private boolean checkAccessKey(String key, String uId) throws IOException {
		/*디비에서 불러와야 함*/
//		String accessKey = "aa";//디비 
		System.out.println("check access Key"); 
		
		String user_id = "user_id = \'" + uId + "\'";
		String accessKey = dao.getKeyFromDB("access_key", "user_info", user_id); 

		System.out.println(accessKey); 
		if (accessKey == null){
			
			accessKey = key;
			
			if (!saveAccessKey(key, uId))
				return false;
			
			System.out.println("access key 새로 설정");
		}
		System.out.println("client AccessKey : " + key);
		System.out.println("server AccessKey : " + accessKey);
		if(accessKey.equals(key)) {
			System.out.println("AccessKey : " + accessKey);
			return true; 
		}
		return false;
	}
	
	
	private String generateSessionKey() {
		
		char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		String output = sb.toString();
		return output;
	}
	
}
