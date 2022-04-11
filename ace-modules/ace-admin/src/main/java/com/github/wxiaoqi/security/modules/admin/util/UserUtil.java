package com.github.wxiaoqi.security.modules.admin.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wxiaoqi.security.common.util.jwt.JWTInfo;
import com.github.wxiaoqi.security.modules.admin.entity.User;
import com.github.wxiaoqi.security.modules.auth.util.user.JwtAuthenticationRequest;
import com.github.wxiaoqi.security.modules.auth.util.user.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author Jusven
 * @Date 2021/7/16 20:45
 */
public class UserUtil {
    static Sha256PasswordEncoder encoder = new Sha256PasswordEncoder();

//    @Autowired
    static JwtTokenUtil jwtTokenUtil2;

    public static void main(String[] args) throws Exception {
        createUser(5000);
        encoder.encode("admin");

    }

    public static void createUser(int count) throws Exception {
        List<User> userList = new ArrayList<User>(count);
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId(5000 + i);    //修改手机号验证正则表达式 ValidatorUtil
            user.setUsername("user" + i);
            user.setPassword(encoder.encode("admin"));
            user.setStatus("1");
            userList.add(user);
        }
        System.out.println("create user");
        Connection conn = getConn();
        String sql = "insert into base_user(id, username, password, status) values(?,?,?,?)";
        PreparedStatement preSta = conn.prepareStatement(sql);
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            preSta.setInt(1, user.getId());
            preSta.setString(2, user.getUsername());
            preSta.setString(3, user.getPassword());
            preSta.setString(4, user.getStatus());
            preSta.addBatch();   //添加执行批
        }
        preSta.executeBatch();  //批量执行
        preSta.clearParameters();
        conn.close();
        System.out.println("insert into db");
        //登录，生成userTicket
        String urlString = "http://localhost:8765/api/auth/jwt/token";
//        File file = new File("D:\\IdeaSpace\\Spring-Cloud-Platform\\user_config.txt");
        File file = new File("D:\\IdeaSpace\\Spring-Cloud-Platform\\user.txt");
        if (file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(0);
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            /*URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection) url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream out = co.getOutputStream();
//            String params = "username=" + user.getUsername() + "&password=admin&verCode=&uuid=";
//            out.write(params.getBytes());

//            JwtAuthenticationRequest jwtAuthenticationRequest = new JwtAuthenticationRequest(user.getUsername(),user.getPassword(),"",user.getId());
//            out.write(jwtAuthenticationRequest.ge);
            out.flush();
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0, len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
            ObjectMapper mapper = new ObjectMapper();
            JWTInfo jwtInfo = new JWTInfo(user.getUsername(), user.getId() + "", user.getName());
            String token = jwtTokenUtil2.generateToken(jwtInfo);
//            ResponseBean responseBean = mapper.readValue(response, ResponseBean.class);
//            String userTicket = (String) responseBean.getObj();
            System.out.println("create userTicket : " + user.getId());
            String row = user.getId() + "," +token;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file" + user.getId());*/

            //将用户名和密码写入文件
            String row = user.getId() + "," +user.getUsername() + "," +"admin";
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
        }
        raf.close();
        System.out.println("over");
    }

    public static Connection getConn() throws Exception {
//        String url = "jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        String url = "jdbc:mysql://localhost:3306/cloud_admin_v1?useUnicode=true&characterEncoding=UTF8&serverTimezone=UTC";
        String userName = "root";
        String password = "";
        String driver = "com.mysql.jdbc.Driver";
//        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url,userName,password);
    }
}
