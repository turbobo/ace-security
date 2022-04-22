package com.github.wxiaoqi.security.modules.admin.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.wxiaoqi.security.common.biz.BaseBiz;
import com.github.wxiaoqi.security.common.msg.ObjectRestResponse;
import com.github.wxiaoqi.security.common.msg.TableResultResponse;
import com.github.wxiaoqi.security.common.util.Query;
import com.github.wxiaoqi.security.modules.admin.rpc.service.PermissionService;
import com.github.wxiaoqi.security.modules.admin.util.HttpClientUtil;
import com.github.wxiaoqi.security.modules.admin.util.HttpsClientUtil;
import com.github.wxiaoqi.security.modules.admin.vo.SongVo;
import com.github.wxiaoqi.security.modules.auth.biz.MusicBiz;
import com.github.wxiaoqi.security.modules.auth.entity.Song;
import com.github.wxiaoqi.security.modules.auth.service.MusicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
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

    @Autowired
    private PermissionService permissionService;

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
        query.put("page",params.get("page"));
        query.put("limit",params.get("limit"));
        List<Song> songList = musicService.getSongList(query);
        int page = query.getPage();
        int limit = query.getLimit();
        int start = (page -1) * page;
        int end = start + limit;
        if (end > songList.size()) {
            end = songList.size();
        }
        return new TableResultResponse<Song>(songList.size(), songList.subList(start,end));
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

    /**
     * 将个性化推荐列表和用户id绑定存入redis
     * 个性化推荐列表的Song和热门推荐表结构不同！！！！！！！！！！！！！！！！！！！！！！！！！！
     * @param params
     * @return
     */
    @RequestMapping(value="/personalized", method = RequestMethod.GET)
    @ResponseBody
