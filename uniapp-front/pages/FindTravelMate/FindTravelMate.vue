<template>
  <view class="travel-mate-page">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <text class="title">旅游结伴</text>
      <button class="publish-btn" @click="showPublishModal = true">
        <text class="icon">+</text> 发布组队
      </button>
    </view>

    <!-- 标签切换 -->
    <view class="tabs">
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'hall' }"
        @click="switchTab('hall')"
      >
        大厅
      </view>
      <view 
        class="tab-item" 
        :class="{ active: activeTab === 'my' }"
        @click="switchTab('my')"
      >
        我的组队
      </view>
    </view>

    <!-- 大厅内容 -->
    <view v-if="activeTab === 'hall'" class="tab-content">
      <!-- 搜索框 -->
      <view class="search-box">
        <input 
          type="text" 
          placeholder="搜索目的地或关键词" 
          v-model="searchKeyword"
          class="search-input"
          @confirm="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">搜索</button>
      </view>

      <!-- 筛选条件 -->
      <view class="filters">
        <view class="filter-item">
          <text>目的地</text>
          <picker @change="onDestinationChange" :value="destinationIndex" :range="destinations">
            <text class="picker-text">{{ destinations[destinationIndex] }}</text>
          </picker>
        </view>
        <view class="filter-item">
          <text>日期</text>
          <picker mode="date" @change="onDateChange" :value="selectedDate">
            <text class="picker-text">{{ selectedDate || '不限' }}</text>
          </picker>
        </view>
        <view class="filter-item">
          <text>人数</text>
          <picker @change="onPeopleCountChange" :value="peopleCountIndex" :range="peopleCountOptions">
            <text class="picker-text">{{ peopleCountOptions[peopleCountIndex] }}</text>
          </picker>
        </view>
      </view>

      <!-- 组队列表 -->
      <view class="team-list">
        <view v-if="loading" class="loading-state">
          <text>加载中...</text>
        </view>
        <view v-else>
          <view class="team-card" v-for="(team, index) in teams" :key="team.id">
            <view class="team-header">
              <image 
                :src="team.creatorAvatar || '/static/default-avatar.png'" 
                class="avatar"
                mode="aspectFill"
              ></image>
              <view class="publisher-info">
                <text class="publisher-name">{{ team.creatorName || '匿名用户' }}</text>
                <text class="publish-time">{{ formatTime(team.createTime) }}</text>
              </view>
              <button class="join-btn" @click="joinTeam(index)">加入</button>
            </view>
            
            <view class="team-details">
              <text class="destination">{{ team.destination }}</text>
              <view class="info-row">
                <view class="info-item">
                  <text class="icon">📅</text>
                  <text>{{ team.startDate }} - {{ team.endDate }}</text>
                </view>
                <view class="info-item">
                  <text class="icon">👥</text>
                  <text>{{ team.currentPeople }}/{{ team.totalPeople }}人</text>
                </view>
              </view>
              <text class="description">{{ team.description }}</text>
            </view>
          </view>
          
          <view class="empty-state" v-if="teams.length === 0">
            <text>暂无符合条件的组队信息</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 我的组队内容 -->
    <view v-if="activeTab === 'my'" class="tab-content">
      <view class="my-teams-tabs">
        <view 
          class="my-tab-item" 
          :class="{ active: myTeamsTab === 'created' }"
          @click="switchMyTeamsTab('created')"
        >
          我创建的
        </view>
        <view 
          class="my-tab-item" 
          :class="{ active: myTeamsTab === 'joined' }"
          @click="switchMyTeamsTab('joined')"
        >
          我加入的
        </view>
      </view>

      <view class="team-list">
        <view v-if="myLoading" class="loading-state">
          <text>加载中...</text>
        </view>
        <view v-else>
          <view class="team-card" v-for="(team, index) in currentMyTeams" :key="team.id">
            <view class="team-header">
              <image 
                :src="team.creatorAvatar || '/static/default-avatar.png'" 
                class="avatar"
                mode="aspectFill"
              ></image>
              <view class="publisher-info">
                <text class="publisher-name">{{ team.creatorName || '匿名用户' }}</text>
                <text class="publish-time">{{ formatTime(team.createTime) }}</text>
              </view>
              <button 
                class="manage-btn" 
                v-if="myTeamsTab === 'created'" 
                @click="manageTeam(index)"
              >
                管理
              </button>
              <button 
                class="quit-btn" 
                v-if="myTeamsTab === 'joined'" 
                @click="quitTeam(index)"
              >
                退出
              </button>
            </view>
            
            <view class="team-details">
              <text class="destination">{{ team.destination }}</text>
              <view class="info-row">
                <view class="info-item">
                  <text class="icon">📅</text>
                  <text>{{ team.startDate }} - {{ team.endDate }}</text>
                </view>
                <view class="info-item">
                  <text class="icon">👥</text>
                  <text>{{ team.currentPeople }}/{{ team.totalPeople }}人</text>
                </view>
              </view>
              <text class="description">{{ team.description }}</text>
            </view>
          </view>
          
          <view class="empty-state" v-if="currentMyTeams.length === 0">
            <text v-if="myTeamsTab === 'created'">你还没有创建任何组队</text>
            <text v-if="myTeamsTab === 'joined'">你还没有加入任何组队</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 发布组队弹窗 -->
    <view class="modal-mask" v-if="showPublishModal" @click="showPublishModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">发布组队</text>
          <text class="modal-close" @click="showPublishModal = false">×</text>
        </view>
        
        <view class="form-group">
          <text class="form-label">目的地</text>
          <input 
            type="text" 
            placeholder="请输入旅游目的地" 
            v-model="newTeam.destination"
            class="form-input"
          />
        </view>
        
        <view class="form-row">
          <view class="form-group half">
            <text class="form-label">开始日期</text>
            <picker mode="date" @change="(e) => newTeam.startDate = e.detail.value">
              <view class="form-picker">
                {{ newTeam.startDate || '选择日期' }}
              </view>
            </picker>
          </view>
          <view class="form-group half">
            <text class="form-label">结束日期</text>
            <picker mode="date" @change="(e) => newTeam.endDate = e.detail.value">
              <view class="form-picker">
                {{ newTeam.endDate || '选择日期' }}
              </view>
            </picker>
          </view>
        </view>
        
        <view class="form-group">
          <text class="form-label">计划人数</text>
          <picker 
            @change="(e) => newTeam.totalPeople = parseInt(peopleCountOptionsWithoutAll[e.detail.value])" 
            :range="peopleCountOptionsWithoutAll"
          >
            <view class="form-picker">
              {{ newTeam.totalPeople ? newTeam.totalPeople + '人' : '选择人数' }}
            </view>
          </picker>
        </view>
        
        <view class="form-group">
          <text class="form-label">组队描述</text>
          <textarea 
            placeholder="请输入组队详情、旅游计划等信息" 
            v-model="newTeam.description"
            class="form-textarea"
          ></textarea>
        </view>
        
        <view class="modal-footer">
          <button class="cancel-btn" @click="showPublishModal = false">取消</button>
          <button class="confirm-btn" @click="publishTeam">发布</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import {
  createTeam,
  getTeamList,
  joinTeam as joinTeamApi,
  getMyCreatedTeams,
  getMyJoinedTeams,
  cancelTeam,
  quitTeam as quitTeamApi
} from '@/api/team.js';

