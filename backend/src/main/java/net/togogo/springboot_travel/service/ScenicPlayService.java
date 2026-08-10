package net.togogo.springboot_travel.service;

import net.togogo.springboot_travel.Result.Result;
import net.togogo.springboot_travel.entity.ScenicPlay;

import java.util.List;

public interface ScenicPlayService {
    Result<List<ScenicPlay>> getPlayListByScenicId(Integer scenicId);
}
