package com.yupi.yupao.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍用户视图对象，用于展示队伍及用户相关信息。
 * @ClassName:TeamUserVO
 * @author Hsu琛君珩
 * @date 2024-05-22
 * @apiNote
 * @Version: v1.0
 */
@Data
public class TeamUserVO implements Serializable {

    private static final long serialVersionUID = 5696227771870706774L;

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
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人用户信息
     */
    private UserVO createUser;

    /**
     * 已加入的用户数
     */
    private Integer hasJoinNum;

    /**
     * 是否已加入队伍
     */
    private boolean hasJoin = false;



}