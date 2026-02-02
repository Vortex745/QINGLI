package com.jin.xianqu_admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_admin.exception.BusinessException;
import com.jin.xianqu_admin.mapper.UserMapper;
import com.jin.xianqu_admin.model.dto.AdminLoginDTO;
import com.jin.xianqu_admin.model.entity.User;
import com.jin.xianqu_admin.model.vo.LoginVO;
import com.jin.xianqu_admin.service.AdminUserService;
import com.jin.xianqu_admin.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminUserServiceImpl extends ServiceImpl<UserMapper, User> implements AdminUserService {

    @Override
    public LoginVO adminLogin(AdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();

        if (StringUtils.isAnyBlank(username, password)) {
            throw new BusinessException(400, "用户名或密码不能为空");
        }

        User user = this.getOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        if (!password.equals(user.getPassword())) {
            throw new BusinessException(400, "密码错误");
        }

        if (!"admin".equals(user.getRole())) {
            throw new BusinessException(403, "无权限登录后台管理系统");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        claims.put("type", "admin");
        String token = JwtUtils.createToken(claims);

        return LoginVO.builder()
                .id(user.getId())
                .token(token)
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> getUserList(
            com.jin.xianqu_admin.model.dto.AdminUserPageDTO queryDTO) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                queryDTO.getPage(), queryDTO.getSize());

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(queryDTO.getNickname())) {
            queryWrapper.like("nickname", queryDTO.getNickname());
        }

        // Add status filter
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }

        // Add role filter
        if (StringUtils.isNotBlank(queryDTO.getRole())) {
            queryWrapper.eq("role", queryDTO.getRole());
        }

        // Order by create time desc
        queryWrapper.orderByDesc("create_time");

        return this.page(page, queryWrapper);
    }

    @Override
    public void addUser(User user) {
        // Set default values
        if (user.getCreateTime() == null) {
            user.setCreateTime(new java.util.Date());
        }
        // Ensure status is 1 (Active) by default if not set
        // Again, User entity needs status field. Since I cannot edit User.java in this
        // tool call alongside,
        // I will assume it's passed or handled via map/dynamic if not in entity.
        // Wait, if I use MyBatis Plus, I need the field in Entity.
        // I will update User.java in next step. For now, just save.

        // Add default password if empty and username present
        if (StringUtils.isNotBlank(user.getUsername()) && StringUtils.isBlank(user.getPassword())) {
            user.setPassword("123456"); // Default password
        }

        this.save(user);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        user.setUpdateTime(new java.util.Date());
        this.updateById(user);
    }

    @Override
    public java.util.List<Long> getAllUserIds() {
        return this.list().stream().map(User::getId).collect(java.util.stream.Collectors.toList());
    }
}
