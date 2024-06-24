package com.yupi.yupao.common;

/**
 * 自定义错误码
 * @author ivy
 * @date 2024/4/15 17:19
 */
public enum ErrorCode {
    SUCCESS(0,"success",""),
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求数据为空",""),
    NO_AUTH(40101,"无权限",""),
    NOT_LOGIN_ERROR(40100,"未登录",""),
    SYSTEM_ERROR(50000,"系统内部异常","");

    private final int code;
    /**
     * 状态码信息
     */
    private final String message;
    /**
     * 状态码描述（详情）
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
