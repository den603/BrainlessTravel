<template>
  <view class="page-wrapper">
    <!-- ==================== 自定义导航栏 ==================== -->
    <view class="nav_bar_custom">
      <view class="top_height"></view>
      <view class="nav_content">
        <image src="/static/tabbar/安全大脑_sel.png" mode="widthFix"></image>
        <view class="nave_text">
          <text>AI助手</text>
          <text>您的智能助手，帮助你规划行程</text>
        </view>
      </view>
    </view>
    <!-- ==================== 双模式切换标签 ==================== -->
    <view class="tab-container" :style="{ top: navHeightNum + 'px' }">
      <view class="tab-item" :class="{ active: currentTab === 0 }" @click="switchTab(0)">
        自由聊天
      </view>
      <view class="tab-item" :class="{ active: currentTab === 1 }" @click="switchTab(1)">
        表单模式
      </view>
    </view>
    <!-- ==================== 自由聊天模式 ==================== -->
    <view v-show="currentTab === 0">
      <!-- 【新增】知识库增强（RAG）开关，默认开启 -->
      <view class="rag-switch-bar" :style="{ top: (navHeightNum + tabHeight) + 'px' }">
        <view class="rag-switch-info">
          <text class="rag-switch-title">知识库增强</text>
          <text class="rag-switch-tip">
            {{ useRag ? '已开启：优先依据景点与知识库文档作答' : '已关闭：使用通用大模型作答' }}
          </text>
        </view>
        <switch :checked="useRag" color="#2897CE" :disabled="sendInState" @change="onRagSwitchChange" />
      </view>
      <!-- 顶部占位：导航栏 + 标签栏 + 知识库开关栏 -->
      <view :style="{ height: (navHeightNum + tabHeight + ragBarHeight) + 'px' }"></view>
      <!-- 欢迎语 -->
      <view class="Sent_information backdrop your-elemt" v-if="messageData.length <= 0">
        {{ greetSb }}
      </view>
      <view v-if="messageData.length <= 0" class="Sent_information backdrop widthAuto your-elemt">
        <view class="nav_content problem_top">
          <image src="/static/tabbar/问.png" mode="widthFix"></image>
          <text>你可以这样问我</text>
        </view>
        <view
          class="Default_problem"
          v-for="(item, index) in problemData"
          :key="index"
          @click="selectText(item)"
        >
          {{ item }}
        </view>
      </view>
      <!-- 对话列表 -->
      <block v-for="(item, index) in messageData" :key="index">
        <view class="user_backdrop" v-if="item.role === 'user'">
          <view>
            <image src="/static/tabbar/用户.png" mode="widthFix"></image>
          </view>
          <view>{{ item.content }}</view>
        </view>
        <view class="Sent_information backdrop" v-else>
          <view class="loading" v-if="item.loadShow">
            <view class="loader"></view>
            <view>AI正在思考中..</view>
          </view>
          <view class="ai_content" v-if="item.content !== ''">
            <text user-select>{{ item.content }}</text>
            <image
              v-if="item.copyIcon"
              @click="copyData(item.content)"
              src="/static/tabbar/复制.png"
              mode="widthFix"
            ></image>
          </view>
          <!-- 【新增】RAG 引用来源展示：开启知识库增强且有命中片段时才显示 -->
          <view class="rag-sources" v-if="item.sources && item.sources.length > 0">
            <view class="rag-sources-header">
              <text>📚 参考来源</text>
              <text class="rag-sources-count">{{ item.sources.length }} 条</text>
            </view>
            <view
              class="rag-source-item"
              v-for="(source, sIndex) in item.sources"
              :key="sIndex"
              @click="toggleSource(index, sIndex)"
            >
              <view class="rag-source-head">
                <text class="rag-source-name">[{{ sIndex + 1 }}] {{ source.title || '未命名文档' }}</text>
                <text class="rag-source-score">{{ formatScore(source.score) }}</text>
              </view>
              <view class="rag-source-meta">
                {{ source.fileType || '文档' }}
                <text v-if="source.chunkIndex !== null && source.chunkIndex !== undefined">
                  · 片段 {{ source.chunkIndex }}
                </text>
                <text class="rag-source-toggle">{{ isSourceExpanded(index, sIndex) ? '收起 ▲' : '展开 ▼' }}</text>
              </view>
              <view class="rag-source-content" v-if="isSourceExpanded(index, sIndex)">
                {{ source.content }}
              </view>
            </view>
          </view>
        </view>
      </block>
      <!-- 底部输入框 -->
      <view class="Input_field">
        <view @click="clearMessage">
          <image src="/static/tabbar/清除.png" mode="aspectFill"></image>
        </view>
        <input
          placeholder="你可以问我有关旅游攻略的问题"
          maxlength="-1"
          cursor-spacing="40"
          confirm-type="send"
          auto-blur
          v-model="text"
          @confirm="sendMessage"
          :disabled="sendInState"
        />
        <view @click="sendMessage">
          <image src="/static/tabbar/发送.png" mode="aspectFill"></image>
        </view>
      </view>
      <!-- 底部留白，防止输入框遮挡最后一条消息 -->
      <view style="height: 500rpx"></view>
    </view>
    <!-- ==================== 表单模式 ==================== -->
    <view v-show="currentTab === 1" class="form-mode">
      <!-- 顶部占位 -->
      <view :style="{ height: (navHeightNum + tabHeight) + 'px' }"></view>
      <!-- 表单 -->
      <view v-if="!currentPlan" class="form-container">
        <view class="form-item">
          <text class="label">出发地</text>
          <input v-model="formData.departure" placeholder="如：广州" class="form-input" />
        </view>
        <view class="form-item">
          <text class="label">目的地 </text>
          <input v-model="formData.destination" placeholder="如：深圳" class="form-input" />
        </view>
        <view class="form-item">
          <text class="label">出行日期</text>
          <picker mode="date" :value="formData.travelDate" @change="onDateChange">
            <view class="picker-view">{{ formData.travelDate || '请选择日期' }}</view>
          </picker>
        </view>
        <view class="form-item">
          <text class="label">旅行天数</text>
          <input type="number" v-model.number="formData.days" placeholder="3" class="form-input" />
        </view>
        <view class="form-item">
          <text class="label">同行人数</text>
          <input type="number" v-model.number="formData.peopleCount" placeholder="2" class="form-input" />
        </view>
        <view class="form-item">
          <text class="label">同行类型</text>
          <radio-group @change="radioChange" class="radio-group">
            <label class="radio-label" v-for="type in peopleTypes" :key="type">
              <radio :value="type" :checked="formData.peopleType === type" />{{ type }}
            </label>
          </radio-group>
        </view>
        <view class="form-item">
          <text class="label">预算范围</text>
          <view class="budget-range">
            <input type="number" v-model.number="formData.budgetMin" placeholder="最低" class="form-input budget-input" />
            <text>—</text>
            <input type="number" v-model.number="formData.budgetMax" placeholder="最高" class="form-input budget-input" />
          </view>
        </view>
        <view class="form-item">
          <text class="label">兴趣偏好</text>
          <checkbox-group @change="checkboxChange" class="checkbox-group">
            <label class="checkbox-label" v-for="item in allPreferences" :key="item">
              <checkbox :value="item" :checked="formData.preferences.includes(item)" />{{ item }}
            </label>
          </checkbox-group>
        </view>
        <button type="primary" @click="submitForm" :loading="formLoading" class="submit-btn">
          {{ formLoading ? '生成中...' : '生成计划' }}
        </button>
        <button @click="toHistoryPage" class="history-btn">历史记录</button>
      </view>
      <!-- 计划结果 -->
      <view v-else class="plan-result">
        <view class="plan-header">
          <text class="title">{{ formData.days }}天{{ formData.destination }}之旅</text>
          <view class="meta">
            <text>📍 {{ formData.destination }}</text>
            <text>📅 {{ currentPlan.dateRange }}</text>
            <text>⏱️ {{ formData.days }}天</text>
          </view>
          <view class="budget-info">
            <text>预算 ¥{{ currentPlan.totalBudget }} / 已花 ¥0</text>
          </view>
        </view>
        <view v-for="(day, idx) in currentPlan.dailyPlans" :key="idx" class="day-card">
          <view class="day-title">
            <view class="day-number">{{ day.day }}</view>
            <view class="day-info">
              <text>第{{ day.day }}天</text>
              <text>{{ day.date }}</text>
            </view>
            <text class="theme">{{ day.theme }}</text>
          </view>
          <view v-for="(item, i) in day.attractions" :key="i" class="activity-item" :class="getActivityClass(item)">
            <view class="activity-icon">
              <text>{{ getActivityIcon(item) }}</text>
            </view>
            <view class="time-line">
              <text class="time">{{ item.time }}</text>
              <text class="duration">{{ item.duration }}</text>
            </view>
            <view class="activity-content">
              <text class="name">{{ item.name }}</text>
              <text class="desc">{{ item.description }}</text>
              <text v-if="item.ticket" class="ticket">门票 ¥{{ item.ticket }}</text>
            </view>
          </view>
          <view class="meal-section">
            <text class="section-label">餐饮推荐</text>
            <view v-for="(meal, mIdx) in day.meals" :key="mIdx" class="meal-item">
              <text>{{ meal.type }}：{{ meal.restaurant }}（{{ meal.cuisine }}） 人均¥{{ meal.avgCost }}</text>
            </view>
          </view>
          <view class="hotel-section">
            <text class="section-label">住宿</text>
            <text>{{ day.hotel.name }} ¥{{ day.hotel.nightCost }}/晚</text>
            <text class="note">{{ day.hotel.note }}</text>
          </view>
          <view class="tips">
            <text>💡 {{ day.tips }}</text>
          </view>
        </view>
        <view class="action-buttons">
          <button @click="resetForm" class="reset-btn">重新规划</button>
          <button type="primary" @click="saveCurrentPlan" class="save-btn">保存到历史</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { MenuButton } from '@/api/MenuButton.js';
