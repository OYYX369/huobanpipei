package com.yupi.yupao.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.model.domain.User;
import com.yupi.yupao.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务类，用于预缓存推荐用户列表。
 * 旨在减少高峰时段数据库的压力，提高用户获取推荐列表的响应速度。
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Resource
    private RedissonClient redissonClient;

    // 重点用户列表，目前包含一个用户ID为15的示例
    private List<Long> mainUserList = Arrays.asList(15L);

    /**
     * 定时任务，每天22点04分执行，为重点用户预缓存推荐列表。
     * 默认查询前20个用户并设置30秒的缓存过期时间。
     */
    @Scheduled(cron = "0 31 0 * * *")
    public void doCacheRecommendUser() {
        // 获取分布式锁，防止多个任务同时执行
        RLock lock = redissonClient.getLock("youyuan:precachejob:docache:lock");
        try {
            // 尝试立即获取锁，如果成功获取到锁，则执行下面的代码块；如果获取失败，则不进行等待，直接执行后续的逻辑
            if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)) {
                System.out.println("getLock: " + Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    //查询数据库获取用户列表
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("ousir:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    // 尝试写入缓存，缓存期限设置为30秒
                    try {
                        valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        }catch (InterruptedException e) {
            log.error("doCacheRecommendUser error",e);
        } finally {
            //释放锁
            if (lock.isHeldByCurrentThread()){
                System.out.println("unlock:" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
