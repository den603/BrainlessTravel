package net.togogo.springboot_travel.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.togogo.springboot_travel.dto.TeamCreateDTO;
import net.togogo.springboot_travel.dto.TeamQueryDTO;
import net.togogo.springboot_travel.entity.Team;
import net.togogo.springboot_travel.entity.TeamMember;
import net.togogo.springboot_travel.entity.User;
import net.togogo.springboot_travel.mapper.TeamMapper;
import net.togogo.springboot_travel.mapper.TeamMemberMapper;
import net.togogo.springboot_travel.mapper.UserMapper;
import net.togogo.springboot_travel.service.TeamService;
import net.togogo.springboot_travel.vo.TeamDetailVO;
import net.togogo.springboot_travel.vo.TeamListVO;
import net.togogo.springboot_travel.vo.TeamMemberVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private UserMapper userMapper;

    // ====================== 1. 创建组队（原有逻辑，兼容不变） ======================
    @Override
    @Transactional
    public Long createTeam(Long userId, TeamCreateDTO dto) {
        Team team = new Team();
        BeanUtils.copyProperties(dto, team);
        team.setCreatorId(userId);
        team.setCurrentPeople(1);
        team.setStatus(1);
        team.setIsDelete(0);
        teamMapper.insert(team);

        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(userId);
        member.setRole(1);
        member.setJoinTime(LocalDateTime.now());
        member.setIsDelete(0);
        teamMemberMapper.insert(member);
        return team.getId();
    }

    // ====================== 2. 大厅列表（过滤：未删除+未取消+招募中） ======================
    @Override
    public Page<TeamListVO> queryTeamList(TeamQueryDTO queryDTO, int pageNum, int pageSize) {
        Page<Team> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        // 核心：只展示 未逻辑删除 + 未取消 + 招募中
        wrapper.eq(Team::getIsDelete, 0)
                .ne(Team::getStatus, 4)
                .eq(Team::getStatus,1);

        // 原有筛选逻辑
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.and(w -> w.like(Team::getDestination, queryDTO.getKeyword())
                    .or()
                    .like(Team::getDescription, queryDTO.getKeyword()));
        }
        if (queryDTO.getDestination() != null && !queryDTO.getDestination().isEmpty() && !"不限".equals(queryDTO.getDestination())) {
            wrapper.like(Team::getDestination, queryDTO.getDestination());
        }
        if (queryDTO.getDate() != null) {
            wrapper.le(Team::getStartDate, queryDTO.getDate())
                    .ge(Team::getEndDate, queryDTO.getDate());
        }
        if (queryDTO.getPeopleRange() != null) {
            switch (queryDTO.getPeopleRange()) {
                case 1: wrapper.eq(Team::getTotalPeople, 2); break;
                case 2: wrapper.between(Team::getTotalPeople, 3, 5); break;
                case 3: wrapper.between(Team::getTotalPeople, 6, 10); break;
                case 4: wrapper.gt(Team::getTotalPeople, 10); break;
            }
        }
        wrapper.orderByDesc(Team::getCreateTime);
        Page<Team> teamPage = teamMapper.selectPage(page, wrapper);
        return convertToTeamListVOPage(teamPage);
    }

    // ====================== 3. 组队详情（过滤已取消/已删除） ======================
    @Override
    public TeamDetailVO getTeamDetail(Long teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null || team.getIsDelete() == 1 || team.getStatus() == 4) {
            throw new RuntimeException("组队不存在或已取消");
        }
        TeamDetailVO vo = new TeamDetailVO();
        BeanUtils.copyProperties(team, vo);
        User creator = userMapper.selectById(team.getCreatorId());
        if (creator != null) {
            vo.setCreatorId(creator.getId());
            vo.setCreatorName(creator.getNickName() != null ? creator.getNickName() : "匿名用户");
            vo.setCreatorAvatar(creator.getAvatarUrl());
        }
        List<TeamMember> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getIsDelete, 0));
        List<TeamMemberVO> memberVOs = members.stream().map(m -> {
            TeamMemberVO mvo = new TeamMemberVO();
            mvo.setUserId(m.getUserId());
            mvo.setRole(m.getRole());
            mvo.setJoinTime(m.getJoinTime());
            User u = userMapper.selectById(m.getUserId());
            if (u != null) {
                mvo.setNickName(u.getNickName() != null ? u.getNickName() : "匿名用户");
                mvo.setAvatarUrl(u.getAvatarUrl());
            }
            return mvo;
        }).collect(Collectors.toList());
        vo.setMembers(memberVOs);
        return vo;
    }

    // ====================== 4. 加入组队（原有逻辑，兼容不变） ======================
    @Override
    @Transactional
    public void joinTeam(Long userId, Long teamId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null || team.getIsDelete() == 1 || team.getStatus() != 1) {
            throw new RuntimeException("组队不存在或已停止招募");
        }
        Long count = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getIsDelete, 0));
        if (count > 0) throw new RuntimeException("您已加入该组队");
        if (team.getCurrentPeople() >= team.getTotalPeople()) throw new RuntimeException("组队人数已满");

        TeamMember member = new TeamMember();
        member.setTeamId(teamId);
        member.setUserId(userId);
        member.setJoinTime(LocalDateTime.now());
        member.setRole(2);
        member.setIsDelete(0);
        teamMemberMapper.insert(member);

        team.setCurrentPeople(team.getCurrentPeople() + 1);
        if (team.getCurrentPeople() >= team.getTotalPeople()) {
            team.setStatus(2);
        }
        teamMapper.updateById(team);
    }

    // ====================== 5. 我创建的组队（过滤：未删除+未取消） ======================
    @Override
    public Page<TeamListVO> getMyCreatedTeams(Long userId, int pageNum, int pageSize) {
        Page<Team> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<Team>()
                .eq(Team::getCreatorId, userId)
                .eq(Team::getIsDelete, 0)
                .ne(Team::getStatus,4)
                .orderByDesc(Team::getCreateTime);
        Page<Team> teamPage = teamMapper.selectPage(page, wrapper);
        return convertToTeamListVOPage(teamPage);
    }

    // ====================== 6. 我加入的组队（过滤：未删除+未取消+成员未退出） ======================
    @Override
    public Page<TeamListVO> getMyJoinedTeams(Long userId, int pageNum, int pageSize) {
        // 只查未退出的成员关联
        List<TeamMember> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getIsDelete, 0));
        if (members.isEmpty()) return new Page<>(pageNum, pageSize, 0);
        List<Long> teamIds = members.stream().map(TeamMember::getTeamId).collect(Collectors.toList());

        Page<Team> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<Team>()
                .in(Team::getId, teamIds)
                .eq(Team::getIsDelete, 0)
                .ne(Team::getStatus, 4)
                .orderByDesc(Team::getCreateTime);
        Page<Team> teamPage = teamMapper.selectPage(page, wrapper);
        return convertToTeamListVOPage(teamPage);
    }

    // ====================== 7. 【完善】队长取消组队（前端全隐藏） ======================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTeam(Long userId, Long teamId) {
        // 1. 校验队伍存在+权限
        Team team = teamMapper.selectById(teamId);
        if (team == null || team.getIsDelete() == 1) {
            throw new RuntimeException("组队不存在");
        }
        if (!team.getCreatorId().equals(userId)) {
            throw new RuntimeException("仅队长可取消组队");
        }

        // 2. 核心：标记取消+逻辑删除（前端所有列表自动过滤）
        team.setStatus(4);
        team.setIsDelete(1);
        teamMapper.updateById(team);

        // 3. 批量逻辑删除所有成员（退出关联）
        teamMemberMapper.update(null,
                new LambdaUpdateWrapper<TeamMember>() //
                        .eq(TeamMember::getTeamId, teamId) // WHERE 条件：teamId = ?
                        .set(TeamMember::getIsDelete, 1)    // SET 字段：is_delete = 1
        );
    }

    // ====================== 8. 【完善】队员退出组队（我的加入列表隐藏） ======================
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitTeam(Long userId, Long teamId) {
        // 1. 校验队伍+成员身份
        Team team = teamMapper.selectById(teamId);
        if (team == null || team.getIsDelete() == 1 || team.getStatus() == 4) {
            throw new RuntimeException("组队不存在或已取消");
        }

        TeamMember member = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getIsDelete, 0));
        if (member == null) {
            throw new RuntimeException("您未加入该组队");
        }
        // 队长不能退出，只能取消
        if (member.getRole() == 1) {
            throw new RuntimeException("队长无法退出，请取消组队");
        }

        // 2. 逻辑删除成员（我的加入列表不再查询到）
        member.setIsDelete(1);
        teamMemberMapper.updateById(member);

        // 3. 更新人数+状态
        team.setCurrentPeople(team.getCurrentPeople() - 1);
        // 满员→招募中
        if (team.getStatus() == 2 && team.getCurrentPeople() < team.getTotalPeople()) {
            team.setStatus(1);
        }
        // 人数为0→标记结束
        if (team.getCurrentPeople() <= 0) {
            team.setStatus(3);
        }
        teamMapper.updateById(team);
    }

    // ====================== 私有转换方法（原有不变） ======================
    private Page<TeamListVO> convertToTeamListVOPage(Page<Team> teamPage) {
        Page<TeamListVO> voPage = new Page<>(teamPage.getCurrent(), teamPage.getSize(), teamPage.getTotal());
        List<TeamListVO> records = teamPage.getRecords().stream().map(team -> {
            TeamListVO vo = new TeamListVO();
            BeanUtils.copyProperties(team, vo);
            User creator = userMapper.selectById(team.getCreatorId());
            if (creator != null) {
                vo.setCreatorId(creator.getId());
                vo.setCreatorName(creator.getNickName() != null ? creator.getNickName() : "匿名用户");
                vo.setCreatorAvatar(creator.getAvatarUrl());
            }
            return vo;
        }).collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }
}