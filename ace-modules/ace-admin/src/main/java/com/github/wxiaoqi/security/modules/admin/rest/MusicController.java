package com.github.wxiaoqi.security.modules.admin.rest;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.wxiaoqi.security.common.biz.BaseBiz;
import com.github.wxiaoqi.security.common.constant.RedisKeyConstant;
import com.github.wxiaoqi.security.common.msg.ObjectRestResponse;
import com.github.wxiaoqi.security.common.msg.TableResultResponse;
import com.github.wxiaoqi.security.common.util.Query;
import com.github.wxiaoqi.security.modules.admin.entity.PlayList;
import com.github.wxiaoqi.security.modules.auth.biz.MusicBiz;
import com.github.wxiaoqi.security.modules.auth.entity.Song;
import com.github.wxiaoqi.security.modules.auth.service.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @Author Jusven
 * @Date 2022/3/28 15:53
 */
@RestController
@RequestMapping("music")
@Slf4j
public class MusicController<Biz extends BaseBiz,Entity> {

    @Autowired
    private MusicService musicService;

    @Autowired
    protected MusicBiz musicBiz;

    @RequestMapping(value="/top", method = RequestMethod.GET)
    @ResponseBody
//    public TableResultResponse<PlayList> getPlayList(@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "1") int offset) {
    public TableResultResponse<PlayList> getPlayList(@RequestParam Map<String, Object> params) {
        /*stringRedisTemplate.opsForValue();
        //倒序查询分页的ids
        Set<String> ids = stringRedisTemplate.opsForZSet().reverseRange(RedisKeyConstant.REDIS_KEY_TOKEN, (offset - 1) * limit, (offset - 1) * limit + limit - 1);

        List<PlayList> playLists = new ArrayList<>(ids.size());
        for (String id : ids) {
            String s = stringRedisTemplate.opsForValue().get(RedisKeyConstant.REDIS_KEY_TOKEN + ":" + id);
            if (s == null) {
                stringRedisTemplate.opsForZSet().remove(RedisKeyConstant.REDIS_KEY_TOKEN, id);
            } else {
                playLists.add(JSON.parseObject(s, PlayList.class));
            }
        }

        String playLists = stringRedisTemplate.opsForValue().get("playLists");
        if (playLists != null && !playLists.isEmpty()){
            return new TableResultResponse<>(stringRedisTemplate.opsForZSet().size(RedisKeyConstant.REDIS_KEY_TOKEN), playLists);
        }


                return new TableResultResponse<>(stringRedisTemplate.opsForZSet().size(RedisKeyConstant.REDIS_KEY_TOKEN), playLists);


        //查询列表数据
        Map<String, Object> params = null;
        List<PlayList> playList = authService.getPlayList(limit,offset);
        Query query = new Query(params);
        query.setLimit(5);
        query.setPage(1);
        //        return baseBiz.selectByQuery(query);
        Page<Object> result = PageHelper.startPage(query.getPage(), query.getLimit());
        return new TableResultResponse<PlayList>(result.getTotal(), playList);

        //查询列表数据
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("page","1");
        params.put("limit","5");*/
        Query query = new Query(params);
//        return authService.getPlayList(limit,offset,query);
        List<PlayList> playLists = musicService.getPlayLists();
        return  new TableResultResponse<PlayList>(playLists.size(), playLists);

    }

    /**
     * 查询所有歌曲
     * @param params
     * @return
     */
    @RequestMapping(value = "/page",method = RequestMethod.GET)
    @ResponseBody
    public TableResultResponse<Song> list(@RequestParam Map<String, Object> params){
        //查询列表数据
        Query query = new Query(params);
//        return (TableResultResponse<Entity>) musicBiz.selectByQuery(query);

        List<Song> songList = musicService.getSongList(query);
        return new TableResultResponse<Song>(songList.size(), songList);
    }

    /**
     * 新增歌曲
     * @param entity
     * @return
     */
    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public ObjectRestResponse<Entity> add(@RequestBody Song entity){
//        musicBiz.insertSelective((Song) entity);
        musicService.insertSelectiveMine((Song) entity);
        return new ObjectRestResponse<Entity>();
    }

    @RequestMapping(value = "/getSong/{id}",method = RequestMethod.GET)
    @ResponseBody
    public ObjectRestResponse<Entity> getSong(@PathVariable int id){
        //track_id
        ObjectRestResponse<Entity> entityObjectRestResponse = new ObjectRestResponse<>();
        Object o = musicService.selectById(id);
        entityObjectRestResponse.data((Entity)o);
        return entityObjectRestResponse;
    }

    @RequestMapping(value = "/logicDelete/{id}",method = RequestMethod.DELETE)
    @ResponseBody
    public ObjectRestResponse<String> remove(@PathVariable int id) throws Exception {
        //逻辑删除
        musicService.logicDeleteById(id);
        return new ObjectRestResponse<String>();
    }


    @RequestMapping(value = "/update",method = RequestMethod.PUT)
    @ResponseBody
    public ObjectRestResponse<Entity> update(@RequestBody Song song){
        musicService.updateSelectiveByEntity(song);
        return new ObjectRestResponse<Entity>();
    }
}
