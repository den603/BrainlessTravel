package net.togogo.springboot_travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.togogo.springboot_travel.dto.TeamCreateDTO;
import net.togogo.springboot_travel.dto.TeamQueryDTO;
import net.togogo.springboot_travel.vo.TeamDetailVO;
import net.togogo.springboot_travel.vo.TeamListVO;
/**
 * 组队业务逻辑接口
 * 定义组队模块的核心业务方法：
 * - 发布组队
 * - 查询组队列表（带筛选分页）
 * - 查看详情
 * - 加入/退出组队
 * - 队长取消组队
 * - 查询我创建/加入的组队
 */
public interface TeamService {
    // 发布组队
    Long createTeam(Long userId, TeamCreateDTO dto);

    // 分页查询大厅列表
    Page<TeamListVO> queryTeamList(TeamQueryDTO queryDTO, int pageNum, int pageSize);

    // 查看组队详情
    TeamDetailVO getTeamDetail(Long teamId);

    // 加入组队
    void joinTeam(Long userId, Long teamId);

    // 我创建的组队
    Page<TeamListVO> getMyCreatedTeams(Long userId, int pageNum, int pageSize);

    // 我加入的组队
    Page<TeamListVO> getMyJoinedTeams(Long userId, int pageNum, int pageSize);

    // 取消组队（队长）
    void cancelTeam(Long userId, Long teamId);

    // 退出组队（队员）
    void quitTeam(Long userId, Long teamId);
}