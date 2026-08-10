// import http from './http.js'

// export const getBanner = () => {
// 	return http('/user/getBanner')
// }

// //首页列表
// export const getHomeList = () => {
// 	return http('/user/getHomeList')
// }

// //登陆
// export const login =(code)=>{
// 	return http('/login',{code},'POST')
// }

// // 获取用户信息
// export const getUserInfo=()=>{
// 	return http('/getUserInfo')
// }

//游玩项目获取
// export const detailProject=()=>{
// 	return http('/detail/project')
// }

//项目详情
export const projectInfo=(data) =>{
	return http('/project/info',data)
}

//我的喜欢
// export const likeList=()=>{
// 	return http('/like/list')
// }

// 这是你uni-app的 api.js 完整代码
// 后端基础地址：SpringBoot 运行地址 + 端口
// 后端基础地址：SpringBoot 运行地址 + 端口
const baseUrl = "http://localhost:8080/api";

/**
 * 通用请求封装
 */
const request = (url, data = {}, method = 'POST') => {
	return new Promise((resolve, reject) => {
		// 统一请求头
		const header = {
			'Content-Type': 'application/json'
		};
		// 从缓存拿token，拼接正确的Bearer格式
		const token = uni.getStorageSync('token');
		if (token) {
			header.Authorization = 'Bearer ' + token;
		}

		uni.request({
			url: baseUrl + url,
			method,
			data,
			header,
			success: (res) => {
				console.log("接口响应：", res);
				if (res.statusCode === 200) {
					resolve(res.data);
				} else {
					uni.showToast({ title: '请求失败：' + res.statusCode, icon: 'none' });
					reject(res.data);
				}
			},
			fail: (err) => {
				uni.showToast({ title: '网络异常/后端未启动', icon: 'none' });
				reject(err);
			}
		});
	});
};

// 【修复】微信登录接口 → 正确地址：/login/wechat
export const login = (code) => {
	return request('/login/wechat', { code }, 'POST');
};

// 【修复】获取用户信息接口 → 正确地址：/login/userInfo
export const getUserInfo = () => {
	return request('/login/userInfo', {}, 'GET');
};

// 【新增】更新用户信息接口（完善功能）
export const updateUserInfo = (userInfo) => {
	return request('/login/updateUserInfo', userInfo, 'POST');
};

// 获取轮播图
export function getBanner() {
	return new Promise((resolve, reject) => {
		uni.request({
			// 修正：匹配后端BannerController的 /api/banner/list
			url: baseUrl + "/banner/list", 
			method: "GET",
			success: (res) => {
				resolve(res.data);
			},
			fail: (err) => {
				reject(err);
			}
		});
	});
}

// 获取首页瀑布流数据
export function getHomeList() {
	return new Promise((resolve, reject) => {
		uni.request({
			// 修正：匹配后端ScenicController的 /api/scenic/list
			url: baseUrl + "/scenic/list", 
			method: "GET",
			success: (res) => {
				resolve(res.data);
			},
			fail: (err) => {
				reject(err);
			}
		});
	});
}
/**
 * 获取景区游玩项目列表
 * @param {number} scenicId - 景区ID
 * @returns {Promise} 游玩项目列表请求Promise对象
 */
export const detailProject = (scenicId) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: baseUrl + "/scenic/play/list?scenic_id",
      method: "GET",
      data: { scenic_id: scenicId }, // 对应后端接口的scenic_id参数
      success: (res) => resolve(res.data),
      fail: (err) => reject(err)
    });
  });
};
