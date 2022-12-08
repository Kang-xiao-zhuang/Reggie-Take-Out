package com.zhuang.reggie_take_out.common;

/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 */
public class BaseContext {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    // 构造方法
    public BaseContext() {

    }

    /**
     * 设置值
     *
     * @param id Long
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取值
     *
     * @return Long
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }
}