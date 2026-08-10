<template>
  <view class="list-page">
    <!-- 顶部插画 -->
    <view class="header-pic">
      <view class="cloud a"></view>
      <view class="cloud b"></view>
      <text class="list-title">精选老年团</text>
    </view>

    <!-- 加载中提示 -->
    <view v-if="loading" class="loading">
      <text>加载中...</text>
    </view>

    <!-- 空数据提示 -->
    <view v-if="!loading && routeList.length === 0" class="empty">
      <text>暂无老年团信息</text>
    </view>

    <!-- 列表 -->
    <scroll-view v-if="!loading && routeList.length > 0" scroll-y class="route-scroll">
      <view class="route-list">
        <view
          class="route-card"
          v-for="r in routeList"
          :key="r.id"
          @click="goToDetail(r)"
        >
          <view class="card-left">
            <text class="route-name">{{ r.name }}</text>
            <text class="route-time">📅 {{ r.startTime }}</text>
            <text class="route-group">🏢 {{ r.groupName }}</text>
          </view>
          <view class="card-right">
            <text class="price">¥{{ r.price }}</text>
            <text class="arrow">›</text>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      routeList: [], // 清空模拟数据，改为空数组
      loading: false, // 加载状态
      baseUrl: 'http://localhost:8080/api' // 后端接口基础地址，根据实际部署地址修改
    }
  },
  onLoad() {
    // 页面加载时请求后端数据
    this.getTravelGroupList();
  },
  methods: {
    // 请求老年团列表接口
    getTravelGroupList() {
      this.loading = true;
      uni.request({
        url: `${this.baseUrl}/base/group/list`,
        method: 'GET', 
        success: (res) => {
          this.loading = false;
          // 后端返回Result格式，数据在data字段中
		  console.log('接口完整返回：', res); 
		  console.log('res.data具体内容：', res.data);
          if (res.statusCode === 200) {
            this.routeList = res.data.data;
          } else {
            uni.showToast({
              title: '获取数据失败',
              icon: 'none'
            });
          }
        },
        fail: (err) => {
          this.loading = false;
          uni.showToast({
            title: '网络异常，请重试',
            icon: 'none'
          });
          console.error('请求老年团列表失败：', err);
        }
      });
    },
    goToDetail(r) {
      uni.navigateTo({
        // 确保 route 参数正确编码
        url: `/pages/SilverHairedModel/group-travel/detail?route=${encodeURIComponent(JSON.stringify(r))}`
      });
    }
  }
}
</script>

<style lang="less">
.list-page{
  background: #faf7f2;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.header-pic{
  position: relative;
  height: 240rpx;
  background: linear-gradient(180deg,#fff9c4 0%,#ffe0b2 100%);
  border-radius: 0 0 30% 30%/0 0 20% 20%;
  overflow: hidden;
  .cloud{
    position: absolute;
    background: #fff;
    border-radius: 100rpx;
    opacity: .8;
    &.a{ width: 120rpx; height: 40rpx; top: 60rpx; left: 10%; animation: drift 20s linear infinite; }
    &.b{ width: 80rpx; height: 30rpx; top: 100rpx; left: 70%; animation: drift 25s linear infinite reverse; }
  }
  .list-title{
    position: absolute;
    bottom: 30rpx;
    left: 50%;
    transform: translateX(-50%);
    font-size: 40rpx;
    color: #5d4037;
    font-weight: 600;
  }
}
@keyframes drift{
  from{ transform: translateX(-200rpx); }
  to{ transform: translateX(750rpx); }
}

// 加载中样式
.loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  color: #795548;
}

// 空数据样式
.empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  color: #795548;
}

.route-scroll{
  flex: 1;
  padding: 30rpx;
}
.route-list{
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}
.route-card{
  display: flex;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 8rpx 20rpx rgba(0,0,0,.06);
  transition: transform .2s;
  &:active{ transform: scale(.98); }
  .card-left{
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 12rpx;
    .route-name{ font-size: 34rpx; color: #3e2723; font-weight: 600; }
    .route-time,.route-group{ font-size: 26rpx; color: #795548; }
  }
  .card-right{
    text-align: right;
    .price{ display: block; font-size: 40rpx; color: #ff8f00; font-weight: bold; }
    .arrow{ font-size: 48rpx; color: #bdbdbd; line-height: 1; }
  }
}
</style>