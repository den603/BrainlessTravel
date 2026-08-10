package net.togogo.springboot_travel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.togogo.springboot_travel.Result.Result;
import net.togogo.springboot_travel.dto.TeamCreateDTO;
import net.togogo.springboot_travel.dto.TeamJoinDTO;
import net.togogo.springboot_travel.dto.TeamQueryDTO;
import net.togogo.springboot_travel.entity.User;
import net.togogo.springboot_travel.service.TeamService;
import net.togogo.springboot_travel.service.UserService;
import net.togogo.springboot_travel.util.JwtUtil;
import net.togogo.springboot_travel.vo.TeamDetailVO;
import net.togogo.springboot_travel.vo.TeamListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
/**
 * 组队模块接口控制器
 * 基础路径：/api/team
 *
 * 鉴权说明：
 * - 除大厅列表(/list)和详情(/detail/{id})外，其他接口均需携带 JWT token
 * - token 通过请求头 Authorization 传递，格式为 "Bearer {token}"
 * - 通过 JwtUtil 解析 token 获取 openid，再查询 User 表得到当前用户 ID
 *
 * 统一响应格式：使用 Result 类封装，code=1 表示成功，code=0 表示失败
 *
 * 接口列表：
 * - POST   /create          发布组队
 * - GET    /list            大厅列表（公开）
 * - GET    /detail/{id}     组队详情（公开）
 * - POST   /join            加入组队
 * - GET    /my/created      我创建的组队
 * - GET    /my/joined       我加入的组队
 * - POST   /cancel/{id}     取消组队（队长）
 * - POST   /quit/{id}       退出组队（队员）
 */
@RestController
@RequestMapping("/team")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    // 从token中解析出当前用户ID
    private Long getCurrentUserId(@RequestHeader("Authorization") String token) {
        String realToken = token.replace("Bearer ", "");
        if (!jwtUtil.validateToken(realToken)) {
            throw new RuntimeException("token无效或已过期");
        }
        String openid = jwtUtil.getOpenidFromToken(realToken);
        User user = userService.getUserByOpenid(openid);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getId();
    }

    @PostMapping("/create")
    public Result<Long> createTeam(@RequestBody TeamCreateDTO dto,
                                   @RequestHeader("Authorization") String token) {
        Long userId = getCurrentUserId(token);
        Long teamId = teamService.createTeam(userId, dto);
        return Result.success(teamId);
    }

    @GetMapping("/list")
    public Result<Page<TeamListVO>> listTeams(TeamQueryDTO queryDTO,
                                              @RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        Page<TeamListVO> page = teamService.queryTeamList(queryDTO, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/detail/{teamId}")
    public Result<TeamDetailVO> getTeamDetail(@PathVariable Long teamId) {
        TeamDetailVO vo = teamService.getTeamDetail(teamId);
        return Result.success(vo);
    }

    @PostMapping("/join")
    public Result<String> joinTeam(@RequestBody TeamJoinDTO dto,
                                   @RequestHeader("Authorization") String token) {
        Long userId = getCurrentUserId(token);
        teamService.joinTeam(userId, dto.getTeamId());
        return Result.success("加入成功");
    }

    @GetMapping("/my/created")
    public Result<Page<TeamListVO>> myCreatedTeams(@RequestHeader("Authorization") String token,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId(token);
        Page<TeamListVO> page = teamService.getMyCreatedTeams(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/my/joined")
    public Result<Page<TeamListVO>> myJoinedTeams(@RequestHeader("Authorization") String token,
                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                  @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getCurrentUserId(token);
        Page<TeamListVO> page = teamService.getMyJoinedTeams(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping("/cancel/{teamId}")
    public Result<String> cancelTeam(@PathVariable Long teamId,
                                     @RequestHeader("Authorization") String token) {
        Long userId = getCurrentUserId(token);
        teamService.cancelTeam(userId, teamId);
        return Result.success("组队已取消");
    }

    @PostMapping("/quit/{teamId}")
    public Result<String> quitTeam(@PathVariable Long teamId,
                                   @RequestHeader("Authorization") String token) {
        Long userId = getCurrentUserId(token);
        teamService.quitTeam(userId, teamId);
        return Result.success("已退出组队");
    }
}