import { generatePlan } from '@/api/plan.js';
import { chatAsk } from '@/api/chat.js'; // 引入聊天 API
import { chatAskRag } from '@/api/rag.js'; // 【新增】引入 RAG 知识库增强聊天 API

// ============ 获取导航栏纯数字高度 ============
const menuInfo = uni.getStorageSync('MenuButton');
const navHeightNum = menuInfo.top + menuInfo.height;
const tabHeight = 50;
const ragBarHeight = 46; // 【新增】知识库开关栏高度(px)，用于顶部占位计算

// ==================== 【新增】知识库增强（RAG）开关 ====================
// 默认开启：回答优先依据景点库/知识库文档，并在气泡下方展示引用来源
const useRag = ref(true);
const onRagSwitchChange = (e) => {
  useRag.value = e.detail.value;
  uni.showToast({
    title: useRag.value ? '已开启知识库增强' : '已关闭知识库增强',
    icon: 'none'
  });
};

// 引用来源展开状态：按「消息下标-片段下标」记录，避免给每条来源单独加字段
const expandedSourceKeys = ref({});
const sourceKey = (messageIndex, sourceIndex) => `${messageIndex}-${sourceIndex}`;
const isSourceExpanded = (messageIndex, sourceIndex) =>
  !!expandedSourceKeys.value[sourceKey(messageIndex, sourceIndex)];
