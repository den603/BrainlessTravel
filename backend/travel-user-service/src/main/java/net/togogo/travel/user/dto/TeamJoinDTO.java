package net.togogo.travel.user.dto;

import lombok.Data;

/**
 * 加入组队请求参数 DTO
 * 仅需传递要加入的组队ID
 */
@Data
public class TeamJoinDTO {
    private Long teamId;
}
