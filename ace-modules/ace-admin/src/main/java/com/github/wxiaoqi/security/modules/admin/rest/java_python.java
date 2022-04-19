package com.github.wxiaoqi.security.modules.admin.rest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;

public class java_python {

	 public static void main(String[] args) {
         File csv = new File("D:\\IdeaSpace\\PythonMusicRecommend\\LibRecommender-master\\test\\modelSave\\deepfm_model_20\\songsSave.csv");
         csv.setReadable(true);
         csv.setWritable(true);
         InputStreamReader isr = null;
         BufferedReader br = null;
         try {
             isr = new InputStreamReader(new FileInputStream(csv), "UTF-8");
             br = new BufferedReader(isr);
         } catch (Exception e) {
             e.printStackTrace();
         }
         String line = "";
         ArrayList<String> records = new ArrayList<>();
         try { //读取表头  br.readLine()
             while ((line = br.readLine()) != null) {
                 System.out.println(line);
                 records.add(line);
             }
             System.out.println("csv表格读取行数：" + records.size());
         } catch (IOException e) {
             e.printStackTrace();
         }


//         String set = stringRedisTemplate.opsForValue().get("set");

         Scanner input = new Scanner(System.in);
//         System.out.println("请输入需要传递给python的参数");
         String userId = "input.nextLine()";
//         String a = "jia";
//         System.out.println(integers);
//         String url="http://blog.csdn.net/thorny_v/article/details/61417386";
         String[] cmds = new String[]{"python",
                 "D:\\IdeaSpace\\PythonMusicRecommend\\LibRecommender-master\\test\\04-19\\testLoad.py",
                 userId};
//         System.out.println(cmds);
         System.out.println("调用python程序");
         Process pcs;
         try {
             pcs = Runtime.getRuntime().exec(cmds);
             pcs.waitFor();
             // 定义Python脚本的返回值
             String result = null;
             // 获取CMD的返回流
             BufferedInputStream in = new BufferedInputStream(pcs.getInputStream());
             // 字符流转换字节流
             BufferedReader br2 = new BufferedReader(new InputStreamReader(in));
             // 这里也可以输出文本日志
             String lineStr = null;
//             System.out.println(br.readLine());
             while ((lineStr = br2.readLine()) != null) {
                 result = lineStr;
//                 System.out.println(br.readLine());
                 System.out.println(result);
             }
//
             // 关闭输入流
             br.close();
             in.close();
 
 
         } catch (IOException | InterruptedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
 
 
 }
 
}