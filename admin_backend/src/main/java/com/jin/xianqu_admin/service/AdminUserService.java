package com.jin.xianqu_admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jin.xianqu_admin.model.dto.AdminLoginDTO;
import com.jin.xianqu_admin.model.entity.User;
import com.jin.xianqu_admin.model.vo.LoginVO;

public interface AdminUserService extends IService<User> {
    LoginVO adminLogin(AdminLoginDTO adminLoginDTO);

    com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> getUserList(
            com.jin.xianqu_admin.model.dto.AdminUserPageDTO queryDTO);

    void addUser(com.jin.xianqu_admin.model.entity.User user);

    /**
     * Update user status (ban/unban)
     * 
     * @param userId User ID
     * @param status 0: Banned, 1: Active
     */
    void updateUserStatus(Long userId, Integer status);

    java.util.List<Long> getAllUserIds();
}
