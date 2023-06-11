package com.vprserver.global;

public class GlobalConstant_server {
	public static final int CODEDIGIT = 6;   //验证码位数
	public static final String SPATH = System.getProperty("user.dir"); 
	
	public static final String CMD_SELECT_SERVICE = "select_service";
	
	public static final int REGISTER = 1;
	public static final int LOGIN = 2;
	
	public static final int UPDATE_VP = 100;
	public static final int UPDATE_PW = 101;
	public static final int DELETE_USER = 102;
	public static final int LOG_OUT = 103;
	
	public static final String UPDATE_VP_SUCCESS = "update_vp_success";
	
	public static final int SCORE = 26;
	
	public static final int MANDARIN = 1;
	public static final int LOCALISM = 2;
	public static final int ENGLISH = 3;
	
	public static final int ASR_MANDARIN = 1537;
	public static final int ASR_LOCALISM = 1837;  //四川话
	public static final int ASR_ENGLISH = 1737;
	
	public static final String ENROLL = "enroll";
	public static final String AUTHENTICATE = "authenticate";
	
	public static final int RP_PORT = 7776;
	public static final int IdP_PORT_PR = 7777;
	public static final int IdP_PORT_UA = 7778;
	
	public static final String RELOGIN = "relogin";
	public static final String SUCCESS = "success";
	public static final String FAIL = "fail";
	
	public static final String SERVICE_1 = "service_1";
	public static final String SERVICE_2 = "service_2";
	public static final String SERVICE_3 = "service_3";
	public static final String SERVICE_4 = "service_4";
	
	public static final String VPR_AUTH = "vpr";
	public static final String PW_AUTH = "password";
}
