package com.jin.xianqu_admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_admin.mapper.PostMapper;
import com.jin.xianqu_admin.mapper.UserMapper;
import com.jin.xianqu_admin.model.dto.PostPageDTO;
import com.jin.xianqu_admin.model.entity.Post;
import com.jin.xianqu_admin.model.entity.User;
import com.jin.xianqu_admin.model.vo.PostVO;
import com.jin.xianqu_admin.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final UserMapper userMapper;

    @Override
    public Page<PostVO> getPostList(PostPageDTO dto) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();

        // 按内容模糊搜索
        if (StringUtils.hasText(dto.getContent())) {
            wrapper.like(Post::getContent, dto.getContent());
        }

        // 按区域筛选
        if (StringUtils.hasText(dto.getArea())) {
            wrapper.eq(Post::getLocation, dto.getArea());
        }

        // 按类型筛选 (默认查询校园广场 type=3)
        if (dto.getType() != null) {
            wrapper.eq(Post::getType, dto.getType());
        } else {
            wrapper.eq(Post::getType, 3); // 默认只查校园广场
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Post::getCreateTime);

        // 2. 分页查询
        Page<Post> page = new Page<>(dto.getPage(), dto.getSize());
        Page<Post> postPage = this.page(page, wrapper);

        // 3. 转换为 VO 并填充用户信息
        Page<PostVO> voPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());

        List<Post> records = postPage.getRecords();
        if (records.isEmpty()) {
            return voPage;
        }

        // 批量查询用户信息
        Set<Long> userIds = records.stream().map(Post::getUserId).collect(Collectors.toSet());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        // 按昵称筛选（如果有）
        String nicknameFilter = dto.getNickname();

        List<PostVO> voList = records.stream()
                .map(post -> {
                    PostVO vo = new PostVO();
                    BeanUtils.copyProperties(post, vo);

                    // 填充用户信息
                    User user = userMap.get(post.getUserId());
                    if (user != null) {
                        vo.setNickname(user.getNickname());
                        vo.setAvatarUrl(user.getAvatarUrl());
                    }

                    vo.setArea(post.getLocation());
                    vo.setHasImage(StringUtils.hasText(post.getImageUrls()));

                    return vo;
                })
                .filter(vo -> {
                    // 如果有昵称筛选条件
                    if (StringUtils.hasText(nicknameFilter)) {
                        return vo.getNickname() != null && vo.getNickname().contains(nicknameFilter);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void deletePost(Long postId) {
        // 逻辑删除
        this.removeById(postId);
    }
}
