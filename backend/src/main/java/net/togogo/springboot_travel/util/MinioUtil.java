package net.togogo.springboot_travel.util;

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
 * 景点图片上传后，返回可直接访问的 URL 存入数据库。
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
     * @param file      前端上传的文件
     * @param folder    存储目录，如 "scenic"（景点图片存 travel/scenic/ 下）
     * @return 文件的访问 URL，如 http://127.0.0.1:9000/travel/scenic/xxx.jpg
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

        // 3. 返回可直接访问的 URL（Bucket 设为 public 后可直接访问）
        return endpoint + "/" + bucketName + "/" + fileName;
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
     * 从完整 URL 中提取 objectName
     *
     * @param url 完整 URL，如 http://127.0.0.1:9000/travel/scenic/xxx.jpg
     * @return objectName，如 scenic/xxx.jpg
     */
    public String extractObjectName(String url) {
        String prefix = endpoint + "/" + bucketName + "/";
        if (url != null && url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return null;
    }
}