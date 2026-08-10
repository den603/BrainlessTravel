// api/chat.js
const BASE_URL = 'http://localhost:8080'; // 与 plan.js 保持一致

const request = (url, method, data) => {
  const token = uni.getStorageSync('token');
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      timeout: 120000, // 聊天接口 2 分钟超时（大模型响应较慢）
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data && res.data.code === 1) {
          resolve(res.data.data);
        } else {
          reject(res.data.msg || '请求失败');
        }
      },
      fail: (err) => {
        console.error('请求失败：', err);
        reject(err);
      }
    });
  });
};

/**
 * AI 自由聊天
 * @param {Array} messages 对话历史 [{role:'user',content:'...'}, ...]
 */
export const chatAsk = (messages) => request('/api/chat/ask', 'POST', { messages });