package com.github.wxiaoqi.security.modules.admin.mapper;

import com.github.wxiaoqi.security.modules.admin.entity.PlayList;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PlayListDAO继承基类
 */
@Repository
public interface PlayListMapper {
    public List<PlayList> getPlayList(@Param("limit") int limit, @Param("offset") int offset);

    public List<PlayList> getPlayLists();
}