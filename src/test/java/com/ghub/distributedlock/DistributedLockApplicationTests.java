package com.ghub.distributedlock;

import com.ghub.distributedlock.provide.DistributeLockService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DistributedLockApplicationTests {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DistributeLockService distributeLockService;

    @Test
    public void testString() {
        stringRedisTemplate.opsForValue().set("aaa", "111", 20, TimeUnit.SECONDS);
        Assert.assertEquals("111", stringRedisTemplate.opsForValue().get("aaa"));
        System.out.println(stringRedisTemplate.opsForValue().get("aaa"));
    }

    @Test
    public void testObj() throws InterruptedException {
        User user = new User("xxxx", 18);
        ValueOperations<String, User> operations = redisTemplate.opsForValue();
        operations.set("setUser", user, 1, TimeUnit.SECONDS);
        Thread.sleep(1000);
        boolean exists = redisTemplate.hasKey("setUser");
        if (exists) {
            System.out.println("exists is true");
        } else {
            System.out.println("exists is false");
        }
    }

    @Test
    public void testLock() throws InterruptedException {
        String key = "test";
        boolean success = distributeLockService.lock(key, 10);
        System.out.println("lock success:" + success);
        boolean success1 = distributeLockService.lock(key, 5);
        System.out.println("lock success1:" + success1);
        Thread.sleep(10000);
        boolean success2 = distributeLockService.lock(key, 5);
        System.out.println("lock success2:" + success2);
    }

    @Test
    public void testLock1() throws InterruptedException {
        String key = "test";
        boolean success = distributeLockService.lock(key, 10);
        System.out.println("lock success:" + success);
        distributeLockService.release(key);
        boolean success2 = distributeLockService.lock(key, 5);
        System.out.println("lock success2:" + success2);
    }

    class User {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

}
