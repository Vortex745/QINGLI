package com.jin.xianqu_backend.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_backend.common.BaseResponse;
import com.jin.xianqu_backend.common.ResultUtils;
import com.jin.xianqu_backend.model.dto.PartTimeJobAddDTO;
import com.jin.xianqu_backend.model.vo.PartTimeJobVO;
import com.jin.xianqu_backend.service.PartTimeJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/part-time-job")
public class PartTimeJobController {

    @Autowired
    private PartTimeJobService partTimeJobService;

    @PostMapping("/add")
    public BaseResponse<Boolean> addJob(@RequestBody PartTimeJobAddDTO dto) {
        if (dto == null) {
            throw new RuntimeException("参数错误");
        }
        return ResultUtils.success(partTimeJobService.addJob(dto));
    }

    @GetMapping("/list")
    public BaseResponse<Page<PartTimeJobVO>> listJobs(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location) {
        return ResultUtils.success(partTimeJobService.listJobs(page, size, sort, userId, keyword, location));
    }

    @GetMapping("/my")
    public BaseResponse<Page<PartTimeJobVO>> listMyJobs(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResultUtils.success(partTimeJobService.listMyJobs(page, size, keyword));
    }

    @GetMapping("/{id}")
    public BaseResponse<PartTimeJobVO> getJobDetail(@PathVariable Long id) {
        return ResultUtils.success(partTimeJobService.getJobDetail(id));
    }

    /**
     * 编辑页获取详情 - 不增加浏览量
     */
    @GetMapping("/edit/{id}")
    public BaseResponse<PartTimeJobVO> getJobForEdit(@PathVariable Long id) {
        return ResultUtils.success(partTimeJobService.getJobForEdit(id));
    }

    @PutMapping("/update")
    public BaseResponse<Boolean> updateJob(@RequestBody PartTimeJobAddDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new RuntimeException("参数错误，缺少ID");
        }
        return ResultUtils.success(partTimeJobService.addJob(dto));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteJob(@PathVariable Long id) {
        return ResultUtils.success(partTimeJobService.removeById(id));
    }

    @PostMapping("/{id}/want")
    public BaseResponse<Boolean> incrementWantCount(@PathVariable Long id) {
        return ResultUtils.success(partTimeJobService.incrementWantCount(id));
    }
}
