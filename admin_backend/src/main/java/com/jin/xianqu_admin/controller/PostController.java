package com.jin.xianqu_admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_admin.common.Result;
import com.jin.xianqu_admin.model.dto.PostPageDTO;
import com.jin.xianqu_admin.model.entity.Post;
import com.jin.xianqu_admin.model.vo.PostVO;
import com.jin.xianqu_admin.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 分页查询帖子列表
     */
    @PostMapping("/list")
    public Result<Page<PostVO>> getPostList(@RequestBody PostPageDTO dto) {
        return Result.success(postService.getPostList(dto));
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{id}")
    public Result<Post> getPostDetail(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null) {
            return Result.error(404, "帖子不存在");
        }
        return Result.success(post);
    }

    /**
     * 删除帖子（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<String> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return Result.success("删除成功");
    }
}
