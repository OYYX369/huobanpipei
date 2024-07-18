package com.yupi.yupao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;

@SpringBootTest
public class RedisTest {

        @Test
        public static void main(String[] args) {
            Jedis jedis = new Jedis("43.139.79.107",6379);
            jedis.auth("123456");					// redis密码
            System.out.println(jedis.ping());	// 结果输出应该为：PONG
        }

    //
//    @Resource
//    private RedisTemplate redisTemplate;
//    @Test
//    void test(){
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//        //增
//        valueOperations.set("yupiString","456");
//        valueOperations.set("yupiInt",1);
//        valueOperations.set("yupiDouble",2.0);
//        User user = new User();
//        user.setId(1L);
//        user.setUsername("yupi");
//        valueOperations.set("yupiUser",user);
//        //查
//        Object yupi = valueOperations.get("yupiString");
//        Assertions.assertTrue("456".equals((String)yupi));
//        yupi = valueOperations.get("yupiInt");
//        Assertions.assertTrue(1==((Integer)yupi));
//        yupi = valueOperations.get("yupiDouble");
//        Assertions.assertTrue(2.0==((Double)yupi));
//        System.out.println(valueOperations.get("yupiUser"));
////        valueOperations.set("yupiString","dog");
////        redisTemplate.delete("yupiString");
//    }
}