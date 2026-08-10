package net.togogo.springboot_travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.togogo.springboot_travel.entity.GuideBooking;

import java.util.List;
import java.util.Map;

public interface GuideBookingService extends IService<GuideBooking> {
    // 预约私人导游
    boolean bookGuide(Long userId, Long guideId, List<Long> travelerIds);
    // 查询用户导游预约信息
    List<Map<String, Object>> getGuideBookingsByUserId(Long userId);
    // 取消预约
    boolean cancelGuideBooking(Long bookingId);
}