package net.togogo.travel.user.service;

import net.togogo.travel.common.Result.Result;
import net.togogo.travel.user.entity.ScenicPlay;

import java.util.List;

public interface ScenicPlayService {
    Result<List<ScenicPlay>> getPlayListByScenicId(Integer scenicId);
}
