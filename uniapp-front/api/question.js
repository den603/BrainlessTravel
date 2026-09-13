import { API_BASE as BASE_URL } from './config.js'; // 地址统一在 config.js 配置 

export const request = (options) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': uni.getStorageSync('token') ? 'Bearer ' + uni.getStorageSync('token') : ''
      },
      success: (res) => {
        resolve(res.data)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}