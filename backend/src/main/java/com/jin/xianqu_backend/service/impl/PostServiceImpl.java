package com.jin.xianqu_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_backend.common.UserContext;
import com.jin.xianqu_backend.exception.BusinessException;
import com.jin.xianqu_backend.mapper.PostMapper;
import com.jin.xianqu_backend.mapper.UserMapper;
import com.jin.xianqu_backend.model.dto.PostAddDTO;
import com.jin.xianqu_backend.model.dto.PostUpdateDTO;
import com.jin.xianqu_backend.model.entity.Post;
import com.jin.xianqu_backend.model.entity.User;
import com.jin.xianqu_backend.service.PostService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jin.xianqu_backend.model.vo.PostVO;
import com.jin.xianqu_backend.mapper.FavoriteMapper;
import com.jin.xianqu_backend.mapper.CommentMapper;
import com.jin.xianqu_backend.model.entity.Favorite;
import com.jin.xianqu_backend.model.entity.Comment;
import com.jin.xianqu_backend.utils.ImageUtils;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private com.jin.xianqu_backend.mapper.UserFollowMapper userFollowMapper;

    @Override
    public Page<PostVO> listPosts(int page, int size, Long areaId, Integer type, Long userId, String keyword,
            String sort, String location) {
        Page<Post> pageParam = new Page<>(page, size);
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (areaId != null) {
            queryWrapper.eq("area_id", areaId);
        }
        if (type != null) {
            queryWrapper.eq("type", type);
        }
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("content", keyword.trim());
        }
        // Location filter for university isolation
        if (location != null && !location.trim().isEmpty()) {
            queryWrapper.like("location", location.trim());
        }

        if ("hot".equals(sort)) {
            queryWrapper.orderByDesc("view_count", "want_count", "create_time");
        } else {
            queryWrapper.orderByDesc("create_time");
        }

        Page<Post> postPage = this.page(pageParam, queryWrapper);
        return convertToVO(postPage, page, size);
    }

    @Override
    public Page<PostVO> listMyPosts(int page, int size, String keyword) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        Page<Post> pageParam = new Page<>(page, size);
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            queryWrapper.and(q -> q.like("content", kw).or().like("title", kw));
        }

        queryWrapper.orderByDesc("create_time");

        Page<Post> postPage = this.page(pageParam, queryWrapper);
        return convertToVO(postPage, page, size);
    }

    @Override
    public PostVO getPostDetail(Long id) {
        Post post = this.getById(id);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }

        // 增加浏览量
        post.setViewCount((post.getViewCount() != null ? post.getViewCount() : 0) + 1);
        this.updateById(post);

        // Wrap single post into a list page to reuse convertToVO logic, or just manual
        // convert
        // Manual convert for efficiency
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);

        // Fill User Info
        if (Integer.valueOf(1).equals(post.getIsAnonymous())) {
            vo.setUserNickname("匿名用户");
            vo.setUserAvatar("/assets/images/avatar_default.png");
        } else {
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserAvatar(ImageUtils.cleanImageUrl(user.getAvatarUrl()));
            } else {
                vo.setUserNickname("匿名用户");
                vo.setUserAvatar("/assets/images/avatar_default.png");
            }
        }

        // Favorite
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null) {
            QueryWrapper<Favorite> favQuery = new QueryWrapper<>();
            favQuery.eq("user_id", currentUserId);
            favQuery.eq("target_id", post.getId());
            favQuery.eq("type", 2); // 2-Post Like
            vo.setFavorite(favoriteMapper.selectCount(favQuery) > 0);

            QueryWrapper<Favorite> bookmarkQuery = new QueryWrapper<>();
            bookmarkQuery.eq("user_id", currentUserId);
            bookmarkQuery.eq("target_id", post.getId());
            bookmarkQuery.eq("type", 5); // 5-Post Bookmark
            vo.setBookmarked(favoriteMapper.selectCount(bookmarkQuery) > 0);

            // Populate follow status
            if (Integer.valueOf(1).equals(post.getIsAnonymous())) {
                vo.setIsFollowed(false);
            } else {
                Long count = userFollowMapper.selectCount(
                        new QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>()
                                .eq("follower_id", currentUserId)
                                .eq("followed_id", post.getUserId()));
                vo.setIsFollowed(count > 0);
            }
        } else {
            vo.setFavorite(false);
            vo.setBookmarked(false);
            vo.setIsFollowed(false);
        }

        // Favorite Count
        QueryWrapper<Favorite> favCountQuery = new QueryWrapper<>();
        favCountQuery.eq("target_id", post.getId());
        favCountQuery.eq("type", 2);
        vo.setFavoriteCount(favoriteMapper.selectCount(favCountQuery).intValue());

        // Comment Count
        Integer pType = post.getType();
        int queryCommentType = (pType != null && pType == 1) ? 6 : 5;
        Long cCount = commentMapper.selectCount(new QueryWrapper<Comment>().lambda()
                .eq(Comment::getTargetId, post.getId())
                .eq(Comment::getType, queryCommentType));
        vo.setCommentCount(cCount != null ? cCount.intValue() : 0);
        // System.out.println("DEBUG: PostId=" + post.getId() + ", Type=" + pType + ",
        // QType=" + queryCommentType + ", Count=" + cCount);

        return vo;
    }

    private Page<PostVO> convertToVO(Page<Post> postPage, int page, int size) {
        Long currentUserId = UserContext.getUserId();

        List<PostVO> voList = postPage.getRecords().stream().map(post -> {
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);

            // User Info
            if (Integer.valueOf(1).equals(post.getIsAnonymous())) {
                vo.setUserNickname("匿名用户");
                vo.setUserAvatar("/assets/images/avatar_default.png");
            } else {
                User user = userMapper.selectById(post.getUserId());
                if (user != null) {
                    vo.setUserNickname(user.getNickname());
                    vo.setUserAvatar(ImageUtils.cleanImageUrl(user.getAvatarUrl()));
                } else {
                    vo.setUserNickname("匿名用户");
                    vo.setUserAvatar("/assets/images/avatar_default.png");
                }
            }

            // Favorite Status
            if (currentUserId != null) {
                QueryWrapper<Favorite> favQuery = new QueryWrapper<>();
                favQuery.eq("user_id", currentUserId);
                favQuery.eq("target_id", post.getId());
                favQuery.eq("type", 2); // 2-Post
                vo.setFavorite(favoriteMapper.selectCount(favQuery) > 0);
            } else {
                vo.setFavorite(false);
            }

            // Favorite Count
            QueryWrapper<Favorite> favCountQuery = new QueryWrapper<>();
            favCountQuery.eq("target_id", post.getId());
            favCountQuery.eq("type", 2);
            vo.setFavoriteCount(favoriteMapper.selectCount(favCountQuery).intValue());

            // Comment Count
            Integer pType = post.getType();
            int queryCommentType = (pType != null && pType == 1) ? 6 : 5;
            Long cCount = commentMapper.selectCount(new QueryWrapper<Comment>().lambda()
                    .eq(Comment::getTargetId, post.getId())
                    .eq(Comment::getType, queryCommentType));
            vo.setCommentCount(cCount != null ? cCount.intValue() : 0);
            return vo;
        }).collect(Collectors.toList());

        Page<PostVO> resultPage = new Page<>(page, size);
        resultPage.setRecords(voList);
        resultPage.setTotal(postPage.getTotal());
        return resultPage;
    }

    @Override
    public Long addPost(PostAddDTO postAddDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        User user = userMapper.selectById(userId);
        // if (user == null || user.getCurrentAreaId() == null) {
        // throw new BusinessException(400, "请先匹配您的位置");
        // }
        Post post = new Post();
        BeanUtils.copyProperties(postAddDTO, post);
        post.setUserId(userId);
        // 临时默认区域ID 1L
        Long areaId = (user != null && user.getCurrentAreaId() != null) ? user.getCurrentAreaId() : 1L;
        post.setAreaId(areaId);
        post.setCreateTime(new Date());
        post.setUpdateTime(new Date());
        post.setStatus(0); // 初始状态为正常显示
        boolean result = this.save(post);
        if (!result) {
            throw new BusinessException(500, "发布失败");
        }
        return post.getId();
    }

    @Override
    public boolean updatePost(PostUpdateDTO postUpdateDTO) {
        Long userId = UserContext.getUserId();
        Post oldPost = this.getById(postUpdateDTO.getId());
        if (oldPost == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (!oldPost.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改");
        }
        Post post = new Post();
        BeanUtils.copyProperties(postUpdateDTO, post);
        if (postUpdateDTO.getStatus() != null) {
            post.setUpdateTime(new Date()); // Already handled by copying? No, set explicitly if needed
        }
        post.setUpdateTime(new Date());
        return this.updateById(post);
    }

    @Override
    public boolean deletePost(Long id) {
        Long userId = UserContext.getUserId();
        Post post = this.getById(id);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除");
        }
        return this.removeById(id);
    }

    @Override
    public boolean incrementWantCount(Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(401, "请先登录");
        }

        Post post = this.getById(id);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        // 不能给自己点"我想要"
        if (currentUserId.equals(post.getUserId())) {
            throw new BusinessException(400, "不能对自己的发布表示想要");
        }

        // 检查是否已经点过 (Type 12)
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId)
                .eq("target_id", id)
                .eq("type", 12);
        if (favoriteMapper.selectCount(queryWrapper) > 0) {
            return true;
        }

        // 记录
        Favorite favorite = new Favorite();
        favorite.setUserId(currentUserId);
        favorite.setTargetId(id);
        favorite.setType(12);
        favorite.setCreateTime(new Date());
        favoriteMapper.insert(favorite);

        Integer currentWantCount = post.getWantCount() != null ? post.getWantCount() : 0;
        post.setWantCount(currentWantCount + 1);
        return this.updateById(post);
    }
}
