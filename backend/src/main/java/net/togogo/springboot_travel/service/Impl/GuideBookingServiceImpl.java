package net.togogo.springboot_travel.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.togogo.springboot_travel.entity.GuideBooking;

import net.togogo.springboot_travel.entity.GuideBookingTraveler;
import net.togogo.springboot_travel.entity.PrivateGuide;
import net.togogo.springboot_travel.entity.Traveler;
import net.togogo.springboot_travel.mapper.GuideBookingMapper;

import net.togogo.springboot_travel.mapper.GuideBookingTravelerMapper;
import net.togogo.springboot_travel.mapper.PrivateGuideMapper;
import net.togogo.springboot_travel.mapper.TravelerMapper;
import net.togogo.springboot_travel.service.GuideBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GuideBookingServiceImpl extends ServiceImpl<GuideBookingMapper, GuideBooking> implements GuideBookingService {

    @Autowired
    private GuideBookingTravelerMapper guideBookingTravelerMapper;
   @Autowired
    private PrivateGuideMapper privateGuideMapper;
   @Autowired
    private TravelerMapper travelerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bookGuide(Long userId, Long guideId, List<Long> travelerIds) {
        // 1. 保存预约主表
        GuideBooking booking = new GuideBooking();
        booking.setUserId(userId);
        booking.setGuideId(guideId);
        baseMapper.insert(booking);

        // 2. 保存预约-出行人关联
        for (Long travelerId : travelerIds) {
            GuideBookingTraveler relation = new GuideBookingTraveler();
            relation.setBookingId(booking.getId());
            relation.setTravelerId(travelerId);
            guideBookingTravelerMapper.insert(relation);
        }
        return true;
    }

    @Override
    public List<Map<String, Object>> getGuideBookingsByUserId(Long userId) {
        // 1. 查询用户预约的导游主记录
        LambdaQueryWrapper<GuideBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GuideBooking::getUserId, userId)
                .eq(GuideBooking::getIsDelete, 0);
        List<GuideBooking> bookings = baseMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (GuideBooking booking : bookings) {
            Map<String, Object> map = new HashMap<>();
            // 2. 查询导游信息
            PrivateGuide guide = privateGuideMapper.selectById(booking.getGuideId());
            map.put("bookingId", booking.getId());
            map.put("guideName", guide.getName());
            map.put("region", guide.getRegion());
            map.put("price", guide.getPrice());
            map.put("createTime", booking.getCreateTime());

            // 3. 查询预约的出行人
            LambdaQueryWrapper<GuideBookingTraveler> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(GuideBookingTraveler::getBookingId, booking.getId());
            List<GuideBookingTraveler> relations = guideBookingTravelerMapper.selectList(relationWrapper);

            List<Map<String, Object>> travelers = new ArrayList<>();
            for (GuideBookingTraveler relation : relations) {
                Traveler traveler = travelerMapper.selectById(relation.getTravelerId());
                Map<String, Object> travelerMap = new HashMap<>();
                travelerMap.put("id", traveler.getId());
                travelerMap.put("name", traveler.getName());
                travelerMap.put("age", traveler.getAge());
                travelerMap.put("phone", traveler.getPhone());
                travelers.add(travelerMap);
            }
            map.put("travelers", travelers);
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelGuideBooking(Long bookingId) {
        // 1. 逻辑删除预约主表
        GuideBooking booking = new GuideBooking();
        booking.setId(bookingId);
        booking.setIsDelete(1);
        baseMapper.updateById(booking);

        // 2. 删除关联表
        LambdaQueryWrapper<GuideBookingTraveler> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GuideBookingTraveler::getBookingId, bookingId);
        guideBookingTravelerMapper.delete(wrapper);
        return true;
    }
}