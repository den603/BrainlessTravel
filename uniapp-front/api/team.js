// api/team.js
const BASE_URL = 'http://localhost:8080/api'; 

const request = (url, method, data, needToken = true) => {
  return new Promise((resolve, reject) => {
    const header = { 'Content-Type': 'application/json' };
    if (needToken) {
      const token = uni.getStorageSync('token');
      if (token) header['Authorization'] = `Bearer ${token}`;
    }
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header,
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 1) {
          resolve(res.data.data);
        } else {
          uni.showToast({ title: res.data.msg || '请求失败', icon: 'none' });
          reject(res.data);
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误', icon: 'none' });
        reject(err);
      }
    });
  });
};

export const createTeam = (data) => request('/team/create', 'POST', data);
export const getTeamList = (params) => {
  const query = Object.keys(params).map(k => `${k}=${encodeURIComponent(params[k] || '')}`).join('&');
  return request(`/team/list?${query}`, 'GET', null, false);
};
export const getTeamDetail = (teamId) => request(`/team/detail/${teamId}`, 'GET', false);
export const joinTeam = (teamId) => request('/team/join', 'POST', { teamId });
export const getMyCreatedTeams = (pageNum = 1, pageSize = 10) =>
  request(`/team/my/created?pageNum=${pageNum}&pageSize=${pageSize}`, 'GET');
export const getMyJoinedTeams = (pageNum = 1, pageSize = 10) =>
  request(`/team/my/joined?pageNum=${pageNum}&pageSize=${pageSize}`, 'GET');
export const cancelTeam = (teamId) => request(`/team/cancel/${teamId}`, 'POST');
export const quitTeam = (teamId) => request(`/team/quit/${teamId}`, 'POST');