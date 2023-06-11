package com.vprserver.server;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.vprserver.asr.BaiduASR;
import com.vprserver.asr.log;
import com.vprserver.global.GlobalConstant_server;
import com.vprserver.password.PasswordCodes;
import com.vprserver.password.PasswordHash;
import com.vprserver.sql.MySQLconnect;
import com.vprserver.xvector.Xvector;

public class IdentityProvider {
	private HttpsServer sslServer_IdP_toRP = new HttpsServer();
	private HttpsServer sslServer_IdP_toUA = new HttpsServer();
	private static Logger logger = Logger.getLogger(log.class);
//	public IdentityProvider() {
//		MySQLconnect.creatDatabase();
//		MySQLconnect.creatTable_IdP();
//	}
	
	private void startIdP() {
		while (true) {
			JSONObject json_from_RP = JSON.parseObject(sslServer_IdP_toRP.acceptCMD());
			if (json_from_RP == null) {
				sslServer_IdP_toRP.reAccept();
				continue;
			}
			String user_name = json_from_RP.getString("username");
			String user_service = json_from_RP.getString("service");
			
			//生成验证码
			String codes1 = generateRandom();
			String codes2 = generateRandom();
			logger.info("***IdP***: 生成验证码code1,code2");
			
			switch(Integer.parseInt(json_from_RP.getString("cmd"))) {
			case GlobalConstant_server.REGISTER:
				//用户注册
				logger.info("***IdP***: 接收到用户"+user_name+"的注册请求, 选择的服务为"+user_service);
				String service = MySQLconnect.queryUserService_IdP(user_name);
				if (service != null) {
					//用户存在，但未注册该服务，更新用户注册的service
					logger.info("***IdP***: "+user_name+"用户已存在, 更新用户注册的service");
					service = service + "," + json_from_RP.getString("service");
					MySQLconnect.updateUserService(user_name, service);
					
					logger.info("***IdP***: 通知UA进入认证流程");
					sslServer_IdP_toUA.sendCMD("{"
													+ "'cmd':'" + GlobalConstant_server.LOGIN + "'"
													+ "}");
					//进入用户认证流程
					//用户登陆
					JSONObject auth_select = JSON.parseObject(sslServer_IdP_toUA.acceptCMD());
					if (auth_select == null) {
						sslServer_IdP_toRP.reAccept();
						continue;
					}
					String auth_type = auth_select.getString("auth_type");
					
					if (auth_type.equals(GlobalConstant_server.VPR_AUTH)) {   //声纹认证
						if(!vpr_auth(user_name, codes1, codes2)) {
							continue;
						}
					} else if (auth_type.equals(GlobalConstant_server.PW_AUTH)) {   //密码认证
						if (!pw_auth(user_name)) {
							continue;
						}
					}	
					
				}else {
					//用户不存在，新建用户
					logger.info("***IdP***: "+user_service+"新建用户"+user_name);
					MySQLconnect.insertUser_IdP(user_name, json_from_RP.getString("service"));
					logger.info("***IdP***: "+user_service+"新建用户"+user_name+"完成");
					sslServer_IdP_toUA.sendCMD("{"
												+ "'cmd':'" + GlobalConstant_server.REGISTER + "',"
												+ "'codes1':'" + codes1 + "',"
												+ "'codes2':'" + codes2 + "',"
												+ "'username':'" + user_name + "'"
												+ "}");
					//保存客户端发送的音频文件
					boolean accept_result = true;
					String language_mode = accept_voice(user_name, codes1, codes2, GlobalConstant_server.ENROLL);
					if(language_mode == null) {
						accept_result = false;
					}
					
					if (!accept_result) {
						MySQLconnect.deleteUser_IdP(user_name);
						MySQLconnect.deleteUser_RP(user_name, user_service);
						continue;
					}
					
					logger.info("***IdP***: 接收音频完毕");
					
					MySQLconnect.updateUserLanguage(user_name, language_mode);
					logger.info("***IdP***: 用户说话模式组合储存完成");
					
					//训练用户声纹模型(提取x-vector)
					Xvector.extract_xvector(user_name, GlobalConstant_server.ENROLL);
					logger.info("***IdP***: 提取x-vector完毕");

					//发送用户注册成功信息到RP
					sslServer_IdP_toRP.sendCMD(GlobalConstant_server.SUCCESS);
					logger.info("***IdP***: 发送用户注册成功信息到RP");
					//接收客户端发送的密码
					JSONObject jsonPW = JSON.parseObject(sslServer_IdP_toUA.acceptCMD());
					if (jsonPW == null) {
						sslServer_IdP_toUA.reAccept();
						MySQLconnect.deleteUser_IdP(user_name);
						MySQLconnect.deleteUser_RP(user_name, user_service);
						continue;
					}
					String password = jsonPW.getString("password");
					logger.info("***IdP***: 接收密码完毕");
					try {
						String pwHash = PasswordHash.createHash(password);
						logger.info("***IdP***: 计算密码hash完毕");
						MySQLconnect.updateUserPassword(user_name, pwHash);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					} catch (InvalidKeySpecException e) {
						e.printStackTrace();
					}
				}
		
				break;  //case break
				
			///////////////////LOGIN////////////////////////////////////////////////////////////////////////////////			
			case GlobalConstant_server.LOGIN:
				//用户登陆
				logger.info("***IdP***: 接收到用户"+user_name+"的登陆请求, 选择的服务为"+user_service);
				JSONObject auth_select = JSON.parseObject(sslServer_IdP_toUA.acceptCMD());
				if(auth_select == null) {
					break;
				}
				String auth_type = auth_select.getString("auth_type");
				
				if (auth_type.equals(GlobalConstant_server.VPR_AUTH)) {   //声纹认证
					if (!vpr_auth(user_name, codes1, codes2)) {
						continue;
					}
										
				} else if (auth_type.equals(GlobalConstant_server.PW_AUTH)) {   //密码认证
					if (!pw_auth(user_name)) {
						continue;
					}
				}
				
				break;	//case break
			
			//更新声纹模型	
			case GlobalConstant_server.UPDATE_VP:
				logger.info("***IdP***: 接收到用户"+user_name+"的更新声纹模型请求, 进入更新声纹模型流程");
				//保存两个新的用户音频
				codes1 = generateRandom();
				codes2 = generateRandom();
				sslServer_IdP_toUA.sendCMD("{"
						+ "'codes1':'" + codes1 + "',"
						+ "'codes2':'" + codes2 + "',"
						+ "'username':'" + user_name + "'"
						+ "}");
				String language_mode = accept_voice(user_name, codes1, codes2, GlobalConstant_server.ENROLL);
				if(language_mode == null) {
					continue;
				}
				
				//新的用户音频通过语音内容识别，替换原来的音频
//				String[] new_language = language_mode.split(" ");
//				File voice1 = new File(GlobalConstant_server.SPATH+"/voicedata/"+user_name+"/enroll/"+user_name+"/enroll_"+new_language[0]+".wav");
//				voice1.delete();
//				File voice2 = new File(GlobalConstant_server.SPATH+"/voicedata/"+user_name+"/enroll/"+user_name+"/enroll_"+new_language[1]+".wav");
//				voice2.delete();
//				File new_voice1 = new File(GlobalConstant_server.SPATH+"/voicedata/"+user_name+"/enroll/"+user_name+"/new_enroll_"+new_language[0]+".wav");
//				new_voice1.renameTo(voice1);
//				File new_voice2 = new File(GlobalConstant_server.SPATH+"/voicedata/"+user_name+"/enroll/"+user_name+"/new_enroll_"+new_language[1]+".wav");
//				new_voice2.renameTo(voice2);
				
				logger.info("***IdP***: 接收音频完毕");
				MySQLconnect.updateUserLanguage(user_name, language_mode);
				logger.info("***IdP***: 用户说话模式组合储存完成");
				
				//训练用户声纹模型(提取x-vector)
				Xvector.extract_xvector(user_name, GlobalConstant_server.ENROLL);
				logger.info("***IdP***: 提取x-vector完毕");

				//发送用户更新声纹模型成功到RP
				sslServer_IdP_toRP.sendCMD(GlobalConstant_server.UPDATE_VP_SUCCESS);
				logger.info("***IdP***: 发送用户更新声纹模型成功到RP");
				break;  //case break
			
			//重置密码	
			case GlobalConstant_server.UPDATE_PW:
				logger.info("***IdP***: 接收到用户"+user_name+"的重置密码请求, 进入重置密码流程");
				JSONObject new_pw = JSON.parseObject(sslServer_IdP_toUA.acceptCMD());
				if (new_pw == null) {
					continue;
				}
				String new_password = new_pw.getString("password");
				logger.info("***IdP***: 接收密码完毕");
				try {
					String new_pwHash = PasswordHash.createHash(new_password);
					logger.info("***IdP***: 计算密码hash完毕");
					MySQLconnect.updateUserPassword(user_name, new_pwHash);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (InvalidKeySpecException e) {
					e.printStackTrace();
				}
				break;  //case break
				
			//删除账户	
			case GlobalConstant_server.DELETE_USER:
				logger.info("***IdP***: 接收到用户"+user_name+"的删除账户请求, 进入删除账户流程");
				String service_all = MySQLconnect.queryUserService_IdP(user_name);
				MySQLconnect.deleteUser_IdP(user_name);
				delFiles(new File(GlobalConstant_server.SPATH+"/voicedata/"+user_name));
				sslServer_IdP_toRP.sendCMD(service_all);
				break;  //case break
			}
			
		}
		
	}
	
	/**
	 * 声纹认证
	 */
	private boolean vpr_auth(String user_name, String codes1, String codes2) {
		logger.info("***IdP***: 进入声纹认证流程");
		String language = MySQLconnect.queryUserLanguage_IdP(user_name);
		logger.info("***IdP***: 通知UA说话模式组合"+language);
		sslServer_IdP_toUA.sendCMD("{"
										+ "'cmd':'" + GlobalConstant_server.LOGIN + "',"
										+ "'codes1':'" + codes1 + "',"
										+ "'codes2':'" + codes2 + "',"
										+ "'username':'" + user_name + "',"
										+ "'language':'" + language + "'"
										+ "}");
		
		//保存客户端发送的音频文件
		boolean accept_result = true;
		if(accept_voice(user_name, codes1, codes2, GlobalConstant_server.AUTHENTICATE) == null) {
			accept_result = false;
		}
		
		if (!accept_result) {
			return accept_result;
		}else {
			logger.info("***IdP***: 接收音频完毕");
			
			//训练用户声纹模型(提取x-vector)
			Xvector.extract_xvector(user_name, GlobalConstant_server.AUTHENTICATE);
			
			logger.info("***IdP***: 提取x-vector完毕");
			
			//计算用户声纹认证分数
			double score = Xvector.score_xvector(user_name);
			
			if(score>GlobalConstant_server.SCORE) {
				logger.info("***IdP***: 用户"+user_name+"认证分数为: "+score+", 认证通过");
				sslServer_IdP_toRP.sendCMD(GlobalConstant_server.SUCCESS);
			}else {
				logger.info("***IdP***: 用户"+user_name+"认证分数为: "+score+", 认证失败");
				sslServer_IdP_toRP.sendCMD(GlobalConstant_server.FAIL);
			}
			return accept_result;
		}
	}
	
	/**
	 * 接收用户音频
	 */
	private String accept_voice(String user_name, String codes1, String codes2, String enroll_or_auth) {
		String language1 = "";
		String language2 = "";
		while(true) {   //接收用户音频，并检验语音内容是否符合验证码
			boolean voice1_result = false;
			logger.info("***IdP***: 准备接收第1个用户音频");
			JSONObject rcd_mess = JSON.parseObject(sslServer_IdP_toUA.acceptCMD());
			if (rcd_mess == null) {
				sslServer_IdP_toUA.reAccept();
				return null;
			}
			language1 = rcd_mess.getString("language");
			logger.info("***IdP***: 接收到第1个用户音频说话模式 "+language1);
			String voice1 = GlobalConstant_server.SPATH + "/voicedata/" + user_name + "/"+ enroll_or_auth +"/" + user_name + "/"+ enroll_or_auth +"_" + language1 + ".wav";
			sslServer_IdP_toUA.acceptFile(voice1,rcd_mess.getString("username"),enroll_or_auth);
			logger.info("***IdP***: 接收到第1个用户音频");
			int lan = Integer.parseInt(language1);
			//使用百度ASR进行语音内容识别
			if(lan == GlobalConstant_server.MANDARIN) {
				if (BaiduASR.asr(voice1, GlobalConstant_server.ASR_MANDARIN).equals(codes1)) {
					logger.info("***IdP***: 语音1与验证码匹配");
					voice1_result = true;
				}else {
					logger.info("***IdP***: 语音1与验证码不匹配");
					deleteFile(voice1);
				}
			} else if (lan == GlobalConstant_server.LOCALISM) {
				if (BaiduASR.asr(voice1, GlobalConstant_server.ASR_LOCALISM).equals(codes1)) {
					logger.info("***IdP***: 语音1与验证码匹配");
					voice1_result = true;
				}else {
					logger.info("***IdP***: 语音1与验证码不匹配");
					deleteFile(voice1);
				}
			} else if (lan == GlobalConstant_server.ENGLISH) {
				if (BaiduASR.asr(voice1, GlobalConstant_server.ASR_ENGLISH).equals(codes1)) {
					logger.info("***IdP***: 语音1与验证码匹配");
					voice1_result = true;
				}else {
					logger.info("***IdP***: 语音1与验证码不匹配");
					deleteFile(voice1);
				}
			}
			
			logger.info("***IdP***: 准备接收第2个用户音频");
			rcd_mess = JSON.parseObject(sslServer_IdP_toUA.acceptCMD());
			if (rcd_mess == null) {
				sslServer_IdP_toUA.reAccept();
				return null;
			}
			language2 = rcd_mess.getString("language");
			logger.info("***IdP***: 接收到第2个用户音频说话模式 "+language2);
			
			String voice2 = GlobalConstant_server.SPATH + "/voicedata/" + user_name + "/"+ enroll_or_auth +"/" + user_name + "/"+ enroll_or_auth +"_" + language2 + ".wav";
			sslServer_IdP_toUA.acceptFile(voice2,rcd_mess.getString("username"),enroll_or_auth);
			logger.info("***IdP***: 接收到第2个用户音频");
			lan = Integer.parseInt(language2);
			
			//使用百度ASR进行语音内容识别
			if (!voice1_result) {
				deleteFile(voice2);
				logger.info("***IdP***: 语音1与验证码不匹配,重新生成验证码code1,code2");
				codes1 = generateRandom();
				codes2 = generateRandom();
				sslServer_IdP_toUA.sendCMD("{"
						+ "'codes1':'" + codes1 + "',"
						+ "'codes2':'" + codes2 + "',"
						+ "'username':'" + user_name + "'"
						+ "}");
				continue;
			}
			if(lan == GlobalConstant_server.MANDARIN) {
				if (BaiduASR.asr(voice2, GlobalConstant_server.ASR_MANDARIN).equals(codes2)) {
					logger.info("***IdP***: 语音2与验证码匹配");
					sslServer_IdP_toUA.sendCMD(GlobalConstant_server.SUCCESS);
					break;
				}else {
					logger.info("***IdP***: 语音2与验证码不匹配，重新生成验证码code1,code2");
					codes1 = generateRandom();
					codes2 = generateRandom();
					sslServer_IdP_toUA.sendCMD("{"
							+ "'codes1':'" + codes1 + "',"
							+ "'codes2':'" + codes2 + "',"
							+ "'username':'" + user_name + "'"
							+ "}");
					deleteFile(voice1);
					deleteFile(voice2);
					continue;
				}
			} else if (lan == GlobalConstant_server.LOCALISM) {
				if (BaiduASR.asr(voice2, GlobalConstant_server.ASR_LOCALISM).equals(codes2)) {
					logger.info("***IdP***: 语音2与验证码匹配");
					sslServer_IdP_toUA.sendCMD(GlobalConstant_server.SUCCESS);
					break;
				}else {
					logger.info("***IdP***: 语音2与验证码不匹配，重新生成验证码code1,code2");
					codes1 = generateRandom();
					codes2 = generateRandom();
					sslServer_IdP_toUA.sendCMD("{"
							+ "'codes1':'" + codes1 + "',"
							+ "'codes2':'" + codes2 + "',"
							+ "'username':'" + user_name + "'"
							+ "}");
					deleteFile(voice1);
					deleteFile(voice2);
					continue;
				}
			} else if (lan == GlobalConstant_server.ENGLISH) {
				if (BaiduASR.asr(voice2, GlobalConstant_server.ASR_ENGLISH).equals(codes2)) {
					logger.info("***IdP***: 语音2与验证码匹配");
					sslServer_IdP_toUA.sendCMD(GlobalConstant_server.SUCCESS);
					break;
				}else {
					logger.info("***IdP***: 语音2与验证码不匹配，重新生成验证码code1,code2");
					codes1 = generateRandom();
					codes2 = generateRandom();
					sslServer_IdP_toUA.sendCMD("{"
							+ "'codes1':'" + codes1 + "',"
							+ "'codes2':'" + codes2 + "',"
							+ "'username':'" + user_name + "'"
							+ "}");
					deleteFile(voice1);
					deleteFile(voice2);
					continue;
				}
			} 
			
			
		} //while end
		return language1+" "+language2;
	}
	
	/**
	 * 密码认证
	 */
	private boolean pw_auth(String user_name) {
		logger.info("***IdP***: 进入密码认证流程");
		String PW_codes = PasswordCodes.getPWcodes();
		String codejpg = GlobalConstant_server.SPATH + "/voicedata/temp.jpg";
		sslServer_IdP_toUA.sendFile(codejpg);  //发送图片验证码
		File file = new File(codejpg);
		file.delete();
		try {
			JSONObject json_password = JSON.parseObject(sslServer_IdP_toUA.acceptCMD());
			if (json_password == null) {
				return false;
			}
			logger.info("***IdP***: 接收到用户"+user_name+"密码");
			String password = json_password.getString("password");
			String codes = json_password.getString("codes");
			if (PasswordHash.validatePassword(password, MySQLconnect.queryUserPassword_IdP(user_name)) && PW_codes.equals(codes.toUpperCase())) {
				logger.info("***IdP***: 用户"+user_name+"认证通过");
				sslServer_IdP_toRP.sendCMD(GlobalConstant_server.SUCCESS);
			} else {
				logger.info("***IdP***: 用户"+user_name+"认证失败");
				sslServer_IdP_toRP.sendCMD(GlobalConstant_server.FAIL);
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * 生成6位随机验证码
	 */
	private static String generateRandom() {
		int[] random = new int[GlobalConstant_server.CODEDIGIT];
		Random rand = new SecureRandom();
		for (int i = 0; i < GlobalConstant_server.CODEDIGIT; i++) {
			random[i]=rand.nextInt(10);
			
			//保证每位验证码不相同
			for (int j = 0; j < i; j++) {
				if (random[j] == random[i]) {
					i--;
					break;
				}
			}
		}
		
		String randomStr = "";
		for (int i = 0; i < random.length; i++) {
			randomStr += random[i];
		}
		return randomStr;
	}
	
	/**
	 * 删除文件
	 */
	private void deleteFile(String filepath) {
		File file = new File(filepath);
		file.delete();
	}
	
	/**
     * 递归删除
     * 删除某个目录及目录下的所有子目录和文件
     * @param file 文件或目录
     * @return 删除结果
     */
    public static boolean delFiles(File file){
        boolean result = false;
        //目录
        if(file.isDirectory()){
            File[] childrenFiles = file.listFiles();
            for (File childFile:childrenFiles){
                result = delFiles(childFile);
                if(!result){
                    return result;
                }
            }
        }
        //删除 文件、空目录
        result = file.delete();
        return result;
    }
	
	public void IdP_Run() {
//		MySQLconnect.creatDatabase();
//		MySQLconnect.creatTable_IdP();
		//绑定端口
		sslServer_IdP_toRP.init_port(GlobalConstant_server.IdP_PORT_PR);
		logger.info("***IdP***: 绑定"+GlobalConstant_server.IdP_PORT_PR+"端口成功");
		
		sslServer_IdP_toUA.init_port(GlobalConstant_server.IdP_PORT_UA);
		logger.info("***IdP***: 绑定"+GlobalConstant_server.IdP_PORT_UA+"端口成功");
		
		startIdP();
		
		//关闭socket
		sslServer_IdP_toRP.closeSSLSocket();
		sslServer_IdP_toUA.closeSSLSocket();
	}
	
	
	
	
	public static void main(String[] args) {
		MySQLconnect.creatDatabase();
		MySQLconnect.creatTable_IdP();
		IdentityProvider IdP = new IdentityProvider();
		IdP.IdP_Run();
		
	}
}
