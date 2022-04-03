package com.github.wxiaoqi.security.modules.auth.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.wxiaoqi.security.common.util.Query;
import com.github.wxiaoqi.security.modules.admin.entity.PlayList;
import com.github.wxiaoqi.security.modules.admin.mapper.PlayListMapper;
import com.github.wxiaoqi.security.modules.admin.mapper.SongMapper;
import com.github.wxiaoqi.security.modules.auth.entity.Song;
import com.github.wxiaoqi.security.modules.auth.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Jusven
 * @Date 2022/4/1 21:02
 */
@Service
public class MusicServiceImpl implements MusicService {
    @Autowired
    private PlayListMapper playListMapper;

    @Autowired
    private SongMapper songMapper;

    @Override
    public List<PlayList> getPlayLists() {
        List<PlayList> playList = playListMapper.getPlayLists();
        return playList;
    }

    @Override
    public List<Song> getSongList(Query query) {
//        Page<Object> result = PageHelper.startPage(query.getPage(), query.getLimit());
        List<Song> songList = songMapper.getSongList();
        return songList;
    }

    @Override
    public int insertSelectiveMine(Song song) {
        return songMapper.insertSelectiveMine(song);
    }

    @Override
    public Song selectById(int id) {
        return songMapper.selectById(id);
    }

    @Override
    public void logicDeleteById(int id) {
        songMapper.logicDeleteById(id);
    }

    @Override
    public void updateSelectiveByEntity(Song song) {
        songMapper.updateSelectiveByEntity(song);
    }
}