// 状态管理
const activeTab = ref('hall');
const myTeamsTab = ref('created');
const showPublishModal = ref(false);

// 搜索和筛选
const searchKeyword = ref('');
const destinations = ['不限', '北京', '上海', '广州', '深圳', '成都', '杭州', '三亚', '丽江', '青岛'];
const destinationIndex = ref(0);
const selectedDate = ref('');
const peopleCountOptions = ['不限', '2人', '3-5人', '6-10人', '10人以上'];
const peopleCountOptionsWithoutAll = ['2人', '3-5人', '6-10人', '10人以上'];
const peopleCountIndex = ref(0);

// 组队数据
const teams = ref([]);
const myCreatedTeams = ref([]);
const myJoinedTeams = ref([]);

// 加载状态
const loading = ref(false);
const myLoading = ref(false);

// 分页
const currentPage = ref(1);
const pageSize = ref(10);
const totalCount = ref(0);

// 新组队数据
const newTeam = ref({
  destination: '',
  startDate: '',
  endDate: '',
  totalPeople: null,
  description: ''
});

// 当前显示的我的组队
const currentMyTeams = computed(() => {
  return myTeamsTab.value === 'created' ? myCreatedTeams.value : myJoinedTeams.value;
});

// 时间格式化
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString();
};

// 获取大厅列表
const fetchTeamList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
    };
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    if (destinationIndex.value > 0) params.destination = destinations[destinationIndex.value];
    if (selectedDate.value) params.date = selectedDate.value;
    if (peopleCountIndex.value > 0) params.peopleRange = peopleCountIndex.value;

    const res = await getTeamList(params);
    teams.value = res.records || [];
    totalCount.value = res.total || 0;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

