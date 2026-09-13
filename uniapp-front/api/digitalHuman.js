// api/digitalHuman.js
// 数字人模块接口封装
// 与 api/plan.js 风格保持一致

import { BASE_URL } from './config.js'; // 地址统一在 config.js 配置（真机调试改那一处即可）

const request = (url, method, data) => {
  const token = uni.getStorageSync('token');
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      timeout: 300000,
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      },
      success: (res) => {
        console.log('数字人接口响应：', res.data);
        if (res.data && res.data.code === 1) {
          resolve(res.data.data);
        } else if (res.data && res.data.code !== undefined) {
          reject(res.data.msg || '请求失败');
        } else {
          resolve(res.data);
        }
      },
      fail: (err) => {
        console.error('数字人接口请求失败：', err);
        reject(err);
      }
    });
  });
};

// 创建会话
export const createSession = (userId) => request('/api/digital-human/session?userId=' + userId, 'POST');

// 语音转文字
export const asrTranscribe = (filePath) => {
  const token = uni.getStorageSync('token');
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + '/api/digital-human/asr',
      filePath: filePath,
      name: 'file',
      header: {
        Authorization: token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data);
          if (data.code === 1) {
            resolve(data.data);
          } else {
            reject(data.msg || '识别失败');
          }
        } catch (e) {
          reject('解析失败');
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
};

// 发送对话
export const sendChat = (params) => request('/api/digital-human/chat', 'POST', params);

// 获取历史消息
export const getChatHistory = (sessionId) => request(`/api/digital-human/history/${sessionId}`, 'GET');