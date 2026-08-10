package net.togogo.springboot_travel.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.togogo.springboot_travel.entity.Banner;
import net.togogo.springboot_travel.mapper.BannerMapper;
import net.togogo.springboot_travel.service.BannerService;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper ,Banner> implements BannerService {
}
