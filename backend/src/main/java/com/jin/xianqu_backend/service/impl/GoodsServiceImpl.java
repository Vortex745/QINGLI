package com.jin.xianqu_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_backend.common.UserContext;
import com.jin.xianqu_backend.exception.BusinessException;
import com.jin.xianqu_backend.mapper.GoodsMapper;
import com.jin.xianqu_backend.mapper.UserMapper;
import com.jin.xianqu_backend.model.dto.GoodsAddDTO;
import com.jin.xianqu_backend.model.dto.GoodsUpdateDTO;
import com.jin.xianqu_backend.model.entity.Favorite;
import com.jin.xianqu_backend.model.entity.Goods;
import com.jin.xianqu_backend.model.entity.User;
import com.jin.xianqu_backend.service.GoodsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import com.jin.xianqu_backend.model.vo.GoodsVO;
import com.jin.xianqu_backend.mapper.FavoriteMapper;
import com.jin.xianqu_backend.utils.ImageUtils;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private com.jin.xianqu_backend.mapper.UserFollowMapper userFollowMapper;

    @Override
    public Page<GoodsVO> listGoods(int page, int size, Long areaId, Long userId, String keyword, String university,
            String sort) {
        Page<Goods> pageParam = new Page<>(page, size);
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        if (areaId != null) {
            queryWrapper.eq("area_id", areaId);
        }
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("name", keyword.trim());
        }
        if (university != null && !university.trim().isEmpty()) {
            queryWrapper.like("location", university.trim());
        }
        queryWrapper.eq("status", 0);

        if ("hot".equals(sort)) {
            queryWrapper.orderByDesc("view_count", "want_count", "create_time");
        } else {
            queryWrapper.orderByDesc("create_time");
        }

        Page<Goods> goodsPage = this.page(pageParam, queryWrapper);
        return convertToVO(goodsPage, page, size);
    }

    @Override
    public Page<GoodsVO> listMyGoods(int page, int size, String keyword) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        Page<Goods> pageParam = new Page<>(page, size);
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(q -> q.like("name", keyword).or().like("description", keyword));
        }

        // 不限制 status，自己发布的可能包含了交易中或已售出
        queryWrapper.orderByDesc("create_time");

        Page<Goods> goodsPage = this.page(pageParam, queryWrapper);
        return convertToVO(goodsPage, page, size);
    }

    private Page<GoodsVO> convertToVO(Page<Goods> goodsPage, int page, int size) {
        Long currentUserId = UserContext.getUserId();

        List<GoodsVO> voList = goodsPage.getRecords().stream().map(goods -> {
            GoodsVO vo = GoodsVO.builder()
                    .id(goods.getId())
                    .userId(goods.getUserId())
                    .areaId(goods.getAreaId())
                    .name(goods.getName())
                    .description(goods.getDescription())
                    .price(goods.getPrice())
                    .imageUrl(ImageUtils.cleanImageUrl(goods.getImageUrl()))
                    .status(goods.getStatus())
                    .createTime(goods.getCreateTime())
                    .viewCount(goods.getViewCount())
                    // 发布表单扩展字段
                    .originalPrice(goods.getOriginalPrice())
                    .category(goods.getCategory())
                    .condition(goods.getGoodsCondition()) // goodsCondition -> condition 映射
                    .tradingMethod(goods.getTradingMethod())
                    .location(goods.getLocation())
                    .bargainAllowed(goods.getBargainAllowed())
                    .wantCount(goods.getWantCount() != null ? goods.getWantCount() : 0)
                    .build();

            // 填充用户信息
            User seller = userMapper.selectById(goods.getUserId());
            if (seller != null) {
                vo.setSellerName(seller.getNickname());
                vo.setSellerAvatar(ImageUtils.cleanImageUrl(seller.getAvatarUrl()));
            }

            // 填充收藏状态
            if (currentUserId != null) {
                QueryWrapper<Favorite> favQuery = new QueryWrapper<>();
                favQuery.eq("user_id", currentUserId);
                favQuery.eq("target_id", goods.getId());
                favQuery.eq("type", 1); // 1-商品
                vo.setFavorite(favoriteMapper.selectCount(favQuery) > 0);
            } else {
                vo.setFavorite(false);
            }
            return vo;
        }).collect(Collectors.toList());

        Page<GoodsVO> resultPage = new Page<>(page, size);
        resultPage.setRecords(voList);
        resultPage.setTotal(goodsPage.getTotal());

        return resultPage;
    }

    @Override
    public Long addGoods(GoodsAddDTO goodsAddDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        User user = userMapper.selectById(userId);
        // if (user == null || user.getCurrentAreaId() == null) {
        // throw new BusinessException(400, "请先匹配您的位置（定位）");
        // }
        Goods goods = new Goods();
        BeanUtils.copyProperties(goodsAddDTO, goods);
        // 手动设置 condition -> goodsCondition 映射（因为字段名不同）
        goods.setGoodsCondition(goodsAddDTO.getCondition());
        goods.setUserId(userId);
        // 临时使用默认区域ID 1L，因为还未接入定位
        Long areaId = (user != null && user.getCurrentAreaId() != null) ? user.getCurrentAreaId() : 1L;
        goods.setAreaId(areaId);
        goods.setStatus(0);
        goods.setCreateTime(new Date());
        goods.setUpdateTime(new Date());
        boolean result = this.save(goods);
        if (!result) {
            throw new BusinessException(500, "发布失败");
        }
        return goods.getId();
    }

    @Override
    public boolean updateGoods(GoodsUpdateDTO goodsUpdateDTO) {
        Long userId = UserContext.getUserId();
        Goods oldGoods = this.getById(goodsUpdateDTO.getId());
        if (oldGoods == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!oldGoods.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改");
        }
        Goods goods = new Goods();
        BeanUtils.copyProperties(goodsUpdateDTO, goods);
        // 手动设置 condition -> goodsCondition 映射（因为字段名不同）
        goods.setGoodsCondition(goodsUpdateDTO.getCondition());
        goods.setUpdateTime(new Date());
        return this.updateById(goods);
    }

    @Override
    public boolean deleteGoods(Long id) {
        Long userId = UserContext.getUserId();
        Goods goods = this.getById(id);
        if (goods == null) {
            throw new BusinessException(404, "商品不存在");
        }
        // 管理员也可以删除，这里暂只允许本人
        if (!goods.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除");
        }
        return this.removeById(id);
    }

    @Override
    public GoodsVO getGoodsDetail(Long id) {
        Long currentUserId = UserContext.getUserId();
        Goods goods = this.getById(id);
        if (goods == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 增加浏览量
        Integer currentViewCount = goods.getViewCount() != null ? goods.getViewCount() : 0;
        goods.setViewCount(currentViewCount + 1);
        this.updateById(goods);

        // 计算收藏量
        QueryWrapper<Favorite> favCountQuery = new QueryWrapper<>();
        favCountQuery.eq("target_id", goods.getId());
        favCountQuery.eq("type", 1); // 1-商品
        Long favoriteCount = favoriteMapper.selectCount(favCountQuery);

        GoodsVO vo = GoodsVO.builder()
                .id(goods.getId())
                .userId(goods.getUserId())
                .areaId(goods.getAreaId())
                .name(goods.getName())
                .description(goods.getDescription())
                .price(goods.getPrice())
                .imageUrl(ImageUtils.cleanImageUrl(goods.getImageUrl()))
                .status(goods.getStatus())
                .createTime(goods.getCreateTime())
                .viewCount(goods.getViewCount())
                .favoriteCount(favoriteCount.intValue())
                // 发布表单扩展字段
                .originalPrice(goods.getOriginalPrice())
                .category(goods.getCategory())
                .condition(goods.getGoodsCondition()) // goodsCondition -> condition 映射
                .tradingMethod(goods.getTradingMethod())
                .location(goods.getLocation())
                .bargainAllowed(goods.getBargainAllowed())
                .wantCount(goods.getWantCount() != null ? goods.getWantCount() : 0)
                .build();

        // 填充用户信息
        User seller = userMapper.selectById(goods.getUserId());
        if (seller != null) {
            vo.setSellerName(seller.getNickname());
            vo.setSellerAvatar(ImageUtils.cleanImageUrl(seller.getAvatarUrl()));
        }

        // 填充收藏状态
        if (currentUserId != null) {
            QueryWrapper<Favorite> favQuery = new QueryWrapper<>();
            favQuery.eq("user_id", currentUserId);
            favQuery.eq("target_id", goods.getId());
            favQuery.eq("type", 1); // 1-商品
            vo.setFavorite(favoriteMapper.selectCount(favQuery) > 0);

            // 填充关注状态
            Long sellerId = goods.getUserId();
            Long count = userFollowMapper.selectCount(
                    new QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>()
                            .eq("follower_id", currentUserId)
                            .eq("followed_id", sellerId));
            vo.setIsFollowed(count > 0);
        } else {
            vo.setFavorite(false);
            vo.setIsFollowed(false);
        }

        return vo;
    }

    @Override
    public boolean incrementWantCount(Long goodsId) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }

        Goods goods = this.getById(goodsId);
        if (goods == null) {
            throw new BusinessException(404, "商品不存在");
        }
        // 不能给自己的商品点"我想要"
        if (currentUserId.equals(goods.getUserId())) {
            throw new BusinessException(400, "不能对自己的商品表示想要");
        }

        // 检查是否已经点过"我想要" (Type 11)
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId)
                .eq("target_id", goodsId)
                .eq("type", 11);
        if (favoriteMapper.selectCount(queryWrapper) > 0) {
            return true; // 已经点过了，直接返回成功但不重复计数
        }

        // 记录用户点击行为
        Favorite favorite = new Favorite();
        favorite.setUserId(currentUserId);
        favorite.setTargetId(goodsId);
        favorite.setType(11);
        favorite.setCreateTime(new Date());
        favoriteMapper.insert(favorite);

        // 增加商品表中的计数
        Integer currentWantCount = goods.getWantCount() != null ? goods.getWantCount() : 0;
        goods.setWantCount(currentWantCount + 1);
        return this.updateById(goods);
    }
}
