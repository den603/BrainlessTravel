package net.togogo.travel.user.dto;

import lombok.Data;

/**
 * 小程序登录请求参数
 */
@Data
public class WeChatLoginDTO {
    /**
     * 小程序登录临时凭证code
     */
    private String code;
}