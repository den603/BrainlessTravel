<template>
  <view class="detail-page">
    <!-- 顶部标题栏（适配导游页面） -->
    <view class="head-img">
      <text class="head-title">{{ guide.name || '导游详情' }}</text>
    </view>

    <!-- 导游头部卡片（整合到信息卡片风格） -->
    <view class="info-card guide-header-card">
      <image :src="guide.avatar || '/static/default-avatar.png'" class="guide-avatar" mode="aspectFill" :lazy-load="true"></image>
      <view class="guide-header-info">
        <view class="guide-name">{{ guide.name || '暂无名称' }}</view>
        <view class="guide-meta">
          <text class="rating">⭐ {{ guide.rating || '5.0' }}</text>
          <text class="region">📍 {{ guide.region || '暂无地区' }}</text>
        </view>
      </view>
    </view>

    <!-- 详细信息区块（统一信息卡片样式） -->
    <view class="info-card">
      <!-- 导游介绍 -->
      <view class="info-row single-row">
        <text class="label">导游介绍</text>
        <text class="value">{{ guide.intro || '暂无介绍' }}</text>
      </view>

      <!-- 服务信息 -->
      <view class="info-row service-row">
        <text class="label">服务信息</text>
        <view class="value service-list">
          <view class="service-item">
            <text class="service-label">服务语言</text>
            <text class="service-value">{{ (guide.languages || []).join('、') || '暂无' }}</text>
          </view>
          <view class="service-item">
            <text class="service-label">从业经验</text>
            <text class="service-value">{{ guide.experience || '暂无' }}</text>
          </view>
          <view class="service-item">
            <text class="service-label">服务费用</text>
            <text class="service-value price">¥{{ guide.price || '0' }} 元/天</text>
          </view>
          <view class="service-item">
            <text class="service-label">联系电话</text>
            <text class="service-value">{{ guide.phone || '暂无' }}</text>
          </view>
        </view>
      </view>

      <!-- 可预约日期 -->
      <view class="info-row">
        <text class="label">可预约日期</text>
        <view class="value schedule-tags">
          <view class="schedule-tag" v-for="(day, index) in guide.schedule" :key="index">
            {{ day }}
          </view>
          <view v-if="!guide.schedule || guide.schedule.length === 0" class="empty-text">暂无排期</view>
        </view>
      </view>
    </view>

    <!-- 预约按钮（统一样式） -->
    <button class="book-btn" @click="openTravelerDialog">立即预约</button>

    <!-- 选择出行人弹窗（统一样式） -->
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
        <!-- 确认预约按钮 -->
        <button 
          class="confirm-btn" 
          @click="confirmAppointment"
          :disabled="selectedTravelers.length === 0"
        >
          确认预约
        </button>
      </view>
    </uni-popup>

    <!-- 新增出行人弹窗（统一样式） -->
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
          <button class="cancel-btn" @click="closeAddDialog">取消</button>
          <button class="save-btn" @click="addTraveler">保存</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
export default {
  data() {
    return {
      // 导游数据
      guide: {
        id: '',
        name: '',
        avatar: '/static/default-avatar.png',
        rating: '5.0',
        region: '',
        intro: '',
        languages: [],
        experience: '',
        price: '0',
        phone: '',
        schedule: []
      },
      bookedActivities: [],
      // 弹窗控制
      travelerDialog: false,
      addTravelerDialog: false,
      // 出行人数据
      travelers: [],
      selectedTravelers: [],
      // 新增出行人表单
      newTraveler: {
        name: '',
        age: '',
        phone: '',
        idCard: ''
      }
    };
  },
  onLoad(options) {
    try {
      if (options.guide) {
        this.guide = JSON.parse(decodeURIComponent(options.guide));
      }
    } catch (e) {
      console.error('导游数据解析失败', e);
    }
    if (!this.guide.languages) this.guide.languages = [];
    this.bookedActivities = uni.getStorageSync('bookedActivities') || [];
    this.travelers = uni.getStorageSync('travelers') || [];
  },
  methods: {
    // 打开选择出行人弹窗
    openTravelerDialog() {
      this.travelerDialog = true;
      this.selectedTravelers = [];
    },
    // 打开新增出行人弹窗
    openAddTravelerDialog() {
      this.addTravelerDialog = true;
      this.newTraveler = { name: '', age: '', phone: '', idCard: '' };
    },
    // 关闭新增弹窗
    closeAddDialog() {
      this.addTravelerDialog = false;
    },
    // 切换出行人选择状态
    toggleTraveler(item) {
      const index = this.selectedTravelers.findIndex(s => s.id === item.id);
      if (index > -1) {
        this.selectedTravelers.splice(index, 1);
      } else {
        this.selectedTravelers.push(item);
      }
    },
    // 新增出行人（校验+保存）
    addTraveler() {
      if (!this.newTraveler.name) {
        return uni.showToast({ title: '请输入姓名', icon: 'none' });
      }
      if (!this.newTraveler.age || isNaN(this.newTraveler.age)) {
        return uni.showToast({ title: '请输入有效年龄', icon: 'none' });
      }
      if (!/^1[3-9]\d{9}$/.test(this.newTraveler.phone)) {
        return uni.showToast({ title: '请输入有效手机号', icon: 'none' });
      }
      if (!/^\d{15}|\d{18}$/.test(this.newTraveler.idCard)) {
        return uni.showToast({ title: '请输入有效身份证号', icon: 'none' });
      }

      const travelerItem = {
        id: Date.now(),
        ...this.newTraveler
      };

      this.travelers.push(travelerItem);
      uni.setStorageSync('travelers', this.travelers);
      uni.showToast({ title: '添加成功', icon: 'success' });
      this.closeAddDialog();
    },
    // 确认预约
    confirmAppointment() {
      if (this.selectedTravelers.length === 0) {
        return uni.showToast({ title: '请选择出行人', icon: 'none' });
      }

      const bookingInfo = {
        id: Date.now(),
        type: 'guide',
        guideId: this.guide.id || '',
        guideName: this.guide.name || '导游',
        region: this.guide.region || '',
        price: this.guide.price || '0',
        travelers: this.selectedTravelers,
        createTime: new Date().toLocaleString()
      };

      this.bookedActivities.push(bookingInfo);
      uni.setStorageSync('bookedActivities', this.bookedActivities);

      uni.showToast({
        title: `预约成功！`,
        icon: 'success',
        duration: 2000
      });
      
      this.travelerDialog = false;
      this.addTravelerDialog = false;
      setTimeout(() => {
        uni.navigateBack();
      }, 2000);
    }
  }
};
</script>

