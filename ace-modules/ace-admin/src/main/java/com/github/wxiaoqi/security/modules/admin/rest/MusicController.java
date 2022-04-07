package com.github.wxiaoqi.security.modules.admin.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.wxiaoqi.security.common.biz.BaseBiz;
import com.github.wxiaoqi.security.common.msg.ObjectRestResponse;
import com.github.wxiaoqi.security.common.msg.TableResultResponse;
import com.github.wxiaoqi.security.common.util.Query;
import com.github.wxiaoqi.security.modules.admin.entity.PlayList;
import com.github.wxiaoqi.security.modules.admin.util.HttpClientUtil;
import com.github.wxiaoqi.security.modules.admin.util.HttpsClientUtil;
import com.github.wxiaoqi.security.modules.admin.vo.SongVo;
import com.github.wxiaoqi.security.modules.auth.biz.MusicBiz;
import com.github.wxiaoqi.security.modules.auth.entity.Song;
import com.github.wxiaoqi.security.modules.auth.service.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.web.Link;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 将个性化推荐列表和用户id绑定存入redis
     * 个性化推荐列表的Song和热门推荐表结构不同！！！！！！！！！！！！！！！！！！！！！！！！！！
     * @param params
     * @return
     */
    @RequestMapping(value="/personalized", method = RequestMethod.GET)
    @ResponseBody
