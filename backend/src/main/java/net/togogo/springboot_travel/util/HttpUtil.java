package net.togogo.springboot_travel.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * HTTP请求工具类：调用微信第三方接口
 */
@Component
public class HttpUtil {

    private final OkHttpClient okHttpClient = new OkHttpClient();

    /**
     * 发送GET请求
     */
    public JSONObject doGet(String url) throws IOException {
        // 构建请求
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        // 执行请求
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonStr = response.body().string();
                // 转换为JSONObject（hutool工具类）
                return JSONUtil.parseObj(jsonStr);
            }
            return null;
        }
    }
}