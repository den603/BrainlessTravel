<template>
  <view class="history-page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input
        class="search-input"
        placeholder="搜索目的地..."
        v-model="keyword"
        confirm-type="search"
        @confirm="onSearch"
      />
      <button size="mini" type="primary" @click="onSearch">搜索</button>
    </view>

    <!-- 计划列表 -->
    <view v-if="planList.length > 0" class="plan-list">
      <view
        v-for="plan in planList"
        :key="plan.id"
        class="plan-card"
        @click="showDetail(plan)"
      >
        <view class="card-header">
          <text class="destination">{{ plan.destination }}</text>
          <text class="date">{{ formatDate(plan.travelDate) }}</text>
        </view>
        <view class="card-body">
          <text>天数：{{ plan.days }}天 | 人数：{{ plan.peopleCount }}人</text>
        </view>
        <view class="card-footer">
          <text class="budget">预算：¥{{ plan.budgetMin }}-{{ plan.budgetMax }}</text>
          <text class="created-at">{{ formatDateTime(plan.createdAt) }}</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-state">
      <image src="/static/empty.png" mode="widthFix" class="empty-img"></image>
      <text>暂无历史计划</text>
      <text class="sub-text">快去生成一份旅行计划吧！</text>
    </view>

    <!-- 加载更多 -->
    <view v-if="hasMore" class="load-more" @click="loadMore">
      <text>{{ loadingMore ? '加载中...' : '点击加载更多' }}</text>
    </view>

    <!-- 计划详情弹窗（以标签页形式展示） -->
    <view v-if="selectedPlan" class="detail-overlay" @click="closeDetail">
      <view class="detail-container" @click.stop>
        <view class="detail-header">
          <text class="title">{{ selectedPlan.days }}天{{ selectedPlan.destination }}之旅</text>
          <text class="close-btn" @click="closeDetail">×</text>
        </view>

        <!-- 标签页切换 -->
        <view class="tab-bar">
          <view
            v-for="(tab, index) in tabs"
            :key="index"
            class="tab-item"
            :class="{ active: currentTabIndex === index }"
            @click="switchTab(index)"
          >
            {{ tab }}
          </view>
        </view>

        <!-- 标签页内容 -->
        <view class="tab-content">
          <!-- 概要标签页 -->
          <view v-show="currentTabIndex === 0">
            <view class="plan-summary">
              <text class="summary-title">计划概要</text>
              <text class="summary-text">{{ planSummary }}</text>
            </view>
            <view class="plan-meta">
              <text>📍 {{ selectedPlan.departure || '未指定' }} → {{ selectedPlan.destination }}</text>
              <text>📅 {{ formatDate(selectedPlan.travelDate) }}</text>
              <text>⏱️ {{ selectedPlan.days }}天</text>
              <text>👥 {{ selectedPlan.peopleCount }}人 ({{ selectedPlan.peopleType }})</text>
              <text>💰 预算 ¥{{ selectedPlan.budgetMin }}-{{ selectedPlan.budgetMax }} (预估 ¥{{ selectedPlan.totalBudget }})</text>
            </view>
          </view>

          <!-- 行程标签页 -->
          <view v-show="currentTabIndex === 1">
            <view v-if="dailyPlans && dailyPlans.length > 0">
              <view v-for="(day, idx) in dailyPlans" :key="idx" class="day-card">
                <view class="day-header">
                  <text class="day-title">Day {{ day.day }}</text>
                  <text class="day-date">{{ formatDate(day.date) }}</text>
                  <text class="day-theme">{{ day.theme }}</text>
                </view>

                <!-- 景点列表 -->
                <view class="section">
                  <text class="section-title">🏛️ 景点安排</text>
                  <view v-for="(attraction, aIdx) in day.attractions" :key="aIdx" class="activity-item">
                    <view class="act-time-box">
                      <text class="act-time">{{ attraction.time }}</text>
                      <text class="act-duration">{{ attraction.duration }}</text>
                    </view>
                    <view class="act-detail">
                      <text class="act-name">{{ attraction.name }}</text>
                      <text class="act-desc">{{ attraction.description }}</text>
                      <text v-if="attraction.ticket" class="act-ticket">🎫 门票 ¥{{ attraction.ticket }}</text>
                    </view>
                  </view>
                </view>

                <!-- 餐饮推荐 -->
                <view class="section" v-if="day.meals && day.meals.length > 0">
                  <text class="section-title">🍽️ 餐饮推荐</text>
                  <view v-for="(meal, mIdx) in day.meals" :key="mIdx" class="meal-item">
                    <text class="meal-type">{{ meal.type }}</text>
                    <text class="meal-restaurant">{{ meal.restaurant }}（{{ meal.cuisine }}）</text>
                    <text class="meal-cost">人均 ¥{{ meal.avgCost }}</text>
                  </view>
                </view>

                <!-- 住宿信息 -->
                <view class="section" v-if="day.hotel">
                  <text class="section-title">🏨 住宿</text>
                  <view class="hotel-item">
                    <text>{{ day.hotel.name }}</text>
                    <text class="hotel-cost">¥{{ day.hotel.nightCost }}/晚</text>
                    <text class="hotel-note">{{ day.hotel.note }}</text>
                  </view>
                </view>

                <!-- 交通建议 -->
                <view class="section" v-if="day.transport">
                  <text class="section-title">🚇 交通建议</text>
                  <text class="transport-text">{{ day.transport }}</text>
                </view>

                <!-- 小贴士 -->
                <view class="section" v-if="day.tips">
                  <text class="section-title">💡 小贴士</text>
                  <text class="tips-text">{{ day.tips }}</text>
                </view>
              </view>
            </view>
            <view v-else class="no-data">暂无详细行程</view>
          </view>

          <!-- 预算标签页 -->
          <view v-show="currentTabIndex === 2">
            <view class="budget-detail">
              <view class="budget-total-box">
                <text class="budget-label">预估总花费</text>
                <text class="budget-amount">¥{{ selectedPlan.totalBudget || '未估算' }}</text>
              </view>
              <view class="budget-range-box">
                <text>预算范围：¥{{ selectedPlan.budgetMin }} - ¥{{ selectedPlan.budgetMax }}</text>
              </view>
              <!-- 如果有详细的费用拆解，可在此展示 -->
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue';
import { searchHistoryPlans } from '@/api/plan.js';

