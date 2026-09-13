package net.togogo.travel.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.togogo.travel.common.Result.Result;
import net.togogo.travel.user.entity.ScenicPlay;

import net.togogo.travel.user.mapper.ScenicPlayMapper;
import net.togogo.travel.user.service.ScenicPlayService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScenicPlayServiceImpl extends ServiceImpl<ScenicPlayMapper, ScenicPlay> implements ScenicPlayService {

    @Override
    public Result<List<ScenicPlay>> getPlayListByScenicId(Integer scenicId) {
        // 校验参数
        if (scenicId == null) {
            return Result.fail("景区ID不能为空");
        }
        // 查询条件：启用状态 + 按排序升序
        LambdaQueryWrapper<ScenicPlay> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScenicPlay::getScenicId, scenicId)
                .eq(ScenicPlay::getStatus, 1)
                .orderByAsc(ScenicPlay::getSort);

        List<ScenicPlay> list = this.list(wrapper);
        return Result.success(list);
    }
}
