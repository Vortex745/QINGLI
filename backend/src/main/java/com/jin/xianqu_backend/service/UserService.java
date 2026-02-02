package com.jin.xianqu_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_backend.model.dto.LoginDTO;
import com.jin.xianqu_backend.model.dto.UserUpdateDTO;
import com.jin.xianqu_backend.model.entity.User;
import com.jin.xianqu_backend.model.vo.LoginVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    LoginVO login(LoginDTO loginDTO);

    /**
     * 更新用户信息
     * 
     * @param userUpdateDTO
     * @return
     */
    boolean updateUserInfo(UserUpdateDTO userUpdateDTO);

    /**
     * 获取用户统计数据
     * 
     * @return
     */
    com.jin.xianqu_backend.model.vo.UserStatsVO getUserStats();

    /**
     * Set user bio and background image
     */
    // handled by updateUserInfo already? Check DTO.

    boolean followUser(Long followedId);

    boolean unfollowUser(Long followedId);

    com.jin.xianqu_backend.model.vo.UserProfileVO getUserProfile(Long userId);

    java.util.List<com.jin.xianqu_backend.model.vo.UserSimpleVO> getFollowList(Long userId);

    java.util.List<com.jin.xianqu_backend.model.vo.UserSimpleVO> getFanList(Long userId);

    /**
     * 获取当前用户信息（从数据库刷新）
     */
    com.jin.xianqu_backend.model.vo.UserInfoVO getCurrentUserInfo();
}
