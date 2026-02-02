package com.jin.xianqu_backend.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_backend.common.Result;
import com.jin.xianqu_backend.model.dto.PostAddDTO;
import com.jin.xianqu_backend.model.dto.PostUpdateDTO;

import com.jin.xianqu_backend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.jin.xianqu_backend.model.vo.PostVO;

/**
 * 帖子接口
 */
@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/list")
    public Result<Page<PostVO>> listPosts(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String location) {
        Page<PostVO> postPage = postService.listPosts(page, size, areaId, type, userId, keyword, sort, location);
        return Result.success(postPage);
    }

    @GetMapping("/my")
    public Result<Page<PostVO>> listMyPosts(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<PostVO> postPage = postService.listMyPosts(page, size, keyword);
        return Result.success(postPage);
    }

    @GetMapping("/{id}")
    public Result<PostVO> getPostDetail(@PathVariable Long id) {
        PostVO postVO = postService.getPostDetail(id);
        return Result.success(postVO);
    }

    @PostMapping("/add")
    public Result<Long> addPost(@RequestBody PostAddDTO postAddDTO) {
        Long postId = postService.addPost(postAddDTO);
        return Result.success(postId);
    }

    @PutMapping("/update")
    public Result<Boolean> updatePost(@RequestBody PostUpdateDTO postUpdateDTO) {
        boolean result = postService.updatePost(postUpdateDTO);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deletePost(@PathVariable Long id) {
        boolean result = postService.deletePost(id);
        return Result.success(result);
    }

    @PostMapping("/{id}/want")
    public Result<Boolean> incrementWantCount(@PathVariable Long id) {
        boolean result = postService.incrementWantCount(id);
        return Result.success(result);
    }
}
