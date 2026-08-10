<template>
  <view class="content">
    <!-- 搜索框 -->
    <up-search placeholder="搜索景点" bg-color="#e3e3e3" v-model="keyword"></up-search>
    
    <!-- 轮播图 -->
    <up-swiper 
      v-if="bannerList.length"
      :list="bannerList"
      keyName="image"
      showTitle
      radius="8" 
      :autoplay="true" 
      height="160"
    ></up-swiper>
    
    <!-- 通知栏 -->
    <up-notice-bar text="项目数据仅为示例,非真实数据"> </up-notice-bar>
    
    <!-- 瀑布流列表 -->
    <view class="list">
      <up-waterfall v-model="flowList" ref="uWaterfallRef">
        <!-- 左侧列 -->
        <template v-slot:left="{leftList}">
          <view class="demo-water" v-for="(item,index) in leftList":key="index" @click="goDetail(item)">
            <up-lazy-load threshold="-450" border-radius="10" :image="item.img" :index="index"></up-lazy-load>
            <view class="demo-title">
              {{item.title}}
            </view>
            <view class="demo-prie">
              {{item.times}}
            </view>
            <view class="demo-tag">
              <view class="demo-tag-owner">
                <!-- {{item.tag[0] || ''}} -->
				推荐
              </view>
              <view class="demo-tag-text">
               <!-- {{item.tag[1] || ''}} -->
			   名胜古迹
              </view>
            </view>
            <view class="isDot" v-if="item.isDot">
              {{ item.isDot }}
            </view>
          </view>
        </template>
        
        <!-- 右侧列 -->
        <template v-slot:right="{rightList}">
          <view class="demo-water" v-for="(item,index) in rightList":key="index" @click="goDetail(item)">
            <up-lazy-load threshold="-450" border-radius="10" :image="item.img" :index="index"></up-lazy-load>
            <view class="demo-title">
              {{item.title}}
            </view>
            <view class="demo-prie">
              {{item.times}}
            </view>
            <view class="demo-tag">
              <view class="demo-tag-owner">
                <!-- {{item.tag[0] || ''}} -->
				推荐
              </view>
              <view class="demo-tag-text">
                <!-- {{item.tag[1] || ''}} -->
				名声古迹
              </view>
            </view>
            
            <view class="isDot" v-if="item.isDot">
              {{ item.isDot }}
            </view>
          </view>
        </template>
      </up-waterfall>
    </view>
    
   <!-- ========== 所有悬浮按钮统一放到页面最顶层，不受列表渲染影响 ========== -->
       <view class="float-btn-group">
         <!-- 添加景点按钮 -->
         <view @click="goAddScenic" class="add-scenic-btn">
           <up-icon name="plus" color="#fff" size="32"></up-icon>
         </view>
         <!-- 返回顶部 -->
         <view v-if="showTopBtn" @click="Totop" class="topClass">
           <up-icon name="arrow-upward" color="#fff" size="28"></up-icon>
         </view>
         <!-- 自定义悬浮组件 -->
         <!-- <DhFloatBtn /> -->
	    </view>
  </view>
</template>

