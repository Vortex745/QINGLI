package com.jin.xianqu_backend.controller.app;

import com.jin.xianqu_backend.common.Result;
import com.jin.xianqu_backend.model.vo.AreaMatchVO;
import com.jin.xianqu_backend.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 区域接口
 */
@RestController
@RequestMapping("/area")
public class AreaController {

    @Autowired
    private AreaService areaService;

    @GetMapping("/match")
    public Result<AreaMatchVO> matchArea(@RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(required = false) Long userId) {
        AreaMatchVO match = areaService.matchArea(latitude, longitude, userId);
        return Result.success(match);
    }
}
