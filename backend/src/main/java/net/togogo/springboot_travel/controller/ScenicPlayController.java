package net.togogo.springboot_travel.controller;

import jakarta.annotation.Resource;
import net.togogo.springboot_travel.Result.Result;
import net.togogo.springboot_travel.entity.ScenicPlay;
import net.togogo.springboot_travel.service.ScenicPlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/scenic/play")
public class ScenicPlayController {

    @Resource
    private ScenicPlayService scenicPlayService;

    /**
     * 根据景区ID获取游玩推荐
     * 接口地址：GET /api/scenic/play/list?scenic_id=2
     */
    @GetMapping("/list")
    public Result<List<ScenicPlay>> getList(@RequestParam("scenic_id") Integer scenicId) {
        return scenicPlayService.getPlayListByScenicId(scenicId);
    }
}