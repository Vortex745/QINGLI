package com.jin.xianqu_admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_admin.model.dto.GoodsPageDTO;
import com.jin.xianqu_admin.model.entity.Goods;
import com.jin.xianqu_admin.model.vo.GoodsVO;

public interface GoodsService extends IService<Goods> {

    Page<GoodsVO> getGoodsList(GoodsPageDTO dto);

    void updateGoodsStatus(Long goodsId, Integer status);
}
