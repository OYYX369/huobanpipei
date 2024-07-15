package com.yupi.yupao.model.request;


import lombok.Data;

import java.io.Serializable;

/**
 *  用户注册请求体！
 */
/**
 * 用户注册请求体
 * @author Hsu琛君珩
 * @ClassName:UserRegisterRequest
 * @date 2024-03-12
 * @apiNote
 * @Version: v1.0
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -7042601578107547540L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;

}
