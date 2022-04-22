package com.github.wxiaoqi.security.modules.admin.rest;

import com.github.wxiaoqi.security.common.msg.ObjectRestResponse;
import com.github.wxiaoqi.security.common.msg.TableResultResponse;
import com.github.wxiaoqi.security.common.rest.BaseController;
import com.github.wxiaoqi.security.common.util.Query;
import com.github.wxiaoqi.security.modules.admin.biz.MenuBiz;
import com.github.wxiaoqi.security.modules.admin.biz.UserBiz;
import com.github.wxiaoqi.security.modules.admin.entity.Menu;
import com.github.wxiaoqi.security.modules.admin.entity.User;
import com.github.wxiaoqi.security.modules.admin.mapper.UserMapper;
import com.github.wxiaoqi.security.modules.admin.rpc.service.PermissionService;
import com.github.wxiaoqi.security.modules.admin.vo.FrontUser;
import com.github.wxiaoqi.security.modules.admin.vo.FrontUserV2;
import com.github.wxiaoqi.security.modules.admin.vo.MenuTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ${DESCRIPTION}
 *
 * @author the sun
 * @create 2017-06-08 11:51
 */
@RestController
@RequestMapping("user")
public class UserController extends BaseController<UserBiz,User> {
    @Autowired
    private PermissionService permissionService;

    @Autowired
    private MenuBiz menuBiz;

    @Autowired
    private UserBiz userBiz;

    @Autowired
    private UserMapper userMapper;

    @RequestMapping(value = "/front/info", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getUserInfo(String token) throws Exception {
        FrontUser userInfo = permissionService.getUserInfo(token);
        if(userInfo==null) {
            return ResponseEntity.status(401).body(false);
        } else {
            return ResponseEntity.ok(userInfo);
        }
    }

    @RequestMapping(value = "/v2/front/info", method = RequestMethod.GET)
    @ResponseBody
    public ObjectRestResponse getUserInfoV2() throws Exception {
        FrontUserV2 userInfo = permissionService.getUserInfoV2();
        return new ObjectRestResponse<FrontUserV2>().data(userInfo);
    }

    @RequestMapping(value = "/front/menus", method = RequestMethod.GET)
    public @ResponseBody
    List<MenuTree> getMenusByUsername(String token) throws Exception {
        return permissionService.getMenusByUsername(token);
    }

    @RequestMapping(value = "/front/menu/all", method = RequestMethod.GET)
    public @ResponseBody
    List<Menu> getAllMenus() throws Exception {
        return menuBiz.selectListAll();
    }

    /**
     * 查询所有用户
     * @param params
     * @return
     */
    @RequestMapping(value = "/v2/single",method = RequestMethod.GET)
    @ResponseBody
    public TableResultResponse<User> list(@RequestParam Map<String, Object> params){
        //查询列表数据
        Query query = new Query(params);
//        query.put("page",params.get("page"));
//        query.put("limit",params.get("limit"));
        return userBiz.selectByQuery(query);
    }

    @RequestMapping(value = "/v2/single/username",method = RequestMethod.GET)
    @ResponseBody
    public TableResultResponse<User> getSingleByUserName(@RequestParam Map<String, Object> params){
        String username = (String) params.get("username");
        User user = userMapper.selectUserByUserName(username);
        List<User> result = new ArrayList<>();
        result.add(user);
        return new TableResultResponse<>(result.size(),result);
    }

    @RequestMapping(value = "/v2/single/{id}",method = RequestMethod.GET)
    @ResponseBody
    public ObjectRestResponse<User> getSingle(@PathVariable int id){
        ObjectRestResponse<User> entityObjectRestResponse = new ObjectRestResponse<>();
        Object o = userBiz.selectById(id);
        entityObjectRestResponse.data((User)o);
        return entityObjectRestResponse;
    }

    @RequestMapping(value = "/v2/single/{id}",method = RequestMethod.PUT)
    @ResponseBody
    public ObjectRestResponse<User> update(@RequestBody User entity){
        userBiz.updateSelectiveById(entity);
        return new ObjectRestResponse<User>();
    }

    @RequestMapping(value = "/v2/single/updatePass/{id}",method = RequestMethod.PUT)
    @ResponseBody
    public ObjectRestResponse<User> updatePass(@RequestBody User entity){
        userBiz.updateUserPassById(entity);
        return new ObjectRestResponse<User>();
    }
}
