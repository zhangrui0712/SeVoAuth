package com.vprserver.asr;


import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import com.baidu.aip.speech.AipSpeech;
import com.vprserver.global.GlobalConstant_server;

public class BaiduASR {
	//设置APPID/AK/SK
	private static final String APP_ID = "19147594";
	private static final String API_KEY = "hZVRG3tv1ryB1Sl6xsKykMW6";
	private static final String SECRET_KEY = "2xl1rsSr5sGUkNGiXVl8fIp8koTrF4C2";
	private static Logger logger = Logger.getLogger(log.class);
    
    public static String asr(String filepath,int language) {
    	AipSpeech client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);
    	HashMap<String, Object> map = new HashMap<String,Object>();
        map.put("dev_pid", language); 
    	JSONObject res = client.asr(filepath, "wav", 16000, map);
//    	System.out.println(res.toString());
    	if (!res.get("err_msg").equals("success.")) {
			return "";
		}
//    	System.out.println(res.get("err_msg"));
    	logger.info("***IdP***: 音频语音识别结果 "+res.get("result"));
    	String result_raw = res.get("result").toString();
    	
    	//格式化语音识别结果
    	if (language != GlobalConstant_server.ASR_ENGLISH) {
    		String result = result_raw.substring(2, result_raw.length()-2);
    		String codes = "";
			for(String temp : result.split("")) {
				if (temp.compareTo("0")>=0&&temp.compareTo("9")<=0) {
					codes+=temp;
				}else if (temp.matches(",|，|。|\"")) {
					continue;
				}else {
					return "";
				}
			}
			if (codes.length() == 6) {
				return codes;
			}else {
				return "";
			}
		}else {
			HashMap<String, Integer> dict = new HashMap<String, Integer>();
			dict.put("zero", 0);
			dict.put("one", 1);
			dict.put("two", 2);
			dict.put("three", 3);
			dict.put("four", 4);
			dict.put("five", 5);
			dict.put("six", 6);
			dict.put("seven", 7);
			dict.put("eight", 8);
			dict.put("nine", 9);
			String[] eng_res = result_raw.substring(2, result_raw.length()-2).split(" ");
			String eng_digit = "";
			for(String str : eng_res) {
//				System.out.println(str);
				if (dict.containsKey(str)) {
					eng_digit+=dict.get(str);
				}else {
//					System.out.println(eng_digit);
					return "";
				}
			}
			return eng_digit;
		}
	}

    public static void main(String[] args) {
//    	System.out.println(asr("/home/taotao/Downloads/xvector/wav_test_enroll/test/new_ENGLISH_1.wav",GlobalConstant_server.ASR_ENGLISH));
    	System.out.println(asr("/home/taotao/Downloads/xvector/wav_test_enroll.1/fengyue2/MANDARIN_2.wav", GlobalConstant_server.ASR_MANDARIN));
        // 初始化一个AipSpeech
//        AipSpeech client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);

//        // 可选：设置网络连接参数
//        client.setConnectionTimeoutInMillis(2000);
//        client.setSocketTimeoutInMillis(60000);
//
//        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
//        client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
//        client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
        // 也可以直接通过jvm启动参数设置此环境变量
//        System.setProperty("aip.log4j.conf", GlobalConstant_server.SPATH+"/asrlog/log4j.properties");
//        HashMap<String, Object> map = new HashMap<String,Object>();
//        map.put("dev_pid", 1537); 
//        map.put("dev_pid", 1837);
//        map.put("dev_pid", 1737);
        // 调用接口
//        JSONObject res = client.asr("/home/taotao/Downloads/xvector/wav_enroll/user1/1.wav", "wav", 16000, null);
//        System.out.println(res.toString(2));
//        String result = res.get("result").toString();
//        System.out.println(result.length());
//    	System.out.println(asr(GlobalConstant_server.SPATH+"/voicedata/12/test2.wav",GlobalConstant_server.ASR_LOCALISM));
//    	System.out.println(asr(GlobalConstant_server.SPATH+"/voicedata/11/enroll/enroll_2.wav",GlobalConstant_server.ASR_LOCALISM));
//    	System.out.println(asr("/home/taotao/my_project/eclipse-workspace/vprclient/voicedata/enroll_2.wav",GlobalConstant_server.ASR_LOCALISM));
//      System.out.println(asr("/home/taotao/my_project/eclipse-workspace/vprclient/voicedata/test_1.wav",GlobalConstant_server.ASR_MANDARIN));
//    	System.out.println(asr(GlobalConstant_server.SPATH+"/voicedata/enroll/1/enroll_3.wav",GlobalConstant_server.ASR_ENGLISH));
//    	System.out.println(asr("/home/taotao/Downloads/xvector/wav_enroll/user1/1.wav",GlobalConstant_server.ASR_LOCALISM));
    	
//    	String result_raw = "[\"967158就是想。\"]";
//    	String result = result_raw.substring(2, result_raw.length()-2);
//    	System.out.println(result);
//    	String codes = "";
//    	for(String temp : result.split("")) {
//			if (temp.compareTo("0")>=0&&temp.compareTo("9")<=0) {
//				codes+=temp;
//			}else if (temp.matches(",|，|。|\"")) {
//				continue;
//			}else {
//				System.out.println(temp);
//			}
//		}
    }
}
