package com.ghub.distributedlock.provide.impl;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.ghub.distributedlock.provide.DistributeLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import java.util.UUID;

/**
 * dubbo 服务提供锁相关服务
 *
 * @author loufeng
 * @date 2018/7/17 上午11:00.
 */
@Service(version = "${demo.service.version}",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}")
public class DistributeLockServiceImpl implements DistributeLockService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributeLockService.class);
    @Autowired
    private RedisTemplate redisTemplate;
    private static final String UNLOCK_LUA;

    static {
        UNLOCK_LUA = "if redis.call(\"get\",KEYS[1]) == ARGV[1] " +
                "then " +
                "    return redis.call(\"del\",KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end ";
    }

    @Override
    public boolean lock(String key, int expire) {
        return setRedis(key, expire);
    }

    @Override
    public boolean release(String key) {
        // 使用lua脚本删除redis中匹配value的key，可以避免由于方法执行时间过长而redis锁自动过期失效的时候误删其他线程的锁
        // spring自带的执行脚本方法中，集群模式直接抛出不支持执行脚本的异常，所以只能拿到原redis的connection来执行脚本
        try {
            Long result = (Long) redisTemplate.execute((RedisCallback<Long>) connection -> {
                Object nativeConnection = connection.getNativeConnection();
                // 集群模式和单机模式虽然执行脚本的方法一样，但是没有共同的接口，所以只能分开执行
                // 集群模式
                if (nativeConnection instanceof JedisCluster) {
                    return (Long) ((JedisCluster) nativeConnection).eval(UNLOCK_LUA, key);
                }

                // 单机模式
                else if (nativeConnection instanceof Jedis) {
                    return (Long) ((Jedis) nativeConnection).eval(UNLOCK_LUA, 1, key);
                }
                return 0L;
            });

            return result != null && result > 0;
        } catch (Exception e) {
            LOGGER.error("release lock occured an exception", e);
        }
        return false;
    }

    /**
     * 执行NX set原子操作
     *
     * @param key    缓存key
     * @param expire 过期时间
     * @return true/false
     */
    private boolean setRedis(String key, long expire) {
        try {
            String result = (String) redisTemplate.execute((RedisCallback<String>) connection -> {
                JedisCommands commands = (JedisCommands) connection.getNativeConnection();
                String uuid = UUID.randomUUID().toString();
                return commands.set(key, uuid, "NX", "PX", expire);
            });
            return !StringUtils.isEmpty(result);
        } catch (Exception e) {
            LOGGER.error("set redis occured an exception", e);
        }
        return false;
    }
}
