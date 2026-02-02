package com.jin.xianqu_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_backend.model.dto.GoodsAddDTO;
import com.jin.xianqu_backend.model.dto.GoodsUpdateDTO;
import com.jin.xianqu_backend.model.entity.Goods;
import com.jin.xianqu_backend.model.vo.GoodsVO;

/**
 * 商品服务接口
 */
public interface GoodsService extends IService<Goods> {

    Page<GoodsVO> listGoods(int page, int size, Long areaId, Long userId, String keyword, String university,
            String sort);

    Page<GoodsVO> listMyGoods(int page, int size, String keyword);

    Long addGoods(GoodsAddDTO goodsAddDTO);

    /**
     * 更新商品
     * 
     * @param goodsUpdateDTO
     * @return
     */
    boolean updateGoods(GoodsUpdateDTO goodsUpdateDTO);

    /**
     * 删除商品
     * 
     * @param id
     * @return
     */
    boolean deleteGoods(Long id);

    /**
     * 获取商品详情
     * 
     * @param id
     * @return
     */
    GoodsVO getGoodsDetail(Long id);

    /**
     * 增加商品"我想要"计数
     * 
     * @param goodsId
     * @return
     */
    boolean incrementWantCount(Long goodsId);
}
