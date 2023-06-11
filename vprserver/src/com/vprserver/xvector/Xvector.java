package com.vprserver.xvector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.vprserver.global.GlobalConstant_server;
public class Xvector {
	//工作空间路径
	private static String workspace = "/home/taotao/Downloads/xvector/";
	private static void exeCmd(String[] commandStr) {
		BufferedReader br = null;
	    try {
	    	Process p = Runtime.getRuntime().exec(commandStr,null,new File(workspace));
	    	br = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    	String line = null;
	    	StringBuilder sb = new StringBuilder();
	    	while ((line = br.readLine()) != null) {
	    		sb.append(line + "\n");
	    	}
//	    	System.out.println(sb.toString());
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    finally
	    {
	    	if (br != null)
	    	{
	    		try {
	    			br.close();
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    	}
	    }
	}
  
	/**
	 * 提取说话人x-vector
	 * @param username
	 * @param enroll_or_auth
	 */
	public static void extract_xvector(String username, String enroll_or_auth) {
		String voicepath = GlobalConstant_server.SPATH + "/voicedata/" + username +"/"+enroll_or_auth;
		String info_txt = voicepath+"/" + enroll_or_auth +".txt";
		
		//生成用于提取说话人x-vector的配置文件(包含音频路径以及对应的用户姓名)
		String[] generate_ini = new String[] {"python","generate_speaker.py",voicepath,info_txt};
		exeCmd(generate_ini);
		
		//提取说话人x-vector
		if (enroll_or_auth.equals(GlobalConstant_server.ENROLL)) {
			String[] extract = new String[] {"enroll.sh",info_txt,"2",username};
			exeCmd(extract);
		}else if (enroll_or_auth.equals(GlobalConstant_server.AUTHENTICATE)) {
			String[] extract = new String[] {"auth.sh",info_txt,"2",username};
			exeCmd(extract);
		}
		
	}
	
	/**
	 * 计算用户声纹认证的平均分数
	 * @param username
	 */
	public static double score_xvector(String username) {
		String[] score = new String[] {"score.sh",username};
		exeCmd(score);
		String[] result = readFileByLines(workspace+"data/" + username + "/scores/scores_adapt");
		double average = (Double.parseDouble(result[0]) + Double.parseDouble(result[1]))/2;
		if(Double.parseDouble(result[0])<10.0 | Double.parseDouble(result[1])<10.0) {
			average = 0;
		}
		return average;
	}
	
	/**
     * 以行为单位读取文件
     */
    public static String[] readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        String[] score = new String[2];
        try {
//            System.out.println("以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int cnt = 0;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                String[] temp = tempString.split(" ");
                score[cnt] = temp[temp.length-1];
//                System.out.println(score[cnt]);
                cnt++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
		return score;
    }
  
	public static void main(String[] args) {
//    	String[] commandStr = new String[]{"python","/home/taotao/Downloads/xvector/generate_speaker.py","/home/taotao/Downloads/xvector/wav_enroll","/home/taotao/Downloads/xvector/speaker111.txt"};
//		String[] commandStr = new String[]{"enroll.sh","speaker.txt","2","32"};
//	  	String[] commandStr = new String[]{"/home/taotao/Downloads/test.sh"};
//	  	Xvector.exeCmd(commandStr);
//		extract_xvector("32", GlobalConstant_server.ENROLL);
//		extract_xvector("32", GlobalConstant_server.AUTHENTICATE);
//		System.out.println(score_xvector("32"));
//		readFileByLines("/home/taotao/Downloads/xvector/data/32/scores/scores_adapt");
		extract_xvector("1", GlobalConstant_server.ENROLL);
		extract_xvector("1", GlobalConstant_server.AUTHENTICATE);
		System.out.println(score_xvector("1"));
	}
}

