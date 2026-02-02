package com.jin.xianqu_backend.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_backend.common.Result;
import com.jin.xianqu_backend.model.dto.GoodsAddDTO;
import com.jin.xianqu_backend.model.dto.GoodsUpdateDTO;
import com.jin.xianqu_backend.model.vo.GoodsVO;
import com.jin.xianqu_backend.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jin.xianqu_backend.model.entity.Goods;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * 商品接口
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/list")
    public Result<Page<GoodsVO>> listGoods(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String university,
            @RequestParam(required = false) String sort) {
        Page<GoodsVO> goodsPage = goodsService.listGoods(page, size, areaId, userId, keyword, university, sort);
        return Result.success(goodsPage);
    }

    @GetMapping("/my")
    public Result<Page<GoodsVO>> listMyGoods(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<GoodsVO> goodsPage = goodsService.listMyGoods(page, size, keyword);
        return Result.success(goodsPage);
    }

    @PostMapping("/add")
    public Result<Long> addGoods(@RequestBody @Valid GoodsAddDTO goodsAddDTO) {
        Long goodsId = goodsService.addGoods(goodsAddDTO);
        return Result.success(goodsId);
    }

    @PutMapping("/update")
    public Result<Boolean> updateGoods(@RequestBody GoodsUpdateDTO goodsUpdateDTO) {
        boolean result = goodsService.updateGoods(goodsUpdateDTO);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteGoods(@PathVariable Long id) {
        boolean result = goodsService.deleteGoods(id);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<GoodsVO> getGoodsDetail(@PathVariable Long id) {
        GoodsVO goodsVO = goodsService.getGoodsDetail(id);
        return Result.success(goodsVO);
    }

    @PostMapping("/{id}/want")
    public Result<Boolean> incrementWantCount(@PathVariable Long id) {
        boolean result = goodsService.incrementWantCount(id);
        return Result.success(result);
    }

    @GetMapping("/hotSearch")
    public Result<List<String>> getHotKeywords() {
        // 1. 获取热门分类
        QueryWrapper<Goods> catWrapper = new QueryWrapper<>();
        catWrapper.select("distinct category");
        catWrapper.eq("status", 0);
        catWrapper.last("limit 4");
        List<String> categories = goodsService.list(catWrapper).stream()
                .map(Goods::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .collect(Collectors.toList());

        // 2. 获取热门商品名称
        QueryWrapper<Goods> nameWrapper = new QueryWrapper<>();
        nameWrapper.eq("status", 0);
        nameWrapper.orderByDesc("view_count", "want_count");
        nameWrapper.last("limit 6");
        List<String> names = goodsService.list(nameWrapper).stream()
                .map(g -> {
                    String name = g.getName();
                    return name.length() > 6 ? name.substring(0, 6) : name;
                })
                .collect(Collectors.toList());

        // 3. 合并并去重
        List<String> keywords = new ArrayList<>();
        keywords.addAll(categories);
        keywords.addAll(names);
        keywords = keywords.stream().distinct().collect(Collectors.toList());

        // 4. 兜底数据
        if (keywords.size() < 6) {
            String[] defaults = { "考研", "自行车", "耳机", "教材", "求助" };
            for (String d : defaults) {
                if (!keywords.contains(d) && keywords.size() < 10) {
                    keywords.add(d);
                }
            }
        }

        return Result.success(keywords.size() > 10 ? keywords.subList(0, 10) : keywords);
    }
}
