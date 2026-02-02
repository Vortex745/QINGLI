package com.jin.xianqu_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_backend.common.UserContext;
import com.jin.xianqu_backend.exception.BusinessException;
import com.jin.xianqu_backend.mapper.*;
import com.jin.xianqu_backend.model.dto.FavoriteToggleDTO;
import com.jin.xianqu_backend.model.entity.*;
import com.jin.xianqu_backend.model.vo.*;
import com.jin.xianqu_backend.service.FavoriteService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 收藏/点赞服务实现
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private LostFoundMapper lostFoundMapper;

    @Autowired
    private PartTimeJobMapper partTimeJobMapper;

    @Override
    public boolean toggleFavorite(FavoriteToggleDTO favoriteToggleDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 防止收藏自己的内容
        Long targetId = favoriteToggleDTO.getTargetId();
        Integer type = favoriteToggleDTO.getType();

        if (type == 1) { // 1-商品收藏
            Goods goods = goodsMapper.selectById(targetId);
            if (goods != null && goods.getUserId().equals(userId)) {
                throw new BusinessException(400, "不能收藏自己的商品");
            }
        } else if (type == 5) { // 5-帖子收藏
            Post post = postMapper.selectById(targetId);
            if (post != null && post.getUserId().equals(userId)) {
                throw new BusinessException(400, "不能收藏自己的帖子");
            }
        } else if (type == 6) { // 6-失物招领收藏
            LostFound lostFound = lostFoundMapper.selectById(targetId);
            if (lostFound != null && lostFound.getUserId().equals(userId)) {
                throw new BusinessException(400, "不能收藏自己的失物招领");
            }
        } else if (type == 7) { // 7-兼职收藏
            PartTimeJob partTimeJob = partTimeJobMapper.selectById(targetId);
            if (partTimeJob != null && partTimeJob.getUserId().equals(userId)) {
                throw new BusinessException(400, "不能收藏自己的兼职");
            }
        }

        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("target_id", favoriteToggleDTO.getTargetId());
        queryWrapper.eq("type", favoriteToggleDTO.getType());

        Favorite favorite = this.getOne(queryWrapper);
        if (favorite != null) {
            // 已收藏，则取消
            this.removeById(favorite.getId());
            return false;
        } else {
            // 未收藏，则添加
            favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setTargetId(favoriteToggleDTO.getTargetId());
            favorite.setType(favoriteToggleDTO.getType());
            favorite.setCreateTime(new Date());
            this.save(favorite);
            return true;
        }
    }

    @Override
    public boolean isFavorite(Long targetId, Integer type) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return false;
        }
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("target_id", targetId);
        queryWrapper.eq("type", type);
        return this.count(queryWrapper) > 0;
    }

    @Override
    public Page<GoodsVO> listMyFavoriteGoods(int page, int size) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 1. 分页查询收藏表
        Page<Favorite> pageParam = new Page<>(page, size);
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("type", 1); // 1-商品
        queryWrapper.orderByDesc("create_time");

        Page<Favorite> favoritePage = this.page(pageParam, queryWrapper);

        // 2. 转换 VO
        List<GoodsVO> voList = favoritePage.getRecords().stream().map(fav -> {
            Goods goods = goodsMapper.selectById(fav.getTargetId());
            if (goods == null)
                return null; // 商品可能已删除

            GoodsVO vo = GoodsVO.builder()
                    .id(goods.getId())
                    .userId(goods.getUserId())
                    .areaId(goods.getAreaId())
                    .name(goods.getName())
                    .description(goods.getDescription())
                    .price(goods.getPrice())
                    .imageUrl(goods.getImageUrl())
                    .status(goods.getStatus())
                    .createTime(goods.getCreateTime())
                    .favorite(true) // 在收藏列表肯定是true
                    .build();

            // 卖家信息
            User seller = userMapper.selectById(goods.getUserId());
            if (seller != null) {
                vo.setSellerName(seller.getNickname());
                vo.setSellerAvatar(seller.getAvatarUrl());
            }
            return vo;
        }).filter(item -> item != null).collect(Collectors.toList());

        Page<GoodsVO> resultPage = new Page<>(page, size);
        resultPage.setRecords(voList);
        resultPage.setTotal(favoritePage.getTotal());

        return resultPage;
    }

    @Override
    public Page<Object> listMyFavorites(int page, int size, int type) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        // 1. 分页查询收藏表
        Page<Favorite> pageParam = new Page<>(page, size);
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("type", type);
        queryWrapper.orderByDesc("create_time");

        Page<Favorite> favoritePage = this.page(pageParam, queryWrapper);
        List<Favorite> favorites = favoritePage.getRecords();

        if (favorites.isEmpty()) {
            Page<Object> emptyPage = new Page<>(page, size);
            emptyPage.setTotal(0);
            return emptyPage;
        }

        // 2. 提取目标ID
        List<Long> targetIds = favorites.stream()
                .map(Favorite::getTargetId)
                .collect(Collectors.toList());

        // 3. 根据类型查询并转换
        List<Object> voList = new ArrayList<>();

        if (type == 1) { // 商品
            List<Goods> goodsList = goodsMapper.selectBatchIds(targetIds);
            Map<Long, Goods> goodsMap = goodsList.stream()
                    .collect(Collectors.toMap(Goods::getId, g -> g));

            for (Favorite fav : favorites) {
                Goods goods = goodsMap.get(fav.getTargetId());
                if (goods == null)
                    continue;

                GoodsVO vo = GoodsVO.builder()
                        .id(goods.getId())
                        .userId(goods.getUserId())
                        .name(goods.getName())
                        .price(goods.getPrice())
                        .imageUrl(goods.getImageUrl())
                        .createTime(goods.getCreateTime())
                        .favorite(true)
                        .build();

                User seller = userMapper.selectById(goods.getUserId());
                if (seller != null) {
                    vo.setSellerName(seller.getNickname());
                    vo.setSellerAvatar(seller.getAvatarUrl());
                }
                voList.add(vo);
            }
        } else if (type == 5) { // 帖子
            List<Post> postList = postMapper.selectBatchIds(targetIds);
            Map<Long, Post> postMap = postList.stream()
                    .collect(Collectors.toMap(Post::getId, p -> p));

            for (Favorite fav : favorites) {
                Post post = postMap.get(fav.getTargetId());
                if (post == null)
                    continue;

                PostVO vo = new PostVO();
                BeanUtils.copyProperties(post, vo);
                vo.setBookmarked(true);

                User user = userMapper.selectById(post.getUserId());
                if (user != null) {
                    vo.setUserNickname(user.getNickname());
                    vo.setUserAvatar(user.getAvatarUrl());
                }
                voList.add(vo);
            }
        } else if (type == 6) { // 失物招领
            List<LostFound> lostFoundList = lostFoundMapper.selectBatchIds(targetIds);
            Map<Long, LostFound> lostFoundMap = lostFoundList.stream()
                    .collect(Collectors.toMap(LostFound::getId, l -> l));

            for (Favorite fav : favorites) {
                LostFound lostFound = lostFoundMap.get(fav.getTargetId());
                if (lostFound == null)
                    continue;

                LostFoundVO vo = new LostFoundVO();
                BeanUtils.copyProperties(lostFound, vo);
                vo.setBookmarked(true);

                User user = userMapper.selectById(lostFound.getUserId());
                if (user != null) {
                    vo.setUserNickname(user.getNickname());
                    vo.setUserAvatar(user.getAvatarUrl());
                }
                voList.add(vo);
            }
        } else if (type == 7) { // 兼职
            List<PartTimeJob> jobList = partTimeJobMapper.selectBatchIds(targetIds);
            Map<Long, PartTimeJob> jobMap = jobList.stream()
                    .collect(Collectors.toMap(PartTimeJob::getId, j -> j));

            for (Favorite fav : favorites) {
                PartTimeJob job = jobMap.get(fav.getTargetId());
                if (job == null)
                    continue;

                PartTimeJobVO vo = new PartTimeJobVO();
                BeanUtils.copyProperties(job, vo);
                vo.setBookmarked(true);

                User user = userMapper.selectById(job.getUserId());
                if (user != null) {
                    vo.setUserNickname(user.getNickname());
                    vo.setUserAvatar(user.getAvatarUrl());
                }
                voList.add(vo);
            }
        }

        Page<Object> resultPage = new Page<>(page, size);
        resultPage.setRecords(voList);
        resultPage.setTotal(favoritePage.getTotal());
        return resultPage;
    }
}
