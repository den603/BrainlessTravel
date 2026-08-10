<template>
  <div style="padding: 20px;">
    <h2>Vue + SpringBoot 连接测试</h2>

    <!-- GET 请求按钮 -->
    <button @click="getData" style="margin-right:10px;">GET 请求后端数据</button>

    <!-- POST 请求按钮 -->
    <button @click="postData">POST 提交数据</button>

    <!-- 显示后端返回数据 -->
    <div style="margin-top:20px; padding:10px; border:1px solid #ccc;">
      <h4>后端返回结果：</h4>
      <p>{{ result }}</p>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios';
import { ref } from 'vue';

// 存储后端返回的数据
const result = ref("");

// 后端基础地址
const baseUrl = "http://localhost:8080/api";

// GET 请求
const getData = async () => {
  try {
    const res = await axios.get(`${baseUrl}/getData`);
    result.value = JSON.stringify(res.data, null, 2);
  } catch (err) {
    result.value = "请求失败：" + err.message;
  }
};

// POST 请求
const postData = async () => {
  try {
    const params = { username: "Vue前端用户" };
    const res = await axios.post(`${baseUrl}/postData`, params);
    result.value = JSON.stringify(res.data, null, 2);
  } catch (err) {
    result.value = "请求失败：" + err.message;
  }
};
</script>
