package net.togogo.travel.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.togogo.travel.user.entity.Traveler;

import java.util.List;

public interface TravelerService extends IService<Traveler> {
    // 根据用户ID查询出行人
    List<Traveler> getTravelersByUserId(Long userId);
}