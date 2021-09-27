package com.covid.dashboard.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.IOException;


@Slf4j
@Configuration
public class RedisConfig {

    @Value("${redis.config.file.path}")
    private String redisConfigFilePath;

    @Bean
    public RedissonClient redissonClient() {
        log.info("Start:: Inside createConn method, this method will create connection to Redis and create client");
        Config config = new Config();
        try {
            config = Config.fromYAML(ResourceUtils.getFile(redisConfigFilePath));
            config.setCodec(new StringCodec());
            return Redisson.create(config);
        }catch(IOException e){
            log.error("Exception Occurred while creating Redis connection : ",e.getMessage());
        }
        return Redisson.create(config);
    }

}
