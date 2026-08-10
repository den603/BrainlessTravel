<template>
  <view class="wrap">
    <!-- 顶部插画区域 -->
    <view class="header-pic">
      <view class="mountain"></view>
      <view class="sun"></view>
      <view class="cloud a"></view>
      <view class="cloud b"></view>
    </view>

    <!-- 标题 -->
    <view class="title-area">
      <text class="title">老年旅游服务</text>
      <text class="subtitle">安心 · 省心 · 开心</text>
    </view>

    <!-- 两个大按钮 + 新增「我的预约」按钮 -->
    <view class="btn-box">
      <button class="func-btn primary" @click="goToGroupTravel">
        <text class="btn-txt">👫老年人组团旅游</text>
        <text class="btn-desc">同龄结伴，同段限选1个活动</text>
      </button>

      <button class="func-btn secondary" @click="goToPrivateGuide">
        <text class="btn-txt">🧑‍🏫私人导游服务</text>
        <text class="btn-desc">一对一讲解，按地域筛选</text>
      </button>

      <!-- 新增：我的预约按钮 -->
      <button class="func-btn tertiary" @click="goToMyBookings">
        <text class="btn-txt">📅我的预约</text>
        <text class="btn-desc">查看/管理我的旅游预约</text>
      </button>
    </view>

    <!-- 底部提示 -->
    <view class="tips">
      <text class="tips-title">温馨提示</text>
      <text class="tips-item">• 参加团游请提前 3 天预约</text>
      <text class="tips-item">• 私人导游需提前 1 周预约</text>
      <text class="tips-item">• 出行前请带好身份证</text>
    </view>
  </view>
</template>

<script>
export default { 
  methods: {
    goToGroupTravel() {
      uni.navigateTo({ url: '/pages/SilverHairedModel/group-travel/list' })
    },
    goToPrivateGuide() {
      uni.navigateTo({ url: '/pages/SilverHairedModel/private-guide/list' })
    },
    // 新增：跳转到我的预约页面
    goToMyBookings() {
      uni.navigateTo({ url: '/pages/SilverHairedModel/my-bookings/my-bookings' })
    }
  }
}
</script>

<style lang="less">
/* 变量：银发友好配色 */
@primary: #ff8f00;      /* 温暖橙 */
@secondary: #43a047;    /* 柔和绿 */
@tertiary: #2196f3;     /* 新增：柔和蓝（适配银发视觉） */
@grey: #f5f5f5;
@text: #3e2723;

.wrap{
  background: @grey;
  min-height: 100vh;
  font-family: -apple-system,"Helvetica Neue",Arial;
  padding-bottom: 40rpx;
}

/* ---- 顶部插画 ---- */
.header-pic{
  position: relative;
  height: 280rpx;
  background: linear-gradient(180deg, #fff9c4 0%, #ffe0b2 100%);
  overflow: hidden;
  border-radius: 0 0 40% 40%/0 0 20% 20%;
}
.sun{
  position: absolute;
  right: 60rpx;
  top: 40rpx;
  width: 100rpx;
  height: 100rpx;
  background: #ffd54f;
  border-radius: 50%;
  box-shadow: 0 0 0 20rpx rgba(255,213,79,.3);
}
.mountain{
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 120rpx;
  background: #a5d6a7;
  clip-path: polygon(0 100%, 0 40%, 30% 60%, 60% 20%, 100% 50%, 100% 100%);
}
.cloud{
  position: absolute;
  background: #fff;
  border-radius: 100rpx;
  opacity: .8;
  &.a{ width: 120rpx; height: 40rpx; top: 70rpx; left: 10%; animation: drift 18s linear infinite; }
  &.b{ width: 80rpx;  height: 30rpx; top: 110rpx; left: 60%; animation: drift 22s linear infinite reverse; }
}
@keyframes drift{
  from{ transform: translateX(-150rpx); }
  to{ transform: translateX(750rpx); }
}

/* ---- 标题 ---- */
.title-area{
  text-align: center;
  padding: 50rpx 0 40rpx;
  .title{ font-size: 48rpx; color: @text; font-weight: 600; }
  .subtitle{ display: block; font-size: 28rpx; color: #795548; margin-top: 10rpx; }
}

/* ---- 大按钮 ---- */
.btn-box{
  padding: 0 40rpx;
  display: flex;
  flex-direction: column;
  gap: 40rpx;
}
.func-btn{
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200rpx;
  border-radius: 24rpx;
  border: none;
  color: #fff;
  box-shadow: 0 8rpx 20rpx rgba(0,0,0,.1);
  transition: transform .2s;
  &:active{ transform: scale(.97); }

  &.primary{ background: linear-gradient(135deg, @primary 0%, #ffb74d 100%); }
  &.secondary{ background: linear-gradient(135deg, @secondary 0%, #66bb6a 100%); }
  /* 新增：我的预约按钮样式 */
  &.tertiary{ background: linear-gradient(135deg, @tertiary 0%, #64b5f6 100%); }

  .btn-icon{ font-size: 60rpx; margin-bottom: 5rpx; }
  .btn-txt{ font-size: 36rpx; font-weight: 500; }
  .btn-desc{ font-size: 24rpx; opacity: .9; margin-top: 6rpx; }
}

/* ---- 底部提示 ---- */
.tips{
  margin: 60rpx 40rpx 0;
  background: #e8f5e9;
  border-radius: 16rpx;
  padding: 30rpx;
  .tips-title{ display: block; font-size: 30rpx; color: @secondary; font-weight: 500; margin-bottom: 15rpx; }
  .tips-item{ display: block; font-size: 26rpx; color: #558b2f; line-height: 1.8; }
}
</style>