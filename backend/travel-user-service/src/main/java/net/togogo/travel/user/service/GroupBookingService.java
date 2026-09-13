package net.togogo.travel.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.togogo.travel.user.entity.GroupBooking;
import net.togogo.travel.user.entity.Traveler;

import java.util.List;
import java.util.Map;

public interface GroupBookingService extends IService<GroupBooking> {
    // 报名老年团
    boolean bookGroup(Long userId, Long groupId, List<Long> travelerIds);
    // 查询用户老年团报名信息
    List<Map<String, Object>> getGroupBookingsByUserId(Long userId);
    // 取消报名
    boolean cancelGroupBooking(Long bookingId);
}