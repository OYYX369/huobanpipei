package com.yupi.yupao;


import com.yupi.yupao.mapper.UserMapper;
import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.service.UserService;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@SpringBootTest
class InsertUserTest {
    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    @Test
     void doConcurrencyInsertUser() {
        long startTime = System.currentTimeMillis();
        final int INSERT_NUM = 100000; // 总插入数据量
        final int batchSize = 1000; // 每批次处理的数据量
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // 根据批次大小分割任务
        for (int i = 0; i < Math.ceil((double) INSERT_NUM / batchSize); i++) {
            List<User> userList = new ArrayList<>();
            // 创建每批次的用户数据
            for (int j = 0; j < batchSize; j++) {
                User user = new User();
                user.setUsername("假Hsu");
                user.setUserAccount("fakeHsu");
                user.setAvatarUrl("https://thirdwx.qlogo.cn/mmopen/vi_32/PiajxSqBRaELkfM4IsxxWrB70flGuaDcq55mDxh8r4DuwOJLuluSmRCH9Pk1MFibry5icVgHtfwMmnYGqT49svVKV3X1wMer2OCC3ob5leZX5lF8HMbPo1Qww/132");
                user.setProfile("你好啊");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123456789101");
                user.setEmail("Hsu@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("24525");
                user.setTags("[]");
                userList.add(user);
            }
            // 异步执行数据库插入操作
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userService.saveBatch(userList, batchSize);
            });
            futureList.add(future);
        }
        // 等待所有异步任务完成
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("程序运行时间：" + elapsedTime + " 毫秒");

    }
    @Test
    void doInsertUser() {
        long startTime = System.currentTimeMillis();
        final int INSERT_NUM = 100000; // 定义插入的用户数量

        List<User> userList=new ArrayList<>();  //建立一个列表，存入数据，然后批量插入
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            // 配置用户信息
            user.setUsername("假Hsu");
            user.setUserAccount("fakeHsu");
            user.setAvatarUrl("https://thirdwx.qlogo.cn/mmopen/vi_32/PiajxSqBRaELkfM4IsxxWrB70flGuaDcq55mDxh8r4DuwOJLuluSmRCH9Pk1MFibry5icVgHtfwMmnYGqT49svVKV3X1wMer2OCC3ob5leZX5lF8HMbPo1Qww/132");
            user.setProfile("你好啊");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123456789101");
            user.setEmail("Hsu@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("24525");
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList,2000);// 插入用户
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("程序运行时间：" + elapsedTime + " 毫秒");
    }


}
