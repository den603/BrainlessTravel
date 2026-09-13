package net.togogo.travel.common.util;

/**
 * 用户上下文：从网关转发的请求头 X-User-Id 中获取当前用户ID
 * 替代原单体架构中的 LoginInterceptor.getCurrentUserId()
 *
 * 【新增文件】微服务改造后，JWT 校验统一收敛到网关层，
 * 网关解析出 userId 后写入请求头 X-User-Id，下游服务由 UserIdInterceptor
 * 读取该请求头并写入本 ThreadLocal，业务层统一用 UserContext.getUserId() 获取。
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前请求的用户ID（由下游服务的 UserIdInterceptor 调用）
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前登录用户ID；未登录场景返回 null
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 清理 ThreadLocal，防止线程池复用导致的用户身份串号与内存泄漏
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
