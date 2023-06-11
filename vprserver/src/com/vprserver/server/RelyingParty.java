package com.vprserver.server;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.vprserver.asr.log;
import com.vprserver.global.GlobalConstant_server;
import com.vprserver.sql.MySQLconnect;

public class RelyingParty {
	
	private HttpsServer httpsServer_RP = new HttpsServer();
	private HttpsClient httpsClient_RP = new HttpsClient();
	private static Logger logger = Logger.getLogger(log.class);
	
	private void startRP() {
		while (true) {
			JSONObject jsonObject = JSON.parseObject(httpsServer_RP.acceptCMD());
			if (jsonObject == null) {
				httpsServer_RP.reAccept();
				continue;
			}
			String user_name = "";
			String service = "";
			switch(Integer.parseInt(jsonObject.getString("cmd"))) {
				case GlobalConstant_server.REGISTER:
					logger.info("***RP***: 接收到用户"+user_name+"的注册请求, 选择的服务为"+service);
					//用户注册
//					System.out.println(jsonObject.toJSONString());
					user_name = jsonObject.getString("username");
					service = jsonObject.getString("service");
					if (MySQLconnect.queryUser_RP(user_name, service)) {
						logger.info("***RP***: 用户"+user_name+"已存在，请重新注册");
						httpsServer_RP.sendCMD(GlobalConstant_server.RELOGIN);
						continue;
					} else {
						logger.info("***RP***: "+service+"新建用户"+user_name);
						//查询不存在用户，进行注册
						MySQLconnect.insertUser_RP(user_name, service);
						logger.info("***RP***: "+service+"新建用户"+user_name+"成功");
						httpsServer_RP.sendCMD(GlobalConstant_server.SUCCESS);
						logger.info("***RP***: 发送注册指令到IdP");
						//发送注册指令到IdP
						httpsClient_RP.sendCMD(jsonObject.toJSONString());
						
						//接收来自IdP的用户注册是否成功的信息
						if (httpsClient_RP.acceptCMD().equals(GlobalConstant_server.SUCCESS)) {
							httpsServer_RP.sendCMD(GlobalConstant_server.SUCCESS); //success
							logger.info("***RP***: 用户"+user_name+"注册成功");
						}else {
							httpsServer_RP.sendCMD(GlobalConstant_server.FAIL); //fail
							logger.info("***RP***: 用户"+user_name+"注册失败");
						}
					}

					break;
					
				case GlobalConstant_server.LOGIN:
					//用户登陆
					user_name = jsonObject.getString("username");
					service = jsonObject.getString("service");
					logger.info("***RP***: 接收到用户"+user_name+"的登陆请求, 选择的服务为"+service);
					if (!MySQLconnect.queryUser_RP(user_name, service)) {
						logger.info("***RP***: 用户"+user_name+"不存在，请重新登陆");
						httpsServer_RP.sendCMD(GlobalConstant_server.RELOGIN);
						continue;
					} else {
						logger.info("***RP***: 用户"+user_name+"存在");
						httpsServer_RP.sendCMD(GlobalConstant_server.SUCCESS);
						//发送登陆指令到IdP
						logger.info("***RP***: 发送登陆指令到IdP");
						httpsClient_RP.sendCMD(jsonObject.toJSONString());
						//接收来自IdP的用户登陆是否成功的信息
						String result = httpsClient_RP.acceptCMD();
						if (result.equals(GlobalConstant_server.SUCCESS)) {  //success
							httpsServer_RP.sendCMD(GlobalConstant_server.SUCCESS);
							logger.info("***RP***: 用户"+user_name+"登陆成功");
							//用户使用服务
							service_use(user_name);
						}else {
							httpsServer_RP.sendCMD(GlobalConstant_server.FAIL); //fail
							logger.info("***RP***: 用户"+user_name+"登陆失败");
						}
					}
					break;	
			}
		}
			
	}
	
	/**
	 * 用户使用服务
	 */
	private void service_use(String user_name) {
		String user_cmd = httpsServer_RP.acceptCMD();
		JSONObject jsonObject = JSON.parseObject(user_cmd);
		if (jsonObject == null) {
			httpsServer_RP.reAccept();
			logger.info("***RP***: 用户"+user_name+"退出登录");
			return;
		}
		switch(Integer.parseInt(jsonObject.getString("cmd"))) {
		//更新声纹模型
		case GlobalConstant_server.UPDATE_VP:
			logger.info("***RP***: 用户"+user_name+"选择更新声纹模型");
			httpsClient_RP.sendCMD(user_cmd);
			String update_result =  httpsClient_RP.acceptCMD();
			if (update_result.equals(GlobalConstant_server.UPDATE_VP_SUCCESS)) {
				httpsServer_RP.sendCMD(GlobalConstant_server.UPDATE_VP_SUCCESS);
			}
			break;
		//重置密码
		case GlobalConstant_server.UPDATE_PW:
			logger.info("***RP***: 用户"+user_name+"选择重置密码");
			httpsClient_RP.sendCMD(user_cmd);
			break;
		//删除账户
		case GlobalConstant_server.DELETE_USER:
			logger.info("***RP***: 用户"+user_name+"选择删除账户");
			httpsClient_RP.sendCMD(user_cmd);
			String service_all =  httpsClient_RP.acceptCMD();
			String[] service_list = service_all.split(",");
			for(String sv : service_list){
				MySQLconnect.deleteUser_RP(user_name, sv);
			}
			httpsServer_RP.sendCMD(GlobalConstant_server.SUCCESS);
			break;
		//退出登录
		case GlobalConstant_server.LOG_OUT:
			logger.info("***RP***: 用户"+user_name+"选择退出登录");
			break;
		}
	}
	
	public void RP_Run() {
		//检查RP数据库或数据表是否存在，不存在则创建，储存用户及用户注册过的服务
		MySQLconnect.creatTable_RP(GlobalConstant_server.SERVICE_1);
		MySQLconnect.creatTable_RP(GlobalConstant_server.SERVICE_2);
		MySQLconnect.creatTable_RP(GlobalConstant_server.SERVICE_3);
		MySQLconnect.creatTable_RP(GlobalConstant_server.SERVICE_4);
		
		httpsServer_RP.init_port(GlobalConstant_server.RP_PORT);
		logger.info("***RP***: 绑定"+GlobalConstant_server.RP_PORT+"端口成功");
		httpsClient_RP.init(GlobalConstant_server.IdP_PORT_PR);
		logger.info("***RP***: 连接到IdP"+GlobalConstant_server.IdP_PORT_PR+"端口成功");
		
		startRP();
		httpsServer_RP.closeSSLSocket();
	}
	
	
	public static void main(String[] args) {
		RelyingParty rp = new RelyingParty();
		rp.RP_Run();
	}
	
}
