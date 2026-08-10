<template>
	<view class="container">
		<view class="icon-container card1" @click="goToFindTravelMate">
			<image 
				src="/static/tabbar/团建活动.png" 
				mode="widthFix"
				class="mode-icon"
			></image>
			<view class="card-title">结伴出行</view>
		</view>
		
		<view class="divider"></view>
		
		<view class="icon-container card2" @click="goToSilverHairedModel">
			<image 
				src="/static/tabbar/都市银发.png" 
				mode="widthFix" 
				class="mode-icon"
			></image>
			<view class="card-title">银发模式</view>
		</view>
	</view>
</template>

<script setup>
	const goToFindTravelMate = () => {
		uni.navigateTo({
			url: '/pages/FindTravelMate/FindTravelMate' 
		})
	}
	
	const goToSilverHairedModel = () => {
		uni.navigateTo({
			url: '/pages/SilverHairedModel/SilverHairedModel' 
		})
	}
</script>

<style lang="scss">
	page {
		background-color: #f7f8fa;
	}
	view, image {
		margin: 0;
		padding: 0;
		box-sizing: border-box;
	}

	.container {
		width: 100vw;
		min-height: 100vh;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		padding: 40rpx 30rpx;
		background: linear-gradient(135deg, #f7f8fa 0%, #eef2f5 100%);
		gap: 30rpx;
	}

	.icon-container {
		width: 100%;
		max-width: 680rpx;
		height: 320rpx;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		border-radius: 36rpx;
		overflow: hidden;
		position: relative;
		cursor: pointer;
		transition: all 0.3s ease;
		box-shadow: 0 12rpx 30rpx rgba(0, 0, 0, 0.08);
		
		.mode-icon {
			width: 180rpx;
			height: 180rpx;
			object-fit: contain;
			margin-bottom: 16rpx;
		}
		
		.card-title {
			font-size: 32rpx;
			font-weight: 500;
			color: #333;
		}
	}

	.icon-container:active {
		transform: scale(0.96);
		box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.12);
	}

	.card1 {
		background: linear-gradient(135deg, #ffffff 0%, #f0f8ff 100%);
		border: 2rpx solid rgba(100, 149, 237, 0.15);
	}

	.card2 {
		background: linear-gradient(135deg, #ffffff 0%, #f5f0ff 100%);
		border: 2rpx solid rgba(159, 122, 234, 0.15);
	}

	.divider {
		width: 60vw;
		height: 2rpx;
		background-color: rgba(0, 0, 0, 0.05);
		border-radius: 1rpx;
		margin: 10rpx 0;
	}
</style>