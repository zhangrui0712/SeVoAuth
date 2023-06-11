package com.vprserver.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManagerFactory;

import com.vprserver.global.GlobalConstant_server;

public class HttpsServer {
	private SSLServerSocket sslServerSocket;
	private Socket socket;
//	public static void main(String[] args) throws Exception {
//		HttpsServer server = new HttpsServer();
//		server.init_port(7778);
//		System.out.println("SSLServer initialized.");
//		String a = server.acceptCMD();
//		System.out.println(a);
//		server.sendFile("/home/taotao/Documents/2BE3.jpg");
//		a = server.acceptCMD();
//		System.out.println(a);
//	}
	
	//服务器端将要使用到server.keystore和ca-trust.keystore
	/**
	 * 绑定服务到指定端口
	 * @throws Exception
	 */
	public void init_port(int port) {
		String keystorePath = GlobalConstant_server.SPATH + "/lib/server.keystore";
		String trustKeystorePath = GlobalConstant_server.SPATH + "/lib/ca-trust.keystore";
		String keystorePassword = "123456";
		try {
			SSLContext context = SSLContext.getInstance("SSL");
			//客户端证书库
			KeyStore keystore = KeyStore.getInstance("pkcs12");
			FileInputStream keystoreFis = new FileInputStream(keystorePath);
			keystore.load(keystoreFis, keystorePassword.toCharArray());
			//信任证书库
			KeyStore trustKeystore = KeyStore.getInstance("jks");
			FileInputStream trustKeystoreFis = new FileInputStream(trustKeystorePath);
			trustKeystore.load(trustKeystoreFis, keystorePassword.toCharArray());
			
			//密钥库
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("sunx509");
			kmf.init(keystore, keystorePassword.toCharArray());

			//信任库
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("sunx509");
			tmf.init(trustKeystore);
			
			//初始化SSL上下文
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			//初始化SSLSocket
			sslServerSocket = (SSLServerSocket)context.getServerSocketFactory().createServerSocket(port);
			//设置这个SSLServerSocket需要授权的客户端访问
			sslServerSocket.setNeedClientAuth(true);
			socket = sslServerSocket.accept();
		} catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException | UnrecoverableKeyException | KeyManagementException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 接收客户端消息
	 * @return
	 */
	public String acceptCMD() {
		String cmd = "";
		try {
	         BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	         //读取客户端发送来的消息
	         cmd = br.readLine();
	      } catch (IOException e) {
	         e.printStackTrace();
	      }
		return cmd;
	}
	
	/**
	 * 发送消息到客户端
	 * @param mess
	 */
	public void sendCMD(String cmd) {
		try {
	        OutputStream os = socket.getOutputStream();
	        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
	        bw.write(cmd+"\n");
	        bw.flush();
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	}
	
	/**
	 * 重新监听
	 */
	public void reAccept() {
		try {
			socket.close();
			socket = sslServerSocket.accept();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 接收客户端发送的文件
	 */
	public void acceptFile(String filename, String username, String enroll_or_auth) {
		
		File floder = new File(GlobalConstant_server.SPATH+"/voicedata/"+username+"/"+enroll_or_auth+"/"+username);
	    if (!floder.exists()) {
	    	floder.mkdirs();
	    }
		
		byte[] buf = new byte[1024 * 200];
		try {
//            System.out.println("接收到客户端的连接");

            DataInputStream dis = new DataInputStream(socket.getInputStream());
//            @SuppressWarnings("resource")
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename));

            int len = 0;

            while ((len = dis.read(buf)) != -1) {
                dos.write(buf, 0, len);
//                System.out.println(len);
            }
            dos.flush();
//            socket.shutdownOutput();
//            System.out.println("文件接受结束");
            dis.close();
            dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
		reAccept();
	}
	
	public void sendFile(String filename) {
		byte[] buffer = new byte[1024 * 200];
		try {
//			File file = new File(filename);
			DataInputStream dis = new DataInputStream(new FileInputStream(filename));
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			int len = 0;
            while ((len = dis.read(buffer)) != -1) {
                dos.write(buffer, 0, len);

            }
            dos.flush();
//            sslSocket.shutdownInput();
//            System.out.println("发送成功");
            File file = new File(filename);
            file.delete();
            dis.close();
            dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		reAccept();
	}
	
	
	
	
	
	
	
	/**
	 * 关闭sslSocket
	 */
	public void closeSSLSocket() {
		try {
			sslServerSocket.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}