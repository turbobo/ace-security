package com.github.wxiaoqi.security.modules.auth.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.wxiaoqi.security.common.biz.BaseBiz;
import com.github.wxiaoqi.security.common.constant.RedisKeyConstant;
import com.github.wxiaoqi.security.common.exception.auth.UserInvalidException;
import com.github.wxiaoqi.security.common.msg.ObjectRestResponse;
import com.github.wxiaoqi.security.common.msg.TableResultResponse;
import com.github.wxiaoqi.security.common.util.Query;
import com.github.wxiaoqi.security.modules.auth.service.AuthService;
import com.github.wxiaoqi.security.modules.auth.util.user.JwtAuthenticationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.github.wxiaoqi.security.common.constant.RedisKeyConstant.REDIS_KEY_CAPTCHA;

@RestController
@RequestMapping("jwt")
@Slf4j
public class AuthController<Biz extends BaseBiz,Entity> {
    @Autowired
    protected HttpServletRequest request;

    @Value("${jwt.token-header}")
    private String tokenHeader;

    @Autowired
    private AuthService authService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping(value = "token", method = RequestMethod.POST)
    public ObjectRestResponse<String> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest) throws Exception {

        log.info(authenticationRequest.getUsername() + " require logging...");
        // 获取session中的验证码
        String sessionCode = stringRedisTemplate.opsForValue().get(String.format(REDIS_KEY_CAPTCHA, authenticationRequest.getUuid()));
        if(sessionCode == null){
            throw new UserInvalidException("验证码已过期");
        }
        // 判断验证码
        if (authenticationRequest.getVerCode() == null || !sessionCode.equals(authenticationRequest.getVerCode().trim().toLowerCase())) {
            throw new UserInvalidException("验证码不正确");
        }
        Map result = authService.login(authenticationRequest);

        return new ObjectRestResponse<>().data(result);
    }

    @RequestMapping(value = "refresh", method = RequestMethod.GET)
    public ObjectRestResponse<String> refreshAndGetAuthenticationToken(
            HttpServletRequest request) throws Exception {
        String token = request.getHeader(tokenHeader);
        String refreshedToken = authService.refresh(token);
        return new ObjectRestResponse<>().data(refreshedToken);
    }

    @RequestMapping(value = "verify", method = RequestMethod.GET)
    public ObjectRestResponse<?> verify(String token) throws Exception {
        authService.validate(token);
        return new ObjectRestResponse<>();
    }

    @RequestMapping(value = "logout", method = RequestMethod.DELETE)
    public ObjectRestResponse<?> logout(String token) throws Exception {
        authService.logout(token);
        return new ObjectRestResponse<>();
    }

    @RequestMapping(value = "signup", method = RequestMethod.POST)
    public ObjectRestResponse<String> signup(@RequestBody JwtAuthenticationRequest authenticationRequest) throws Exception {

        log.info(authenticationRequest.getUsername() + " require logging...");
        // 获取session中的验证码
        String sessionCode = stringRedisTemplate.opsForValue().get(String.format(REDIS_KEY_CAPTCHA, authenticationRequest.getUuid()));
        if(sessionCode == null){
            throw new UserInvalidException("验证码已过期");
        }
        // 判断验证码
        if (authenticationRequest.getVerCode() == null || !sessionCode.equals(authenticationRequest.getVerCode().trim().toLowerCase())) {
            throw new UserInvalidException("验证码不正确");
        }
        //注册
//        Map result = authService.login(authenticationRequest);
        Map result = authService.signup(authenticationRequest);

        return new ObjectRestResponse<>().data(result);
    }

    @RequestMapping(value = "logic/{id}",method = RequestMethod.DELETE)
    @ResponseBody
    public ObjectRestResponse<String> remove(@PathVariable int id) throws Exception {
        //逻辑删除
        authService.logicDeleteById(id);
        return new ObjectRestResponse<String>();
    }

    /*@RequestMapping(value="music/top", method = RequestMethod.GET)
    @ResponseBody
//    public TableResultResponse<PlayList> getPlayList(@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "1") int offset) {
    public TableResultResponse<PlayList> getPlayList(@RequestParam Map<String, Object> params) {
//        stringRedisTemplate.opsForValue();
//        //倒序查询分页的ids
//        Set<String> ids = stringRedisTemplate.opsForZSet().reverseRange(RedisKeyConstant.REDIS_KEY_TOKEN, (offset - 1) * limit, (offset - 1) * limit + limit - 1);
//
//        List<PlayList> playLists = new ArrayList<>(ids.size());
//        for (String id : ids) {
//            String s = stringRedisTemplate.opsForValue().get(RedisKeyConstant.REDIS_KEY_TOKEN + ":" + id);
//            if (s == null) {
//                stringRedisTemplate.opsForZSet().remove(RedisKeyConstant.REDIS_KEY_TOKEN, id);
//            } else {
//                playLists.add(JSON.parseObject(s, PlayList.class));
//            }
//        }

//        String playLists = stringRedisTemplate.opsForValue().get("playLists");
//        if (playLists != null && !playLists.isEmpty()){
//            return new TableResultResponse<>(stringRedisTemplate.opsForZSet().size(RedisKeyConstant.REDIS_KEY_TOKEN), playLists);
//        }


        //        return new TableResultResponse<>(stringRedisTemplate.opsForZSet().size(RedisKeyConstant.REDIS_KEY_TOKEN), playLists);


*//*        //查询列表数据
        Map<String, Object> params = null;
        List<PlayList> playList = authService.getPlayList(limit,offset);
        Query query = new Query(params);
        query.setLimit(5);
        query.setPage(1);
        //        return baseBiz.selectByQuery(query);
        Page<Object> result = PageHelper.startPage(query.getPage(), query.getLimit());
        return new TableResultResponse<PlayList>(result.getTotal(), playList);*//*

        //查询列表数据
//        Map<String, Object> params = new LinkedHashMap<>();
//        params.put("page","1");
//        params.put("limit","5");
        Query query = new Query(params);
//        return authService.getPlayList(limit,offset,query);
        List<PlayList> playLists = authService.getPlayLists();
        return  new TableResultResponse<PlayList>(playLists.size(), playLists);

    }*/

//    @Autowired
//    protected MusicBiz musicBiz;

    /**
     * 查询所有歌曲
     * @param params
     * @return
     */
/*    @RequestMapping(value = "/music/page",method = RequestMethod.GET)
    @ResponseBody
    public TableResultResponse<Entity> getSongList(@RequestParam Map<String, Object> params){
        //查询列表数据
        Query query = new Query(params);
        return (TableResultResponse<Entity>) musicBiz.selectByQuery(query);
    }*/
}
