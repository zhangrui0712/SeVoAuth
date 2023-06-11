package com.vprserver.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class MySQLconnect {
 
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  //加载MySQL驱动

    private static final String DB_TABLE_URL = "jdbc:mysql://localhost:3306/vioceprintrecognition?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC";
 
    // 数据库的用户名与密码
    private static final String USER = "root";
    private static final String PASS = "zyt990611";
    
    private static String sPath = System.getProperty("user.dir");
    /**
     * 创建数据库
     */
    public static void creatDatabase() {
    	Connection conn = null;
    	Statement stmt = null;
    	try{
    		// 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//          System.out.println("连接MySQL");
    	    conn = DriverManager.getConnection(DB_URL, USER, PASS);

    	    // 执行查询
//    	    System.out.println("创建数据库");
    	    stmt = conn.createStatement();
    	    String sql = "select SCHEMA_NAME from information_schema.SCHEMATA where SCHEMA_NAME='vioceprintrecognition';";
    	    ResultSet rs = stmt.executeQuery(sql);
    	    
    	    // 展开结果集，查看是否有查询结果
            if(rs.next()) {
//            	System.out.println("数据库已存在");
            	// 关闭资源
                rs.close();
                stmt.close();
                conn.close();
    			return;
            }
    	    
            // 创建数据库
    	    sql = "create database vioceprintrecognition";
    	    stmt.executeUpdate(sql);
//    	    System.out.println("数据库创建成功");
    	    
    	    // 关闭资源
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
	}
    
    /**
     * 创建RP服务数据表
     */
    public static void creatTable_RP(String service) {
        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);
        
            // 执行查询
//            System.out.println("创建数据表");
            stmt = conn.createStatement();
            String sql = "select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='vioceprintrecognition' and TABLE_NAME='"+ service +"';";
            ResultSet rs = stmt.executeQuery(sql);
                        
            // 展开结果集，查看是否有查询结果
            if(rs.next()) {
//            	System.out.println("数据表已存在");
            	// 关闭资源
                rs.close();
                stmt.close();
                conn.close();
    			return;
            }
            
            // 创建数据表
            sql = "CREATE TABLE "+ service +"("
            		+ "id INT NOT NULL AUTO_INCREMENT,"
            		+ "user_name VARCHAR(20) NOT NULL UNIQUE,"
            		+ "PRIMARY KEY (id)"
            		+ ");";
    	    stmt.executeUpdate(sql);
//    	    System.out.println("数据表创建成功");
            
    	    // 关闭资源
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }
     
    
    /**
     * 创建IdP用户数据表
     */
    public static void creatTable_IdP() {
        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);
        
            // 执行查询
//            System.out.println("创建数据表");
            stmt = conn.createStatement();
            String sql = "select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='vioceprintrecognition' and TABLE_NAME='user';";
            ResultSet rs = stmt.executeQuery(sql);
                        
            // 展开结果集，查看是否有查询结果
            if(rs.next()) {
//            	System.out.println("数据表已存在");
            	// 关闭资源
                rs.close();
                stmt.close();
                conn.close();
    			return;
            }
            
            // 创建数据表
            sql = "CREATE TABLE user("
            		+ "id INT NOT NULL AUTO_INCREMENT,"
            		+ "user_name VARCHAR(20) NOT NULL UNIQUE,"
            		+ "password VARCHAR(103),"
            		+ "service VARCHAR(50) NOT NULL,"
            		+ "language VARCHAR(3),"
//            		+ "vppattern BLOB,"
            		+ "PRIMARY KEY (id)"
            		+ ");";
    	    stmt.executeUpdate(sql);
//    	    System.out.println("数据表创建成功");
            
    	    // 关闭资源
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }
    
    /**
     * RP查询用户
     */
    public static boolean queryUser_RP(String user_name,String service) {
    	Connection conn = null;
        PreparedStatement psQuery = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);
            
            // 查询用户是否已经存在
            String sql = "select user_name from "+ service +" where user_name = ?;";
            psQuery = conn.prepareStatement(sql);
            psQuery.setString(1, user_name);
            
            ResultSet rs = psQuery.executeQuery();
                        
            // 展开结果集，查看是否有查询结果
            if(rs.next()) {
            	psQuery.close();
                rs.close();
                conn.close();
    			return true;
            }

    	    psQuery.close();
            rs.close();
            conn.close();  
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(psQuery!=null) psQuery.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
		return false;
    }
    
    /**
     * RP插入新用户
     */
    public static boolean insertUser_RP(String user_name, String service) {
    	Connection conn = null;
        PreparedStatement psInsert = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);
        
            // 执行查询
            System.out.println("新建用户" + user_name);

            // 插入用户
            String sql = "insert into "+ service +" (user_name) value (?);";
            psInsert = conn.prepareStatement(sql);
            psInsert.setString(1, user_name);
    	    int row = psInsert.executeUpdate();
    	    
    	    // 关闭资源
    	    psInsert.close();
            conn.close();
    	    if (row > 0) {
    	    	System.out.println("用户" + user_name + "注册" + service + "成功");
			}else {
				System.out.println("用户" + user_name + "注册" + service + "失败");
				return false;
			}
  
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(psInsert!=null) psInsert.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
		return true;
    }
    
    /**
     * IdP查询用户service
     */
    public static String queryUserService_IdP(String user_name) {
    	Connection conn = null;
        PreparedStatement psQuery = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);
            
            // 查询用户是否已经存在
            String sql = "select user_name,service from user where user_name = ?;";
            psQuery = conn.prepareStatement(sql);
            psQuery.setString(1, user_name);
            
            ResultSet rs = psQuery.executeQuery();
                        
            // 展开结果集，查看是否有查询结果
            if(rs.next()) {
            	String service = rs.getString("service");
            	psQuery.close();
                rs.close();
                conn.close();
    			return service;
            }

    	    psQuery.close();
            rs.close();
            conn.close();  
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(psQuery!=null) psQuery.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
		return null;
    }
    
    /**
     * IdP查询用户language
     */
    public static String queryUserLanguage_IdP(String user_name) {
    	Connection conn = null;
        PreparedStatement psQuery = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);
            
            // 查询用户是否已经存在
            String sql = "select user_name,language from user where user_name = ?;";
            psQuery = conn.prepareStatement(sql);
            psQuery.setString(1, user_name);
            
            ResultSet rs = psQuery.executeQuery();
                        
            // 展开结果集，查看是否有查询结果
            if(rs.next()) {
            	String service = rs.getString("language");
            	psQuery.close();
                rs.close();
                conn.close();
    			return service;
            }

    	    psQuery.close();
            rs.close();
            conn.close();  
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(psQuery!=null) psQuery.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
		return null;
    }
    
    /**
     * IdP查询用户password
     */
    public static String queryUserPassword_IdP(String user_name) {
    	Connection conn = null;
        PreparedStatement psQuery = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);
            
            // 查询用户是否已经存在
            String sql = "select user_name,password from user where user_name = ?;";
            psQuery = conn.prepareStatement(sql);
            psQuery.setString(1, user_name);
            
            ResultSet rs = psQuery.executeQuery();
                        
            // 展开结果集，查看是否有查询结果
            if(rs.next()) {
            	String service = rs.getString("password");
            	psQuery.close();
                rs.close();
                conn.close();
    			return service;
            }

    	    psQuery.close();
            rs.close();
            conn.close();  
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(psQuery!=null) psQuery.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
		return null;
    }
    
    /**
     * IdP更新用户service
     */
    public static boolean updateUserService(String user_name, String service) {
    	Connection conn = null;
        PreparedStatement psUpdate = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);
        
            // 执行查询
