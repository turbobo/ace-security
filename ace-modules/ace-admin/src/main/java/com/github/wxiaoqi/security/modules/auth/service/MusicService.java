package com.github.wxiaoqi.security.modules.auth.service;

import com.github.wxiaoqi.security.common.util.Query;
import com.github.wxiaoqi.security.modules.admin.entity.PlayList;
import com.github.wxiaoqi.security.modules.auth.entity.Song;

import java.util.List;

/**
 * @Author Jusven
 * @Date 2022/4/1 21:02
 */
public interface MusicService {
    List<PlayList> getPlayLists();
    List<Song> getSongList(Query query);
    int insertSelectiveMine(Song song);
    Song selectById(int id);
    void logicDeleteById(int id);
    void updateSelectiveByEntity(Song song);
}
