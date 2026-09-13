package net.togogo.travel.user.controller;

import net.togogo.travel.common.Result.Result;
import net.togogo.travel.user.service.GuideBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/guide/booking")
public class GuideBookingController {

    @Autowired
    private GuideBookingService guideBookingService;

    // 预约私人导游
    @PostMapping("/book")
    public Result<Boolean> bookGuide(@RequestParam Long userId,
                                     @RequestParam Long guideId,
                                     @RequestParam List<Long> travelerIds) {
        boolean success = guideBookingService.bookGuide(userId, guideId, travelerIds);
        return success ? Result.success(true) : Result.fail("预约失败");
    }

    // 查询用户导游预约信息
    @GetMapping("/list/{userId}")
    public Result<List<Map<String, Object>>> getGuideBookings(@PathVariable Long userId) {
        List<Map<String, Object>> bookings = guideBookingService.getGuideBookingsByUserId(userId);
        return Result.success(bookings);
    }

    // 取消导游预约
    @PostMapping("/cancel/{bookingId}")
    public Result<Boolean> cancelGuideBooking(@PathVariable Long bookingId) {
        boolean success = guideBookingService.cancelGuideBooking(bookingId);
        return success ? Result.success(true) : Result.fail("取消失败");
    }
}