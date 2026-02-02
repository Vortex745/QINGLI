package com.jin.xianqu_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_backend.common.UserContext;
import com.jin.xianqu_backend.exception.BusinessException;
import com.jin.xianqu_backend.mapper.CommentMapper;
import com.jin.xianqu_backend.mapper.UserMapper;
import com.jin.xianqu_backend.model.dto.CommentAddDTO;
import com.jin.xianqu_backend.model.entity.Comment;
import com.jin.xianqu_backend.model.entity.User;
import com.jin.xianqu_backend.model.vo.CommentVO;
import com.jin.xianqu_backend.service.CommentService;
import com.jin.xianqu_backend.utils.ImageUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean addComment(CommentAddDTO commentAddDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentAddDTO, comment);
        comment.setUserId(userId);
        comment.setCreateTime(new Date());
        return this.save(comment);
    }

    @Override
    public Page<CommentVO> listComments(Long targetId, Integer type, int page, int size) {
        Page<Comment> pageParam = new Page<>(page, size);
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_id", targetId);
        queryWrapper.eq("type", type);
        queryWrapper.orderByDesc("create_time");

        Page<Comment> commentPage = this.page(pageParam, queryWrapper);

        // 转换VO并填充用户信息
        List<CommentVO> voList = commentPage.getRecords().stream().map(comment -> {
            User user = userMapper.selectById(comment.getUserId());
            CommentVO vo = CommentVO.builder()
                    .id(comment.getId())
                    .userId(comment.getUserId())
                    .content(comment.getContent())
                    .createTime(comment.getCreateTime())
                    .build();
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatarUrl(ImageUtils.cleanImageUrl(user.getAvatarUrl()));
            } else {
                vo.setNickname("用户" + comment.getUserId());
            }
            return vo;
        }).collect(Collectors.toList());

        Page<CommentVO> resultPage = new Page<>(page, size);
        resultPage.setRecords(voList);
        resultPage.setTotal(commentPage.getTotal());
        return resultPage;
    }

    @Override
    public boolean deleteComment(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }

        // 只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此评论");
        }

        return this.removeById(id);
    }
}
