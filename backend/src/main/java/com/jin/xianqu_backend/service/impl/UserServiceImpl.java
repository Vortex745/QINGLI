package com.jin.xianqu_backend.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_backend.common.UserContext;
import com.jin.xianqu_backend.exception.BusinessException;
import com.jin.xianqu_backend.mapper.UserMapper;
import com.jin.xianqu_backend.model.dto.LoginDTO;
import com.jin.xianqu_backend.model.dto.UserUpdateDTO;
import com.jin.xianqu_backend.model.entity.User;
import com.jin.xianqu_backend.model.vo.LoginVO;
import com.jin.xianqu_backend.service.UserService;
import com.jin.xianqu_backend.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

        @Autowired
        private RestTemplate restTemplate;

        @Autowired
        private com.jin.xianqu_backend.mapper.GoodsMapper goodsMapper;
        @Autowired
        private com.jin.xianqu_backend.mapper.PostMapper postMapper;
        @Autowired
        private com.jin.xianqu_backend.mapper.PartTimeJobMapper partTimeJobMapper;
        @Autowired
        private com.jin.xianqu_backend.mapper.LostFoundMapper lostFoundMapper;
        @Autowired
        private com.jin.xianqu_backend.mapper.OrderMapper orderMapper;
        @Autowired
        private com.jin.xianqu_backend.mapper.FavoriteMapper favoriteMapper;
        @Autowired
        private com.jin.xianqu_backend.mapper.UserFollowMapper userFollowMapper;

        @Value("${wx.open.app_id:wx_appid_placeholder}")
        private String appId;

        @Value("${wx.open.app_secret:wx_secret_placeholder}")
        private String appSecret;

        // Access Token Cache
        private String accessToken;
        private long accessTokenExpiresAt = 0;

        @Override
        public LoginVO login(LoginDTO loginDTO) {
                String code = loginDTO.getCode();
                if (StringUtils.isBlank(code)) {
                        throw new BusinessException(400, "参数错误: Code不能为空");
                }

                // 1. 获取 OpenID (用于标识用户)
                String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId +
                                "&secret=" + appSecret + "&js_code=" + code + "&grant_type=authorization_code";
                String response = restTemplate.getForObject(url, String.class);
                JSONObject jsonObject = JSON.parseObject(response);

                if (jsonObject.containsKey("errcode") && jsonObject.getIntValue("errcode") != 0) {
                        System.err.println("WeChat Login Error: " + response);
                        throw new BusinessException(500, "微信登录失败: " + jsonObject.getString("errmsg"));
                }

                String openid = jsonObject.getString("openid");
                if (StringUtils.isBlank(openid)) {
                        throw new BusinessException(500, "登录失败，未获取到openid");
                }

                // 2. 获取手机号 (如果提供了 phoneCode)
                String mobile = null;
                if (StringUtils.isNotBlank(loginDTO.getPhoneCode())) {
                        try {
                                mobile = getWechatPhoneNumber(loginDTO.getPhoneCode());
                                System.out.println("📱 Got Mobile from WeChat: " + mobile);
                        } catch (Exception e) {
                                System.err.println("❌ Failed to get phone number: " + e.getMessage());
                                // 对于不支持该API的账号（如个人号），这里会失败，我们选择容错继续，或者抛出异常提醒用户
                                // throw new BusinessException(500, "获取手机号失败: " + e.getMessage());
                                // 为了演示效果，如果失败我们暂时忽略，通过 OpenID 登录
                        }
                }

                // 3. 查询数据库
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("openid", openid);
                User user = this.getOne(queryWrapper);

                // 4. 不存在则注册
                if (user == null) {
                        user = new User();
                        user.setOpenid(openid);
                        user.setCreateTime(new Date());
                        user.setRole("user");
                        user.setNickname("微信用户");
                        user.setAvatarUrl(""); // 默认空，前端显示占位符
                        if (mobile != null) {
                                user.setMobile(mobile);
                        }
                        this.save(user);
                } else {
                        // 更新手机号
                        if (mobile != null && !StringUtils.equals(user.getMobile(), mobile)) {
                                user.setMobile(mobile);
                                this.updateById(user);
                        }
                }

                // 5. 生成 JWT Token
                Map<String, Object> claims = new HashMap<>();
                claims.put("userId", user.getId());
                claims.put("role", user.getRole());
                String token = JwtUtils.createToken(claims);

                return LoginVO.builder()
                                .id(user.getId())
                                .openid(user.getOpenid())
                                .token(token)
                                .nickname(user.getNickname())
                                .avatarUrl(com.jin.xianqu_backend.utils.ImageUtils.cleanImageUrl(user.getAvatarUrl()))
                                .mobile(user.getMobile())
                                .build();
        }

        /**
         * 获取微信 access_token
         */
        private synchronized String getAccessToken() {
                long now = System.currentTimeMillis();
                if (this.accessToken != null && now < this.accessTokenExpiresAt) {
                        return this.accessToken;
                }

                String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId
                                + "&secret=" + appSecret;
                String response = restTemplate.getForObject(url, String.class);
                JSONObject json = JSON.parseObject(response);

                if (json.containsKey("errcode") && json.getIntValue("errcode") != 0) {
                        throw new BusinessException(500, "获取Access Token失败: " + json.getString("errmsg"));
                }

                this.accessToken = json.getString("access_token");
                // 提前 5 分钟过期
                this.accessTokenExpiresAt = now + (json.getLongValue("expires_in") * 1000) - 300000;
                System.out.println("🔑 Renewed Access Token: " + this.accessToken);
                return this.accessToken;
        }

        /**
         * 获取微信用户手机号
         */
        private String getWechatPhoneNumber(String phoneCode) {
                String token = getAccessToken();
                String url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=" + token;

                Map<String, String> param = new HashMap<>();
                param.put("code", phoneCode);

                // Explicitly set headers for JSON content type
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                org.springframework.http.HttpEntity<Map<String, String>> request = new org.springframework.http.HttpEntity<>(
                                param, headers);

                String response = restTemplate.postForObject(url, request, String.class);
                JSONObject json = JSON.parseObject(response);

                if (json.containsKey("errcode") && json.getIntValue("errcode") != 0) {
                        throw new BusinessException(500, "微信API报错: " + json.getString("errmsg"));
                }

                if (json.containsKey("phone_info")) {
                        return json.getJSONObject("phone_info").getString("phoneNumber");
                }
                return null;
        }

        @Override
        public boolean updateUserInfo(UserUpdateDTO userUpdateDTO) {
                Long userId = UserContext.getUserId();
                System.out.println("💾 Updating user info for ID: " + userId);
                System.out.println("💾 Received DTO: " + userUpdateDTO);

                User user = this.getById(userId);
                if (user == null) {
                        throw new BusinessException(404, "用户不存在");
                }

                if (userUpdateDTO.getNickname() != null)
                        user.setNickname(userUpdateDTO.getNickname());
                if (userUpdateDTO.getAvatarUrl() != null)
                        user.setAvatarUrl(userUpdateDTO.getAvatarUrl());
                if (userUpdateDTO.getBio() != null)
                        user.setBio(userUpdateDTO.getBio());
                if (userUpdateDTO.getBgImage() != null) {
                        System.out.println("💾 Setting bgImage to: " + userUpdateDTO.getBgImage());
                        user.setBgImage(userUpdateDTO.getBgImage());
                }

                user.setUpdateTime(new Date());
                boolean result = this.updateById(user);
                System.out.println("💾 Update result: " + result);
                return result;
        }

        @Override
        public com.jin.xianqu_backend.model.vo.UserStatsVO getUserStats() {
                Long userId = UserContext.getUserId();
                if (userId == null) {
                        return com.jin.xianqu_backend.model.vo.UserStatsVO.builder()
                                        .publishedCount(0L)
                                        .soldCount(0L)
                                        .boughtCount(0L)
                                        .favoriteCount(0L)
                                        .build();
                }

                // 1. My Published (Goods + Post + PartTime + LostFound)
                Long goodsPublish = goodsMapper
                                .selectCount(new QueryWrapper<com.jin.xianqu_backend.model.entity.Goods>().eq("user_id",
                                                userId));
                Long postPublish = postMapper
                                .selectCount(new QueryWrapper<com.jin.xianqu_backend.model.entity.Post>().eq("user_id",
                                                userId));
                Long partTimePublish = partTimeJobMapper
                                .selectCount(new QueryWrapper<com.jin.xianqu_backend.model.entity.PartTimeJob>()
                                                .eq("user_id", userId));
                Long lostFoundPublish = lostFoundMapper
                                .selectCount(new QueryWrapper<com.jin.xianqu_backend.model.entity.LostFound>()
                                                .eq("user_id", userId));
                Long totalPublished = goodsPublish + postPublish + partTimePublish + lostFoundPublish;

                // 2. My Sold (Goods status = 2)
                Long soldCount = goodsMapper.selectCount(new QueryWrapper<com.jin.xianqu_backend.model.entity.Goods>()
                                .eq("user_id", userId)
                                .eq("status", 2));

                // 3. My Bought (Order buyerId = userId)
                Long boughtCount = orderMapper.selectCount(new QueryWrapper<com.jin.xianqu_backend.model.entity.Order>()
                                .eq("buyer_id", userId));

                // 4. Favorites (Only count Goods type=1 as only goods are shown in favorites
                // page)
                Long favoriteCount = favoriteMapper
                                .selectCount(new QueryWrapper<com.jin.xianqu_backend.model.entity.Favorite>()
                                                .eq("user_id", userId)
                                                .eq("type", 1));

                // 5. Follows/Fans
                Long followerCount = userFollowMapper
                                .selectCount(new QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>()
                                                .eq("followed_id", userId));
                Long followingCount = userFollowMapper
                                .selectCount(new QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>()
                                                .eq("follower_id", userId));

                return com.jin.xianqu_backend.model.vo.UserStatsVO.builder()
                                .publishedCount(totalPublished)
                                .soldCount(soldCount)
                                .boughtCount(boughtCount)
                                .favoriteCount(favoriteCount)
                                .followerCount(followerCount)
                                .followingCount(followingCount)
                                .build();
        }

        @Override
        public boolean followUser(Long followedId) {
                Long currentUserId = UserContext.getUserId();
                if (currentUserId.equals(followedId)) {
                        throw new BusinessException(400, "不能关注自己");
                }

                com.jin.xianqu_backend.model.entity.UserFollow follow = new com.jin.xianqu_backend.model.entity.UserFollow();
                follow.setFollowerId(currentUserId);
                follow.setFollowedId(followedId);
                follow.setCreateTime(new Date());

                try {
                        userFollowMapper.insert(follow);
                        return true;
                } catch (Exception e) {
                        // Duplicate key means already followed, ignore or return true
                        return true;
                }
        }

        @Override
        public boolean unfollowUser(Long followedId) {
                Long currentUserId = UserContext.getUserId();
                userFollowMapper.delete(new QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>()
                                .eq("follower_id", currentUserId)
                                .eq("followed_id", followedId));
                return true;
        }

        @Override
        public com.jin.xianqu_backend.model.vo.UserProfileVO getUserProfile(Long userId) {
                User user = this.getById(userId);
                if (user == null) {
                        throw new BusinessException(404, "用户不存在");
                }

                // Counts
                Long followerCount = userFollowMapper
                                .selectCount(new QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>()
                                                .eq("followed_id", userId));
                Long followingCount = userFollowMapper
                                .selectCount(new QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>()
                                                .eq("follower_id", userId));
                Long goodsCount = goodsMapper.selectCount(new QueryWrapper<com.jin.xianqu_backend.model.entity.Goods>()
                                .eq("user_id", userId).eq("status", 0));

                // Is Followed?
                Boolean isFollowed = false;
                try {
                        Long currentUserId = UserContext.getUserId(); // Might be null if not logged in?
                        if (currentUserId != null) {
                                Long count = userFollowMapper.selectCount(
                                                new QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>()
                                                                .eq("follower_id", currentUserId)
                                                                .eq("followed_id", userId));
                                isFollowed = count > 0;
                        }
                } catch (Exception e) {
                        // Ignore if not logged in
                }

                com.jin.xianqu_backend.model.vo.UserProfileVO vo = new com.jin.xianqu_backend.model.vo.UserProfileVO();
                vo.setUserInfo(user);
                vo.setFollowerCount(followerCount.intValue());
                vo.setFollowingCount(followingCount.intValue());
                vo.setGoodsCount(goodsCount.intValue());
                vo.setIsFollowed(isFollowed);

                return vo;
        }

        @Override
        public java.util.List<com.jin.xianqu_backend.model.vo.UserSimpleVO> getFollowList(Long userId) {
                // Find people userId follows
                java.util.List<com.jin.xianqu_backend.model.entity.UserFollow> follows = userFollowMapper.selectList(
                                new QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>().eq("follower_id",
                                                userId));

                if (follows.isEmpty())
                        return new java.util.ArrayList<>();

                java.util.List<Long> followedIds = follows.stream()
                                .map(com.jin.xianqu_backend.model.entity.UserFollow::getFollowedId)
                                .collect(java.util.stream.Collectors.toList());
                java.util.List<User> users = this.listByIds(followedIds);

                return users.stream().map(u -> {
                        com.jin.xianqu_backend.model.vo.UserSimpleVO vo = new com.jin.xianqu_backend.model.vo.UserSimpleVO();
                        BeanUtils.copyProperties(u, vo);
                        vo.setIsFollowed(true); // Since we follow them
                        return vo;
                }).collect(java.util.stream.Collectors.toList());
        }

        @Override
        public java.util.List<com.jin.xianqu_backend.model.vo.UserSimpleVO> getFanList(Long userId) {
                // Find people who follow userId
                java.util.List<com.jin.xianqu_backend.model.entity.UserFollow> fans = userFollowMapper.selectList(
                                new QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>().eq("followed_id",
                                                userId));

                if (fans.isEmpty())
                        return new java.util.ArrayList<>();

                java.util.List<Long> fanIds = fans.stream()
                                .map(com.jin.xianqu_backend.model.entity.UserFollow::getFollowerId)
                                .collect(java.util.stream.Collectors.toList());
                java.util.List<User> users = this.listByIds(fanIds);

                // Check if I follow them back?
                Long currentUserId = UserContext.getUserId();
                java.util.Set<Long> myFollows = new java.util.HashSet<>();
                if (currentUserId != null) {
                        java.util.List<com.jin.xianqu_backend.model.entity.UserFollow> myFollowList = userFollowMapper
                                        .selectList(
                                                        new QueryWrapper<com.jin.xianqu_backend.model.entity.UserFollow>()
                                                                        .eq("follower_id", currentUserId));
                        myFollows = myFollowList.stream()
                                        .map(com.jin.xianqu_backend.model.entity.UserFollow::getFollowedId)
                                        .collect(java.util.stream.Collectors.toSet());
                }

                final java.util.Set<Long> finalMyFollows = myFollows;
                return users.stream().map(u -> {
                        com.jin.xianqu_backend.model.vo.UserSimpleVO vo = new com.jin.xianqu_backend.model.vo.UserSimpleVO();
                        BeanUtils.copyProperties(u, vo);
                        vo.setIsFollowed(finalMyFollows.contains(u.getId()));
                        return vo;
                }).collect(java.util.stream.Collectors.toList());
        }

        @Override
        public com.jin.xianqu_backend.model.vo.UserInfoVO getCurrentUserInfo() {
                Long userId = UserContext.getUserId();
                if (userId == null) {
                        throw new BusinessException(401, "未登录");
                }

                User user = this.getById(userId);
                if (user == null) {
                        throw new BusinessException(404, "用户不存在");
                }

                System.out.println("💾 Fetching current user info for: " + userId);
                System.out.println("💾 Raw bgImage from DB: " + user.getBgImage());

                return com.jin.xianqu_backend.model.vo.UserInfoVO.builder()
                                .id(user.getId())
                                .userId(user.getId())
                                .openid(user.getOpenid())
                                .nickname(user.getNickname())
                                .avatarUrl(com.jin.xianqu_backend.utils.ImageUtils.cleanImageUrl(user.getAvatarUrl()))
                                .bio(user.getBio())
                                .bgImage(com.jin.xianqu_backend.utils.ImageUtils.cleanImageUrl(user.getBgImage()))
                                .mobile(user.getMobile())
                                .role(user.getRole())
                                .build();
        }
}
