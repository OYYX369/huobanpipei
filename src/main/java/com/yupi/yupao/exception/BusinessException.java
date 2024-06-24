package com.yupi.yupao.exception;

import com.yupi.yupao.common.ErrorCode;

/**
 * 自定义异常类
 * @author ivy
 * @date 2024/4/15 19:52
 */
public class
BusinessException extends RuntimeException{

    //异常中的属性不需要 setter
    //这里就相当于给原来的 RuntimeException 扩充了这两个属性
    private static final long serialVersionUID = -6937582848772423714L;
    private final int code;
    private final String description;

    //并且创建了几个构造函数，让他能够使用 ErrorCode
    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