const toggleSource = (messageIndex, sourceIndex) => {
  const key = sourceKey(messageIndex, sourceIndex);
  expandedSourceKeys.value[key] = !expandedSourceKeys.value[key];
};
// 相似度得分展示：保留3位小数
const formatScore = (score) =>
  score === null || score === undefined ? '' : `相似度 ${Number(score).toFixed(3)}`;

// ==================== 模式切换 ====================
const currentTab = ref(0);
const switchTab = (index) => {
  currentTab.value = index;
};

// ==================== 自由聊天相关 ====================
const greetSb = ref('你好，我是你的AI旅游助手');
const problemData = ref([
  '请给出一份广州旅游计划，预算3000',
  '请给出一份深圳旅游计划，预算3000',
  '请给出一份上海旅游计划，预算3000',
  '请给出一份北京旅游计划，预算3000',
  '请给出一份长沙旅游计划，预算3000'
]);
const text = ref('');
const historyTestList = ref([]);
const messageData = ref([]);
const sendInState = ref(false);

/**
 * 发送消息（HTTP 调用后端）
 *
 * 【RAG 改造说明】
 * - 知识库增强开关开启时：调用 /api/chat/ask/rag，返回 { answer, ragEnabled, sources }
 *   并把 sources 挂到消息对象上用于气泡下方展示
 * - 开关关闭时：调用原有的 /api/chat/ask，行为与改造前完全一致
 */
