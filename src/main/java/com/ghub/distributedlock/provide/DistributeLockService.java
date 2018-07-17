package com.ghub.distributedlock.provide;


/**
 * @author loufeng
 * @date 2018/7/17 上午10:58.
 */
public interface DistributeLockService {
    /**
     * 基本设置，按key锁定n秒
     *
     * @param key     锁key
     * @param seconds 秒数
     * @return true、false
     */
    boolean lock(String key, int seconds);

    /**
     * 释放锁
     *
     * @param key 锁key
     * @return true、false
     */
    boolean release(String key);
}
