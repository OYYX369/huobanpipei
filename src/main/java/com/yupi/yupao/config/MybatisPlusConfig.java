package com.yupi.yupao.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus配置类，用于配置数据库相关的中间件功能，如分页。
 * @ClassName: MybatisPlusConfig
 * @Author: Hsu琛君珩
 * @Date: 2024-04-05
 * @Version: v1.0
 * @apiNote 该配置主要是为了添加MyBatis Plus的分页插件，允许对数据库查询进行自动分页处理，
 *          这样可以提高应用的响应速度和数据处理能力。配置适用于MySQL数据库。
 */
@Configuration
@MapperScan("com.yupi.yupao.mapper") // 适当调整以匹配你的Mapper组件的包路径
public class MybatisPlusConfig {

    /**
     * 配置MyBatis Plus的分页插件。
     * @return 返回配置了分页功能的MybatisPlusInterceptor。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加针对MySQL数据库的分页拦截器，自动处理分页逻辑
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}