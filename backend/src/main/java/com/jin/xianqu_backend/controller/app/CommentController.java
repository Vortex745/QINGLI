package com.jin.xianqu_backend.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_backend.common.Result;
import com.jin.xianqu_backend.model.dto.CommentAddDTO;
import com.jin.xianqu_backend.model.vo.CommentVO;
import com.jin.xianqu_backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评论接口
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    public Result<Boolean> addComment(@RequestBody CommentAddDTO commentAddDTO) {
        boolean result = commentService.addComment(commentAddDTO);
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<Page<CommentVO>> listComments(@RequestParam Long targetId,
            @RequestParam Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CommentVO> commentPage = commentService.listComments(targetId, type, page, size);
        return Result.success(commentPage);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteComment(@PathVariable Long id) {
        boolean result = commentService.deleteComment(id);
        return Result.success(result);
    }
}
