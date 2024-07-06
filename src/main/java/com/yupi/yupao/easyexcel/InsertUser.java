package com.yupi.yupao.easyexcel;

import com.yupi.yupao.mapper.UserMapper;
import com.yupi.yupao.model.domain.User;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class InsertUser {
    @Resource
    private UserMapper userMapper;
    /**
     * 执行用户数据的循环插入操作。
     * 创建了5000个用户实例，并逐一插入数据库中。
     */
    public void doInsertUser() {

        long startTime = System.currentTimeMillis();
        final int INSERT_NUM = 1000; // 定义插入的用户数量
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
            userMapper.insert(user); // 插入用户
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("程序运行时间：" + elapsedTime + " 毫秒");
    }


}