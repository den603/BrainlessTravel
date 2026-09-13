package net.togogo.travel.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI 微服务启动类（端口 8082）
 *
 * 【关键注意事项】
 * 1. @ComponentScan 必须显式包含 net.togogo.travel.common，
 *    否则 travel-common 中的 JwtUtil Bean 不会被注册；
 * 2. @EnableAsync 用于文档解析与向量化的异步执行（不阻塞上传接口）；
 * 3. 【C盘防护关键点】在 Spring 容器启动之前，先把 JVM 临时目录与
 *    LangChain4j 模型目录指向 D 盘。LangChain4j 0.36.0 的 ONNX 运行时
 *    (onnxruntime) 在部分平台上会通过临时目录落盘，提前锁定 java.io.tmpdir
 *    可确保任何临时文件都写到 D 盘，避免 C 盘剩余空间被吃光。
 *    （已核实：BGE 模型本身是从 jar 内以 getResourceAsStream 流式读取的，
 *     不会解压到系统临时目录；此处属于双保险。）
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@MapperScan("net.togogo.travel.ai.mapper")
@ComponentScan(basePackages = {"net.togogo.travel.ai", "net.togogo.travel.common"})
public class AiServiceApplication {

    public static void main(String[] args) {
        // ==================== 【C盘防护关键点】必须放在 SpringApplication.run 之前 ====================
        // JVM 临时文件目录 → D 盘（File 类的临时目录静态字段是首次使用时初始化的，此处设置最有效）
        System.setProperty("java.io.tmpdir", "D:\\dev-resources\\temp");
        // LangChain4j 嵌入模型目录 → D 盘（0.36.0 无此属性，保留以兼容后续版本/其他组件）
        System.setProperty("langchain4j.embeddings.cache.dir", "D:\\dev-resources\\rag-models");
        // ==========================================================================================

        SpringApplication.run(AiServiceApplication.class, args);
    }
}
