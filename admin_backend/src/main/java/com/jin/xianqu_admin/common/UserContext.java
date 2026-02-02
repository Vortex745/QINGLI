package com.jin.xianqu_admin.common;

/**
 * 用户上下文，用于存储当前线程登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<Long> userHolder = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        userHolder.set(userId);
    }

    public static Long getUserId() {
        return userHolder.get();
    }

    public static void remove() {
        userHolder.remove();
    }
}
