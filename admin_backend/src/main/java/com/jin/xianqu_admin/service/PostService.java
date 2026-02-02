package com.jin.xianqu_admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_admin.model.dto.PostPageDTO;
import com.jin.xianqu_admin.model.entity.Post;
import com.jin.xianqu_admin.model.vo.PostVO;

public interface PostService extends IService<Post> {

    Page<PostVO> getPostList(PostPageDTO dto);

    void deletePost(Long postId);
}
