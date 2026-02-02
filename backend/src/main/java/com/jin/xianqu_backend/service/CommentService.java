package com.jin.xianqu_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_backend.model.dto.CommentAddDTO;
import com.jin.xianqu_backend.model.entity.Comment;
import com.jin.xianqu_backend.model.vo.CommentVO;

/**
 * 评论服务
 */
public interface CommentService extends IService<Comment> {

    /**
     * 添加评论
     * 
     * @param commentAddDTO
     * @return
     */
    boolean addComment(CommentAddDTO commentAddDTO);

    /**
     * 获取评论列表
     * 
     * @param targetId
     * @param type
     * @param page
     * @param size
     * @return
     */
    Page<CommentVO> listComments(Long targetId, Integer type, int page, int size);

    /**
     * 删除评论
     * 
     * @param id 评论ID
     * @return
     */
    boolean deleteComment(Long id);
}
