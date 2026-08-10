<template>
  <view class="detail-page">
    <!-- 顶部标题栏 -->
    <view class="head-img">
      <text class="head-title">{{ route.name || '' }}</text>
    </view>

    <!-- 信息卡片 -->
    <view class="info-card">
      <view class="info-row" v-for="(item, index) in infoList" :key="index">
        <text class="label">{{ item.label }}</text>
        <view class="value" v-if="item.key === 'guide'">
          <text>姓名：{{ route.guide.name || '暂无' }}</text>
          <text>电话：{{ route.guide.phone || '暂无' }}</text>
          <text>经验：{{ route.guide.experience || '暂无' }}</text>
        </view>
        <text class="value" v-else-if="item.key === 'price'">¥{{ route[item.key] || 0 }}</text>
        <text class="value" v-else-if="item.key === 'time'">
          {{ route.startTime || '' }} 至 {{ route.endTime || '' }}
        </text>
        <text class="value" v-else>{{ route[item.key] || '' }}</text>
      </view>
    </view>

    <!-- 报名按钮 -->
    <button class="book-btn" @click="openTravelerDialog">立即报名</button>

    <!-- 选择出行人弹窗 -->
    <uni-popup v-model="travelerDialog" type="center" :style="{ width: '80%', borderRadius: '24rpx' }">
      <view class="traveler-popup">
        <view class="popup-title">选择出行人</view>
        <!-- 新增出行人按钮 -->
        <button class="add-traveler-btn" @click="openAddTravelerDialog">新增出行人</button>
        <!-- 出行人列表 -->
        <view class="traveler-list" v-if="travelers.length">
          <view 
            class="traveler-item" 
            v-for="t in travelers" 
            :key="t.id"
            @click="toggleTraveler(t)"
          >
            <view class="traveler-info">
              <text class="name">{{ t.name }}</text>
              <text class="phone">{{ t.phone }}</text>
              <text class="age">年龄：{{ t.age }}岁</text>
            </view>
            <view class="checkbox" :class="{ checked: selectedTravelers.some(s => s.id === t.id) }">
              <text v-if="selectedTravelers.some(s => s.id === t.id)">✓</text>
            </view>
          </view>
        </view>
        <view class="empty" v-else>暂无出行人，请先新增</view>
        <!-- 确认报名按钮 -->
        <button 
          class="confirm-btn" 
          @click="confirmBook"
          :disabled="selectedTravelers.length === 0"
        >
          确认报名
        </button>
      </view>
    </uni-popup>

    <!-- 新增出行人弹窗 -->
    <uni-popup v-model="addTravelerDialog" type="center" :style="{ width: '80%', borderRadius: '24rpx' }">
      <view class="add-traveler-popup">
        <view class="popup-title">新增出行人</view>
        <uni-forms class="traveler-form">
          <uni-forms-item label="姓名" class="form-item">
            <input v-model="newTraveler.name" placeholder="请输入姓名" class="form-input" />
          </uni-forms-item>
          <uni-forms-item label="年龄" class="form-item">
            <input v-model="newTraveler.age" type="number" placeholder="请输入年龄" class="form-input" />
          </uni-forms-item>
          <uni-forms-item label="手机号" class="form-item">
            <input v-model="newTraveler.phone" type="number" placeholder="请输入手机号" class="form-input" />
          </uni-forms-item>
          <uni-forms-item label="身份证号" class="form-item">
            <input v-model="newTraveler.idCard" placeholder="请输入身份证号" class="form-input" />
          </uni-forms-item>
        </uni-forms>
        <view class="btn-group">
          <button class="cancel-btn" @click="addTravelerDialog = false">取消</button>
          <button class="save-btn" @click="addTraveler">保存</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import travelerMixin from '@/mixins/travelerMixin.js'
