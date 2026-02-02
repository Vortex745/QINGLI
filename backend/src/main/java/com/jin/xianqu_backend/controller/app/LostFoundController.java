package com.jin.xianqu_backend.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_backend.common.BaseResponse;
import com.jin.xianqu_backend.common.ResultUtils;
import com.jin.xianqu_backend.model.dto.LostFoundAddDTO;
import com.jin.xianqu_backend.model.vo.LostFoundVO;
import com.jin.xianqu_backend.service.LostFoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lost-found")
public class LostFoundController {

    @Autowired
    private LostFoundService lostFoundService;

    @PostMapping("/add")
    public BaseResponse<Boolean> add(@RequestBody LostFoundAddDTO dto) {
        return ResultUtils.success(lostFoundService.add(dto));
    }

    @GetMapping("/list")
    public BaseResponse<Page<LostFoundVO>> list(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String sort) {
        return ResultUtils.success(lostFoundService.list(page, size, type, userId, keyword, location, sort));
    }

    @GetMapping("/my")
    public BaseResponse<Page<LostFoundVO>> listMy(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResultUtils.success(lostFoundService.listMy(page, size, keyword));
    }

    @GetMapping("/{id}")
    public BaseResponse<LostFoundVO> getDetail(@PathVariable Long id) {
        return ResultUtils.success(lostFoundService.getDetail(id));
    }

    @PutMapping("/update")
    public BaseResponse<Boolean> update(@RequestBody LostFoundAddDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new RuntimeException("参数错误，缺少ID");
        }
        return ResultUtils.success(lostFoundService.add(dto));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> delete(@PathVariable Long id) {
        try {
            boolean success = lostFoundService.removeById(id);
            if (!success) {
                return ResultUtils.error(500, "删除失败，记录可能不存在");
            }
            return ResultUtils.success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtils.error(500, "删除异常: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/want")
    public BaseResponse<Boolean> incrementWantCount(@PathVariable Long id) {
        return ResultUtils.success(lostFoundService.incrementWantCount(id));
    }
}
