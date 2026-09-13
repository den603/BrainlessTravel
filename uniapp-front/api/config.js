// ============================================================================
// 全局配置：整个小程序只有这一个文件需要按环境修改
//
// 【换网络 / 换电脑时怎么改】
//   只改下面 BASE_URL 这一行即可，其余（API 地址、MinIO 图片地址）全部自动跟随。
//
//   - 只在微信开发者工具里调试（后端跑在同一台电脑）：
//         export const BASE_URL = 'http://localhost:8080'
//   - 要用真机预览（手机和电脑连同一个 WiFi）：
//         export const BASE_URL = 'http://192.168.x.x:8080'   ← 换成电脑的局域网 IP
//     查 IP：命令行执行 ipconfig，看「无线局域网适配器 WLAN」的 IPv4 地址
//     注意：真机预览还需在开发者工具里勾选「不校验合法域名…」并开启调试模式
//
// 【为什么图片地址不用单独改】
//   MinIO 与网关部署在同一台电脑上，因此 MINIO_BASE 由 BASE_URL 自动推导出
//   （协议 + 主机名不变，端口换成 9000）。
//   若哪天 MinIO 部署到别的机器，把下面的 MINIO_BASE 直接改成完整地址即可。
//
// 【为什么数据库只存相对路径】
//   数据库里 scenic.img 存的是 `scenic/xxx.jpg` 这样的相对路径，不是完整 URL。
//   完整地址在渲染时由 resolveImage() 拼接，所以电脑换 IP 后**历史数据无需任何修改**。
// ============================================================================

/** 后端网关地址（不含 /api） */
export const BASE_URL = 'http://localhost:8080';

/** 网关地址 + /api，供「以相对路径拼接」的旧接口模块使用 */
export const API_BASE = `${BASE_URL}/api`;

/**
 * MinIO 对象存储访问前缀（含 bucket 名 travel）
 * 默认由 BASE_URL 推导：把端口换成 9000。
 * 如需指向别的机器，把下面这行改成完整地址即可，例如：
 *   export const MINIO_BASE = 'http://192.168.1.50:9000/travel';
 */
export const MINIO_BASE = `${BASE_URL.replace(/:\d+$/, '')}:9000/travel`;

/**
 * 把数据库里的图片路径解析成可用于 <image src> 的完整地址
 *
 * 兼容三种情况：
 *   1. 相对路径 `scenic/xxx.jpg`        → 拼成 `${MINIO_BASE}/scenic/xxx.jpg`
 *   2. 历史绝对 URL `http://.../a.jpg`  → 原样返回（老数据/其他存储）
 *   3. 第三方外链 `https://.../a.jpg`   → 原样返回（如 banner 表存的外链图）
 *   4. 空值                              → 返回空字符串，避免 <image> 报警告
 *
 * @param {String} path 数据库中的 img / url 字段值
 * @returns {String} 可直接用于 :src 的完整地址
 */
export const resolveImage = (path) => {
  if (!path) return '';
  const value = String(path).trim();
  if (!value) return '';
  // 已经是完整地址（历史数据 / 外链）→ 原样使用，不做任何改动
  if (/^https?:\/\//i.test(value)) return value;
  // 相对路径 → 去掉可能的前导斜杠后拼接
  return `${MINIO_BASE}/${value.replace(/^\/+/, '')}`;
};