<script setup>
  // 导入接口请求方法
  import { getBanner, getHomeList } from '../../api/api.js';
  // 导入uni-app生命周期和页面事件
  import { onLoad, onReachBottom, onPageScroll } from '@dcloudio/uni-app';
  // 导入vue响应式API
  import { ref } from 'vue';
 // ====== 新增：导入悬浮窗组件 ======
  // import DhFloatBtn from '../../components/DhFloatBtn.vue';

  // 搜索关键词
  const keyword = ref('');
  // 轮播图数据
  const bannerList = ref([]);
  // 瀑布流数据
  const flowList = ref([]);
  // 是否显示返回顶部按钮
  const showTopBtn = ref(0);

  /**
   * 页面加载时请求数据
   */
  onLoad(() => {
     // 请求轮播图数据（不变）
     getBanner().then(res => {
       console.log('轮播图数据：', res);
       bannerList.value = res.bannerList || [];
     }).catch(err => {
       console.error('获取轮播图失败：', err);
       bannerList.value = [];
       uni.showToast({ title: '轮播图加载失败', icon: 'none' });
     });
   
     // 请求瀑布流（景点）数据（修复核心错误）
     getHomeList().then(res => {
       console.log('景点列表数据：', res);
       // 关键修改：先取res.data（真正的数组），再兜底空数组
       const listData = res?.data || []; 
       // 格式化数据：处理tag空值、统一isDot字段名
       flowList.value = listData.map(item => ({
         ...item,
         tag: item.tag || [], // 把null/undefined转为空数组
         isDot: item.is_dot || item.isDot || '' // 统一字段名，兜底空值
       }));
     }).catch(err => {
       console.error('获取景点列表失败：', err);
       flowList.value = [];
       uni.showToast({ title: '景点列表加载失败', icon: 'none' });
     });
   });

  /**
   * 页面触底加载更多（模拟）
   */
  onReachBottom(() => {
    console.log('页面触底，加载更多');
    // 模拟接口请求延迟
    setTimeout(() => {
      addRandomData();
      uni.showToast({ title: '已加载更多', icon: 'none', duration: 800 });
    }, 1000);
  });

  /**
   * 页面滚动监听（控制返回顶部按钮显示）
   */
  onPageScroll((e) => {
    showTopBtn.value = e.scrollTop > 600 ? 1 : 0;
  });
  
 /**
   * 跳转到添加景点页
   */
  const goAddScenic = () => {
    // 简单校验：未登录提示登录（因为 /api/upload/scenic 和 /api/scenic/add 都走拦截器）
    const token = uni.getStorageSync('token');
    if (!token) {
      uni.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    uni.navigateTo({
      url: '/pages/addScenic/addScenic'
    });
  };
  /**
   * 跳转到详情页
   * @param {Object} item 景点数据
   */
  const goDetail = (item) => {
    try {
      const cam = JSON.stringify(item);
      uni.navigateTo({
        url: `/pages/detail/detail?item=${encodeURIComponent(cam)}`
      });
    } catch (err) {
      console.error('跳转详情页失败：', err);
      uni.showToast({ title: '跳转失败', icon: 'none' });
    }
  };

  /**
   * 返回顶部
   */
  const Totop = () => {
    uni.pageScrollTo({
      scrollTop: 0,
      duration: 300
    });
  };

  /**
   * 模拟加载更多数据（复制现有数据并修改ID）
   */
  const addRandomData = () => {
    // 生成随机整数工具函数
    const getRandom = (min, max) => {
      return Math.floor(Math.random() * (max - min + 1)) + min;
    };
    // 生成唯一ID工具函数
    const generateGuid = () => {
      return Date.now().toString(36) + Math.random().toString(36).substr(2, 5);
    };

    // 无数据时不加载
    if (flowList.value.length === 0) return;

    // 模拟新增10条数据
    for (let i = 1; i < 10; i++) {
      let index = getRandom(0, flowList.value.length - 1);
      let item = JSON.parse(JSON.stringify(flowList.value[index]));
      item.id = generateGuid(); // 生成新ID
      flowList.value.push(item);
    }
  };
  
</script>

<style>
  page {
    background-color: rgb(240, 240, 240);
  }
</style>

<style lang="scss" scoped>
 page {
   background-color: rgb(240, 240, 240);
 }
 
 /* 最外层根容器 */
 .page-root {
   position: relative;
   width: 100%;
   min-height: 100vh;
 }
 
 .content {
   padding: 20rpx 20rpx;
 }
 
 .list {
   margin: 30rpx 0rpx;
 
   .demo-water {
     margin: 10rpx 10rpx 10rpx 0;
     background-color: #fff;
     border-radius: 16rpx;
     padding: 16rpx;
     position: relative;
   }
 
   .demo-title {
     font-size: 30rpx;
     margin-top: 10rpx;
     color: #303133;
   }
 
   .demo-prie {
     font-size: 24rpx;
     color: #777;
     margin: 10rpx;
   }
 
   .demo-tag {
     display: flex;
     margin-top: 10rpx;
     gap: 10rpx;
 
     .demo-tag-owner {
       border: 1px solid orange;
       color: #ffaa00;
       font-size: 20rpx;
       display: flex;
       align-items: center;
       padding: 4rpx 14rpx;
       border-radius: 50rpx;
     }
 
     .demo-tag-text {
       border: 1px solid #0af;
       color: #0af;
       border-radius: 50rpx;
       padding: 4rpx 14rpx;
       display: flex;
       align-items: center;
     }
   }
 
   .isDot {
     position: absolute;
     top: 20rpx;
     right: 20rpx;
     font-size: 24rpx;
     color: #fff;
     background-color: #f00;
     text-align: center;
     border-radius: 10rpx;
     padding: 4rpx 10rpx;
   }
 }
 
 /* 悬浮按钮统一容器，全局最高层级 */
 .float-btn-group {
   position: fixed;
   right: 30rpx;
   bottom: 0;
   width: 100rpx;
   height: 100vh;
   pointer-events: none; /* 容器不拦截页面点击，只按钮可点击 */
   z-index: 9999; /* 极高层级，永远不会被列表遮挡 */
   display: flex;
   flex-direction: column-reverse;
   align-items: center;
   gap: 30rpx;
   padding-bottom: 100rpx;
   box-sizing: border-box;
 }
 
 /* 返回顶部按钮 */
 .topClass {
   pointer-events: auto;
   background-color: rgba(0, 0, 0, 0.5);
   padding: 20rpx;
   width: 44rpx;
   border-radius: 40rpx;
   display: flex;
   justify-content: center;
   align-items: center;
 }
 
 /* 添加景点按钮（优化样式+层级） */
 .add-scenic-btn {
   pointer-events: auto;
   background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
   width: 80rpx;
   height: 80rpx;
   border-radius: 50%;
   display: flex;
   justify-content: center;
   align-items: center;
   box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.4);
   /* 增加动画，刷新/滚动不会消失 */
   transition: all 0.2s ease;
 }
 .add-scenic-btn:active {
   transform: scale(0.92);
 }

</style>


