package com.sg.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.sg.dao.mysqlIF;
import com.sg.model.Files;

public class HDFSServer{


	private ServerSocket serverSock;
	private mysqlIF dao;

	public HDFSServer() {
		
		try {
			
			serverSock = new ServerSocket(15000);
			dao = new mysqlIF();
			
			System.out.println("Server started..");
			while(true){
				
				System.out.println("Wating Client");
				
				Socket clientSocket = serverSock.accept();
				Client client = new Client(clientSocket, dao);
				client.start();
					
				
				System.out.println("Client accepted");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public mysqlIF getDao() {
		return dao;
	}

	public void setDao(mysqlIF dao) {
		this.dao = dao;
	}

}
