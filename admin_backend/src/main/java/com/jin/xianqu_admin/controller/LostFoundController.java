package com.jin.xianqu_admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_admin.common.Result;
import com.jin.xianqu_admin.model.dto.LostFoundPageDTO;
import com.jin.xianqu_admin.model.entity.LostFound;
import com.jin.xianqu_admin.service.LostFoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/lostfound")
@RequiredArgsConstructor
public class LostFoundController {

    private final LostFoundService lostFoundService;

    /**
     * 分页查询失物招领列表
     */
    @PostMapping("/list")
    public Result<Page<LostFound>> getLostFoundList(@RequestBody LostFoundPageDTO dto) {
        return Result.success(lostFoundService.getLostFoundList(dto));
    }

    /**
     * 获取失物招领详情
     */
    @GetMapping("/{id}")
    public Result<LostFound> getLostFoundDetail(@PathVariable Long id) {
        LostFound lostFound = lostFoundService.getById(id);
        if (lostFound == null) {
            return Result.error(404, "记录不存在");
        }
        return Result.success(lostFound);
    }

    /**
     * 更新状态
     */
    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        lostFoundService.updateStatus(id, status);
        String msg = status == 1 ? "已标记为已找回/归还" : status == 2 ? "已下架" : "状态已更新";
        return Result.success(msg);
    }

    /**
     * 删除记录（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteLostFound(@PathVariable Long id) {
        lostFoundService.deleteLostFound(id);
        return Result.success("删除成功");
    }
}
