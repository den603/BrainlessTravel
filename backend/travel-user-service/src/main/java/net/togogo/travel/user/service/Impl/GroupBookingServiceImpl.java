package net.togogo.travel.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.togogo.travel.user.entity.GroupBooking;
import net.togogo.travel.user.entity.GroupBookingTraveler;
import net.togogo.travel.user.entity.TravelGroup;
import net.togogo.travel.user.entity.Traveler;
import net.togogo.travel.user.mapper.GroupBookingMapper;
import net.togogo.travel.user.mapper.GroupBookingTravelerMapper;
import net.togogo.travel.user.mapper.TravelGroupMapper;
import net.togogo.travel.user.mapper.TravelerMapper;
import net.togogo.travel.user.service.GroupBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroupBookingServiceImpl extends ServiceImpl<GroupBookingMapper, GroupBooking> implements GroupBookingService {

    @Autowired
    private GroupBookingTravelerMapper groupBookingTravelerMapper;
    @Autowired
    private TravelGroupMapper travelGroupMapper;
    @Autowired
    private TravelerMapper travelerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bookGroup(Long userId, Long groupId, List<Long> travelerIds) {
        // 1. 保存报名主表
        GroupBooking booking = new GroupBooking();
        booking.setUserId(userId);
        booking.setGroupId(groupId);
        baseMapper.insert(booking);

        // 2. 保存报名-出行人关联
        for (Long travelerId : travelerIds) {
            GroupBookingTraveler relation = new GroupBookingTraveler();
            relation.setBookingId(booking.getId());
            relation.setTravelerId(travelerId);
            groupBookingTravelerMapper.insert(relation);
        }
        return true;
    }

    @Override
    public List<Map<String, Object>> getGroupBookingsByUserId(Long userId) {
        // 1. 查询用户报名的老年团主记录
        LambdaQueryWrapper<GroupBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBooking::getUserId, userId)
                .eq(GroupBooking::getIsDelete, 0);
        List<GroupBooking> bookings = baseMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (GroupBooking booking : bookings) {
            Map<String, Object> map = new HashMap<>();
            // 2. 查询旅游团信息
            TravelGroup travelGroup = travelGroupMapper.selectById(booking.getGroupId());
            map.put("bookingId", booking.getId());
            map.put("groupName", travelGroup.getName());
            map.put("startTime", travelGroup.getStartTime());
            map.put("endTime", travelGroup.getEndTime());
            map.put("price", travelGroup.getPrice());
            map.put("createTime", booking.getCreateTime());

            // 3. 查询报名的出行人
            LambdaQueryWrapper<GroupBookingTraveler> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(GroupBookingTraveler::getBookingId, booking.getId());
            List<GroupBookingTraveler> relations = groupBookingTravelerMapper.selectList(relationWrapper);

            List<Map<String, Object>> travelers = new ArrayList<>();
            for (GroupBookingTraveler relation : relations) {
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
    public boolean cancelGroupBooking(Long bookingId) {
        // 1. 删除报名主表（逻辑删除）
        GroupBooking booking = new GroupBooking();
        booking.setId(bookingId);
        booking.setIsDelete(1);
        baseMapper.updateById(booking);

        // 2. 删除关联表（物理删除，也可逻辑删除）
        LambdaQueryWrapper<GroupBookingTraveler> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupBookingTraveler::getBookingId, bookingId);
        groupBookingTravelerMapper.delete(wrapper);
        return true;
    }
}