package com.jin.xianqu_backend.controller.app;

import com.jin.xianqu_backend.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 开放接口
 *
 */
@RestController
@RequestMapping("/")
public class MainController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("ok");
    }
}
