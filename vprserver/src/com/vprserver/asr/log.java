package com.vprserver.asr;

import org.apache.log4j.Logger;


public class log {
	
private static Logger logger = Logger.getLogger(log.class);


public static void main(String[] args) {
//	System.setProperty("aip.log4j.conf", "/home/taotao/my_project/eclipse-workspace/vprserver/src/log4j.properties");
	for(int i=0;i<10;i++){
// 记录debug级别的信息
logger.debug("This is debug message.");
// 记录info级别的信息
logger.info("This is info message.");
// 记录error级别的信息
logger.error("This is error message."); 
}
}
}