package com.yupi.yupao.easyexcel;

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * 导入 Excel 文件
 * @ClassName: ImportExcel
 * @Author: Hsu琛君珩
 * @Date: 2024-05-10
 * @Version: v1.0
 * @Description: 使用 Easy Excel 读取 Excel 文件中的数据并交给监听器处理或以同步方式获取
 */
public class ImportExcel {
    /**
     * 读取 Excel 文件数据的主函数
     */
    public static void main(String[] args) {
        // Excel 文件的绝对路径
        String fileName = "E:\\yupaohuoban\\huobanpipei-master\\src\\main\\resources\\test.xlsx";

        // 可选择两种读取方式之一
        // 使用监听器逐行处理数据
        // readByListener(fileName);

        // 使用同步读取方式一次性读取所有数据
        synchronousRead(fileName);
    }

    /**
     * 监听器方式读取 Excel 文件
     * @param fileName Excel 文件路径
     */
    public static void readByListener(String fileName) {
        // 使用 Easy Excel 读取数据并交给自定义的 TableListener 逐行处理
        EasyExcel.read(fileName, XingqiuTableUserInfo.class, new TableListener())
                .sheet() // 读取默认的第一个工作表
                .doRead(); // 开始逐行读取数据并传递给监听器
    }

    /**
     * 同步方式读取 Excel 文件
     * @param fileName Excel 文件路径
     * @description 同步读取方式不推荐使用，因其会将数据一次性加载到内存中，容易导致内存不足问题
     */
    public static void synchronousRead(String fileName) {
        // 使用 Easy Excel 读取数据，以同步方式将整个工作表的数据一次性返回
        List<XingqiuTableUserInfo> list = EasyExcel.read(fileName)
                .head(XingqiuTableUserInfo.class) // 指定数据模型类
                .sheet() // 读取默认的第一个工作表
                .doReadSync(); // 同步读取，返回完整的列表数据

        // 打印每一行读取到的数据
        for (XingqiuTableUserInfo xingQiuTableUserInfo : list) {
            System.out.println(xingQiuTableUserInfo);
        }
    }
}