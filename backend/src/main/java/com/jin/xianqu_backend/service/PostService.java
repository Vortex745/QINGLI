package com.jin.xianqu_backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_backend.model.dto.PostAddDTO;
import com.jin.xianqu_backend.model.dto.PostUpdateDTO;
import com.jin.xianqu_backend.model.entity.Post;

import com.jin.xianqu_backend.model.vo.PostVO;

/**
 * 帖子服务接口
 */
public interface PostService extends IService<Post> {

    Page<PostVO> listPosts(int page, int size, Long areaId, Integer type, Long userId, String keyword, String sort,
            String location);

    Page<PostVO> listMyPosts(int page, int size, String keyword);

    Long addPost(PostAddDTO postAddDTO);

    /**
     * 更新帖子
     * 
     * @param postUpdateDTO
     * @return
     */
    boolean updatePost(PostUpdateDTO postUpdateDTO);

    /**
     * 删除帖子
     * 
     * @param id
     * @return
     */
    boolean deletePost(Long id);

    /**
     * 获取帖子详情
     * 
     * @param id
     * @return
     */
    PostVO getPostDetail(Long id);

    boolean incrementWantCount(Long id);
}
