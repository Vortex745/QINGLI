package com.jin.xianqu_backend.utils;

/**
 * 图片处理工具类
 */
public class ImageUtils {

    /**
     * 清洗图片路径，过滤掉损坏的已知路径
     */
    public static String cleanImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        // 过滤掉已知的损坏文件 ID
        if (url.contains("f7a30ed7-6dc6-4aca-8ffa-2279b7d8ce6b") ||
                url.contains("465b51d0-99ca-4ec1-9074-ed90a63d1993")) {
            return null;
        }

        // 强力修复：如果包含 192.168.x.x，直接截断为相对路径
        // 例如 http://192.168.5.10:8081/api/profile/abc.jpg -> /profile/abc.jpg
        if (url.contains("192.168.")) {
            // 尝试找到 /profile/ 或 /static/ 的位置
            int profileIndex = url.indexOf("/profile/");
            if (profileIndex != -1) {
                return url.substring(profileIndex);
            }
            int staticIndex = url.indexOf("/static/");
            if (staticIndex != -1) {
                return url.substring(staticIndex);
            }
        }

        return url;
    }
}
