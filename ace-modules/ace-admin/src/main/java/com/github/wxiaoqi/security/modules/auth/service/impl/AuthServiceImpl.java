package com.github.wxiaoqi.security.modules.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.wxiaoqi.security.api.vo.user.UserInfo;
import com.github.wxiaoqi.security.common.constant.RedisKeyConstant;
import com.github.wxiaoqi.security.common.exception.auth.UserInvalidException;
import com.github.wxiaoqi.security.common.msg.TableResultResponse;
import com.github.wxiaoqi.security.common.util.AddressUtils;
import com.github.wxiaoqi.security.common.util.IpUtils;
import com.github.wxiaoqi.security.common.util.Query;
import com.github.wxiaoqi.security.common.util.WebUtils;
import com.github.wxiaoqi.security.common.util.jwt.IJWTInfo;
import com.github.wxiaoqi.security.common.util.jwt.JWTInfo;
import com.github.wxiaoqi.security.modules.admin.entity.OnlineLog;
import com.github.wxiaoqi.security.modules.admin.entity.PlayList;
import com.github.wxiaoqi.security.modules.admin.entity.User;
import com.github.wxiaoqi.security.modules.admin.mapper.PlayListMapper;
import com.github.wxiaoqi.security.modules.admin.mapper.UserMapper;
import com.github.wxiaoqi.security.modules.admin.rpc.service.PermissionService;
import com.github.wxiaoqi.security.modules.auth.service.AuthService;
import com.github.wxiaoqi.security.modules.auth.util.user.JwtAuthenticationRequest;
import com.github.wxiaoqi.security.modules.auth.util.user.JwtTokenUtil;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private PlayListMapper playListMapper;

    @Value("${jwt.expire}")
    private int expire;


    @Override
    public Map login(JwtAuthenticationRequest authenticationRequest) throws Exception {
        UserInfo info = permissionService.validate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        if (!StringUtils.isEmpty(info.getId())) {
            JWTInfo jwtInfo = new JWTInfo(info.getUsername(), info.getId() + "", info.getName());
            String token = jwtTokenUtil.generateToken(jwtInfo);
            Map<String, String> result = new HashMap<>();
            result.put("accessToken", token);
            result.put("id", info.id);
            WebUtils.getRequest();
            writeOnlineLog(jwtInfo);
            return result;
        }
        throw new UserInvalidException("用户不存在或账户密码错误!");
    }

    @Override
    public Map signup(JwtAuthenticationRequest authenticationRequest) throws Exception {
        //根据姓名查询人员
        User user = userMapper.selectUserByUserName(authenticationRequest.getUsername());
        if (user == null){
            //保存新用户
            UserInfo userInfo = permissionService.insertUser(authenticationRequest.getUsername(), authenticationRequest.getPassword());
            if (userInfo == null) {
                throw new UserInvalidException("注册失败，请重试!");
            }
            return null;
        }
        throw new UserInvalidException("用户已存在!");
        /*
        UserInfo info = permissionService.validate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        if (!StringUtils.isEmpty(info.getId())) {
            JWTInfo jwtInfo = new JWTInfo(info.getUsername(), info.getId() + "", info.getName());
            String token = jwtTokenUtil.generateToken(jwtInfo);
            Map<String, String> result = new HashMap<>();
            result.put("accessToken", token);
            result.put("id", info.id);
            WebUtils.getRequest();
            writeOnlineLog(jwtInfo);
            return result;
        }
        throw new UserInvalidException("用户不存在或账户密码错误!");*/
    }

    @Override
    public void validate(String token) throws Exception {
        jwtTokenUtil.getInfoFromToken(token);
    }

    @Override
    public String refresh(String oldToken) throws Exception {
        return jwtTokenUtil.generateToken(jwtTokenUtil.getInfoFromToken(oldToken));
    }

    @Override
    public void logout(String token) throws Exception {
        IJWTInfo infoFromToken = jwtTokenUtil.getInfoFromToken(token);
        String tokenId = infoFromToken.getTokenId();
        stringRedisTemplate.delete(RedisKeyConstant.REDIS_KEY_TOKEN + ":" + tokenId);
        stringRedisTemplate.opsForZSet().remove(RedisKeyConstant.REDIS_KEY_TOKEN, tokenId);
    }

    @Override
    public void logicDeleteById(int id) throws Exception {
        userMapper.logicDeleteById(id);
    }

//    @Override
//    public List<PlayList> getPlayLists(int limit, int offset){
//
//        return null;
//    }

    /*@Override
    public TableResultResponse<PlayList> getPlayList(Query query) {
//        Class<T> clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
//        Example example = new Example(clazz);
//        if(query.entrySet().size()>0) {
//            Example.Criteria criteria = example.createCriteria();
//            for (Map.Entry<String, Object> entry : query.entrySet()) {
//                criteria.andLike(entry.getKey(), "%" + entry.getValue().toString() + "%");
//            }
//        }


//        Page<Object> result = PageHelper.startPage(query.getPage(), query.getLimit());
//        List<PlayList> playList = playListMapper.getPlayList(query.getLimit(),query.getPage());
//        return new TableResultResponse<PlayList>(result.getTotal(), playList);
    }*/

/*    @Override
    public List<PlayList> getPlayLists() {
        List<PlayList> playList = playListMapper.getPlayLists();
        return playList;
    }*/

    @Async
    public void writeOnlineLog(JWTInfo jwtInfo) {
        final UserAgent userAgent = UserAgent.parseUserAgentString(WebUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getRemoteIP(WebUtils.getRequest());
        String address = AddressUtils.getRealAddressByIP(ip);

        OnlineLog onlineLog = new OnlineLog();
        // 获取客户端操作系统
        String os = userAgent.getOperatingSystem().getName();
        // 获取客户端浏览器
        String browser = userAgent.getBrowser().getName();
        onlineLog.setBrowser(browser);
        onlineLog.setIpaddr(ip);
        onlineLog.setTokenId(jwtInfo.getTokenId());
        onlineLog.setLoginTime(System.currentTimeMillis());
        onlineLog.setUserId(jwtInfo.getId());
        onlineLog.setUserName(jwtInfo.getName());
        onlineLog.setLoginLocation(address);
        onlineLog.setOs(os);
        stringRedisTemplate.opsForValue().set(RedisKeyConstant.REDIS_KEY_TOKEN + ":" + jwtInfo.getTokenId(), JSON.toJSONString(onlineLog, false), expire, TimeUnit.MINUTES);
        stringRedisTemplate.opsForZSet().add((RedisKeyConstant.REDIS_KEY_TOKEN), jwtInfo.getTokenId(), 0);
    }

}
