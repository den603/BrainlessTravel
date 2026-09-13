package net.togogo.travel.user.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.togogo.travel.user.entity.Banner;
import net.togogo.travel.user.mapper.BannerMapper;
import net.togogo.travel.user.service.BannerService;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper ,Banner> implements BannerService {
}
