package net.togogo.travel.user.controller;

import net.togogo.travel.common.Result.Result;
import net.togogo.travel.user.entity.PrivateGuide;
import net.togogo.travel.user.entity.TravelGroup;
import net.togogo.travel.user.mapper.PrivateGuideMapper;
import net.togogo.travel.user.mapper.TravelGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

@RestController
@RequestMapping("/base")
public class BaseDataController {

    @Autowired
    private TravelGroupMapper travelGroupMapper;
    @Autowired
    private PrivateGuideMapper privateGuideMapper;

    // 获取老年团列表
    @GetMapping("/group/list")
    public Result<List<TravelGroup>> getTravelGroupList() {
        List<TravelGroup> list = travelGroupMapper.selectList(null);
        return Result.success(list);
    }

    // 获取私人导游列表
    @GetMapping("/guide/list")
    public Result<List<PrivateGuide>> getPrivateGuideList() {
        List<PrivateGuide> list = privateGuideMapper.selectList(null);
        return Result.success(list);
    }

    // 获取单个老年团详情
    @GetMapping("/group/detail/{id}")
    public Result<TravelGroup> getTravelGroupDetail(@PathVariable Long id) {
        TravelGroup group = travelGroupMapper.selectById(id);
        return Result.success(group);
    }

    // 获取单个导游详情
    @GetMapping("/guide/detail/{id}")
    public Result<PrivateGuide> getPrivateGuideDetail(@PathVariable Long id) {
        PrivateGuide guide = privateGuideMapper.selectById(id);
        return Result.success(guide);
    }
}