<style lang="less" scoped>
// 全局页面样式（复用第一个页面的基础样式）
.detail-page {
  min-height: 100vh;
  background-color: #f8f5f2;
  padding-bottom: 60rpx;
}

// 顶部标题栏（复用）
.head-img {
  background: linear-gradient(135deg, #ff8f00 0%, #ffb74d 100%);
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border-radius: 0 0 30rpx 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 143, 0, 0.2);

  .head-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #fff;
    text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
  }
}

// 信息卡片通用样式（复用）
.info-card {
  background: #fff;
  margin: 30rpx 24rpx;
  padding: 36rpx 30rpx;
  border-radius: 24rpx;
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.06);
  overflow: hidden;

  .info-row {
    display: flex;
    align-items: flex-start;
    padding: 20rpx 0;
    border-bottom: 1px solid #f5f2ef;
    &:last-child {
      border-bottom: none;
    }

    .label {
      flex: 0 0 160rpx;
      font-size: 30rpx;
      font-weight: 500;
      color: #3e2723;
      line-height: 1.4;
    }

    .value {
      flex: 1;
      font-size: 28rpx;
      color: #5d4037;
      line-height: 1.6;
      text-align: justify;
    }
  }
}

// 导游头部卡片样式（适配信息卡片）
.guide-header-card {
  display: flex;
  align-items: center;
  padding: 30rpx;
  
  .guide-avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 60rpx;
    margin-right: 24rpx;
    border: 2rpx solid #e9ecef;
    object-fit: cover;
  }
  
  .guide-header-info {
    flex: 1;
    .guide-name {
      font-size: 38rpx;
      font-weight: 700;
      color: #3e2723;
      margin-bottom: 12rpx;
    }
    .guide-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 16rpx;
      .rating, .region {
        font-size: 26rpx;
        color: #5d4037;
        background: #f1f3f7;
        padding: 6rpx 16rpx;
        border-radius: 30rpx;
      }
      .rating {
        color: #f5a623;
      }
    }
  }
  
  .info-row {
    border-bottom: none;
    padding: 0;
  }
}

// 导游介绍单行样式
.single-row {
  align-items: center;
}

// 服务信息样式
.service-row {
  .service-list {
    width: 100%;
    .service-item {
      display: flex;
      justify-content: space-between;
      padding: 8rpx 0;
      
      .service-label {
        font-weight: 500;
        color: #64748b;
        width: 120rpx;
      }
      
      .service-value {
        flex: 1;
        text-align: right;
        &.price {
          color: #e64a19;
          font-size: 32rpx;
          font-weight: 600;
        }
      }
    }
  }
}

// 可预约日期样式
.schedule-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding-top: 8rpx;
  
  .schedule-tag {
    background: #eef2ff;
    color: #1e40af;
    padding: 12rpx 24rpx;
    border-radius: 48rpx;
    font-size: 26rpx;
  }
  
  .empty-text {
    color: #9e9e9e;
    font-size: 28rpx;
  }
}

// 立即预约按钮（复用第一个页面的按钮样式）
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
  transition: all 0.2s ease;
  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 8rpx rgba(255, 143, 0, 0.2);
  }
}

// 选择出行人弹窗（复用第一个页面样式）
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
          transform: scale(1.05);
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

// 新增出行人弹窗（复用第一个页面样式）
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