async function sendMessage() {
  if (text.value.trim().length <= 0) {
    uni.showToast({ title: '请输入询问内容', icon: 'none' });
    return;
  }
  if (sendInState.value) {
    uni.showToast({ title: '等待AI回复完毕', icon: 'none' });
    return;
  }

  // 1. 把用户消息加入对话列表
  const userContent = text.value.trim();
  messageData.value.push({ role: 'user', content: userContent });
  messageData.value.push({
    role: 'assistant',
    content: '',
    loadShow: true,
    copyIcon: false,
    sources: [] // 【新增】该条回答引用的知识库来源
  });

  // 2. 维护对话历史（用于上下文理解）
  historyTestList.value.push({ role: 'user', content: userContent });
  text.value = '';
  sendInState.value = true;

  try {
    let reply = '';
    let sources = [];

    if (useRag.value) {
      // 3a. 开启知识库增强：走 RAG 接口
      const ragResult = await chatAskRag(historyTestList.value, true);
      reply = (ragResult && ragResult.answer) || '';
      sources = (ragResult && ragResult.sources) || [];
    } else {
      // 3b. 关闭知识库增强：走原有接口，请求路径与响应格式都不变
      reply = await chatAsk(historyTestList.value);
    }

    // 4. 收到回复，更新 UI
    const lastIndex = messageData.value.length - 1;
    messageData.value[lastIndex].content = reply;
    messageData.value[lastIndex].loadShow = false;
    messageData.value[lastIndex].copyIcon = true;
    messageData.value[lastIndex].sources = sources;

    // 5. 把 AI 回复也加入历史
    historyTestList.value.push({ role: 'assistant', content: reply });

    // 6. 滚动到底部
    wx.pageScrollTo({ scrollTop: 99999, duration: 300 });

  } catch (e) {
    console.error('AI 聊天失败：', e);
    const lastIndex = messageData.value.length - 1;
    messageData.value[lastIndex].content = '抱歉，服务暂时不可用，请稍后再试。';
    messageData.value[lastIndex].loadShow = false;
    uni.showToast({ title: '请求失败', icon: 'none' });
  } finally {
    sendInState.value = false;
  }
}

function copyData(val) {
  wx.setClipboardData({ data: val });
}

function clearMessage() {
  if (sendInState.value) {
    uni.showToast({ title: '等待AI回复完毕', icon: 'none' });
    return;
  }
  historyTestList.value = [];
  messageData.value = [];
}

function selectText(val) {
  text.value = val;
  sendMessage();
}

// ==================== 表单模式相关 ====================
const peopleTypes = ['情侣', '家庭', '朋友', '独自'];
const allPreferences = ['美食', '自然风光', '城市漫步', '历史文化', '亲子', '自驾'];
const formData = reactive({
  departure: '',
  destination: '',
  travelDate: '',
  days: 3,
  peopleCount: 2,
  peopleType: '情侣',
  budgetMin: 2000,
  budgetMax: 5000,
  preferences: ['美食', '自然风光']
});

const onDateChange = (e) => {
  formData.travelDate = e.detail.value;
};

const radioChange = (e) => {
  formData.peopleType = e.detail.value;
};

const checkboxChange = (e) => {
  formData.preferences = e.detail.value;
};

const currentPlan = ref(null);
const formLoading = ref(false);

const submitForm = async () => {
  if (!formData.destination) {
    uni.showToast({ title: '请填写目的地', icon: 'none' });
    return;
  }
  formLoading.value = true;
  try {
    console.log('开始生成计划，表单数据：', JSON.stringify(formData));
    const res = await generatePlan({
      departure: formData.departure,
      destination: formData.destination,
      travelDate: formData.travelDate,
      days: formData.days,
      peopleCount: formData.peopleCount,
      peopleType: formData.peopleType,
      budgetMin: Number(formData.budgetMin),
      budgetMax: Number(formData.budgetMax),
      preferences: formData.preferences
    });
    console.log('generatePlan 返回的 res：', JSON.stringify(res));
    if (!res || typeof res !== 'object') {
      throw new Error('返回数据格式异常，不是对象');
    }
    if (!res.dailyPlans || !Array.isArray(res.dailyPlans)) {
      console.error('dailyPlans 缺失或不是数组', res);
      if (res.data && res.data.dailyPlans) {
        console.warn('检测到二次包裹，使用 res.data');
        Object.assign(res, res.data);
      } else {
        throw new Error('计划数据不完整，缺少 dailyPlans');
      }
    }
    if (res.dailyPlans.length > 0) {
      const firstDay = res.dailyPlans[0].date;
      const lastDay = res.dailyPlans[res.dailyPlans.length - 1].date;
      res.dateRange = `${firstDay} — ${lastDay}`;
    }
    currentPlan.value = res;
    console.log('currentPlan 设置成功，计划天数：', currentPlan.value.dailyPlans?.length);
    uni.showToast({ title: '生成成功', icon: 'success' });
  } catch (e) {
    console.error('生成计划失败：', e);
    uni.showModal({
      title: '计划生成失败',
      content: e.message || e.toString(),
      showCancel: false
    });
  } finally {
    formLoading.value = false;
  }
};

