package com.jin.xianqu_admin.controller;

import com.jin.xianqu_admin.common.Result;
import com.jin.xianqu_admin.service.SystemNoticeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/system/notice")
@RequiredArgsConstructor
public class SystemController {

    private final SystemNoticeService systemNoticeService;

    @PostMapping("/send")
    public Result<String> sendNotice(@RequestBody NoticeRequest req) {
        systemNoticeService.sendNotice(req.getTarget(), req.getContent());
        return Result.success("消息发送成功");
    }

    @Data
    public static class NoticeRequest {
        private String target; // "all" or userId
        private String content;
    }
}
