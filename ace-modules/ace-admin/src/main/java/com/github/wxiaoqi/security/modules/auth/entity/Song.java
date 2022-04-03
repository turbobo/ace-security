package com.github.wxiaoqi.security.modules.auth.entity;

import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author
 *
 */
@Table(name = "song")
public class Song implements Serializable {
    /**
     * 主键，自增
     */
    private Integer id;

    /**
     * 歌曲id
     */
    private Integer song;

    /**
     * 播放次数
     */
    private Integer playCount;

    /**
     * 歌曲track_id
     */
    private String trackId;

    /**
     * 歌曲名称
     */
    private String title;

    /**
     * 专辑名称
     */
    private String release;

    /**
     * 艺术家id
     */
    private String artitstId;

    /**
     * 艺术家姓名
     */
    private String artistName;

    /**
     * 艺术家热度
     */
    private Float artistHottness;

    /**
     * 歌曲年份
     */
    private Integer year;

    /**
     * 歌曲时长
     */
    private Float duration;

    /**
     * 逻辑删除：1：启用，0：删除
     */
    private String status;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSong() {
        return song;
    }

    public void setSong(Integer song) {
        this.song = song;
    }

    public Integer getPlayCount() {
        return playCount;
    }

    public void setPlayCount(Integer playCount) {
        this.playCount = playCount;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

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

    public String getArtitstId() {
        return artitstId;
    }

    public void setArtitstId(String artitstId) {
        this.artitstId = artitstId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public Float getArtistHottness() {
        return artistHottness;
    }

    public void setArtistHottness(Float artistHottness) {
        this.artistHottness = artistHottness;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Float getDuration() {
        return duration;
    }

    public void setDuration(Float duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Song other = (Song) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getSong() == null ? other.getSong() == null : this.getSong().equals(other.getSong()))
                && (this.getPlayCount() == null ? other.getPlayCount() == null : this.getPlayCount().equals(other.getPlayCount()))
                && (this.getTrackId() == null ? other.getTrackId() == null : this.getTrackId().equals(other.getTrackId()))
                && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
                && (this.getRelease() == null ? other.getRelease() == null : this.getRelease().equals(other.getRelease()))
                && (this.getArtitstId() == null ? other.getArtitstId() == null : this.getArtitstId().equals(other.getArtitstId()))
                && (this.getArtistName() == null ? other.getArtistName() == null : this.getArtistName().equals(other.getArtistName()))
                && (this.getArtistHottness() == null ? other.getArtistHottness() == null : this.getArtistHottness().equals(other.getArtistHottness()))
                && (this.getYear() == null ? other.getYear() == null : this.getYear().equals(other.getYear()))
                && (this.getDuration() == null ? other.getDuration() == null : this.getDuration().equals(other.getDuration()))
                && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getSong() == null) ? 0 : getSong().hashCode());
        result = prime * result + ((getPlayCount() == null) ? 0 : getPlayCount().hashCode());
        result = prime * result + ((getTrackId() == null) ? 0 : getTrackId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getRelease() == null) ? 0 : getRelease().hashCode());
        result = prime * result + ((getArtitstId() == null) ? 0 : getArtitstId().hashCode());
        result = prime * result + ((getArtistName() == null) ? 0 : getArtistName().hashCode());
        result = prime * result + ((getArtistHottness() == null) ? 0 : getArtistHottness().hashCode());
        result = prime * result + ((getYear() == null) ? 0 : getYear().hashCode());
        result = prime * result + ((getDuration() == null) ? 0 : getDuration().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", song=").append(song);
        sb.append(", playCount=").append(playCount);
        sb.append(", trackId=").append(trackId);
        sb.append(", title=").append(title);
        sb.append(", release=").append(release);
        sb.append(", artitstId=").append(artitstId);
        sb.append(", artistName=").append(artistName);
        sb.append(", artistHottness=").append(artistHottness);
        sb.append(", year=").append(year);
        sb.append(", duration=").append(duration);
        sb.append(", status=").append(status);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}