// 获取我的组队
const fetchMyTeams = async () => {
  myLoading.value = true;
  try {
    if (myTeamsTab.value === 'created') {
      const res = await getMyCreatedTeams(1, 100);
      myCreatedTeams.value = res.records || [];
    } else {
      const res = await getMyJoinedTeams(1, 100);
      myJoinedTeams.value = res.records || [];
    }
  } catch (e) {
    console.error(e);
  } finally {
    myLoading.value = false;
  }
};

// 切换 Tab
const switchTab = (tab) => {
  activeTab.value = tab;
  if (tab === 'hall') {
    fetchTeamList();
  } else if (tab === 'my') {
    fetchMyTeams();
  }
};

// 切换我的组队子标签
const switchMyTeamsTab = (tab) => {
  myTeamsTab.value = tab;
  fetchMyTeams();
};

// 筛选条件变化监听
watch([searchKeyword, destinationIndex, selectedDate, peopleCountIndex], () => {
  currentPage.value = 1;
  fetchTeamList();
}, { deep: true });

// 搜索
const handleSearch = () => {
  fetchTeamList();
};

// 筛选方法
const onDestinationChange = (e) => {
  destinationIndex.value = e.detail.value;
};

const onDateChange = (e) => {
  selectedDate.value = e.detail.value;
};

const onPeopleCountChange = (e) => {
  peopleCountIndex.value = e.detail.value;
};

// 加入组队
const joinTeam = async (index) => {
  const team = teams.value[index];
  if (!team) return;
  
  if (team.currentPeople >= team.totalPeople) {
    uni.showToast({ title: '这个组队已经满员了', icon: 'none' });
    return;
  }
  
  // 检查是否已加入（可调用详情接口判断，简化处理直接请求）
  try {
    await joinTeamApi(team.id);
    uni.showToast({ title: '加入成功！', icon: 'success' });
    fetchTeamList();
    if (activeTab.value === 'my') fetchMyTeams();
  } catch (e) {
    // 错误已在 request 中提示
  }
};

// 退出组队
const quitTeam = async (index) => {
  const team = currentMyTeams.value[index];
  if (!team) return;
  
  uni.showModal({
    title: '提示',
    content: '确定要退出该组队吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await quitTeamApi(team.id);
          uni.showToast({ title: '已退出组队', icon: 'success' });
          fetchMyTeams();
          fetchTeamList();
        } catch (e) {}
      }
    }
  });
};

// 发布组队
const publishTeam = async () => {
  if (!newTeam.value.destination || !newTeam.value.startDate || !newTeam.value.endDate || !newTeam.value.totalPeople) {
    uni.showToast({ title: '请填写必要的信息', icon: 'none' });
    return;
  }
  
  if (newTeam.value.startDate > newTeam.value.endDate) {
    uni.showToast({ title: '开始日期不能晚于结束日期', icon: 'none' });
    return;
  }
  
  try {
    await createTeam({
      destination: newTeam.value.destination,
      startDate: newTeam.value.startDate,
      endDate: newTeam.value.endDate,
      totalPeople: newTeam.value.totalPeople,
      description: newTeam.value.description || ''
    });
    
    // 重置表单并关闭弹窗
    newTeam.value = {
      destination: '',
      startDate: '',
      endDate: '',
      totalPeople: null,
      description: ''
    };
    showPublishModal.value = false;
    uni.showToast({ title: '发布成功！', icon: 'success' });
    
    // 刷新列表
    fetchTeamList();
  } catch (e) {}
};

