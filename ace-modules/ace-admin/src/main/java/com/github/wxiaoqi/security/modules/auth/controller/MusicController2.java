//package com.github.wxiaoqi.security.modules.auth.controller;
//
//import com.github.wxiaoqi.security.common.biz.BaseBiz;
//import com.github.wxiaoqi.security.common.msg.TableResultResponse;
//import com.github.wxiaoqi.security.common.util.Query;
//import com.github.wxiaoqi.security.modules.auth.biz.MusicBiz;
//import com.github.wxiaoqi.security.modules.auth.service.MusicService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
///**
// * @Author Jusven
// * @Date 2022/3/28 15:53
// */
//@RestController
//@RequestMapping("music2")
//@Slf4j
//public class MusicController2<Biz extends BaseBiz,Entity> {
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
//
//    @Autowired
//    private MusicService musicService;
//
//    @Autowired
//    protected MusicBiz musicBiz;
//
//    /**
//     * 查询所有歌曲
//     * @param params
//     * @return
//     */
//    @RequestMapping(value = "/page",method = RequestMethod.GET)
//    @ResponseBody
//    public TableResultResponse<Entity> list(@RequestParam Map<String, Object> params){
//        //查询列表数据
//        Query query = new Query(params);
//        return (TableResultResponse<Entity>) musicBiz.selectByQuery(query);
//    }
//}
