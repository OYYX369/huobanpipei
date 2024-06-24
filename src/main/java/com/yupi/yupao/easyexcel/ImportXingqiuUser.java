package com.yupi.yupao.easyexcel;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yupi.yupao.easyexcel.XingqiuTableUserInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入星球表格用户信息
 * @ClassName: ImportXingqiuUser
 * @Author: Hsu琛君珩
 * @Date: 2024-05-10
 * @Version: v1.0
 * @Description: 使用 Easy Excel 读取 Excel 文件，并对用户信息进行分组和统计
 */
public class ImportXingqiuUser {
    public static void main(String[] args) {
        // Excel 文件的绝对路径
        String fileName = "E:\\yupaohuoban\\huobanpipei-master\\src\\main\\resources\\test.xlsx";
        // 使用 Easy Excel 读取文件中所有用户信息，指定数据模型类 XingqiuTableUserInfo
        // 同步读取第一个工作表，返回所有数据
        List<XingqiuTableUserInfo> userInfoList = EasyExcel.read(fileName)
                .head(XingqiuTableUserInfo.class) // 设置数据模型类
                .sheet() // 读取第一个工作表
                .doReadSync(); // 同步读取，返回所有行数据
        // 打印总用户数
        System.out.println("总数 = " + userInfoList.size());
        // 使用 Java Stream 对用户信息按用户名进行分组，并过滤掉空用户名
        Map<String, List<XingqiuTableUserInfo>> listMap =
                userInfoList.stream()
                // 过滤掉空用户名的记录
                .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUserName()))
                // 以用户名为键，对用户信息进行分组
                .collect(Collectors.groupingBy(XingqiuTableUserInfo::getUserName));

        // 输出每个重复用户名及其数量
        for (Map.Entry<String, List<XingqiuTableUserInfo>> entry : listMap.entrySet()) {
            // 如果某个用户名对应的列表大小大于 1，则表示重复
            if (entry.getValue().size() > 1) {
                System.out.println("username = " + entry.getKey());
                System.out.println("1");
            }
        }
        // 打印不重复的用户名数
        System.out.println("不重复昵称数 = " + listMap.keySet().size());
    }
}