export default {
  mixins: [travelerMixin],
  data() {
    return {
      // 修复：初始化route时，给guide赋默认空对象，避免初始渲染报错
      route: {
        guide: {} // 给guide默认空对象
      },
      bookedActivities: [],
      // 信息列表配置（优化循环渲染）
      infoList: [
        { label: '活动时间', key: 'time' },
        { label: '集合地点', key: 'meetingPoint' },
        { label: '导游信息', key: 'guide' },
        { label: '社团信息', key: 'groupInfo' },
        { label: '价格', key: 'price' }
      ]
    }
  },
  onLoad(opt) {
    // 修复：增加参数解析的容错性，避免解析失败导致route异常
    try {
      const parsedRoute = JSON.parse(decodeURIComponent(opt.route || '{}'))
      // 合并默认值，确保guide属性存在
      this.route = {
        guide: {},
        ...parsedRoute
      }
    } catch (e) {
      console.error('路由参数解析失败：', e)
      this.route = { guide: {} } // 解析失败时兜底
    }
    this.bookedActivities = uni.getStorageSync('bookedActivities') || []
  },
  methods: {
    // 确认报名
    confirmBook() {
      // 检查时间冲突
      const hasConflict = this.bookedActivities.some(
        a => new Date(this.route.startTime) <= new Date(a.endTime) &&
             new Date(this.route.endTime) >= new Date(a.startTime)
      )
      if (hasConflict) {
        uni.showToast({ title: '该时间段已报名其他活动', icon: 'none' })
        this.travelerDialog = false
        return
      }
      // 构造报名信息
      const bookingInfo = {
        id: Date.now(),
        type: 'group', // 类型：老年团
        routeId: this.route.id,
        routeName: this.route.name,
        startTime: this.route.startTime,
        endTime: this.route.endTime,
        price: this.route.price,
        travelers: this.selectedTravelers, // 选中的出行人
        createTime: new Date().toLocaleString()
      }
      // 保存报名信息
      this.bookedActivities.push(bookingInfo)
      uni.setStorageSync('bookedActivities', this.bookedActivities)
      uni.showToast({ title: '报名成功', icon: 'success' })
      this.travelerDialog = false
      setTimeout(() => uni.navigateBack(), 1500)
    }
  }
}
</script>

<style lang="less" scoped>
// 全局页面样式
.detail-page {
  min-height: 100vh;
  background-color: #f8f5f2; // 柔和浅底色，提升视觉舒适度
  padding-bottom: 60rpx;
}

// 顶部标题栏
.head-img {
  background: linear-gradient(135deg, #ff8f00 0%, #ffb74d 100%); // 渐变背景
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border-radius: 0 0 30rpx 30rpx; // 底部圆角
  box-shadow: 0 4rpx 12rpx rgba(255, 143, 0, 0.2); // 轻微阴影增强层次感

  .head-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
    text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1); // 文字阴影提升可读性
  }
}

// 信息卡片
.info-card {
  background: #fff;
  margin: 30rpx 24rpx;
  padding: 36rpx 30rpx;
  border-radius: 24rpx; // 大圆角更柔和
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.06); // 轻阴影提升立体感
  overflow: hidden;

  .info-row {
    display: flex;
    align-items: flex-start;
    padding: 20rpx 0;
    border-bottom: 1px solid #f5f2ef; // 柔和分隔线
    &:last-child {
      border-bottom: none; // 最后一行取消分隔线
    }

    .label {
      flex: 0 0 160rpx; // 固定标签宽度，排版更整齐
      font-size: 30rpx;
      font-weight: 500;
      color: #3e2723; // 深棕色调，符合老年群体视觉习惯
      line-height: 1.4;
    }

    .value {
      flex: 1;
      font-size: 28rpx;
      color: #5d4037;
      line-height: 1.6;
      text-align: justify;
      &.price {
        color: #e64a19; // 价格用醒目色
        font-size: 32rpx;
        font-weight: 600;
      }
      text {
        display: block;
        margin: 4rpx 0;
      }
    }
  }
}

