<template>
  <view class="my-bookings">
    <view class="page-title">我的报名/预约</view>
    
    <!-- 老年团报名列表 -->
    <view class="booking-section" v-if="groupBookings.length">
      <view class="section-title">老年团报名</view>
      <view class="booking-list">
        <view class="booking-item" v-for="item in groupBookings" :key="item.id">
          <view class="item-header">
            <text class="item-name">{{ item.routeName }}</text>
            <text class="item-time">{{ item.createTime }}</text>
          </view>
          <view class="item-info">
            <text>时间：{{ item.startTime }} 至 {{ item.endTime }}</text>
            <text>价格：¥{{ item.price }}</text>
            <text>出行人：{{ item.travelers.map(t => t.name).join('、') }}</text>
          </view>
          <button class="cancel-btn" @click="cancelBooking(item.id)">取消报名</button>
        </view>
      </view>
    </view>
    <view class="empty-section" v-else>暂无老年团报名信息</view>

    <!-- 私人导游预约列表 -->
    <view class="booking-section" v-if="guideBookings.length">
      <view class="section-title">私人导游预约</view>
      <view class="booking-list">
        <view class="booking-item" v-for="item in guideBookings" :key="item.id">
          <view class="item-header">
            <text class="item-name">{{ item.guideName }}</text>
            <text class="item-time">{{ item.createTime }}</text>
          </view>
          <view class="item-info">
            <text>服务地区：{{ item.region }}</text>
            <text>费用：¥{{ item.price }}/天</text>
            <text>出行人：{{ item.travelers.map(t => t.name).join('、') }}</text>
          </view>
          <button class="cancel-btn" @click="cancelBooking(item.id)">取消预约</button>
        </view>
      </view>
    </view>
    <view class="empty-section" v-else>暂无私人导游预约信息</view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      bookedActivities: [],
      groupBookings: [], // 老年团报名
      guideBookings: [] // 导游预约
    }
  },
  onLoad() {
    this.loadBookings()
  },
  methods: {
    // 加载报名/预约信息
    loadBookings() {
      this.bookedActivities = uni.getStorageSync('bookedActivities') || []
      // 分类筛选
      this.groupBookings = this.bookedActivities.filter(item => item.type === 'group')
      this.guideBookings = this.bookedActivities.filter(item => item.type === 'guide')
    },
    // 取消报名/预约
    cancelBooking(id) {
      uni.showModal({
        title: '提示',
        content: '确定取消吗？',
        success: (res) => {
          if (res.confirm) {
            this.bookedActivities = this.bookedActivities.filter(item => item.id !== id)
            uni.setStorageSync('bookedActivities', this.bookedActivities)
            this.loadBookings()
            uni.showToast({ title: '取消成功', icon: 'success' })
          }
        }
      })
    }
  }
}
</script>

<style lang="less">
.my-bookings {
  background: #faf7f2;
  min-height: 100vh;
  padding: 30rpx;
  .page-title {
    font-size: 40rpx;
    font-weight: 600;
    color: #5d4037;
    text-align: center;
    margin-bottom: 40rpx;
  }
  .booking-section {
    margin-bottom: 40rpx;
    .section-title {
      font-size: 32rpx;
      color: #3e2723;
      font-weight: 500;
      margin-bottom: 20rpx;
      padding-bottom: 10rpx;
      border-bottom: 1px solid #eee;
    }
    .booking-list {
      .booking-item {
        background: #fff;
        border-radius: 20rpx;
        padding: 30rpx;
        margin-bottom: 20rpx;
        box-shadow: 0 4rpx 12rpx rgba(0,0,0,.05);
        .item-header {
          display: flex;
          justify-content: space-between;
          margin-bottom: 20rpx;
          .item-name {
            font-size: 30rpx;
            font-weight: 600;
            color: #3e2723;
          }
          .item-time {
            font-size: 24rpx;
            color: #795548;
          }
        }
        .item-info {
          display: flex;
          flex-direction: column;
          gap: 10rpx;
          font-size: 26rpx;
          color: #795548;
          margin-bottom: 20rpx;
        }
        .cancel-btn {
          background: #ff5252;
          color: #fff;
          border: none;
          border-radius: 10rpx;
          padding: 10rpx 20rpx;
          font-size: 26rpx;
        }
      }
    }
  }
  .empty-section {
    text-align: center;
    color: #999;
    font-size: 28rpx;
    padding: 40rpx 0;
    background: #fff;
    border-radius: 20rpx;
    box-shadow: 0 4rpx 12rpx rgba(0,0,0,.05);
  }
}
</style>