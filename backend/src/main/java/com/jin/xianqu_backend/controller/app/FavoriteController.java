package com.jin.xianqu_backend.controller.app;

import com.jin.xianqu_backend.common.Result;
import com.jin.xianqu_backend.model.dto.FavoriteToggleDTO;
import com.jin.xianqu_backend.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏/点赞接口
 */
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_backend.model.vo.GoodsVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/toggle")
    public Result<Boolean> toggleFavorite(@RequestBody FavoriteToggleDTO favoriteToggleDTO) {
        boolean result = favoriteService.toggleFavorite(favoriteToggleDTO);
        return Result.success(result);
    }

    @GetMapping("/list/goods")
    public Result<Page<GoodsVO>> listMyFavoriteGoods(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<GoodsVO> result = favoriteService.listMyFavoriteGoods(page, size);
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<Page<Object>> listMyFavorites(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam int type) {
        Page<Object> result = favoriteService.listMyFavorites(page, size, type);
        return Result.success(result);
    }
}
