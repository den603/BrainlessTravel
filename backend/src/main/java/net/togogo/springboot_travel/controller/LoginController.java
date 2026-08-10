package net.togogo.springboot_travel.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import net.togogo.springboot_travel.Result.Result;
import net.togogo.springboot_travel.dto.WeChatLoginDTO;
import net.togogo.springboot_travel.entity.User;
import net.togogo.springboot_travel.service.UserService;
import net.togogo.springboot_travel.util.JwtUtil;
import org.springframework.web.bind.annotation.*;



/**
 * 登录接口控制器
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @Resource
    private UserService userService;

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 小程序登录接口（接收code，返回token）
     * 请求示例：POST /api/login/wechat {"code":"0c3N5g1w3Ka1R638tN3w3AqinJ2N5g1r"}
     */
    @PostMapping("/wechat")
    public Result<String> weChatLogin(@RequestBody WeChatLoginDTO loginDTO) {
        try {
            // 调用Service生成token
            String token = userService.loginByWeChat(loginDTO.getCode());
            return Result.success(token); // 返回token
        } catch (Exception e) {
            return Result.fail("登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户信息接口（需要携带token）
     * 请求示例：GET /api/login/userInfo
     * 请求头：Authorization: Bearer {token}
     */
    @GetMapping("/userInfo")
    public Result<User> getUserInfo(HttpServletRequest request) {
        try {
            // 1. 从请求头获取token（前端需在请求头携带：Bearer + 空格 + token）
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Result.fail("token无效");
            }
            token = token.replace("Bearer ", "");

            // 2. 验证token并解析openid
            if (!jwtUtil.validateToken(token)) {
                return Result.fail("token已过期或无效");
            }
            String openid = jwtUtil.getOpenidFromToken(token);

            // 3. 查询用户信息并返回
            User user = userService.getUserByOpenid(openid);
            return Result.success(user);
        } catch (Exception e) {
            return Result.fail("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户昵称和头像
     * 请求示例：POST /api/login/updateUserInfo
     * 请求头：Authorization: Bearer {token}
     * 请求体：{"nickName":"张三","avatarUrl":"https://xxx.jpg"}
     */
    @PostMapping("/updateUserInfo")
    public Result<Boolean> updateUserInfo(
            HttpServletRequest request,
            @RequestBody User userDTO
    ) {
        try {
            // 1. 解析token获取openid
            String token = request.getHeader("Authorization").replace("Bearer ", "");
            String openid = jwtUtil.getOpenidFromToken(token);

            // 2. 更新用户信息
            boolean success = userService.updateUserInfo(
                    openid,
                    userDTO.getNickName(),
                    userDTO.getAvatarUrl()
            );
            return success ? Result.success(true) : Result.fail("更新失败");
        } catch (Exception e) {
            return Result.fail("更新用户信息失败：" + e.getMessage());
        }
    }
}