<template>
  <view class="list-page">
    <!-- 顶部插画 -->
    <view class="header-pic">
      <view class="cloud a"></view>
      <view class="cloud b"></view>
      <text class="header-title">精选导游</text>
    </view>

    <!-- 地域筛选 -->
    <view class="filter-bar">
      <picker @change="onRegionChange" :value="regionIndex" :range="regions">
        <view class="picker-box">
          <text class="picker-label">选择地区</text>
          <text class="picker-val">{{ regions[regionIndex] }}</text>
          <text class="arrow">▼</text>
        </view>
      </picker>
    </view>

    <!-- 加载中提示 -->
    <view v-if="loading" class="loading">
      <text>加载中...</text>
    </view>

    <!-- 空数据提示 -->
    <view v-if="!loading && guides.length === 0" class="empty">
      <text>暂无导游信息</text>
    </view>

    <!-- 导游卡片 -->
    <scroll-view v-if="!loading && guides.length > 0" scroll-y class="guide-scroll">
      <view class="guide-list">
        <view
          class="guide-card"
          v-for="g in filteredGuides"
          :key="g.id"
          @click="goDetail(g)"
        >
          <view class="avatar-box">
            <image :src="g.avatar" class="avatar"></image>
            <text class="rating">{{ g.rating }} ★</text>
          </view>
          <view class="info-box">
            <text class="name">{{ g.name }}</text>
            <text class="region">🌍 熟悉 {{ g.region }}</text>
            <text class="lang">🗣 {{ g.languages.join(" · ") }}</text>
            <text class="price">¥{{ g.price }}/天</text>
          </view>
          <text class="arrow">›</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      regions: ['全部', '北京', '上海', '广州', '深圳', '成都', '杭州'],
      regionIndex: 0,
      guides: [], // 清空模拟数据，改为空数组
      loading: false, // 加载状态
      baseUrl: 'http://localhost:8080/api' // 后端接口基础地址，根据实际部署地址修改
    }
  },
  onLoad() {
    // 页面加载时请求后端导游列表数据
    this.getPrivateGuideList();
  },
  computed: {
    filteredGuides() {
      return this.regionIndex === 0
        ? this.guides
        : this.guides.filter(g => g.region === this.regions[this.regionIndex])
    }
  },
  methods: {
    // 请求私人导游列表接口
    getPrivateGuideList() {
      this.loading = true;
      uni.request({
        url: `${this.baseUrl}/base/guide/list`,
        method: 'GET',
      // 修改 getPrivateGuideList 方法中的 success 逻辑
      success: (res) => {
        this.loading = false;
        if (res.statusCode === 200) {
          // 对返回的导游列表做数据格式化
          this.guides = res.data.data.map(guide => {
            let languages = guide.languages;
            // 统一处理 languages 为数组
            if (!Array.isArray(languages)) {
              // 情况1：如果是字符串（比如"中文,英文"），按分隔符转数组
              if (typeof languages === 'string' && languages) {
                languages = languages.split(/[,，·、]/); // 兼容常见分隔符
              } 
              // 情况2：null/undefined/空字符串，设为空数组
              else {
                languages = [];
              }
            }
            return {
              ...guide,
              languages // 覆盖为处理后的数组
            };
          });
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
          console.error('请求导游列表失败：', err);
        }
      });
    },
    onRegionChange(e) {
      this.regionIndex = e.detail.value;
    },
    goDetail(g) {
      uni.navigateTo({
        // 确保 guide 参数正确编码
        url: `/pages/SilverHairedModel/private-guide/detail?guide=${encodeURIComponent(JSON.stringify(g))}`
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
  height: 220rpx;
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
  .header-title{
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

.filter-bar{
  margin: 30rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,.05);
  .picker-box{
    display: flex;
    align-items: center;
    justify-content: space-between;
    .picker-label{ font-size: 30rpx; color: #5d4037; }
    .picker-val{ font-size: 32rpx; color: #ff8f00; font-weight: 500; }
    .arrow{ font-size: 24rpx; color: #999; margin-left: 10rpx; }
  }
}

.guide-scroll{
  flex: 1;
  padding: 0 30rpx 40rpx;
}
.guide-list{
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}
.guide-card{
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 8rpx 20rpx rgba(0,0,0,.06);
  transition: transform .2s;
  &:active{ transform: scale(.98); }
  .avatar-box{
    position: relative;
    margin-right: 25rpx;
    .avatar{
      width: 140rpx;
      height: 140rpx;
      border-radius: 50%;
      object-fit: cover;
    }
    .rating{
      position: absolute;
      bottom: 0;
      left: 25%;
      transform: translateX(-50%);
      background: #ff8f00;
      color: #fff;
      font-size: 22rpx;
      padding: 4rpx 12rpx;
      border-radius: 20rpx;
    }
  }
  .info-box{
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    .name{ font-size: 36rpx; color: #3e2723; font-weight: 600; }
    .region,.lang{ font-size: 28rpx; color: #795548; }
    .price{ font-size: 32rpx; color: #ff8f00; font-weight: bold; margin-top: 10rpx; }
  }
  .arrow{ font-size: 48rpx; color: #bdbdbd; }
}
</style>