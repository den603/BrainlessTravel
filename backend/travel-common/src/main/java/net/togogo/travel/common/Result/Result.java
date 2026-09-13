package net.togogo.travel.common.Result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 统一返回结果封装类
 *
 * 【迁移说明】从原单体项目 net.togogo.springboot_travel.Result.Result 原样迁移，
 * 包名改为 net.togogo.travel.common.Result，业务逻辑完全不变。
 *
 * @param <T> 数据泛型，支持任意返回数据类型
 */
@Data
// 排除值为null的字段（如果有），但msg要保留空字符串
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    /**
     * 响应状态码：1=成功，0=失败
     */
    private int code;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应提示信息（失败时可返回具体原因，成功时默认空字符串）
     */
    private String msg = ""; // 初始化为空字符串，匹配示例格式

    // 私有构造器，避免外部直接实例化，统一通过静态方法创建
    private Result() {}

    /**
     * 成功响应（带返回数据，msg为空字符串）
     * @param data 要返回的业务数据
     * @param <T> 数据类型
     * @return 封装后的成功响应对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setData(data);
        result.setMsg(""); // 强制设为空字符串，匹配示例
        return result;
    }

    /**
     * 成功响应（无返回数据，msg为空字符串）
     * @param <T> 数据类型（默认Void）
     * @return 封装后的成功响应对象
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setData(null);
        result.setMsg("");
        return result;
    }

    /**
     * 失败响应（自定义失败消息）
     * @param msg 失败提示信息
     * @param <T> 数据类型
     * @return 封装后的失败响应对象
     */
    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setData(null);
        result.setMsg(msg); // 失败时传自定义消息
        return result;
    }

    /**
     * 失败响应（默认失败消息）
     * @param <T> 数据类型
     * @return 封装后的失败响应对象
     */
    public static <T> Result<T> fail() {
        return fail("操作失败");
    }

    /**
     * 失败响应（自定义状态码+消息，便于扩展异常场景）
     * @param code 自定义失败状态码
     * @param msg 失败提示信息
     * @param <T> 数据类型
     * @return 封装后的失败响应对象
     */
    public static <T> Result<T> fail(int code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setData(null);
        result.setMsg(msg);
        return result;
    }
}
