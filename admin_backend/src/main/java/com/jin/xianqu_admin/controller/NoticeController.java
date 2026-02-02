package com.jin.xianqu_admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jin.xianqu_admin.common.Result;
import com.jin.xianqu_admin.model.dto.PageRequestDTO;
import com.jin.xianqu_admin.model.entity.Notice;
import com.jin.xianqu_admin.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @PostMapping("/list")
    public Result<Page<Notice>> getList(@RequestBody PageRequestDTO queryDTO) {
        return Result.success(noticeService.getList(queryDTO));
    }

    @PostMapping("/send")
    public Result<String> send(@RequestBody Notice notice) {
        System.out.println("NoticeController.send called with title: " + notice.getTitle());
        try {
            noticeService.sendNotice(notice);
            return Result.success("发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                java.nio.file.Files.write(java.nio.file.Paths.get("controller_error_v2.txt"),
                        ("Error in NoticeController: " + e.toString()).getBytes());
            } catch (Exception ex) {
            }
            return Result.error("发送失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        noticeService.removeById(id);
        return Result.success("删除成功");
    }
}
