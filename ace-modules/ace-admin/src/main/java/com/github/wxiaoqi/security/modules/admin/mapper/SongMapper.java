package com.github.wxiaoqi.security.modules.admin.mapper;

import com.github.wxiaoqi.security.modules.auth.entity.Song;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * SongMapper继承基类
 */
@Repository
public interface SongMapper extends Mapper<Song> {
    public List<Song> getSongList();
    public int insertSelectiveMine(Song var1);
    public Song selectById(int id);
    public void logicDeleteById(@Param("id") int id);
    public void updateSelectiveByEntity(Song song);
}