// 管理组队（队长）
const manageTeam = (index) => {
  const team = myCreatedTeams.value[index];
  if (!team) return;
  
  uni.showActionSheet({
    itemList: ['取消组队', '查看详情'],
    success: async (res) => {
      if (res.tapIndex === 0) {
        // 取消组队
        uni.showModal({
          title: '提示',
          content: '确定要取消该组队吗？取消后其他成员将无法继续参与。',
          success: async (modalRes) => {
            if (modalRes.confirm) {
              try {
                await cancelTeam(team.id);
                uni.showToast({ title: '组队已取消', icon: 'success' });
                fetchMyTeams();
                fetchTeamList();
              } catch (e) {}
            }
          }
        });
      } else if (res.tapIndex === 1) {
        // 查看详情（可跳转详情页，此处仅提示）
        uni.showToast({ title: '详情功能开发中', icon: 'none' });
      }
    }
  });
};

// 页面加载
onMounted(() => {
  fetchTeamList();
});
</script>

<style>
/* 样式保持不变，仅增加加载状态样式 */
.travel-mate-page {
  background-color: #f5f7fa;
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background-color: #1677ff;
  color: white;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.publish-btn {
  background-color: white;
  color: #1677ff;
  border-radius: 20px;
  padding: 6px 16px;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.publish-btn .icon {
  font-weight: bold;
  margin-right: 4px;
}

.tabs {
  display: flex;
  background-color: white;
  border-bottom: 1px solid #eee;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 16px 0;
  font-size: 16px;
  color: #666;
  position: relative;
}

.tab-item.active {
  color: #1677ff;
  font-weight: bold;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #1677ff;
}

.tab-content {
  padding: 15px;
}

.search-box {
  display: flex;
  margin-bottom: 15px;
  gap: 10px;
}

.search-input {
  flex: 1;
  padding: 10px 15px;
  background-color: white;
  border-radius: 8px;
  border: 1px solid #eee;
  font-size: 14px;
}

.search-btn {
  background-color: #1677ff;
  color: white;
  border-radius: 8px;
  padding: 0 15px;
}

.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  overflow-x: auto;
  padding-bottom: 5px;
}

.filter-item {
  flex: 1;
  min-width: 100px;
  background-color: white;
  border-radius: 8px;
  padding: 10px;
}

.filter-item text {
  font-size: 14px;
  color: #666;
}

.picker-text {
  color: #333;
  margin-left: 5px;
}

.team-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.loading-state {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

.team-card {
  background-color: white;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 15px;
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
}

.publisher-info {
  flex: 1;
}

.publisher-name {
  font-weight: bold;
  font-size: 15px;
  color: #333;
}

.publish-time {
  font-size: 12px;
  color: #999;
}

.join-btn {
  background-color: #1677ff;
  color: white;
  border-radius: 20px;
  padding: 5px 12px;
  font-size: 14px;
}

.manage-btn {
  background-color: #ff7d00;
  color: white;
  border-radius: 20px;
  padding: 5px 12px;
  font-size: 14px;
}

.quit-btn {
  background-color: #ff4d4f;
  color: white;
  border-radius: 20px;
  padding: 5px 12px;
  font-size: 14px;
}

.team-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.destination {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.info-row {
  display: flex;
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #666;
}

.info-item .icon {
  margin-right: 4px;
}

.description {
  font-size: 14px;
  color: #555;
  line-height: 1.5;
  margin-top: 5px;
}

.my-teams-tabs {
  display: flex;
  margin-bottom: 15px;
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
}

.my-tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  font-size: 14px;
}

.my-tab-item.active {
  background-color: #1677ff;
  color: white;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 15px;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: flex-end;
  z-index: 999;
}

.modal-content {
  width: 100%;
  background-color: white;
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
  padding: 20px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.modal-close {
  font-size: 24px;
  color: #999;
}

.form-group {
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-row {
  display: flex;
  gap: 15px;
}

.half {
  flex: 1;
}

.form-label {
  font-size: 14px;
  color: #666;
}

.form-input, .form-textarea, .form-picker {
  padding: 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  font-size: 15px;
}

.form-textarea {
  min-height: 100px;
  resize: none;
}

.form-picker {
  color: #333;
  background-color: #f9f9f9;
}

.modal-footer {
  display: flex;
  gap: 15px;
  margin-top: 20px;
}

.cancel-btn {
  flex: 1;
  background-color: #f5f5f5;
  color: #333;
  border-radius: 8px;
  padding: 12px 0;
}

.confirm-btn {
  flex: 1;
  background-color: #1677ff;
  color: white;
  border-radius: 8px;
  padding: 12px 0;
}
</style>