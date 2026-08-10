package net.togogo.springboot_travel.controller;

import jakarta.annotation.Resource;
import net.togogo.springboot_travel.Result.Result;
import net.togogo.springboot_travel.util.MinioUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 *
 * 【职责】
 * 提供统一的图片上传接口，返回 MinIO 访问 URL。
 * 管理后台上传景点图片时调用。
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Resource
    private MinioUtil minioUtil;

    /**
     * 上传景点图片
     *
     * 【请求方式】POST
     * 【请求地址】/api/upload/scenic
     * 【Content-Type】multipart/form-data
     * 【参数】file: 图片文件
     *
     * @param file 图片文件
     * @return 图片访问 URL
     */
    @PostMapping("/scenic")
    public Result<String> uploadScenicImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.fail("文件不能为空");
            }
            // 上传到 MinIO 的 scenic 目录下
            String url = minioUtil.upload(file, "scenic");
            System.out.println(">>> 【MinIO】图片上传成功，URL=" + url);
            return Result.success(url);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("上传失败：" + e.getMessage());
        }
    }
}