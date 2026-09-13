package net.togogo.travel.user.controller;

import net.togogo.travel.common.Result.Result;
import net.togogo.travel.user.service.GroupBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/group/booking")
public class GroupBookingController {

    @Autowired
    private GroupBookingService groupBookingService;

    // 报名老年团
    @PostMapping("/book")
    public Result<Boolean> bookGroup(@RequestParam Long userId,
                                     @RequestParam Long groupId,
                                     @RequestParam List<Long> travelerIds) {
        boolean success = groupBookingService.bookGroup(userId, groupId, travelerIds);
        return success ? Result.success(true) : Result.fail("报名失败");
    }

    // 查询用户老年团报名信息
    @GetMapping("/list/{userId}")
    public Result<List<Map<String, Object>>> getGroupBookings(@PathVariable Long userId) {
        List<Map<String, Object>> bookings = groupBookingService.getGroupBookingsByUserId(userId);
        return Result.success(bookings);
    }

    // 取消老年团报名
    @PostMapping("/cancel/{bookingId}")
    public Result<Boolean> cancelGroupBooking(@PathVariable Long bookingId) {
        boolean success = groupBookingService.cancelGroupBooking(bookingId);
        return success ? Result.success(true) : Result.fail("取消失败");
    }
}