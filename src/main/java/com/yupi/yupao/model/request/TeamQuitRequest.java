package com.yupi.yupao.model.request;

import lombok.Data;

import java.io.Serializable;

// * 队伍退出请求类。
// * 用于封装用户请求退出队伍时所需的数据。
// * @ClassName:TeamQuitRequest
// * @author Hsu琛君珩
// * @date 2024-05-23
// * @apiNote
// * @Version: v1.0
// */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = 6062900472170100154L;

    /**
     * 队伍id
     */
    private Long teamId;
}