//    public TableResultResponse<PlayList> getPlayList(@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "1") int offset) {
    public TableResultResponse<PlayList> getPersonalizedSong(@RequestParam Map<String, Object> params) {
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

    //请求热门歌曲列表，并保存至redis
    @RequestMapping(value="/top", method = RequestMethod.GET)
    @ResponseBody
//    public TableResultResponse<PlayList> getPlayList(@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "1") int offset) {
    public TableResultResponse<SongVo> getTopSong() throws UnsupportedEncodingException {
        //获取redis热门列表
        String topSongListRedis = stringRedisTemplate.opsForValue().get("topSongListRedis");
        List<SongVo> topSongList = new ArrayList<>();
        //初始化列表
        if (topSongListRedis == null) {
            String url = "http://ws.audioscrobbler.com/2.0/?method=chart.gettoptracks&api_key=7537459f592d916b49e697b8a0fb53df&format=json";
            String jsonStr = "{xxx}";
            String httpOrgCreateTestRtn = HttpClientUtil.doPost(url, jsonStr, "utf-8");
            JSONObject jsonObject = JSONObject.parseObject(httpOrgCreateTestRtn);
            JSONObject tracks = (JSONObject) jsonObject.get("tracks");
            JSONArray trackArray = tracks.getJSONArray("track");
            //返回结果
            JSONArray resultArray = new JSONArray();
            LinkedList<SongVo> songVoLinkedList = new LinkedList<>();
            for (int i = 0; i < trackArray.size(); i++) {
                JSONObject tempObj = (JSONObject) trackArray.get(i);
                JSONObject artist = (JSONObject) tempObj.get("artist");
//            JSONObject artistName = tempObj.get("artist");
                //返回的单个对象
//                JSONObject tempRes = new JSONObject();
                SongVo songVo = new SongVo();

//                tempRes.put("title", tempObj.get("name").toString());
//                tempRes.put("artistName", artist.get("name").toString());
                songVo.setTitle(tempObj.get("name").toString());
                songVo.setArtistName(artist.get("name").toString());

          /*  //请求专辑封面
            String url_album = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=7537459f592d916b49e697b8a0fb53df&artist=" + tempRes.get("artistName").toString() + "&track=" + tempRes.get("title").toString();
            String jsonStr_album = "{xxx}";
            String res_album = HttpClientUtil.doPost(url_album, jsonStr_album, "utf-8");
            JSONObject json_album = JSONObject.parseObject(res_album);*/


                //直接请求qq专辑图片 -- await同步请求
                //参数--歌名 包含空格无法正常解析
//                String title = URLEncoder.encode(tempRes.get("title").toString(), "utf-8");
                String title = URLEncoder.encode(songVo.getTitle(), "utf-8");

                String url_album = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&n=2&w=" + title + "&format=json&t=8";
                JSONObject jsonObject2 = HttpsClientUtil.getHttpsReq(url_album);
                JSONObject data2 = (JSONObject) jsonObject2.get("data");
                JSONObject album = (JSONObject) data2.get("album");
                JSONArray albumList = album.getJSONArray("list");
                //匹配歌曲
                for (int j = 0; j < albumList.size(); j++) {
                    JSONObject albumObj = (JSONObject) albumList.get(j);
//                    if (tempRes.get("artistName").equals(albumObj.get("singerName").toString())) { //匹配歌手
                    if (songVo.getArtistName().equals(albumObj.get("singerName").toString())) { //匹配歌手
//                        tempRes.put("albumPic", albumObj.get("albumPic").toString());
//                        tempRes.put("release", albumObj.get("albumName").toString());
                        songVo.setRelease(albumObj.get("albumName").toString());
                        songVo.setAlbumPic(albumObj.get("albumPic").toString());
                        break;
                    }
                    if (j == albumList.size() - 1) {
                        albumObj = (JSONObject) albumList.get(0);
//                        tempRes.put("albumPic", albumObj.get("albumPic").toString());
//                        tempRes.put("release", albumObj.get("albumName").toString());
                        songVo.setRelease(albumObj.get("albumName").toString());
                        songVo.setAlbumPic(albumObj.get("albumPic").toString());
                    }
                }
//                resultArray.add(tempRes);

                songVoLinkedList.add(songVo);
            }
            stringRedisTemplate.opsForValue().set("topSongListRedis", JSON.toJSONString(songVoLinkedList), 12, TimeUnit.HOURS);
            topSongList = songVoLinkedList;
        }
        else {
            topSongList = JSON.parseArray(topSongListRedis, SongVo.class);
        }

        /*(1)List转换为JSONArray

        List<T> list = new ArrayList<T>();
        JSONArray array= JSONArray.parseArray(JSON.toJSONString(list))；

        (2)JSONArray转换为List

        JSONArray array = new JSONArray();
        List<EventColAttr> list = JSONObject.parseArray(array.toJSONString(), EventColAttr.class);*/


//        Query query = new Query(params)

//        return authService.getPlayList(limit,offset,query);
//        List<PlayList> playLists = musicService.getPlayLists();
        return  new TableResultResponse<SongVo>(topSongList.size(), topSongList);

    }

    //请求热门歌曲列表，并保存至redis
    @RequestMapping(value="/topDuration", method = RequestMethod.GET)
    @ResponseBody
//    public TableResultResponse<PlayList> getPlayList(@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "1") int offset) {
    public TableResultResponse<SongVo> gettopDuration() throws UnsupportedEncodingException {
        //redis中获取有时长的热门列表
        String topSongDurationStringRedis = stringRedisTemplate.opsForValue().get("topSongDurationListRedis");
        //返回集合
        List<SongVo> topSongDurationListRedis = new LinkedList<>();
        if (topSongDurationStringRedis == null){
            String topSongStringRedis = stringRedisTemplate.opsForValue().get("topSongListRedis");
            List<SongVo> topSongListRedis = JSON.parseArray(topSongStringRedis, SongVo.class);
            for (int i = 0; i < topSongListRedis.size(); i++) {
                SongVo songVo = topSongListRedis.get(i);
                //请求歌曲时长
                //参数--歌名 包含空格无法正常解析
                String title = URLEncoder.encode(songVo.getTitle(), "utf-8");
                String url_duration = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&n=2&w=" + title + "&format=json";
                JSONObject songObjectList = HttpsClientUtil.getHttpsReq(url_duration);
                JSONObject data = (JSONObject) songObjectList.get("data");
                if (data != null) {
                    JSONObject song = (JSONObject) data.get("song");
                    if (song != null) {
                        JSONArray songList = song.getJSONArray("list");
                        for (int l = 0; l < songList.size(); l++) {  //匹配歌曲
                            JSONObject jsonObject1 = (JSONObject) songList.get(l);
                            JSONArray singer = jsonObject1.getJSONArray("singer");
                            for (int m = 0; m < singer.size(); m++) {  //匹配歌手
                                JSONObject o = (JSONObject) singer.get(m);
                                if (o.get("name").equals(songVo.getArtistName())) {
//                                tempRes.put("duration", jsonObject1.get("interval"));
                                    songVo.setDuration((Integer) jsonObject1.get("interval"));
                                    break;
                                }
                            }

                            //匹配不到，则使用第一个专辑：已经最后一个，还没匹配到
                            if (l == songList.size() - 1) {
                                jsonObject1 = (JSONObject) songList.get(0);
//                            tempRes.put("duration", jsonObject1.get("interval"));
                                songVo.setDuration((Integer) jsonObject1.get("interval"));
                            }
                            if (songVo.getDuration() != null) {
                                break;
                            }
                        }
                    }
                }
                //songVo原有数据仍保留
                topSongDurationListRedis.add(songVo);
            }
            //存入redis
            stringRedisTemplate.opsForValue().set("topSongDurationListRedis", JSON.toJSONString(topSongDurationListRedis), 12, TimeUnit.HOURS);
        } else {
            topSongDurationListRedis = JSON.parseArray(topSongDurationStringRedis, SongVo.class);
        }
        return  new TableResultResponse<SongVo>(topSongDurationListRedis.size(), topSongDurationListRedis);
    }
}
