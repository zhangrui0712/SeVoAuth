package com.vprserver.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import com.vprserver.global.GlobalConstant_server;
 
public class HttpsClient {
	private SSLSocket sslSocket;
//	public static void main(String[] args) throws Exception {
//		HttpsClient client = new HttpsClient();
//		client.init(7778);
//		System.out.println("SSLClient initialized.");
//		client.process();
//		client.sendCMD("aaaa");
//		System.out.println(1);
//		System.out.println(client.acceptMess());
//		System.out.println(2);
//		client.sendFile(GlobalConstant_client.SPATH+"/voicedata/enroll_1.wav");
//		System.out.println(3);
//		
//		System.out.println(client.acceptMess());
//
//		System.out.println(4);
//		client.sendFile(GlobalConstant_client.SPATH+"/voicedata/enroll_2.wav");
//		System.out.println(5);
//		client.closeSSLSocket();
//	}
	
	//客户端将要使用到client.keystore和ca-trust.keystore
	/**
	 * 与7777端口的IdP通信
	 * @throws Exception
	 */
	public void init(int port) {
		String host = "127.0.0.1";
		String keystorePath = GlobalConstant_server.SPATH + "/lib/client.keystore";
		String trustKeystorePath = GlobalConstant_server.SPATH + "/lib/ca-trust.keystore";
		String keystorePassword = "123456";
		try {
			SSLContext context = SSLContext.getInstance("SSL");
			//客户端证书库
			KeyStore clientKeystore = KeyStore.getInstance("pkcs12");
			FileInputStream keystoreFis = new FileInputStream(keystorePath);
			clientKeystore.load(keystoreFis, keystorePassword.toCharArray());
			//信任证书库
			KeyStore trustKeystore = KeyStore.getInstance("jks");
			FileInputStream trustKeystoreFis = new FileInputStream(trustKeystorePath);
			trustKeystore.load(trustKeystoreFis, keystorePassword.toCharArray());
			
			//密钥库
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("sunx509");
			kmf.init(clientKeystore, keystorePassword.toCharArray());

			//信任库
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("sunx509");
			tmf.init(trustKeystore);
			
			//初始化SSL上下文
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			
			sslSocket = (SSLSocket)context.getSocketFactory().createSocket(host, port);
		} catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException | UnrecoverableKeyException | KeyManagementException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 发送消息给服务器
	 * @param cmd
	 * @throws Exception
	 */
	public void sendCMD(String cmd) {
		try {
	        OutputStream os = sslSocket.getOutputStream();
	        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
	        bw.write(cmd+"\n");
	        bw.flush();
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	}
	
	/**
	 * 接收服务器消息
	 * @return
	 */
	public String acceptCMD() {
		String cmd = "";
		try {
	         BufferedReader br = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
	         //读取客户端发送来的消息
	         cmd = br.readLine();
	      } catch (IOException e) {
	         e.printStackTrace();
	      }
		return cmd;
	}
	
	/**
	 * 关闭sslSocket
	 */
	public void closeSSLSocket() {
		try {
			sslSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

