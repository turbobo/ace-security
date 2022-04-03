package com.github.wxiaoqi.security.modules.auth.biz;

import com.github.wxiaoqi.security.common.biz.BaseBiz;
import com.github.wxiaoqi.security.modules.auth.entity.Song;
import com.github.wxiaoqi.security.modules.admin.util.Sha256PasswordEncoder;
import com.github.wxiaoqi.security.modules.admin.mapper.SongMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ${DESCRIPTION}
 *
 * @author the sun
 * @create 2017-06-08 16:23
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MusicBiz extends BaseBiz<SongMapper,Song> {

    private Sha256PasswordEncoder encoder = new Sha256PasswordEncoder();

    @Override
    public void insertSelective(Song entity) {
//        String password = encoder.encode(entity.getPassword());
//        entity.setPassword(password);
        super.insertSelective(entity);
    }

    @Override
    public void updateSelectiveById(Song entity) {
        super.updateSelectiveById(entity);
    }

    /**
     * 根据用户名获取用户信息
     * @param title
     * @return
     */
    public Song getSongByTitle(String title){
        Song Song = new Song();
        Song.setTitle(title);
        return mapper.selectOne(Song);
    }

    /**
     * 根据用户名获取用户信息
     * @param title
     * @return
     */
    public Song getSongByTitle2(String title){
        Song Song = new Song();
        Song.setTitle(title);
        try {
            return mapper.selectOne(Song);
        }catch (Exception e){
            return null;
        }
    }

}
