package com.yupi.yupao.service;

import com.yupi.yupao.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

//�û��������

@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;



    @Test
    void searchUsersByTags() {
        //括号里面 字符串转换为 字符串集合
        List<String> tagNameList= Arrays.asList("java","c++");
        List<User> userList = userService.searchUsersByTagsSQL(tagNameList);
        Assertions.assertNotNull(userList);
    }

    @Test
    void userRegister() {
        String userAccount="oyyx";
        String userPassword="12345678";
        String checkPassword="12345678";
        String planetCode="5004";
        long result= userService.userRegister(userAccount,userPassword,checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

    }
}