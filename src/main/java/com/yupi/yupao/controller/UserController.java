package com.yupi.yupao.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.model.request.UserLoginRequest;
import com.yupi.yupao.model.request.UserRegisterRequest;
import com.yupi.yupao.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yupi.yupao.contant.UserConstant.USER_LOGIN_STATE;


/**
 *
 */

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173/"}, allowCredentials = "true")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    /**
     *    推荐用户列表接口。
     *   该接口不进行权限校验，向所有请求者推荐用户列表。
     * @param request
     * @return
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize,long pageNum, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("ousir:user:recommend:%s", loginUser.getId());
        ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
        //如果有缓存，直接读缓存
        Page<User> userPage= (Page<User>)valueOperations.get(redisKey);
        if (userPage!=null){
            log.info("用了缓存");
            return ResultUtils.success(userPage);
        }
        //无缓存，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        //写缓存
        try {
            valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            log.error("redis set key error",e);
        }
        log.info("没有用缓存");
        return ResultUtils.success(userPage);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
//            return ResultUtils.error(ErrorCode.NO_AUTH_ERROR);
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list();
        List<User> collect = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(collect);
    }

    /**
     * 根据标签列表搜索用户的接口。
     * @param tagNameList 用户的标签列表，通过请求参数传递。此参数不是必需的，但如果未提供，则抛出业务异常。
     * @return 返回符合标签搜索条件的用户列表的响应实体。
     */
    @GetMapping("/search/tags")
    //前端传来的是tag列表参数
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTagsSQL(tagNameList);
        return ResultUtils.success(userList);
    }

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest==null){
//          return ResultUtils.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount=userRegisterRequest.getUserAccount();
        String userPassword=userRegisterRequest.getUserPassword();
        String checkPassword=userRegisterRequest.getCheckPassword();
        String planetCode=userRegisterRequest.getPlanetCode();
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return new BaseResponse<>(0,result,"ok");
        //返回自己封装的信息
    }

    /**
     *
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest==null){
            return null;
        }
        String userAccount=userLoginRequest.getUserAccount();
        String userPassword=userLoginRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }
    /**
     * 获取当前用户
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj=request.getSession().getAttribute(USER_LOGIN_STATE);  //得到对象用户的登录态，获取当前登录用户接口，因为已经存到 session 中
        User currentUser= (User) userObj;
        if(currentUser==null){
            return null;
        }
        long userId=currentUser.getId();
        // TODO 校验用户是否合法
        User user=userService.getById(userId);
        User safetyUser=userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }



    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if(request==null){
            return null;
        }
        int i = userService.userLogout(request);
        return ResultUtils.success(i);
    }


    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request){
        // 1.校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提供的用户数据不能为空");
        }
        // 2.鉴权：验证当前请求中的用户是否具有更新所提供用户数据的权限。
        User loginUser = userService.getLoginUser(request);
        // 3.触发更新
        int result = userService.updateUser(user,loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody Long id , HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            return ResultUtils.error(ErrorCode.NO_AUTH,"缺少管理员权限");
        }
        if(id <= 0) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }



}
