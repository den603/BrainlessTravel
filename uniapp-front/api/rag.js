// api/rag.js
// RAG 知识库接口封装
// 说明：后端已微服务化，但前端请求路径完全不变 —— 所有请求统一打到网关
// http://localhost:8080，由网关按 /api/** 前缀转发到对应微服务。

import { BASE_URL } from './config.js'; // 地址统一在 config.js 配置

/**
 * 通用请求封装
 * 后端统一返回体：{ code: 1成功/0失败, msg, data }
 */
const request = (url, method, data, header = {}) => {
  const token = uni.getStorageSync('token');
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      // 知识库同步/重建索引会跑本地 BGE 向量化，耗时较长，给 2 分钟超时
      timeout: 120000,
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json',
        ...header
      },
      success: (res) => {
        if (res.data && res.data.code === 1) {
          resolve(res.data.data);
        } else {
          reject((res.data && res.data.msg) || '请求失败');
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
 * 带知识库增强的 AI 聊天
 * @param {Array}  messages 对话历史 [{role:'user',content:'...'}, ...]
 * @param {Boolean} useRag 是否启用知识库增强，默认开启
 * @returns {Promise<{answer:String, ragEnabled:Boolean, sources:Array}>}
 */
export const chatAskRag = (messages, useRag = true) =>
  request('/api/chat/ask/rag', 'POST', { messages, useRag });

/**
 * 上传知识库文档（PDF / DOCX / TXT / XLSX，单个不超过 10MB）
 * 使用 uni.uploadFile 走 multipart/form-data
 * @param {String} filePath 本地文件临时路径
 * @param {String} title    文档标题
 */
export const uploadRagDocument = (filePath, title) => {
  const token = uni.getStorageSync('token');
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: BASE_URL + '/api/rag/document/upload',
      filePath,
      name: 'file', // 必须与后端 @RequestParam("file") 一致
      formData: { title },
      timeout: 120000,
      header: {
        Authorization: token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        try {
          const body = JSON.parse(res.data);
          if (body.code === 1) {
            resolve(body.data);
          } else {
            reject(body.msg || '上传失败');
          }
        } catch (e) {
          reject('上传响应解析失败');
        }
      },
      fail: (err) => {
        console.error('上传失败：', err);
        reject(err);
      }
    });
  });
};

/**
 * 删除知识库文档（只能删除自己上传的；系统/景点文档不可删）
 * @param {Number} id 文档ID
 */
export const deleteRagDocument = (id) => request(`/api/rag/document/${id}`, 'DELETE');

/**
 * 查询知识库文档列表
 * 返回：当前用户上传的文档 + 系统预置/景点同步的共享文档
 */
export const getRagDocumentList = () => request('/api/rag/document/list', 'GET');

/**
 * 景点一键同步知识库
 * @returns {Promise<Number>} 成功同步的景点数量
 */
export const syncScenicToRag = () => request('/api/rag/sync/scenic', 'POST');

/**
 * 重建全部索引
 * 【使用场景】memory 内存向量模式重启后向量会丢失，需调用本接口重建
 * @returns {Promise<Number>} 重建的文档数量
 */
export const reindexRag = () => request('/api/rag/reindex', 'POST');

/**
 * 纯检索测试（不调用大模型），用于验证知识库是否命中
 * @param {String} query 查询语句
 * @returns {Promise<Array>} 相关片段列表
 */
export const retrieveRag = (query) =>
  request(`/api/rag/retrieve?query=${encodeURIComponent(query)}`, 'GET');