//            System.out.println("新建用户" + user_name);

            // 插入用户
            String sql = "update user set service = ? where user_name = ?;";
            psUpdate = conn.prepareStatement(sql);
            psUpdate.setString(1, service);
            psUpdate.setString(2, user_name);
    	    int row = psUpdate.executeUpdate();
    	    
    	    // 关闭资源
    	    psUpdate.close();
            conn.close();
    	    if (row > 0) {
    	    	System.out.println("用户service更新成功");
			}else {
				System.out.println("用户service更新失败");
				return false;
			}
  
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(psUpdate!=null) psUpdate.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
		return true;
    }
    
    
    /**
     * IdP插入新用户
     */
    public static boolean insertUser_IdP(String user_name, String service) {
    	Connection conn = null;
        PreparedStatement psInsert = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);
        
            // 执行查询
            System.out.println("新建用户" + user_name);

            // 插入用户
            String sql = "insert into user (user_name,service) value (?,?);";
            psInsert = conn.prepareStatement(sql);
            psInsert.setString(1, user_name);
            psInsert.setString(2, service);
    	    int row = psInsert.executeUpdate();
    	    
    	    // 关闭资源
    	    psInsert.close();
            conn.close();
    	    if (row > 0) {
    	    	System.out.println("用户" + user_name + "注册成功");
			}else {
				System.out.println("用户" + user_name + "注册失败");
				return false;
			}
  
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(psInsert!=null) psInsert.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
		return true;
    }
    
    /**
     * IdP更新用户password
     */
    public static boolean updateUserPassword(String user_name, String password) {
    	Connection conn = null;
        PreparedStatement psUpdate = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);

            // 插入用户
            String sql = "update user set password = ? where user_name = ?;";
            psUpdate = conn.prepareStatement(sql);
            psUpdate.setString(1, password);
            psUpdate.setString(2, user_name);
    	    int row = psUpdate.executeUpdate();
    	    
    	    // 关闭资源
    	    psUpdate.close();
            conn.close();
    	    if (row > 0) {
    	    	System.out.println("用户password更新成功");
			}else {
				System.out.println("用户password更新失败");
				return false;
			}
  
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(psUpdate!=null) psUpdate.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
		return true;
    }

    /**
     * IdP更新用户language
     */
    public static boolean updateUserLanguage(String user_name, String language) {
    	Connection conn = null;
        PreparedStatement psUpdate = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);

            // 插入用户
            String sql = "update user set language = ? where user_name = ?;";
            psUpdate = conn.prepareStatement(sql);
            psUpdate.setString(1, language);
            psUpdate.setString(2, user_name);
    	    int row = psUpdate.executeUpdate();
    	    
    	    // 关闭资源
    	    psUpdate.close();
            conn.close();
    	    if (row > 0) {
    	    	System.out.println("用户password更新成功");
			}else {
				System.out.println("用户password更新失败");
				return false;
			}
  
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(psUpdate!=null) psUpdate.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
		return true;
    }
    
    /**
     * IdP删除用户
     */
    public static boolean deleteUser_IdP(String user_name) {
    	Connection conn = null;
        PreparedStatement psDelete = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);
            
            deleteDir(new File(sPath+"/voicedata/"+user_name));
            
            // 删除用户
            String sql = "delete from user where user_name = ?;";
            psDelete = conn.prepareStatement(sql);
            psDelete.setString(1, user_name);
    	    int row = psDelete.executeUpdate();
    	    
    	    // 关闭资源
    	    psDelete.close();
            conn.close();
    	    if (row > 0) {
    	    	System.out.println("用户" + user_name + "删除成功");
			}else {
				System.out.println("用户" + user_name + "删除失败");
				return false;
			}
  
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(psDelete!=null) psDelete.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
		return true;
    }

    /**
     * RP删除用户
     */
    public static boolean deleteUser_RP(String user_name,String service) {
    	Connection conn = null;
        PreparedStatement psDelete = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
        
            // 打开链接
//            System.out.println("连接MySQL");
            conn = DriverManager.getConnection(DB_TABLE_URL,USER,PASS);
            
            // 删除用户
            String sql = "delete from " + service + " where user_name = ?;";
            psDelete = conn.prepareStatement(sql);
            psDelete.setString(1, user_name);
    	    int row = psDelete.executeUpdate();
    	    
    	    // 关闭资源
    	    psDelete.close();
            conn.close();
    	    if (row > 0) {
    	    	System.out.println("用户" + user_name + "删除成功");
			}else {
				System.out.println("用户" + user_name + "删除失败");
				return false;
			}
  
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(psDelete!=null) psDelete.close();
            }catch(SQLException se2){
            }
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
		return true;
    }
    
    /**
     * 删除用户目录
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
    
    public static void main(String[] args) {
//      queryUser_RP("a", GlobalConstant.SERVICE_1);
//    	creatDatabase();
//    	creatTable_IdP();
    	System.out.println(deleteDir(new File(sPath+"/voicedata/2")));
    }
}