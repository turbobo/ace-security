package com.github.wxiaoqi.security.modules.auth.controller;

import com.alibaba.fastjson.JSON;
import com.github.wxiaoqi.security.common.constant.RedisKeyConstant;
import com.github.wxiaoqi.security.common.msg.TableResultResponse;
import com.github.wxiaoqi.security.common.util.Query;
import com.github.wxiaoqi.security.modules.admin.entity.PlayList;
import com.github.wxiaoqi.security.modules.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author Jusven
 * @Date 2022/3/28 15:53
 */
@RestController
@RequestMapping("music")
@Slf4j
public class MusicController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private AuthService authService;

    @RequestMapping("top")
    public TableResultResponse<PlayList> getPlayList(@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "1") int offset) {
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


        //查询列表数据
/*        Map<String, Object> params = null;
        List<PlayList> playList = authService.getPlayList(limit,offset);
        Query query = new Query(params);
        query.setLimit(5);
        query.setPage(1);
        //        return baseBiz.selectByQuery(query);
        Page<Object> result = PageHelper.startPage(query.getPage(), query.getLimit());
        return new TableResultResponse<PlayList>(result.getTotal(), playList);*/

        //查询列表数据
        Map<String, Object> params = null;
        Query query = new Query(params);
        return authService.getPlayList(query);

    }
}
