package com.jin.xianqu_backend.controller.app;

import com.jin.xianqu_backend.common.Result;
import com.jin.xianqu_backend.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传接口
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件为空");
        }

        // 1. 获取文件名和后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = ".jpg"; // 默认后缀
        if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 2. 生成新文件名
        String fileName = UUID.randomUUID().toString() + suffix;

        // 3. 构建存储路径 (使用统一的绝对路径)
        String uploadDir = "E:/testJava/xianqu_uniapp/upload/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 4. 保存文件
        File dest = new File(uploadDir + fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(500, "文件保存失败: " + e.getMessage());
        }

        // 5. 返回访问URL (返回相对路径，由前端拼接基地址)
        // 这样可以兼容 App 端(8080) 和 Admin 端(8082)
        String fileUrl = "/profile/" + fileName;

        return Result.success(fileUrl);
    }
}
