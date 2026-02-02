package com.jin.xianqu_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_backend.model.entity.Area;
import com.jin.xianqu_backend.model.vo.AreaMatchVO;

import java.math.BigDecimal;

/**
 * 区域服务
 */
public interface AreaService extends IService<Area> {

    /**
     * 匹配最近区域并更新用户
     * 
     * @param latitude
     * @param longitude
     * @param userId
     * @return
     */
    AreaMatchVO matchArea(BigDecimal latitude, BigDecimal longitude, Long userId);
}
