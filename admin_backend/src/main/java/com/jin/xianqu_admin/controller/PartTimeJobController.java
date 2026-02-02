package com.jin.xianqu_admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_admin.common.Result;
import com.jin.xianqu_admin.model.dto.PageRequestDTO;
import com.jin.xianqu_admin.model.entity.PartTimeJob;
import com.jin.xianqu_admin.service.PartTimeJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/part-time")
public class PartTimeJobController {

    public PartTimeJobController() {
        System.out.println("====== PartTimeJobController Initialized ======");
    }

    @Autowired
    private PartTimeJobService partTimeJobService;

    @PostMapping("/list")
    public Result<Page<PartTimeJob>> getList(@RequestBody PageRequestDTO queryDTO) {
        return Result.success(partTimeJobService.getList(queryDTO));
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        partTimeJobService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        partTimeJobService.removeById(id);
        return Result.success("删除成功");
    }
}
