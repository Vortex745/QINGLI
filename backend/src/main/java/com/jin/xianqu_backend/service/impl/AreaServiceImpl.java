package com.jin.xianqu_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jin.xianqu_backend.mapper.AreaMapper;
import com.jin.xianqu_backend.mapper.UserMapper;
import com.jin.xianqu_backend.model.entity.Area;
import com.jin.xianqu_backend.model.entity.User;
import com.jin.xianqu_backend.model.vo.AreaMatchVO;
import com.jin.xianqu_backend.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 区域服务实现
 */
@Service
public class AreaServiceImpl extends ServiceImpl<AreaMapper, Area> implements AreaService {

    @Autowired
    private UserMapper userMapper;

    // 默认半径（米），暂定 5000 米
    private static final double MAX_RADIUS = 5000;

    @Override
    public AreaMatchVO matchArea(BigDecimal latitude, BigDecimal longitude, Long userId) {
        List<Area> list = this.list();
        Area bestMatch = null;
        double minDistance = Double.MAX_VALUE;

        for (Area area : list) {
            double distance = getDistance(
                    latitude.doubleValue(), longitude.doubleValue(),
                    area.getLatitude().doubleValue(), area.getLongitude().doubleValue());
            if (distance < minDistance) {
                minDistance = distance;
                bestMatch = area;
            }
        }

        if (bestMatch == null || minDistance > MAX_RADIUS) {
            // 没有匹配到或者超出范围，可以返回 null 或者特定标识
            // 这里假设超出范围就不算在区域内，但为了不报错，可以返回最近的但不更新数据库，或者直接 null
            // 按照需求 "如果用户距离某个区域在 radius 范围内，则判定匹配成功"
            if (minDistance > MAX_RADIUS) {
                return null; // 或者返回一个表示“未在服务区”的对象
            }
        }

        // 更新用户所在区域
        if (userId != null && bestMatch != null) {
            User user = new User();
            user.setId(userId);
            user.setCurrentAreaId(bestMatch.getId());
            userMapper.updateById(user);
        }

        return AreaMatchVO.builder()
                .id(bestMatch.getId())
                .name(bestMatch.getName())
                .type(bestMatch.getType())
                .distance(minDistance)
                .build();
    }

    private double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * 6378137.0;
        return Math.round(s * 10000d) / 10000d;
    }

    private double rad(double d) {
        return d * Math.PI / 180.0;
    }
}
