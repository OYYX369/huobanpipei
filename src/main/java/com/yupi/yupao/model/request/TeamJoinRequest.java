package com.yupi.yupao.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 队伍加入请求对象。
 * 用于封装用户加入队伍时所需的数据。
 * @ClassName:TeamJoinRequest
 * @author Hsu琛君珩
 * @date 2024-05-23
 * @apiNote
 * @Version: v1.0
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 3618557907313533138L;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}