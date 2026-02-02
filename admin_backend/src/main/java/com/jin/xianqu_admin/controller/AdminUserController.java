package com.jin.xianqu_admin.controller;

import com.jin.xianqu_admin.common.Result;
import com.jin.xianqu_admin.model.dto.AdminLoginDTO;
import com.jin.xianqu_admin.model.vo.LoginVO;
import com.jin.xianqu_admin.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台用户接口
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @PostMapping("/login")
    public Result<LoginVO> adminLogin(@RequestBody AdminLoginDTO adminLoginDTO) {
        LoginVO loginVO = adminUserService.adminLogin(adminLoginDTO);
        return Result.success(loginVO);
    }

    @PostMapping("/list")
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.jin.xianqu_admin.model.entity.User>> getUserList(
            @RequestBody com.jin.xianqu_admin.model.dto.AdminUserPageDTO queryDTO) {
        return Result.success(adminUserService.getUserList(queryDTO));
    }

    @PostMapping("/add")
    public Result<String> addUser(@RequestBody com.jin.xianqu_admin.model.entity.User user) {
        adminUserService.addUser(user);
        return Result.success("用户添加成功");
    }

    @PutMapping("/{userId}/status")
    public Result<String> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        adminUserService.updateUserStatus(userId, status);
        return Result.success(status == 1 ? "用户已解封" : "用户已封禁");
    }
}