const keyword = ref('');
const planList = ref([]);
const page = ref(1);
const size = 10;
const total = ref(0);
const loadingMore = ref(false);
const hasMore = ref(false);
const selectedPlan = ref(null);
const currentTabIndex = ref(0);
const tabs = ['概要', '行程', '预算'];
const dailyPlans = ref([]);
const planSummary = ref('');

// 加载历史计划
const loadPlans = async (isLoadMore = false) => {
  if (!isLoadMore) {
    page.value = 1;
    planList.value = [];
  }
  try {
    const res = await searchHistoryPlans(keyword.value, page.value, size);
    if (res && res.records) {
      planList.value = isLoadMore ? [...planList.value, ...res.records] : res.records;
      total.value = res.total;
      hasMore.value = page.value * size < res.total;
    }
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' });
  }
};

// 搜索
const onSearch = () => {
  loadPlans();
};

// 加载更多
const loadMore = () => {
  if (!hasMore.value || loadingMore.value) return;
  loadingMore.value = true;
  page.value++;
  loadPlans(true).finally(() => {
    loadingMore.value = false;
  });
};

// 格式化日期 (处理后端可能返回的 '2026-05-03' 或数组 [2026,5,3])
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  if (Array.isArray(dateStr)) {
    const [year, month, day] = dateStr;
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  }
  return String(dateStr).substring(0, 10);
};

// 格式化时间戳 (后端返回的 createdAt 可能是 ISO 字符串)
const formatDateTime = (dateStr) => {
  if (!dateStr) return '';
  return String(dateStr).replace('T', ' ').substring(0, 19);
};

