package com.yupi.yupao.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍更新请求对象。
 * 用于接收更新队伍信息的请求参数。
 * @ClassName:TeamUpdateRequest
 * @author Hsu琛君珩
 * @date 2024-05-23
 * @apiNote
 * @Version: v1.0
 */
@Data
public class TeamUpdateRequest implements Serializable {

    private static final long serialVersionUID = -8351268843240931365L;

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;
}