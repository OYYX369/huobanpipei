package com.yupi.yupao.contant;

/**
 * 用户常量
 * @author Hsu琛君珩
 * @ClassName:UserConstant
 * @date 2024-03-13
 * @apiNote
 * @Version: v1.0
 */
public interface UserConstant {
    //用户登录态键
    String USER_LOGIN_STATE="userLoginState";


    /**
     * 用户权限
     */
    int DEFAULT_ROLE=0;//普通用户，默认权限
    int ADMIN_ROLE=1;//管理员
}