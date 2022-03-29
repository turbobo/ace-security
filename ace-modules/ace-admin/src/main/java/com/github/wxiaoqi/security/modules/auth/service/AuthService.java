package com.github.wxiaoqi.security.modules.auth.service;


import com.github.wxiaoqi.security.common.msg.TableResultResponse;
import com.github.wxiaoqi.security.common.util.Query;
import com.github.wxiaoqi.security.modules.admin.entity.PlayList;
import com.github.wxiaoqi.security.modules.auth.util.user.JwtAuthenticationRequest;

import java.util.List;
import java.util.Map;

public interface AuthService {
    Map login(JwtAuthenticationRequest authenticationRequest) throws Exception;

    Map signup(JwtAuthenticationRequest authenticationRequest) throws Exception;

    String refresh(String oldToken) throws Exception;
    void validate(String token) throws Exception;

    void logout(String token) throws Exception;

    void logicDeleteById(int id) throws Exception;

//    List<PlayList> getPlayLists(int limit, int offset);

//    TableResultResponse<PlayList> getPlayList(int limit, int offset, Query query);
    TableResultResponse<PlayList> getPlayList(Query query);

    List<PlayList> getPlayLists();
}
