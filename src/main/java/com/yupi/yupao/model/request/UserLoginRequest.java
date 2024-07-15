package com.yupi.yupao.model.request;


import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体
 * @author ousir
 * @ClassName:UserLoginRequest
 * @date 2024-03-13
 * @apiNote
 * @Version: v1.0
 */

@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 1696732939386369403L;

    private String userAccount;
    private String userPassword;

}
