package com.github.wxiaoqi.security.modules.admin.vo;

import io.swagger.models.auth.In;

/**
 * @Author Jusven
 * @Date 2022/4/7 21:52
 */
public class SongVo {
    /**
     * 歌曲名称
     */
    private String title;

    /**
     * 专辑名称
     */
    private String release;


    /**
     * 艺术家姓名
     */
//    @Column(name = "artist_name")
    private String artistName;


    /**
     * 歌曲时长
     */
    private Integer duration;

    /**
     * 专辑图片
     */
    private String albumPic;

    /**
     * 发布时间
     */
    private String publicTime;

    /**
     * 播放量
     */
    private Long playcount;

    /**
     * 听众数量
     */
    private Long listeners;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getAlbumPic() {
        return albumPic;
    }

    public void setAlbumPic(String albumPic) {
        this.albumPic = albumPic;
    }

    public String getPublicTime() {
        return publicTime;
    }

    public void setPublicTime(String publicTime) {
        this.publicTime = publicTime;
    }

    public Long getPlaycount() {
        return playcount;
    }

    public void setPlaycount(Long playcount) {
        this.playcount = playcount;
    }

    public Long getListeners() {
        return listeners;
    }

    public void setListeners(Long listeners) {
        this.listeners = listeners;
    }
}