const resetForm = () => {
  currentPlan.value = null;
};

const saveCurrentPlan = () => {
  uni.showToast({ title: '计划已保存', icon: 'success' });
};

// ==================== 样式与图标辅助方法 ====================
const getActivityClass = (item) => {
  if (item.name?.includes('餐') || item.name?.includes('吃') || item.name?.includes('美食') || item.name?.includes('市场')) {
    return 'food';
  }
  if (item.name?.includes('休息') || item.name?.includes('自由活动')) {
    return 'rest';
  }
  return '';
};

const getActivityIcon = (item) => {
  if (item.name?.includes('餐') || item.name?.includes('吃') || item.name?.includes('美食') || item.name?.includes('市场')) {
    return '🍜';
  }
  if (item.name?.includes('休息') || item.name?.includes('自由活动')) {
    return '🌿';
  }
  if (item.name?.includes('博物馆') || item.name?.includes('馆') || item.name?.includes('城')) {
    return '🏛️';
  }
  if (item.name?.includes('山') || item.name?.includes('公园') || item.name?.includes('湖') || item.name?.includes('海')) {
    return '🌳';
  }
  return '📍';
};

const toHistoryPage = () => {
  uni.navigateTo({
    url: '/pages/TravelPlanHistory/TravelPlanHistory'
  });
};
</script>

