package net.togogo.springboot_travel.controller;

import jakarta.annotation.Resource;
import net.togogo.springboot_travel.entity.Banner;
import net.togogo.springboot_travel.service.BannerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/banner")
public class BannerController {

    @Resource
    private BannerService bannerService;

    /**
     * 获取轮播图列表（适配前端返回格式：{bannerList: [...]}）
     * GET http://localhost:8080/api/banner/list
     */
    @GetMapping("/list")
    public Map<String, List<Banner>> getBannerList() {
        List<Banner> bannerList = bannerService.list();
        Map<String, List<Banner>> result = new HashMap<>();
        result.put("bannerList", bannerList); // 前端接收的key是bannerList，需匹配
        return result;
    }
}