// 立即报名按钮
.book-btn {
  margin: 0 24rpx;
  background: linear-gradient(135deg, #ff8f00 0%, #ffb74d 100%);
  color: #fff;
  border: none;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 500;
  padding: 24rpx 0;
  box-shadow: 0 8rpx 16rpx rgba(255, 143, 0, 0.2);
  transition: all 0.2s ease; // 过渡动画
  &:active {
    transform: scale(0.98); // 点击缩放反馈
    box-shadow: 0 4rpx 8rpx rgba(255, 143, 0, 0.2);
  }
}

// 选择出行人弹窗
.traveler-popup {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);

  .popup-title {
    font-size: 34rpx;
    font-weight: 600;
    color: #3e2723;
    margin-bottom: 30rpx;
    text-align: center;
    position: relative;
    &::after {
      content: '';
      display: block;
      width: 80rpx;
      height: 4rpx;
      background: #ff8f00;
      margin: 12rpx auto 0;
      border-radius: 2rpx;
    }
  }

  .add-traveler-btn {
    background: #faf7f2;
    color: #ff8f00;
    border: 1px solid #ffe0b2;
    border-radius: 12rpx;
    padding: 16rpx 0;
    font-size: 28rpx;
    margin-bottom: 24rpx;
    transition: all 0.2s ease;
    &:active {
      background: #f5efe8;
    }
  }

  .traveler-list {
    max-height: 400rpx;
    overflow-y: auto;
    padding-right: 8rpx;
    // 滚动条美化
    &::-webkit-scrollbar {
      width: 6rpx;
    }
    &::-webkit-scrollbar-thumb {
      background: #ffcc80;
      border-radius: 3rpx;
    }

    .traveler-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20rpx 12rpx;
      border-bottom: 1px solid #f5f2ef;
      border-radius: 12rpx;
      margin-bottom: 8rpx;
      transition: background 0.2s ease;
      &:hover, &:active {
        background: #faf7f2;
      }

      .traveler-info {
        flex: 1;
        .name {
          font-size: 30rpx;
          font-weight: 500;
          color: #3e2723;
          display: block;
          margin-bottom: 4rpx;
        }
        .phone, .age {
          font-size: 24rpx;
          color: #795548;
          display: block;
          line-height: 1.4;
        }
      }

      .checkbox {
        width: 44rpx;
        height: 44rpx;
        border: 2px solid #ffb74d;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 24rpx;
        transition: all 0.2s ease;
        &.checked {
          background: linear-gradient(135deg, #ff8f00 0%, #ffb74d 100%);
          border-color: #ff8f00;
          transform: scale(1.05); // 选中轻微放大
        }
      }
    }
  }

  .empty {
    text-align: center;
    color: #9e9e9e;
    padding: 40rpx 0;
    font-size: 28rpx;
  }

  .confirm-btn {
    background: linear-gradient(135deg, #ff8f00 0%, #ffb74d 100%);
    color: #fff;
    border: none;
    border-radius: 12rpx;
    padding: 20rpx 0;
    font-size: 30rpx;
    font-weight: 500;
    margin-top: 30rpx;
    box-shadow: 0 6rpx 12rpx rgba(255, 143, 0, 0.15);
    transition: all 0.2s ease;
    &:disabled {
      background: #e0e0e0;
      color: #9e9e9e;
      box-shadow: none;
    }
    &:active:not(:disabled) {
      transform: scale(0.98);
      box-shadow: 0 3rpx 6rpx rgba(255, 143, 0, 0.15);
    }
  }
}

// 新增出行人弹窗
.add-traveler-popup {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx 30rpx;
  box-shadow: 0 10rpx 30rpx rgba(0, 0, 0, 0.1);

  .popup-title {
    font-size: 34rpx;
    font-weight: 600;
    color: #3e2723;
    margin-bottom: 30rpx;
    text-align: center;
    position: relative;
    &::after {
      content: '';
      display: block;
      width: 80rpx;
      height: 4rpx;
      background: #ff8f00;
      margin: 12rpx auto 0;
      border-radius: 2rpx;
    }
  }

  .traveler-form {
    margin-bottom: 30rpx;

    .form-item {
      margin-bottom: 24rpx;
      label {
        font-size: 28rpx;
        color: #3e2723;
        font-weight: 500;
      }

      .form-input {
        width: 100%;
        height: 80rpx;
        border: 1px solid #e0e0e0;
        border-radius: 12rpx;
        padding: 0 20rpx;
        font-size: 28rpx;
        background: #fafafa;
        transition: border 0.2s ease;
        &:focus {
          border-color: #ff8f00;
          background: #fff;
          outline: none;
        }
      }
    }
  }

  .btn-group {
    display: flex;
    gap: 20rpx;
    margin-top: 10rpx;

    .cancel-btn {
      flex: 1;
      background: #f5f5f5;
      color: #616161;
      border: none;
      border-radius: 12rpx;
      padding: 20rpx 0;
      font-size: 28rpx;
      transition: all 0.2s ease;
      &:active {
        background: #e0e0e0;
      }
    }

    .save-btn {
      flex: 1;
      background: linear-gradient(135deg, #ff8f00 0%, #ffb74d 100%);
      color: #fff;
      border: none;
      border-radius: 12rpx;
      padding: 20rpx 0;
      font-size: 28rpx;
      font-weight: 500;
      box-shadow: 0 6rpx 12rpx rgba(255, 143, 0, 0.15);
      transition: all 0.2s ease;
      &:active {
        transform: scale(0.98);
        box-shadow: 0 3rpx 6rpx rgba(255, 143, 0, 0.15);
      }
    }
  }
}
</style>