<style>
/* ==================== 基础布局 ==================== */
.page-wrapper {
  min-height: 100vh;
  background-color: #f6f8fe;
}
.nav_content {
  display: flex;
  align-items: center;
}
.nave_text {
  display: flex;
  flex-direction: column;
}
.nave_text text:nth-child(1) {
  font-size: 28rpx;
  font-weight: bold;
}
.nave_text text:nth-child(2) {
  font-size: 25rpx;
  color: #789189;
}
.top_height {
  height: v-bind('MenuButton().top');
}
.nav_bar_custom {
  height: v-bind('MenuButton().seViewHeight');
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to bottom, #b3cffa, #f6f8fe);
  z-index: 999;
}
.nav_bar_custom image {
  width: 50rpx;
  display: block;
  margin: 20rpx 20rpx;
}
/* ==================== 标签栏 ==================== */
.tab-container {
  display: flex;
  background-color: #fff;
  padding: 20rpx 0;
  border-bottom: 1px solid #eee;
  position: fixed;
  left: 0;
  right: 0;
  z-index: 998;
}
.tab-item {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  color: #666;
  position: relative;
}
.tab-item.active {
  color: #3875f6;
  font-weight: bold;
}
.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -18rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 60rpx;
  height: 6rpx;
  background-color: #3875f6;
  border-radius: 3rpx;
}
/* ==================== 自由聊天样式 ==================== */
.Sent_information {
  padding: 10rpx;
  margin: 20rpx;
  line-height: 1.5;
  border-radius: 10rpx;
  font-size: 29rpx;
}
.backdrop {
  background-color: #fefefe;
}
@keyframes fadeInFromTop {
  0% { opacity: 0; transform: translateY(30px); }
  100% { opacity: 1; transform: translateY(0); }
}
.your-elemt {
  animation-name: fadeInFromTop;
  animation-duration: 0.7s;
  animation-timing-function: ease-in;
  animation-fill-mode: forwards;
}
.widthAuto {
  width: auto;
}
.problem_top image {
  width: 35rpx;
  display: block;
  margin-right: 10rpx;
}
.problem_top text {
  font-weight: bold;
}
.Default_problem {
  border: 1rpx solid #e8f0fc;
  border-radius: 40rpx;
  padding: 15rpx 0;
  text-align: center;
  margin: 20rpx 0;
  color: #3875f6;
  font-weight: bold;
}
.user_backdrop {
  color: #555d92;
  display: flex;
  margin: 20rpx;
}
.user_backdrop image {
  width: 38rpx;
  display: block;
  border-radius: 50%;
}
.user_backdrop view:nth-child(1) {
  height: 48rpx;
  margin-right: 10rpx;
  display: flex;
  align-items: center;
}
.user_backdrop view:nth-child(2) {
  line-height: 1.5;
  align-self: center;
}
.loading {
  display: flex;
  align-items: center;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
.loader {
  border: 5rpx solid #f3f3f3;
  border-top: 5rpx solid #3498db;
  border-radius: 50%;
  width: 40rpx;
  height: 40rpx;
  animation: spin 1s linear infinite;
  margin-right: 10rpx;
}
.ai_content {
  width: 100%;
  display: flex;
  flex-direction: column;
}
.ai_content text {
  border-bottom: 1px solid #f3f3f4;
  padding-bottom: 10rpx;
}
.ai_content image {
  width: 29rpx;
  align-self: flex-end;
  margin-top: 10rpx;
}
.Input_field {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  border-top: 1px solid #eee;
  padding-top: 10rpx;
  background-color: #f6f8fe;
  z-index: 998;
}
.Input_field input {
  flex: 1;
  background-color: #fff;
  border-radius: 10rpx;
  padding: 20rpx;
}
.Input_field view {
  height: 80rpx;
  width: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 10rpx;
}
.Input_field image {
  height: 50rpx;
  width: 50rpx;
  display: block;
}
/* ==================== 表单模式样式 ==================== */
.form-mode {
  padding: 20rpx;
}
.form-container {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
}
.form-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1px solid #f0f0f0;
}
.form-item .label {
  width: 160rpx;
  font-size: 28rpx;
  color: #333;
  flex-shrink: 0;
}
.form-input {
  flex: 1;
  height: 60rpx;
  line-height: 60rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  padding: 0 15rpx;
  font-size: 28rpx;
}
.picker-view {
  flex: 1;
  height: 60rpx;
  line-height: 60rpx;
  background-color: #f5f5f5;
  border-radius: 8rpx;
  padding: 0 15rpx;
  font-size: 28rpx;
  color: #333;
}
.radio-group, .checkbox-group {
  display: flex;
  flex-wrap: wrap;
  flex: 1;
}
.radio-label, .checkbox-label {
  display: flex;
  align-items: center;
  margin-right: 30rpx;
  margin-bottom: 10rpx;
  font-size: 28rpx;
}
.radio-label radio, .checkbox-label checkbox {
  margin-right: 8rpx;
}
.budget-range {
  flex: 1;
  display: flex;
  align-items: center;
}
.budget-input {
  flex: 1;
}
.budget-range text {
  margin: 0 10rpx;
  color: #666;
}
.submit-btn {
  margin-top: 30rpx;
  width: 100%;
  background-color: #3875f6;
  color: #fff;
  border-radius: 10rpx;
  padding: 10rpx 0;
  font-size: 32rpx;
}
/* ==================== 计划结果卡片样式 ==================== */
.plan-result {
  background-color: #f6f8fe;
}
.plan-header {
  background-color: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  background: linear-gradient(135deg, #f0f3ff 0%, #ffffff 100%);
  border: 1px solid #e8efff;
}
.plan-header .title {
  font-size: 40rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 10rpx;
  color: #1a1a2e;
}
.plan-header .meta text {
  margin-right: 20rpx;
  color: #666;
  font-size: 26rpx;
}
.plan-header .budget-info {
  margin-top: 20rpx;
  font-size: 32rpx;
  color: #3875f6;
  font-weight: 500;
  background-color: #f0f7ff;
  padding: 10rpx 20rpx;
  border-radius: 10rpx;
}
.day-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  border: 1px solid #f0f0f0;
}
.day-title {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}
.day-number {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-right: 20rpx;
  font-size: 28rpx;
}
.day-info {
  display: flex;
  flex-direction: column;
}
.day-info text:first-child {
  font-size: 36rpx;
  font-weight: bold;
  color: #1a1a2e;
}
.day-info text:last-child {
  color: #666;
  font-size: 26rpx;
}
.theme {
  margin-left: auto;
  color: #667eea;
  font-size: 28rpx;
  background-color: #f0f3ff;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-weight: 500;
}
.activity-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background-color: #f8f9ff;
  border-left: 5rpx solid #667eea;
}
.activity-item.food {
  background-color: #fff8f0;
  border-left-color: #ff9500;
}
.activity-item.rest {
  background-color: #f0fff4;
  border-left-color: #52c41a;
}
.activity-icon {
  width: 50rpx;
  height: 50rpx;
  margin-right: 15rpx;
  font-size: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.time-line {
  width: 140rpx;
  margin-right: 15rpx;
}
.time-line .time { font-weight: bold; font-size: 28rpx; color: #1a1a2e; }
.time-line .duration { color: #999; font-size: 24rpx; margin-top: 5rpx; display: block; }
.activity-content {
  flex: 1;
}
.activity-content .name { font-weight: bold; display: block; font-size: 30rpx; color: #1a1a2e; }
.activity-content .desc { color: #666; font-size: 26rpx; margin-top: 5rpx; display: block; }
.activity-content .ticket { color: #ff9500; font-size: 24rpx; margin-top: 5rpx; display: block; }
.meal-section, .hotel-section {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1px dashed #eee;
}
.section-label { font-weight: bold; margin-right: 20rpx; font-size: 30rpx; color: #1a1a2e; }
.meal-item, .hotel-section text {
  display: block;
  margin-top: 10rpx;
  color: #555;
  font-size: 26rpx;
}
.note {
  color: #999;
  font-size: 24rpx;
  margin-top: 5rpx;
}
.tips {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1px dashed #ddd;
  color: #666;
  font-size: 26rpx;
}
.action-buttons {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
  padding-bottom: 40rpx;
}
.reset-btn {
  flex: 1;
  background-color: #f5f5f5;
  color: #333;
  border-radius: 10rpx;
}
.save-btn {
  flex: 1;
  background-color: #3875f6;
  color: #fff;
  border-radius: 10rpx;
}
.action-buttons-inline {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}
.history-btn {
  flex: 1;
  background-color: #f0f0f0;
  color: #333;
  border-radius: 10rpx;
  padding: 10rpx 0;
  font-size: 32rpx;
}

/* ==================== 【新增】知识库增强（RAG）相关样式 ==================== */
/* 顶部固定的知识库增强开关栏：紧贴在模式切换标签栏下方 */
.rag-switch-bar {
  position: fixed;
  left: 0;
  right: 0;
  z-index: 98;
  height: 46px;
  box-sizing: border-box;
  padding: 0 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f5fbff;
  border-bottom: 1rpx solid #e3f0fa;
}
.rag-switch-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.rag-switch-title {
  font-size: 28rpx;
  color: #2897ce;
  font-weight: bold;
}
.rag-switch-tip {
  font-size: 20rpx;
  color: #8a9aa5;
  margin-top: 2rpx;
}

/* 引用来源区域 */
.rag-sources {
  margin-top: 20rpx;
  padding-top: 16rpx;
  border-top: 1rpx dashed #d8e6ef;
}
.rag-sources-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 26rpx;
  color: #2897ce;
  font-weight: bold;
  margin-bottom: 12rpx;
}
.rag-sources-count {
  font-size: 22rpx;
  color: #8a9aa5;
  font-weight: normal;
}
.rag-source-item {
  background-color: #f5fbff;
  border-radius: 10rpx;
  padding: 14rpx 18rpx;
  margin-bottom: 12rpx;
}
.rag-source-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.rag-source-name {
  font-size: 26rpx;
  color: #22343f;
  font-weight: bold;
  flex: 1;
}
.rag-source-score {
  font-size: 20rpx;
  color: #2897ce;
  margin-left: 12rpx;
}
.rag-source-meta {
  display: flex;
  align-items: center;
  font-size: 20rpx;
  color: #8a9aa5;
  margin-top: 6rpx;
}
.rag-source-toggle {
  margin-left: auto;
  color: #2897ce;
}
.rag-source-content {
  margin-top: 10rpx;
  padding-top: 10rpx;
  border-top: 1rpx solid #e3f0fa;
  font-size: 24rpx;
  color: #4a5c66;
  line-height: 1.6;
}
</style>