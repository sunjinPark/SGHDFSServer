package com.sg.dao;

import java.sql.*;

public class mysqlIF {
	private Connection conn;
	private Statement stmt;
	
	public mysqlIF() {
		connectDB();
	}
	
	
	public String getKeyFromDB (String select, String from, String where) {
		String key=null;
		ResultSet rs;
		
		System.out.println("load key");
		try {
			String sql = "SELECT " + select + " from " + from + " where " + where;
			System.out.println(sql);
			rs = stmt.executeQuery(sql);
			
			if(rs.next()) {  
				key = rs.getString(1); 
			} 
		} catch (SQLException e) {
			System.out.println("쿼리 실패");
			
			e.printStackTrace();
			return key;
		}
		System.out.println("\n\n쿼리 결과 : " + key);
		return key;
	} 
	
	
	public boolean connectDB() {
		try {
			Class.forName("com.mysql.jdbc.Driver");// 드라이버 로딩: DriverManager에 등록
			
		} catch (ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
		}

		try {
			String jdbcUrl = "jdbc:mysql://localhost:3306/user_info";// 사용하는
																	// 데이터베이스명을
																	// 포함한 url
			String userId = "root";// 사용자계정
			String userPass = "";// 사용자 패스워드
			
			conn = DriverManager.getConnection(jdbcUrl, userId, userPass);// Connection
																			// 객체를
																			// 얻어냄

			stmt = conn.createStatement();// Statement 객체를 얻어냄

			System.out.println("DB와 연결 완료");
		} catch (SQLException e) {
			System.out.println("SQLException: " + e.getMessage());
		}
		
		return true;
	}
	
	
	public boolean insertUserInDB(String table_name, String u_id, String accessKey) {
		System.out.println("insert user");
		try {
			
//			String sql = "INSERT INTO " + table_name
//				+ " VALUES (\'" + u_id + "\', \'" + accessKey + "\', null )";
			String sql = "INSERT INTO " + table_name
					+ " VALUES (" + u_id + ", " + accessKey + ", null )";
			System.out.println(sql);
			stmt.executeUpdate(sql);
						
		} catch (SQLException e) {
			System.out.println("user 등록 실패");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	public boolean updateUserSessionKey(String table, String sessionKey, String user_id) {
//		String sql = "UPDATE Registration " +
//                "SET age = 30 WHERE id in (100, 101)";
//   stmt.executeUpdate(sql);
		System.out.println("update sessionkey");
		try {
			String sql = "UPDATE " + table + " SET "+ sessionKey + " WHERE " + user_id;
			System.out.println("update test : " + sql);
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			System.out.println("session key update 실패");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	public void disconnectDB() {
		try {
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Connection getConn() {
		return conn;
	}

	
	public void setConn(Connection conn) {
		this.conn = conn;
	}


	public Statement getStmt() {
		return stmt;
	}


	public void setStmt(Statement stmt) {
		this.stmt = stmt;
	}

	
	
//	public static void main(String[] args) {
//		mysqlIF testCase = new mysqlIF();
//		String table = "test_table";
//		String u_id = "test_user3";
//		String accessKey = "abcde"; 
//		testCase.insertUserInDB(table, u_id, accessKey);
//		
//		String select = "sessionkey";
//		String from = "test_table";
//		String where = "user_id = \'test_user3\'";
//		String key = testCase.getKeyFromDB(select, from, where);
//		System.out.println(key);
//		
//		String table = "test_table";
//		String sessionKey = "sessionkey = \'new_session_key\'";
//		String user_id = "user_id = \'test_user\'";
//		testCase.updateUserSessionKey(table, sessionKey, user_id);
//	}
	
	
}