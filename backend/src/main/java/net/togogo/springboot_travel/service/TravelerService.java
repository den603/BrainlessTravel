package net.togogo.springboot_travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import net.togogo.springboot_travel.entity.Traveler;

import java.util.List;

public interface TravelerService extends IService<Traveler> {
    // 根据用户ID查询出行人
    List<Traveler> getTravelersByUserId(Long userId);
}