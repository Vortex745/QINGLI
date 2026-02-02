package com.jin.xianqu_admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_admin.common.Result;
import com.jin.xianqu_admin.model.dto.GoodsPageDTO;
import com.jin.xianqu_admin.model.entity.Goods;
import com.jin.xianqu_admin.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.jin.xianqu_admin.model.vo.GoodsVO;

@RestController
@RequestMapping("/admin/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @PostMapping("/list")
    public Result<Page<GoodsVO>> getGoodsList(@RequestBody GoodsPageDTO dto) {
        return Result.success(goodsService.getGoodsList(dto));
    }

    @PutMapping("/{goodsId}/status")
    public Result<String> updateGoodsStatus(
            @PathVariable Long goodsId,
            @RequestParam Integer status) {
        goodsService.updateGoodsStatus(goodsId, status);
        String msg = status == 0 ? "商品已上架" : status == 3 ? "商品已下架" : "状态已更新";
        return Result.success(msg);
    }

    @GetMapping("/{goodsId}")
    public Result<Goods> getGoodsDetail(@PathVariable Long goodsId) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            return Result.error(404, "商品不存在");
        }
        return Result.success(goods);
    }
}
