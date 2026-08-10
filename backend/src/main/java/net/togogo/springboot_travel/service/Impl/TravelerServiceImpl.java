package net.togogo.springboot_travel.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.togogo.springboot_travel.entity.Traveler;
import net.togogo.springboot_travel.mapper.TravelerMapper;
import net.togogo.springboot_travel.service.TravelerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TravelerServiceImpl extends ServiceImpl<TravelerMapper, Traveler> implements TravelerService {
    @Override
    public List<Traveler> getTravelersByUserId(Long userId) {
        LambdaQueryWrapper<Traveler> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Traveler::getUserId, userId)
                .eq(Traveler::getIsDelete, 0);
        return baseMapper.selectList(wrapper);
    }
}