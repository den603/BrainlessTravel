package net.togogo.springboot_travel.controller;

import net.togogo.springboot_travel.Result.Result;
import net.togogo.springboot_travel.entity.Traveler;
import net.togogo.springboot_travel.service.TravelerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/traveler")
public class TravelerController {

    @Autowired
    private TravelerService travelerService;

    // 新增出行人
    @PostMapping("/add")
    public Result<Boolean> addTraveler(@RequestBody Traveler traveler) {
        boolean save = travelerService.save(traveler);
        return save ? Result.success(true) : Result.fail("新增失败");
    }

    // 修改出行人
    @PostMapping("/update")
    public Result<Boolean> updateTraveler(@RequestBody Traveler traveler) {
        boolean update = travelerService.updateById(traveler);
        return update ? Result.success(true) : Result.fail("修改失败");
    }

    // 删除出行人
    @PostMapping("/delete/{id}")
    public Result<Boolean> deleteTraveler(@PathVariable Long id) {
        boolean remove = travelerService.removeById(id);
        return remove ? Result.success(true) : Result.fail("删除失败");
    }

    // 根据用户ID查询出行人
    @GetMapping("/list/{userId}")
    public Result<List<Traveler>> getTravelers(@PathVariable Long userId) {
        List<Traveler> travelers = travelerService.getTravelersByUserId(userId);
        return Result.success(travelers);
    }
}