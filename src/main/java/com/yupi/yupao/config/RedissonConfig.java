package com.yupi.yupao.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类，用于配置Redisson客户端连接
 * Redisson是一个基于Redis的Java驻留式内存数据网格（In-Memory Data Grid），用于分布式应用程序，提供分布式Java对象和服务支持。
 * @ClassName: RedissonConfig
 * @author Hsu琛君珩
 * @date 2024-05-20
 * @Version: v1.0
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")   // 用于将配置文件中以特定前缀开头的属性值映射到一个Java Bean 中。
@Data
public class RedissonConfig {

    private String host;// Redis主机地址
    private String port;// Redis端口号

    /**
     * 配置Redisson客户端连接
     * @return 返回RedissonClient对象，用于与Redis交互
     */
    @Bean
    public RedissonClient redissonClient(){
        // 1. 创建配置
        Config config = new Config();
        // 拼接Redis地址
        String redisAddress = String.format("redis://%s:%s", host, port);
        //  使用单个Redis，没有开集群 useClusterServers()  设置地址和使用库
        config.useSingleServer()
                .setAddress(redisAddress)
                .setDatabase(2)
                .setPassword("123456");    // 写到库3
        // 2. 创建实例ou
        return Redisson.create(config);
    }

}

