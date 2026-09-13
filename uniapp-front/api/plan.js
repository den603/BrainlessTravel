// api/plan.js
import { BASE_URL } from './config.js'; // 地址统一在 config.js 配置（真机调试改那一处即可）

const request = (url, method, data) => {
  const token = uni.getStorageSync('token');
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      timeout: 300000,          // 超时时间 5 分钟
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      },
      success: (res) => {
        console.log('请求成功，响应数据：', res.data);
        if (res.data && res.data.code === 1) {
          resolve(res.data.data);
        } else if (res.data && res.data.code !== undefined) {
          reject(res.data.msg || '请求失败');
        } else {
          // 直接是数据，没有 Result 包裹
          resolve(res.data);
        }
      },
      fail: (err) => {
        console.error('请求失败：', err);
        reject(err);
      }
    });
  });
};

export const generatePlan = (params) => request('/api/plan/generate', 'POST', params);
export const getHistoryPlans = () => request('/api/plan/history', 'GET');
export const getPlanDetail = (id) => request(`/api/plan/detail/${id}`, 'GET');
export const searchHistoryPlans = (keyword = '', page = 1, size = 10) => {
  return request(`/api/plan/history/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`, 'GET');
};

