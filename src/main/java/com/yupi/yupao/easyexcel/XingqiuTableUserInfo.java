package com.yupi.yupao.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 星球表格用户信息
 * @ClassName:XingqiuUserInfo
 * @author Hsu琛君珩
 * @date 2024-05-10
 * @apiNote
 * @Version: v1.0
 */
@Data
public final class XingqiuTableUserInfo {
    /**
     * 星球编号
     */
    @ExcelProperty("成员编号")
    private String planetCode;

    /**
     * 昵称
     */
    @ExcelProperty("成员昵称")
    private String userName;
}