package com.jin.xianqu_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_backend.model.dto.FavoriteToggleDTO;
import com.jin.xianqu_backend.model.entity.Favorite;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_backend.model.vo.GoodsVO;

/**
 * 收藏/点赞服务接口
 */
public interface FavoriteService extends IService<Favorite> {

    /**
     * 切换收藏状态
     * 
     * @param favoriteToggleDTO
     * @return true-收藏成功, false-取消收藏
     */
    boolean toggleFavorite(FavoriteToggleDTO favoriteToggleDTO);

    /**
     * 检查是否已收藏
     * 
     * @param targetId
     * @param type
     * @return
     */

    boolean isFavorite(Long targetId, Integer type);

    /**
     * 获取我的收藏商品
     * 
     * @param page
     * @param size
     * @return
     */
    Page<GoodsVO> listMyFavoriteGoods(int page, int size);

    /**
     * 获取我的收藏列表（通用）
     * 
     * @param page
     * @param size
     * @param type 收藏类型：1-商品, 5-帖子, 6-失物招领, 7-兼职
     * @return
     */
    Page<Object> listMyFavorites(int page, int size, int type);
}
