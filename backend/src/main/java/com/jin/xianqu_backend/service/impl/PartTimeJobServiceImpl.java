package com.jin.xianqu_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_backend.common.UserContext;
import com.jin.xianqu_backend.exception.BusinessException;
import com.jin.xianqu_backend.mapper.CommentMapper;
import com.jin.xianqu_backend.mapper.FavoriteMapper;
import com.jin.xianqu_backend.mapper.PartTimeJobMapper;
import com.jin.xianqu_backend.model.dto.PartTimeJobAddDTO;
import com.jin.xianqu_backend.model.entity.Comment;
import com.jin.xianqu_backend.model.entity.Favorite;
import com.jin.xianqu_backend.model.entity.PartTimeJob;
import com.jin.xianqu_backend.model.entity.User;
import com.jin.xianqu_backend.model.vo.PartTimeJobVO;
import com.jin.xianqu_backend.service.PartTimeJobService;
import com.jin.xianqu_backend.service.UserService;
import com.jin.xianqu_backend.utils.ImageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PartTimeJobServiceImpl extends ServiceImpl<PartTimeJobMapper, PartTimeJob> implements PartTimeJobService {

    @Autowired
    private UserService userService;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private com.jin.xianqu_backend.mapper.UserFollowMapper userFollowMapper;

    @Override
    public boolean addJob(PartTimeJobAddDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

        // Check if this is an update (id is present)
        if (dto.getId() != null) {
            PartTimeJob existingJob = this.getById(dto.getId());
            if (existingJob == null) {
                throw new BusinessException(404, "兼职信息不存在");
            }
            if (!existingJob.getUserId().equals(userId)) {
                throw new BusinessException(403, "无权修改");
            }
            BeanUtils.copyProperties(dto, existingJob, "id", "userId", "createTime");
            if (dto.getStatus() != null) {
                existingJob.setStatus(dto.getStatus());
            }
            existingJob.setUpdateTime(new Date());
            return this.updateById(existingJob);
        }

        // New job
        PartTimeJob job = new PartTimeJob();
        BeanUtils.copyProperties(dto, job);
        job.setUserId(userId);
        job.setStatus(0); // 0-热招中
        job.setViewCount(0); // 初始化浏览量为0
        job.setCreateTime(new Date());
        return this.save(job);
    }

    @Override
    public Page<PartTimeJobVO> listJobs(int page, int size) {
        return listJobsQuery(page, size, null, null, null, null);
    }

    @Override
    public Page<PartTimeJobVO> listJobs(int page, int size, String sort, Long userId, String keyword, String location) {
        return listJobsQuery(page, size, sort, userId, keyword, location);
    }

    private Page<PartTimeJobVO> listJobsQuery(int page, int size, String sort, Long userId, String keyword,
            String location) {
        Page<PartTimeJob> pageParam = new Page<>(page, size);
        QueryWrapper<PartTimeJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0);

        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper.like("title", keyword).or().like("content", keyword));
        }

        // Location filter for university isolation
        if (location != null && !location.trim().isEmpty()) {
            queryWrapper.like("location", location.trim());
        }

        // Sorting
        if ("hot".equals(sort)) {
            queryWrapper.orderByDesc("view_count", "want_count", "create_time");
        } else {
            queryWrapper.orderByDesc("create_time");
        }

        Page<PartTimeJob> resultPage = this.page(pageParam, queryWrapper);
        Page<PartTimeJobVO> voPage = new Page<>(page, size, resultPage.getTotal());
        List<PartTimeJob> records = resultPage.getRecords();

        if (records.isEmpty()) {
            return voPage;
        }

        Set<Long> userIds = records.stream().map(PartTimeJob::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Long currentUserId = UserContext.getUserId();

        List<PartTimeJobVO> voList = records.stream()
                .filter(java.util.Objects::nonNull)
                .map(job -> {
                    PartTimeJobVO vo = new PartTimeJobVO();
                    if (job == null)
                        return vo;
                    BeanUtils.copyProperties(job, vo);
                    User user = userMap.get(job.getUserId());
                    if (user != null) {
                        vo.setUserNickname(user.getNickname());
                        vo.setUserAvatar(ImageUtils.cleanImageUrl(user.getAvatarUrl()));
                    }

                    vo.setFavoriteCount(favoriteMapper.selectCount(new QueryWrapper<Favorite>().lambda()
                            .eq(Favorite::getTargetId, job.getId())
                            .eq(Favorite::getType, 4)).intValue());

                    vo.setCommentCount(commentMapper.selectCount(new QueryWrapper<Comment>().lambda()
                            .eq(Comment::getTargetId, job.getId())
                            .eq(Comment::getType, 7)).intValue());

                    if (currentUserId != null) {
                        QueryWrapper<Favorite> userFavQuery = new QueryWrapper<>();
                        userFavQuery.eq("user_id", currentUserId).eq("target_id", job.getId()).eq("type", 4); // 4=PartTimeJob
                        vo.setFavorite(favoriteMapper.selectCount(userFavQuery) > 0);
                    } else {
                        vo.setFavorite(false);
                    }

                    return vo;
                }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public PartTimeJobVO getJobDetail(Long id) {
        PartTimeJob job = this.getById(id);
        if (job == null) {
            throw new BusinessException(404, "兼职信息不存在");
        }
        // 增加浏览量
        job.setViewCount((job.getViewCount() != null ? job.getViewCount() : 0) + 1);
        this.updateById(job);
        PartTimeJobVO vo = new PartTimeJobVO();
        BeanUtils.copyProperties(job, vo);

        User user = userService.getById(job.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(ImageUtils.cleanImageUrl(user.getAvatarUrl()));
        }

        // Social
        vo.setFavoriteCount(favoriteMapper.selectCount(new QueryWrapper<Favorite>().lambda()
                .eq(Favorite::getTargetId, job.getId())
                .eq(Favorite::getType, 4)).intValue());

        vo.setCommentCount(commentMapper.selectCount(new QueryWrapper<Comment>().lambda()
                .eq(Comment::getTargetId, job.getId())
                .eq(Comment::getType, 7)).intValue());

        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null) {
            QueryWrapper<Favorite> userFavQuery = new QueryWrapper<>();
            userFavQuery.eq("user_id", currentUserId).eq("target_id", job.getId()).eq("type", 4); // 4=PartTimeJob
            vo.setFavorite(favoriteMapper.selectCount(userFavQuery) > 0);

            QueryWrapper<Favorite> userBookmarkQuery = new QueryWrapper<>();
            userBookmarkQuery.eq("user_id", currentUserId).eq("target_id", job.getId()).eq("type", 7); // 7=PartTimeJob
                                                                                                       // Bookmark
            vo.setBookmarked(favoriteMapper.selectCount(userBookmarkQuery) > 0);

            // Follow status
            Long count = userFollowMapper.selectCount(
                    new QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>()
                            .eq("follower_id", currentUserId)
                            .eq("followed_id", job.getUserId()));
            vo.setIsFollowed(count > 0);
        } else {
            vo.setFavorite(false);
            vo.setBookmarked(false);
            vo.setIsFollowed(false);
        }

        return vo;
    }

    @Override
    public PartTimeJobVO getJobForEdit(Long id) {
        PartTimeJob job = this.getById(id);
        if (job == null) {
            throw new BusinessException(404, "兼职信息不存在");
        }
        // 编辑页面不增加浏览量
        PartTimeJobVO vo = new PartTimeJobVO();
        BeanUtils.copyProperties(job, vo);

        User user = userService.getById(job.getUserId());
        if (user != null) {
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(ImageUtils.cleanImageUrl(user.getAvatarUrl()));
        }

        return vo;
    }

    @Override
    public Page<PartTimeJobVO> listMyJobs(int page, int size, String keyword) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        Page<PartTimeJob> pageParam = new Page<>(page, size);
        QueryWrapper<PartTimeJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper.like("title", keyword).or().like("content", keyword));
        }
        queryWrapper.orderByDesc("create_time");

        Page<PartTimeJob> resultPage = this.page(pageParam, queryWrapper);
        Page<PartTimeJobVO> voPage = new Page<>(page, size, resultPage.getTotal());
        List<PartTimeJob> records = resultPage.getRecords();

        if (records.isEmpty()) {
            return voPage;
        }

        Long currentUserId = userId;

        Set<Long> userIds = records.stream().map(PartTimeJob::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<PartTimeJobVO> voList = records.stream()
                .filter(java.util.Objects::nonNull)
                .map(job -> {
                    PartTimeJobVO vo = new PartTimeJobVO();
                    if (job == null)
                        return vo;
                    BeanUtils.copyProperties(job, vo);
                    User user = userMap.get(job.getUserId());
                    if (user != null) {
                        vo.setUserNickname(user.getNickname());
                        vo.setUserAvatar(ImageUtils.cleanImageUrl(user.getAvatarUrl()));
                    }

                    vo.setFavoriteCount(favoriteMapper.selectCount(new QueryWrapper<Favorite>().lambda()
                            .eq(Favorite::getTargetId, job.getId())
                            .eq(Favorite::getType, 4)).intValue());

                    vo.setCommentCount(commentMapper.selectCount(new QueryWrapper<Comment>().lambda()
                            .eq(Comment::getTargetId, job.getId())
                            .eq(Comment::getType, 7)).intValue());

                    QueryWrapper<Favorite> userFavQuery = new QueryWrapper<>();
                    userFavQuery.eq("user_id", currentUserId).eq("target_id", job.getId()).eq("type", 4); // 4=PartTimeJob
                    vo.setFavorite(favoriteMapper.selectCount(userFavQuery) > 0);

                    return vo;
                }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public boolean incrementWantCount(Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }

        PartTimeJob job = this.getById(id);
        if (job == null) {
            throw new BusinessException(404, "兼职信息不存在");
        }
        // 不能给自己点"我想要"
        if (currentUserId.equals(job.getUserId())) {
            throw new BusinessException(400, "不能对自己的发布表示想要");
        }

        // 检查 (Type 14)
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId)
                .eq("target_id", id)
                .eq("type", 14);
        if (favoriteMapper.selectCount(queryWrapper) > 0) {
            return true;
        }

        // 记录
        Favorite favorite = new Favorite();
        favorite.setUserId(currentUserId);
        favorite.setTargetId(id);
        favorite.setType(14);
        favorite.setCreateTime(new Date());
        favoriteMapper.insert(favorite);

        Integer currentWantCount = job.getWantCount() != null ? job.getWantCount() : 0;
        job.setWantCount(currentWantCount + 1);
        return this.updateById(job);
    }
}