//    public TableResultResponse<PlayList> getPlayList(@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "1") int offset) {
    public TableResultResponse<SongVo> getPersonalizedSong(@RequestParam Map<String, Object> params) throws IOException {
        LinkedList<SongVo> songVoLinkedList = new LinkedList<>();
        List<SongVo> songVoLinkedListRes = new LinkedList<>();
        String userName = (String) params.get("userName");
        //python推荐结果
        String songsSave = stringRedisTemplate.opsForValue().get("songsSave"+userName);
        //完整推荐结果
//        String songsRec = stringRedisTemplate.opsForValue().get("songsRec"+userName);
        if (songsSave == null){
            //调用python程序推荐
            String[] cmds = new String[]{"python",
                    "D:\\IdeaSpace\\PythonMusicRecommend\\LibRecommender-master\\test\\04-19\\testLoad.py",
                    userName};
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
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                // 这里也可以输出文本日志
                String lineStr = null;
//                StringBuilder sb = new StringBuilder();
//             System.out.println(br.readLine());
                while ((lineStr = br.readLine()) != null) {
//                 System.out.println(br.readLine());
                    if (lineStr.contains("recommendation")){
                        result = lineStr;
                        break;
                    }
//                    System.out.println(result);
                }
                String[] split = result.split(":");
                String result2 = split[1];
                String substring = result2.substring(2, result2.length() - 1);
                String[] split1 = substring.split(",");
                LinkedList<Object> songList = new LinkedList<>();
                for (int i = 0; i < split1.length; i++) {
                    if (i %2 == 0){
                        songList.add(split1[i].split("\\(")[1]);
                    }
                }
                //匹配歌曲信息
                File csv = new File("D:\\IdeaSpace\\PythonMusicRecommend\\LibRecommender-master\\test\\modelSave\\deepfm_model_20\\songsSave.csv");
                csv.setReadable(true);
                csv.setWritable(true);
                InputStreamReader isr = null;
                BufferedReader br2 = null;
                try {
                    isr = new InputStreamReader(new FileInputStream(csv), "UTF-8");
                    br2 = new BufferedReader(isr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String line = "";

                ArrayList<String> records = new ArrayList<>();
                try {
                    //读取表头
                    br2.readLine();
                    while ((line = br2.readLine()) != null) {
                        String[] split2 = line.split(",");
                        if (songList.contains(split2[0])){
                            //song,play_count,track_id,title,release,artist_name,artist_familiarity,artist_hotttnesss,year,duration
                            SongVo songVoTemp = new SongVo();
                            songVoTemp.setTitle(split2[3]);
                            songVoTemp.setArtistName(split2[5]);
                            songVoTemp.setRelease(split2[4]);
                            songVoLinkedList.add(songVoTemp);
                        }
                    }
//                    System.out.println("csv表格读取行数：" + records.size());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 关闭输入流
                br.close();
                in.close();


            } catch (IOException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //获取完成推荐结果
//        if (songsRec == null){
//            JSONArray jsonArray = JSONObject.parseArray(songsSave);
            //解析dataframe
    /*BufferedReader br = new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(songsSave.getBytes(Charset.forName("utf8"))), Charset.forName("utf8")));
    String line;
    StringBuffer strbuf = new StringBuffer();
    //先读取表头
    line = br.readLine();
    while ((line = br.readLine()) != null) {
        if (!line.trim().equals("")) {
            line = "line" + line;//每行数据
            strbuf.append(line + "\r\n");
        }
    }*/

            //先获取播放量
            for (int i = 0; i < songVoLinkedList.size(); i++) {
                SongVo songVo = songVoLinkedList.get(i);
//                JSONArray o = (JSONArray) jsonArray.get(i);
                String title = URLEncoder.encode(songVo.getTitle(), "utf-8");
                String artistName = URLEncoder.encode(songVo.getArtistName(), "utf-8");
//                String url = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=7537459f592d916b49e697b8a0fb53df&artist="
//                        + artistName + "&track="
//                        + title  + "&format=json";
//                String jsonStr = "{xxx}";
//                String httpOrgCreateTestRtn = HttpClientUtil.doPost(url, jsonStr, "utf-8");
//                JSONObject jsonObject = JSONObject.parseObject(httpOrgCreateTestRtn);
//                JSONObject track = (JSONObject) jsonObject.get("track");
//                if (track != null) {
//                    songVo.setPlaycount(Long.parseLong(track.get("playcount").toString()));
//                    songVo.setListeners(Long.parseLong(track.get("listeners").toString()));
//                }
//                SongVo songVo = new SongVo();
//                songVo.setTitle(o.get(0).toString());
//                songVo.setRelease(o.get(1).toString());
//                songVo.setArtistName(o.get(2).toString());


                //再获取图片
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
                        songVo.setAlbumPic(albumObj.get("albumPic").toString());
                        songVo.setPublicTime(albumObj.get("publicTime").toString());
                        break;
                    }
                    if (j == albumList.size() - 1) {
                        albumObj = (JSONObject) albumList.get(0);
//                        tempRes.put("albumPic", albumObj.get("albumPic").toString());
//                        tempRes.put("release", albumObj.get("albumName").toString());
                        songVo.setRelease(albumObj.get("albumName").toString());
                        songVo.setAlbumPic(albumObj.get("albumPic").toString());
                        songVo.setPublicTime(albumObj.get("publicTime").toString());
                    }
                }

                /*//获取时长
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
                                JSONObject o2 = (JSONObject) singer.get(m);
                                if (o2.get("name").equals(songVo.getArtistName())) {
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
                }*/
                songVoLinkedListRes.add(songVo);
            }
            //存入redis
            stringRedisTemplate.opsForValue().set("songsSave"+userName, JSON.toJSONString(songVoLinkedListRes), 12, TimeUnit.HOURS);
//        }

        }
        else {
            songVoLinkedListRes = JSON.parseArray(songsSave, SongVo.class);
        }
        return new TableResultResponse<SongVo>(songVoLinkedListRes.size(), songVoLinkedListRes);

    }


    @RequestMapping(value="/personalizedAll", method = RequestMethod.GET)
    @ResponseBody
//    public TableResultResponse<PlayList> getPlayList(@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "1") int offset) {
    public TableResultResponse<SongVo> getPersonalizedSongAll(@RequestParam Map<String, Object> params) throws IOException {
        //返回集合
        List<SongVo> songsSaveListRedisAll = new LinkedList<>();
        String userName = (String) params.get("userName");
        //python推荐结果
        String songsSaveAll = stringRedisTemplate.opsForValue().get("songsSaveAll"+userName);

        if (songsSaveAll == null){
            String songsSave = stringRedisTemplate.opsForValue().get("songsSave"+userName);
            List<SongVo> songsSaveListRedis = JSON.parseArray(songsSave, SongVo.class);
            for (int i = 0; i < songsSaveListRedis.size(); i++) {
                SongVo songVo = songsSaveListRedis.get(i);
                //获取播放量
                String title = URLEncoder.encode(songVo.getTitle(), "utf-8");
                String artistName = URLEncoder.encode(songVo.getArtistName(), "utf-8");
                String url = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=7537459f592d916b49e697b8a0fb53df&artist="
                        + artistName + "&track="
                        + title + "&format=json";
                String jsonStr = "{xxx}";
                String httpOrgCreateTestRtn = HttpClientUtil.doPost(url, jsonStr, "utf-8");
                JSONObject jsonObject = JSONObject.parseObject(httpOrgCreateTestRtn);
                JSONObject track = (JSONObject) jsonObject.get("track");
                if (track != null) {
                    songVo.setPlaycount(Long.parseLong(track.get("playcount").toString()));
                    songVo.setListeners(Long.parseLong(track.get("listeners").toString()));
                }

                //获取时长
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
                                JSONObject o2 = (JSONObject) singer.get(m);
                                if (o2.get("name").equals(songVo.getArtistName())) {
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
                songsSaveListRedisAll.add(songVo);
            }
            //存入redis
            stringRedisTemplate.opsForValue().set("songsSaveAll"+userName, JSON.toJSONString(songsSaveListRedisAll), 12, TimeUnit.HOURS);
        } else {
            songsSaveListRedisAll = JSON.parseArray(songsSaveAll, SongVo.class);
        }

        return new TableResultResponse<SongVo>(songsSaveListRedisAll.size(), songsSaveListRedisAll);
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
                songVo.setPlaycount(Long.parseLong(tempObj.get("playcount").toString()));
                songVo.setListeners(Long.parseLong(tempObj.get("listeners").toString()));
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
                        songVo.setPublicTime(albumObj.get("publicTime").toString());
                        break;
                    }
                    if (j == albumList.size() - 1) {
                        albumObj = (JSONObject) albumList.get(0);
//                        tempRes.put("albumPic", albumObj.get("albumPic").toString());
//                        tempRes.put("release", albumObj.get("albumName").toString());
                        songVo.setRelease(albumObj.get("albumName").toString());
                        songVo.setAlbumPic(albumObj.get("albumPic").toString());
                        songVo.setPublicTime(albumObj.get("publicTime").toString());
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
    public TableResultResponse<SongVo> getTopDuration() throws UnsupportedEncodingException {
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
        return new TableResultResponse<SongVo>(topSongDurationListRedis.size(), topSongDurationListRedis);
    }

    //请求相似歌曲列表，并保存至redis
    @RequestMapping(value="/similarList", method = RequestMethod.GET)
    @ResponseBody
    public TableResultResponse<SongVo> getSimilarSongList(@RequestParam Map<String, Object> params) throws UnsupportedEncodingException {
        //返回集合
        String title = URLEncoder.encode((String) params.get("title"));
        String artistName = URLEncoder.encode((String) params.get("artistName"));
        String url = "http://ws.audioscrobbler.com/2.0/?method=track.getsimilar&artist=" + artistName +
                "&track=" + title + "&api_key=7537459f592d916b49e697b8a0fb53df&format=json";
        String jsonStr = "{xxx}";
        String httpOrgCreateTestRtn = HttpClientUtil.doPost(url, jsonStr, "utf-8");
        JSONObject jsonObject = JSONObject.parseObject(httpOrgCreateTestRtn);
        JSONObject similartracks = (JSONObject) jsonObject.get("similartracks");
        JSONArray track = null;
        boolean artistGettoptracks = false;
        if (similartracks == null){
            artistGettoptracks = true;
        } else {
            track = similartracks.getJSONArray("track");
            if (track==null ||track.size() == 0){
                artistGettoptracks = true;
            }
        }
//        JSONArray track = similartracks.getJSONArray("track");

        List<SongVo> result = new LinkedList<>();
        if (artistGettoptracks){
            ////未找到相似歌曲，则查找该艺术家的热门歌曲
            //http://ws.audioscrobbler.com/2.0/?method=artist.gettoptracks&artist='+ this.songVo.artistName + '&api_key=7537459f592d916b49e697b8a0fb53df&format=json
            String url2 = "http://ws.audioscrobbler.com/2.0/?method=artist.gettoptracks&artist=" + artistName +"&api_key=7537459f592d916b49e697b8a0fb53df&format=json&t=8";
            String jsonStr2 = "{xxx}";
            String httpOrgCreateTestRtn2 = HttpClientUtil.doPost(url2, jsonStr2, "utf-8");
            JSONObject jsonObject2 = JSONObject.parseObject(httpOrgCreateTestRtn2);
            JSONObject toptracks = (JSONObject) jsonObject2.get("toptracks");
            JSONArray track2 = toptracks.getJSONArray("track");
            for (int i = 0; i < 5; i++) {
                JSONObject o = (JSONObject) track2.get(i);
                SongVo songVo = new SongVo();
                songVo.setTitle(o.get("name").toString());
                JSONObject artistObj = (JSONObject) o.get("artist");
                songVo.setArtistName(artistObj.get("name").toString());
                String encode_title = URLEncoder.encode(songVo.getTitle());

                //专辑图片
                String url_album = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&n=2&w=" + encode_title + "&format=json&t=8";
                JSONObject jsonObject3 = HttpsClientUtil.getHttpsReq(url_album);
                JSONObject data2 = (JSONObject) jsonObject3.get("data");
                JSONObject album = (JSONObject) data2.get("album");
                JSONArray albumList = album.getJSONArray("list");
                if (albumList.size() != 0) {
                    JSONObject albumObj = (JSONObject) albumList.get(0);
                    songVo.setAlbumPic(albumObj.get("albumPic").toString());
                } else {
                    //没有专辑信息
                    songVo.setAlbumPic("");
                }
                result.add(songVo);
            }
        } else {
            for (int i = 0; i < 5; i++) {
                JSONObject o = (JSONObject) track.get(i);
                SongVo songVo = new SongVo();
                songVo.setTitle(o.get("name").toString());
                JSONObject artistObj = (JSONObject) o.get("artist");
                songVo.setArtistName(artistObj.get("name").toString());
                String encode_title = URLEncoder.encode(songVo.getTitle());
                //专辑图片
                String url_album = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&n=2&w=" + encode_title + "&format=json&t=8";
                JSONObject jsonObject3 = HttpsClientUtil.getHttpsReq(url_album);
                JSONObject data2 = (JSONObject) jsonObject3.get("data");
                JSONObject album = (JSONObject) data2.get("album");
                JSONArray albumList = album.getJSONArray("list");
                if (albumList.size() != 0) {
                    JSONObject albumObj = (JSONObject) albumList.get(0);
                    songVo.setAlbumPic(albumObj.get("albumPic").toString());
                } else {
                    //没有专辑信息
                    songVo.setAlbumPic("@/assets/logo/MusicRec.png");
                }
                result.add(songVo);
            }
        }
        //返回结果
        return  new TableResultResponse<SongVo>(result.size(), result);
    }

}
