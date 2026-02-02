package com.jin.xianqu_backend.controller.app;

import com.jin.xianqu_backend.common.Result;
import com.jin.xianqu_backend.model.dto.LoginDTO;
import com.jin.xianqu_backend.model.dto.UserUpdateDTO;
import com.jin.xianqu_backend.model.vo.LoginVO;
import com.jin.xianqu_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success(loginVO);
    }

    @PutMapping("/update")
    public Result<Boolean> updateUserInfo(@RequestBody UserUpdateDTO userUpdateDTO) {
        boolean result = userService.updateUserInfo(userUpdateDTO);
        return Result.success(result);
    }

    @GetMapping("/stats")
    public Result<com.jin.xianqu_backend.model.vo.UserStatsVO> getUserStats() {
        com.jin.xianqu_backend.model.vo.UserStatsVO stats = userService.getUserStats();
        return Result.success(stats);
    }

    @GetMapping("/current")
    public Result<com.jin.xianqu_backend.model.vo.UserInfoVO> getCurrentUser() {
        com.jin.xianqu_backend.model.vo.UserInfoVO userInfo = userService.getCurrentUserInfo();
        return Result.success(userInfo);
    }

    @PostMapping("/follow/{userId}")
    public Result<Boolean> followUser(@PathVariable Long userId) {
        return Result.success(userService.followUser(userId));
    }

    @PostMapping("/unfollow/{userId}")
    public Result<Boolean> unfollowUser(@PathVariable Long userId) {
        return Result.success(userService.unfollowUser(userId));
    }

    @GetMapping("/profile/{userId}")
    public Result<com.jin.xianqu_backend.model.vo.UserProfileVO> getUserProfile(@PathVariable Long userId) {
        return Result.success(userService.getUserProfile(userId));
    }

    @GetMapping("/following/{userId}")
    public Result<java.util.List<com.jin.xianqu_backend.model.vo.UserSimpleVO>> getFollowList(
            @PathVariable Long userId) {
        // If userId is 'me' or 0, use current? Frontend should pass ID.
        return Result.success(userService.getFollowList(userId));
    }

    @GetMapping("/followers/{userId}")
    public Result<java.util.List<com.jin.xianqu_backend.model.vo.UserSimpleVO>> getFanList(@PathVariable Long userId) {
        return Result.success(userService.getFanList(userId));
    }
}