// 显示详情
const showDetail = (plan) => {
  selectedPlan.value = plan;
  currentTabIndex.value = 0;
  dailyPlans.value = [];
  planSummary.value = '无概要';

  if (plan.planContent) {
    let content = plan.planContent;
    if (typeof content === 'string') {
      // 尝试直接解析
      try {
        content = JSON.parse(content);
      } catch (e) {
        console.warn('直接解析失败，尝试清理 JSON 格式');
        // 清理键名和值外围的多余空格
        // 1. 清理键：将 " key " 变为 "key"
        const cleaned = content
          .replace(/"\s*([^"]+?)\s*"\s*:/g, '"$1":')
          // 2. 清理值的前后空格：将 ": " value " 变为 ": "value" (但保留值内部空格)
          .replace(/:\s*"\s+/g, ':"')
          .replace(/\s+"\s*([,}])/g, '"$1');
        try {
          content = JSON.parse(cleaned);
        } catch (e2) {
          console.error('清理后仍解析失败', e2);
          return;
        }
      }
    }
    dailyPlans.value = content.dailyPlans || [];
    planSummary.value = content.summary || '无概要';
  }
};
// 关闭详情
const closeDetail = () => {
  selectedPlan.value = null;
};

// 切换详情标签页
const switchTab = (index) => {
  currentTabIndex.value = index;
};

// 初始化加载
loadPlans();
</script>

<style scoped>
.history-page {
  background-color: #f6f8fe;
  min-height: 100vh;
  padding: 20rpx;
}
.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  background-color: #fff;
  border-radius: 10rpx;
  padding: 10rpx 20rpx;
}
.search-input {
  flex: 1;
  height: 60rpx;
  line-height: 60rpx;
  font-size: 28rpx;
}
.plan-list {
  margin-bottom: 30rpx;
}
.plan-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}
.card-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10rpx;
}
.destination {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a2e;
}
.date {
  color: #666;
  font-size: 24rpx;
}
.card-body, .card-footer {
  font-size: 26rpx;
  color: #555;
  margin-top: 8rpx;
}
.budget {
  color: #3875f6;
}
.created-at {
  color: #999;
  font-size: 24rpx;
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
  color: #999;
}
.empty-img {
  width: 200rpx;
  margin-bottom: 20rpx;
}
.sub-text {
  font-size: 24rpx;
  margin-top: 10rpx;
}
.load-more {
  text-align: center;
  padding: 20rpx;
  color: #3875f6;
  font-size: 28rpx;
}
/* 弹窗样式 */
.detail-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}
.detail-container {
  width: 90%;
  max-height: 80vh;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  overflow-y: auto;
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}
.title {
  font-size: 36rpx;
  font-weight: bold;
}
.close-btn {
  font-size: 40rpx;
  padding: 0 10rpx;
}
.tab-bar {
  display: flex;
  border-bottom: 1px solid #eee;
  margin-bottom: 20rpx;
}
.tab-item {
  flex: 1;
  text-align: center;
  padding: 15rpx 0;
  font-size: 28rpx;
}
.tab-item.active {
  color: #3875f6;
  border-bottom: 2px solid #3875f6;
}
.tab-content {
  font-size: 28rpx;
  line-height: 1.8;
}
.plan-summary {
  margin-bottom: 20rpx;
}
.summary-title {
  font-weight: bold;
  display: block;
  margin-bottom: 10rpx;
}
.plan-meta text {
  display: block;
  margin-bottom: 8rpx;
}

/* 每日计划卡片 */
.day-card {
  background-color: #f8f9ff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}
.day-header {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  margin-bottom: 25rpx;
}
.day-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1a1a2e;
  margin-right: 15rpx;
}
.day-date {
  font-size: 26rpx;
  color: #666;
  margin-right: 15rpx;
}
.day-theme {
  font-size: 24rpx;
  color: #3875f6;
  background-color: #eef2ff;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  margin-top: 5rpx;
}

/* 板块样式 */
.section {
  margin-bottom: 20rpx;
}
.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 12rpx;
  display: block;
}

/* 景点项 */
.activity-item {
  display: flex;
  margin-bottom: 18rpx;
  background-color: #fff;
  border-radius: 12rpx;
  padding: 15rpx;
}
.act-time-box {
  width: 140rpx;
  margin-right: 15rpx;
  flex-shrink: 0;
}
.act-time {
  font-weight: bold;
  font-size: 26rpx;
  color: #3875f6;
  display: block;
}
.act-duration {
  font-size: 22rpx;
  color: #999;
  display: block;
  margin-top: 4rpx;
}
.act-detail {
  flex: 1;
}
.act-name {
  font-weight: bold;
  font-size: 28rpx;
  display: block;
  margin-bottom: 6rpx;
}
.act-desc {
  color: #666;
  font-size: 24rpx;
  display: block;
  margin-bottom: 6rpx;
}
.act-ticket {
  color: #ff9500;
  font-size: 24rpx;
  display: block;
}

/* 餐饮项 */
.meal-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12rpx 15rpx;
  background-color: #fff;
  border-radius: 8rpx;
  margin-bottom: 10rpx;
}
.meal-type {
  font-weight: bold;
  color: #333;
  font-size: 26rpx;
}
.meal-restaurant {
  color: #555;
  font-size: 26rpx;
}
.meal-cost {
  color: #e0301e;
  font-size: 26rpx;
}

/* 住宿 */
.hotel-item {
  padding: 12rpx 15rpx;
  background-color: #fff;
  border-radius: 8rpx;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  font-size: 26rpx;
  color: #555;
}
.hotel-cost {
  color: #e0301e;
  font-weight: bold;
}
.hotel-note {
  width: 100%;
  color: #999;
  font-size: 22rpx;
  margin-top: 6rpx;
}

/* 交通 & 小贴士 */
.transport-text, .tips-text {
  display: block;
  padding: 12rpx 15rpx;
  background-color: #fff;
  border-radius: 8rpx;
  font-size: 26rpx;
  color: #555;
  line-height: 1.6;
}

/* 预算标签页 */
.budget-total-box {
  text-align: center;
  padding: 30rpx 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 20rpx;
}
.budget-label {
  font-size: 28rpx;
  color: #666;
  display: block;
}
.budget-amount {
  font-size: 48rpx;
  color: #3875f6;
  font-weight: bold;
  margin-top: 10rpx;
}
.budget-range-box {
  padding: 15rpx;
  background-color: #f8f9ff;
  border-radius: 10rpx;
  color: #555;
  font-size: 28rpx;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 40rpx;
}
</style>