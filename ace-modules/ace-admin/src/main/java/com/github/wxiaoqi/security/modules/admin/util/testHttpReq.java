package com.github.wxiaoqi.security.modules.admin.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.wxiaoqi.security.modules.admin.vo.SongVo;
import lombok.experimental.var;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

/**
 * @Author Jusven
 * @Date 2022/4/7 13:26
 */
public class testHttpReq {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String url = "http://ws.audioscrobbler.com/2.0/?method=chart.gettoptracks&api_key=7537459f592d916b49e697b8a0fb53df&format=json";
        String jsonStr = "{xxx}";
        String httpOrgCreateTestRtn = HttpClientUtil.doPost(url, jsonStr, "utf-8");
        JSONObject jsonObject = JSONObject.parseObject(httpOrgCreateTestRtn);
        JSONObject tracks = (JSONObject) jsonObject.get("tracks");
        JSONArray trackArray = tracks.getJSONArray("track");
        JSONArray resultArray = new JSONArray();
        for (int i = 0; i < trackArray.size(); i++) {
            JSONObject tempObj = (JSONObject) trackArray.get(i);
            JSONObject artist = (JSONObject) tempObj.get("artist");
//            JSONObject artistName = tempObj.get("artist");
            JSONObject tempRes = new JSONObject();
            tempRes.put("title", tempObj.get("name").toString());
            tempRes.put("artistName", artist.get("name").toString());
          /*  //请求专辑封面
            String url_album = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=7537459f592d916b49e697b8a0fb53df&artist=" + tempRes.get("artistName").toString() + "&track=" + tempRes.get("title").toString();
            String jsonStr_album = "{xxx}";
            String res_album = HttpClientUtil.doPost(url_album, jsonStr_album, "utf-8");
            JSONObject json_album = JSONObject.parseObject(res_album);*/


            //直接请求qq专辑图片 -- await同步请求
            //参数--歌名 包含空格无法正常解析
            String title = URLEncoder.encode(tempRes.get("title").toString(), "utf-8");

            String url_album = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&n=2&w=" + title + "&format=json&t=8";
            JSONObject jsonObject2 = HttpsClientUtil.getHttpsReq(url_album);
            JSONObject data2 = (JSONObject) jsonObject2.get("data");
            JSONObject album = (JSONObject) data2.get("album");
            JSONArray albumList = album.getJSONArray("list");
            //匹配歌曲
            for (int j = 0; j < albumList.size(); j++) {
                JSONObject albumObj = (JSONObject) albumList.get(j);
                if (tempRes.get("artistName").equals(albumObj.get("singerName").toString())) { //匹配歌手
                    tempRes.put("albumPic",albumObj.get("albumPic").toString());
                    tempRes.put("release",albumObj.get("albumName").toString());
                    break;
                }
                if (j == albumList.size()-1){
                    albumObj = (JSONObject) albumList.get(0);
                    tempRes.put("albumPic",albumObj.get("albumPic").toString());
                    tempRes.put("release",albumObj.get("albumName").toString());
                }
            }

            //请求歌曲时长
            String url_duration = "https://c.y.qq.com/soso/fcgi-bin/client_search_cp?p=1&n=2&w=" + title + "&format=json";
            JSONObject songObjectList = HttpsClientUtil.getHttpsReq(url_duration);
            JSONObject data = (JSONObject) songObjectList.get("data");
            if (data != null){
                JSONObject song = (JSONObject) data.get("song");
                if (song != null){
                    JSONArray songList = song.getJSONArray("list");
                    for (int l = 0; l < songList.size(); l++) {  //匹配歌曲
                        JSONObject jsonObject1 = (JSONObject) songList.get(l);
                        JSONArray singer = jsonObject1.getJSONArray("singer");
                        for (int m = 0; m < singer.size(); m++) {  //匹配歌手
                            JSONObject o = (JSONObject) singer.get(m);
                            if (o.get("name").equals(tempRes.get("artistName").toString())){
                                tempRes.put("duration",(Integer) jsonObject1.get("interval"));
                                break;
                            }
                        }

                        //匹配不到，则使用第一个专辑：已经最后一个，还没匹配到
                        if (l == songList.size() - 1) {
                            jsonObject1 = (JSONObject) songList.get(0);
                            tempRes.put("duration",jsonObject1.get("interval"));
                        }
                        if (tempRes.get("duration") != null){
                            break;
                        }
                    }
                }
            }

            resultArray.add(tempRes);
        }


        System.out.println();
    }
}
