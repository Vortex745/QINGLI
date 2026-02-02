package com.jin.xianqu_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_backend.common.UserContext;
import com.jin.xianqu_backend.mapper.LostFoundMapper;
import com.jin.xianqu_backend.mapper.UserMapper;
import com.jin.xianqu_backend.model.dto.LostFoundAddDTO;
import com.jin.xianqu_backend.model.entity.LostFound;
import com.jin.xianqu_backend.model.entity.User;
import com.jin.xianqu_backend.model.vo.LostFoundVO;
import com.jin.xianqu_backend.service.LostFoundService;
import com.jin.xianqu_backend.utils.ImageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LostFoundServiceImpl extends ServiceImpl<LostFoundMapper, LostFound> implements LostFoundService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private com.jin.xianqu_backend.mapper.CommentMapper commentMapper;

    @Autowired
    private com.jin.xianqu_backend.mapper.FavoriteMapper favoriteMapper;

    @Autowired
    private com.jin.xianqu_backend.mapper.UserFollowMapper userFollowMapper;

    @Override
    public Boolean add(LostFoundAddDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("未登录");
        }

        // Check if this is an update (id is present)
        if (dto.getId() != null) {
            LostFound existing = this.getById(dto.getId());
            if (existing == null) {
                throw new RuntimeException("信息不存在");
            }
            if (!existing.getUserId().equals(userId)) {
                throw new RuntimeException("无权修改");
            }
            BeanUtils.copyProperties(dto, existing, "id", "userId", "createTime");
            if (dto.getStatus() != null) {
                existing.setStatus(dto.getStatus());
            }
            existing.setUpdateTime(new java.util.Date());
            return this.updateById(existing);
        }

        // New entry
        LostFound entity = new LostFound();
        BeanUtils.copyProperties(dto, entity);
        entity.setUserId(userId);
        entity.setStatus(0); // Publishing
        return this.save(entity);
    }

    @Override
    public Page<LostFoundVO> list(int page, int size, Integer type, Long userId, String keyword, String location,
            String sort) {
        Page<LostFound> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<LostFound> wrapper = new LambdaQueryWrapper<>();

        if (type != null) {
            wrapper.eq(LostFound::getType, type);
        }
        if (userId != null) {
            wrapper.eq(LostFound::getUserId, userId);
        }

        // Filter by university (which the client sends as 'location')
        if (location != null && !location.trim().isEmpty()) {
            wrapper.eq(LostFound::getUniversity, location.trim());
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(LostFound::getItemName, keyword)
                    .or().like(LostFound::getFeatures, keyword)
                    .or().like(LostFound::getLocation, keyword));
        }

        if ("hot".equals(sort)) {
            wrapper.orderByDesc(LostFound::getViewCount, LostFound::getWantCount, LostFound::getCreateTime);
        } else {
            wrapper.orderByDesc(LostFound::getCreateTime);
        }

        Page<LostFound> resultPage = this.page(pageParam, wrapper);
        List<LostFoundVO> voList = resultPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

        Page<LostFoundVO> voPage = new Page<>(page, size, resultPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<LostFoundVO> listMy(int page, int size, String keyword) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        Page<LostFound> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<LostFound> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LostFound::getUserId, userId);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(LostFound::getItemName, keyword)
                    .or().like(LostFound::getFeatures, keyword)
                    .or().like(LostFound::getLocation, keyword));
        }

        wrapper.orderByDesc(LostFound::getCreateTime);

        Page<LostFound> resultPage = this.page(pageParam, wrapper);
        List<LostFoundVO> voList = resultPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

        Page<LostFoundVO> voPage = new Page<>(page, size, resultPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public LostFoundVO getDetail(Long id) {
        LostFound entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("信息不存在");
        }
        // 增加浏览量
        entity.setViewCount((entity.getViewCount() != null ? entity.getViewCount() : 0) + 1);
        this.updateById(entity);
        return convertToVO(entity);
    }

    @Override
    public boolean incrementWantCount(Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new RuntimeException("请先登录");
        }

        LostFound entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("信息不存在");
        }
        // 不能给自己点"我想要"
        if (currentUserId.equals(entity.getUserId())) {
            throw new RuntimeException("不能对自己的发布表示想要");
        }

        // 检查 (Type 13)
        QueryWrapper<com.jin.xianqu_backend.model.entity.Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId)
                .eq("target_id", id)
                .eq("type", 13);
        if (favoriteMapper.selectCount(queryWrapper) > 0) {
            return true;
        }

        // 记录
        com.jin.xianqu_backend.model.entity.Favorite favorite = new com.jin.xianqu_backend.model.entity.Favorite();
        favorite.setUserId(currentUserId);
        favorite.setTargetId(id);
        favorite.setType(13);
        favorite.setCreateTime(new java.util.Date());
        favoriteMapper.insert(favorite);

        Integer currentWantCount = entity.getWantCount() != null ? entity.getWantCount() : 0;
        entity.setWantCount(currentWantCount + 1);
        return this.updateById(entity);
    }

    private LostFoundVO convertToVO(LostFound entity) {
        LostFoundVO vo = new LostFoundVO();
        if (entity == null)
            return vo;
        BeanUtils.copyProperties(entity, vo);

        // Handle images
        if (StringUtils.hasText(entity.getImageUrls())) {
            vo.setImageUrls(Arrays.asList(entity.getImageUrls().split(",")));
        } else {
            vo.setImageUrls(new ArrayList<>());
        }

        // Fill User Info
        User user = userMapper.selectById(entity.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(ImageUtils.cleanImageUrl(user.getAvatarUrl()));
        }

        // Fill Social Info (Type = 3 for LostFound)
        // 1. Comment Count
        Long commentCount = commentMapper
                .selectCount(new LambdaQueryWrapper<com.jin.xianqu_backend.model.entity.Comment>()
                        .eq(com.jin.xianqu_backend.model.entity.Comment::getTargetId, entity.getId())
                        .eq(com.jin.xianqu_backend.model.entity.Comment::getType, 6)); // 6-LostFound
        vo.setCommentCount(commentCount.intValue());

        // 2. Favorite Count
        Long favCount = favoriteMapper
                .selectCount(new LambdaQueryWrapper<com.jin.xianqu_backend.model.entity.Favorite>()
                        .eq(com.jin.xianqu_backend.model.entity.Favorite::getTargetId, entity.getId())
                        .eq(com.jin.xianqu_backend.model.entity.Favorite::getType, 3));
        vo.setFavoriteCount(favCount.intValue());

        // 3. Is Liked (by current user)
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null) {
            Long isLiked = favoriteMapper
                    .selectCount(new LambdaQueryWrapper<com.jin.xianqu_backend.model.entity.Favorite>()
                            .eq(com.jin.xianqu_backend.model.entity.Favorite::getTargetId, entity.getId())
                            .eq(com.jin.xianqu_backend.model.entity.Favorite::getType, 3)
                            .eq(com.jin.xianqu_backend.model.entity.Favorite::getUserId, currentUserId));
            vo.setFavorite(isLiked > 0);

            Long isBookmarked = favoriteMapper
                    .selectCount(new LambdaQueryWrapper<com.jin.xianqu_backend.model.entity.Favorite>()
                            .eq(com.jin.xianqu_backend.model.entity.Favorite::getTargetId, entity.getId())
                            .eq(com.jin.xianqu_backend.model.entity.Favorite::getType, 6)
                            .eq(com.jin.xianqu_backend.model.entity.Favorite::getUserId, currentUserId));
            vo.setBookmarked(isBookmarked > 0);

            // Populate follow status
            Long count = userFollowMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>()
                            .eq("follower_id", currentUserId)
                            .eq("followed_id", entity.getUserId()));
            vo.setIsFollowed(count > 0);
        } else {
            vo.setFavorite(false);
            vo.setBookmarked(false);
            vo.setIsFollowed(false);
        }

        return vo;
    }
}
