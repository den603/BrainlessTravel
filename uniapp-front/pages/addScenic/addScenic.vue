<template>
  <view class="page-wrapper">
    <view class="form-card">
      <view class="form-title">添加新景点</view>
      
      <!-- 景点名称 -->
      <view class="form-item">
        <text class="label">景点名称</text>
        <input v-model="form.title" placeholder="如：天坛公园" class="input" />
      </view>
      
      <!-- 景点图片（上传到 MinIO） -->
      <view class="form-item">
        <text class="label">景点图片</text>
        <view class="upload-box" @click="chooseImage">
          <image v-if="form.img" :src="resolveImage(form.img)" mode="aspectFill" class="preview-img"></image>
          <view v-else class="upload-placeholder">
            <up-icon name="plus" size="40" color="#ccc"></up-icon>
            <text>点击上传图片</text>
          </view>
        </view>
      </view>
      
      <!-- 开放时间 -->
      <view class="form-item">
        <text class="label">开放时间</text>
        <input v-model="form.times" placeholder="如：09:00-18:00" class="input" />
      </view>
      
      <!-- 景点介绍 -->
      <view class="form-item">
        <text class="label">景点介绍</text>
        <textarea v-model="form.introduce" placeholder="请输入景点详细介绍..." class="textarea" maxlength="500"></textarea>
      </view>
      
      <!-- 推荐标记 -->
      <view class="form-item">
        <text class="label">推荐标记</text>
        <input v-model="form.is_dot" placeholder="如：热门推荐（留空则不显示）" class="input" />
      </view>
      
      <!-- 是否显示红点 -->
      <view class="form-item row">
        <text class="label">显示红点</text>
        <switch :checked="form.dot" @change="e => form.dot = e.detail.value" color="#667eea" />
      </view>
      
      <!-- 标签 -->
      <view class="form-item">
        <text class="label">标签</text>
        <input v-model="tagStr" placeholder="用逗号分隔，如：著名,名胜古迹" class="input" />
      </view>
      
      <!-- 位置/经纬度（简化输入） -->
      <view class="form-item">
        <text class="label">位置描述</text>
        <input v-model="locationStr" placeholder="如：北京市东城区（经纬度后台可补）" class="input" />
      </view>
      
      <!-- 提交按钮 -->
      <button type="primary" class="submit-btn" :loading="submitting" @click="submit">
        {{ submitting ? '保存中...' : '保存景点' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { onLoad } from '@dcloudio/uni-app';

// 地址与图片解析统一从 config.js 读取，换网络只改那一个文件
import { BASE_URL, resolveImage } from '@/api/config.js';

const form = reactive({
  title: '',
  img: '',
  times: '',
  introduce: '',
  is_dot: '',
  dot: false,
  tag: [],
  address: [],
  is_play: false,
  is_delete: 0
});

const tagStr = ref('');
const locationStr = ref('');
const submitting = ref(false);

/**
 * 选择图片并上传到 MinIO
 */
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0];
      uploadToMinio(tempFilePath);
    }
  });
};

/**
 * 上传到 MinIO（调用 SpringBoot 后端）
 */
const uploadToMinio = (filePath) => {
  uni.showLoading({ title: '上传中...' });
  const token = uni.getStorageSync('token');
  
  uni.uploadFile({
    url: `${BASE_URL}/api/upload/scenic`,
    filePath: filePath,
    name: 'file', // 必须与后端 @RequestParam("file") 一致
    header: {
      'Authorization': token ? `Bearer ${token}` : ''
    },
    success: (res) => {
      uni.hideLoading();
      try {
        const data = JSON.parse(res.data);
        if (data.code === 1) {
          form.img = data.data; // 返回的 MinIO URL
          uni.showToast({ title: '上传成功', icon: 'success' });
        } else {
          uni.showToast({ title: data.msg || '上传失败', icon: 'none' });
        }
      } catch (e) {
        console.error('解析上传响应失败：', res.data);
        uni.showToast({ title: '上传响应异常', icon: 'none' });
      }
    },
    fail: (err) => {
      uni.hideLoading();
      console.error('上传失败：', err);
      uni.showToast({ title: '网络错误，上传失败', icon: 'none' });
    }
  });
};

/**
 * 提交表单
 */
const submit = async () => {
  if (!form.title.trim()) {
    uni.showToast({ title: '请输入景点名称', icon: 'none' });
    return;
  }
  if (!form.img) {
    uni.showToast({ title: '请上传景点图片', icon: 'none' });
    return;
  }
  
  // 处理 tag 和 address 数组
  form.tag = tagStr.value ? tagStr.value.split(/,|，/).map(s => s.trim()).filter(Boolean) : [];
  form.address = locationStr.value ? [locationStr.value.trim()] : [];
  
  submitting.value = true;
  const token = uni.getStorageSync('token');
  
  uni.request({
    url: `${BASE_URL}/api/scenic/add`,
    method: 'POST',
    header: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    data: form,
    success: (res) => {
      if (res.data && res.data.code === 1) {
        uni.showToast({ title: '添加成功', icon: 'success' });
        // 延迟返回并刷新首页
        setTimeout(() => {
          uni.navigateBack();
          // 通知首页刷新（通过全局事件或页面栈）
          const pages = getCurrentPages();
          const indexPage = pages[pages.length - 2];
          if (indexPage && indexPage.$vm && indexPage.$vm.refreshList) {
            indexPage.$vm.refreshList();
          }
        }, 1000);
      } else {
        uni.showToast({ title: res.data?.msg || '添加失败', icon: 'none' });
      }
    },
    fail: (err) => {
      console.error('添加失败：', err);
      uni.showToast({ title: '网络错误', icon: 'none' });
    },
    complete: () => {
      submitting.value = false;
    }
  });
};
</script>

<style>
page {
  background-color: #f6f8fe;
}
.page-wrapper {
  padding: 20rpx;
}
.form-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
}
.form-title {
  font-size: 36rpx;
  font-weight: bold;
  text-align: center;
  margin-bottom: 30rpx;
  color: #1a1a2e;
}
.form-item {
  margin-bottom: 30rpx;
}
.form-item.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 12rpx;
  font-weight: 500;
}
.input {
  height: 80rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
}
.textarea {
  height: 200rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  width: 100%;
  box-sizing: border-box;
}
.upload-box {
  width: 100%;
  height: 300rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.preview-img {
  width: 100%;
  height: 100%;
}
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #999;
  font-size: 28rpx;
}
.submit-btn {
  margin-top: 20rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 12rpx;
  font-size: 32rpx;
}
</style>