package com.yupi.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.service.UserService;
import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yupi.yupao.contant.UserConstant.ADMIN_ROLE;


/**
 * @author ousir
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2024-05-05 21:17:24
 */
@Slf4j
@Service
public class    UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    /**
     * 盐值混淆密码
     */
    private static final String SALT = "yupi";

    public String USER_LOGIN_STATE = "userLoginState";
    @Resource
    private UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {

        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            // TODO
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        //账户不能包含特殊字符
        String validPattern = "[ _`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        //密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        //用户不能重复
        //这里有个查询数据库的操作，如果账户包含了特殊字符就不用查了，因此把这个操作放到最后节省了一点性能
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名重复");
        }
        //星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号重复");
        }
        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            // TODO 修改为自定义异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不足4位");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不足8位");
        }
        //账户不能包含特殊字符
        String validPattern = "[ _`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }

        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //用户不能重复
        //这里有个查询数据库的操作，如果账户包含了特殊字符就不用查了，因此把这个操作放到最后节省了一点性能
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);

        //用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        //  3.用户脱敏
        User safetyUser = getSafetyUser(user);

        //   4.记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }


    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        //要先判空
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setProfile(originUser.getProfile());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }


    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户输入的标签列表，将在用户标签字段中进行匹配
     * @return 符合条件的用户列表
     * @throws BusinessException 当 `tagNameList` 为空或 `null` 时抛出参数错误异常
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {

        //是否为空
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //查询所有的用户
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //在内存中判断是否符合包含的标签
        //把每一个用户的tags转成java对象然后判断传进来的在不在这个用户的标签里
        // 不在就跳过，在就存在list里面，最后得到的就是有相关标签的用户
        return userList.stream().filter(user -> {
            //反序列化，把json转换成java对象
            String tagsStr = user.getTags();
            if (StringUtils.isBlank(tagsStr)) {
                return false;
            }
            //集合在o(1)的时间内判断是否包含一个集合
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());   //小技巧，new 一个TypeToken然后.getType得到泛型的类型
            tempTagNameSet = java.util.Optional.ofNullable(tempTagNameSet).orElse((new HashSet<>()));
            for (String tagName : tagNameList) {
                //如果每个用户的标签包含要查的就true
                //tagNameList传来的
                if (tempTagNameSet.contains(tagName)) {
                    return true;
                }
                //一个都不包含就是false
            }
            return false;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            return null;
        }
        Object object =request.getSession().getAttribute(USER_LOGIN_STATE);
        if(object == null)
        {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) object;
    }

    /**
     * 更新用户信息。
     * 该方法用于根据提供的用户对象更新用户数据。
     * 它首先检查用户ID是否有效，然后根据用户的权限（管理员或非管理员）
     * 决定是否可以更新所请求的用户数据。
     * @param user 封装了要更新的用户信息的User对象。
     * @param loginUser 当前登录的User对象，用于进行权限验证。
     * @return 更新操作影响的数据库记录数。通常返回1表示更新成功，返回0表示未进行更新。
     * @throws BusinessException 如果用户ID无效、登录用户无权更新其他用户信息、或指定ID的用户不存在时抛出。
     */
    @Override
    public int updateUser(User user,User loginUser) {
        long userId = user.getId();
        if(userId <= 0 )
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员， 允许 更新 任意  用户
        //如果不是管理员，只允许更新 自己 信息
        if(!isAdmin(loginUser) && userId !=loginUser.getId()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null )
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return userMapper.updateById(user);
    }



    public  boolean isAdmin(HttpServletRequest request) {
        // 从会话中获取用户对象
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if(user == null || user.getUserRole() != ADMIN_ROLE) {
            return false;
        }
        return  true;
    }
    /**
     * 判断当前操作用户是否为管理员。
     * @param loginUser 用户对象
     * @return 如果当前用户为管理员，则返回true；否则返回false。
     */
    @Override
    public boolean isAdmin(User loginUser) {
        // 如果用户为空或者用户角色不是管理员，则返回false
        if (loginUser == null || loginUser.getUserRole() != ADMIN_ROLE) {
            return false;
        }
        // 否则，用户是管理员，返回true
        return true;
    }
    @Deprecated
    public List<User> searchUsersByTagsSQL(List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接tag
        // like '%Java%' and like '%Python%'
        for (String tagList : tagNameList) {
            queryWrapper = queryWrapper.or().like("tags", tagList);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return  userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

}



