package net.togogo.travel.user.util;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 工具类
 *
 * 【职责】
 * 封装文件上传、获取访问 URL、删除等常用操作。
 *
 * ============================================================================
 * 【重要设计决策 —— 为什么 upload() 返回的是「相对路径」而不是完整 URL】
 * 历史问题：早期 upload() 返回 `http://<电脑IP>:9000/travel/scenic/xxx.jpg` 这样的
 * **绝对 URL**，前端把它原样存进数据库。一旦电脑换 WiFi／DHCP 重新分配 IP，
 * 数据库里所有历史图片地址立刻全部失效，必须手工批量改库才能恢复。
 *
 * 解决办法：数据库只保存 **object key 相对路径**（如 `scenic/xxx.jpg`），
 * 完整访问地址由前端在渲染时用「一个统一配置项」拼接（见 uniapp-front/api/config.js
 * 的 MINIO_BASE 与 resolveImage()）。
 * 这样换网络时**只需要改前端一个常量**，历史数据完全不受影响，彻底根治。
 *
 * 因此：
 *   - upload() 返回 `scenic/xxx.jpg`（相对路径）
 *   - extractObjectName() 同时兼容「相对路径」与「历史绝对 URL」两种入参，
 *     保证老数据的删除逻辑依旧可用
 * ============================================================================
 */
@Component
public class MinioUtil {

    @Resource
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * 上传文件到 MinIO
     *
     * @param file   前端上传的文件
     * @param folder 存储目录，如 "scenic"（景点图片存 travel/scenic/ 下）
     * @return **object key 相对路径**，如 scenic/xxx.jpg
     *         （不再返回绝对 URL，原因见类注释；前端用 resolveImage() 拼完整地址）
     * @throws Exception 上传失败时抛出
     */
    public String upload(MultipartFile file, String folder) throws Exception {
        // 1. 生成唯一文件名，防止覆盖
        String originalName = file.getOriginalFilename();
        String suffix = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".jpg";
        String fileName = folder + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;

        // 2. 上传文件到 MinIO
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        // 3. 返回相对 object key（Bucket 设为 public 后可直接访问，完整地址由前端拼接）
        return fileName;
    }

    /**
     * 获取文件的临时访问 URL（带签名，适合私有 Bucket）
     *
     * @param objectName 文件路径，如 scenic/xxx.jpg
     * @param expiry     过期时间
     * @param unit       时间单位
     * @return 带签名的临时 URL
     * @throws Exception 获取失败时抛出
     */
    public String getPresignedUrl(String objectName, int expiry, TimeUnit unit) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry((int) unit.toSeconds(expiry))
                        .build()
        );
    }

    /**
     * 删除文件
     *
     * @param objectName 文件路径，如 scenic/xxx.jpg
     * @throws Exception 删除失败时抛出
     */
    public void delete(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    /**
     * 统一取出 objectName（对象在 Bucket 内的相对路径）
     *
     * 【兼容两种入参，务必同时支持】
     * 1. 新格式（相对路径）：`scenic/xxx.jpg`          → 原样返回
     * 2. 历史数据（绝对 URL）：`http://IP:9000/travel/scenic/xxx.jpg` → 剥离前缀后返回
     * 3. 外链（如 https://xxx.com/a.jpg）：不属于本 Bucket → 返回 null（调用方跳过删除）
     *
     * @param url 相对路径 或 完整 URL
     * @return objectName（如 scenic/xxx.jpg）；无法识别时返回 null
     */
    public String extractObjectName(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        // 情况1：已经是相对路径（不含协议头），直接返回
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return url;
        }
        // 情况2：历史绝对 URL，按当前 endpoint 剥离前缀
        String prefix = endpoint + "/" + bucketName + "/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        // 情况2b：历史绝对 URL，但 endpoint 配置已变化（如换过 IP）——
        // 用 "/<bucket>/" 定位后再取其后的部分，保证老数据仍能被删除
        String bucketMark = "/" + bucketName + "/";
        int idx = url.indexOf(bucketMark);
        if (idx >= 0) {
            return url.substring(idx + bucketMark.length());
        }
        // 情况3：外部链接，不属于本 Bucket
        return null;
    }
}