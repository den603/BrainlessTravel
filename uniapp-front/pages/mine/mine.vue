<template>
	<view class="content">
		<view class="topBox">
			<view class="setbox">
				<view class="set-left">
					<uni-icons type="calendar" size="30" color="#fff"></uni-icons>
					<view class="txt">签到</view>
				</view>
				<view class="set-right">
					<uni-icons type="gear" size="30" color="#fff"></uni-icons>
					<uni-icons type="chat" size="30" color="#fff"></uni-icons>
				</view>
			</view>
			<view class="users" @click="setFun">
				<view class="u-top">
					<template v-if="!usersInfo.nickName">
						<image src="../../static/tabbar/KHCFDC_头像 .png" mode="aspectFill"></image>
						<view class="tit">注册/登陆</view>
					</template>
					<template v-else>
						<image :src="usersInfo.avatarUrl" mode="aspectFill"></image>
						<view class="tit">{{usersInfo.nickName}}</view>
					</template>
				</view>
				<view class="u-bottom">
					<view class="u-item">
						<view class="num">12</view>
						<view class="u-tit">点赞</view>
					</view>
					<view class="u-item">
						<view class="num">12</view>
						<view class="u-tit">喜欢</view>
					</view>
					<view class="u-item">
						<view class="num">12</view>
						<view class="u-tit">浏览</view>
					</view>
					<view class="u-item">
						<view class="num">12</view>
						<view class="u-tit">收藏</view>
					</view>
				</view>
			</view>
		</view>
		<view class="listBox">
			<view class="lists">
				<uni-list>
					<uni-list-item :show-extra-icon="true" :extra-icon="extraIcon1" showArrow title="个人信息" clickable></uni-list-item>
					<uni-list-item :show-extra-icon="true" :extra-icon="extraIcon2" showArrow title="我的购物车" clickable></uni-list-item>
					<uni-list-item :show-extra-icon="true" :extra-icon="extraIcon3" showArrow title="用户反馈" clickable></uni-list-item>
					<uni-list-item :show-extra-icon="true" :extra-icon="extraIcon4" showArrow title="我的邮件" clickable></uni-list-item>
					<uni-list-item :show-extra-icon="true" :extra-icon="extraIcon5" showArrow title="分享有礼" clickable></uni-list-item>
				</uni-list>
			</view>
		</view>
		<up-popup closeable :show="show" @close="close" round="20">
			<view class="popup">
				<view class="title">获取您的昵称、头像</view>
				<view class="flex">
					<view class="label">获取用户头像：</view>
					<button class="avatar-warpper" open-type="chooseAvatar" @chooseavatar="onChooseavatar">
						<image class="avater" :src="usersInfo.avatarUrl || '../../static/tt.jpg'"></image>
					</button>
				</view>
				<view class="flex">
					<view class="label">获取用户昵称：</view>
					<input @input="changeName" type="nickname" :value="usersInfo.nickName" placeholder="请输入昵称" />
				</view>
				<button size="default" type="primary" @click="userSumbit">确定</button>
			</view>
		</up-popup>
	</view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { login, getUserInfo, updateUserInfo } from '../../api/api';

// 初始化用户信息
const usersInfo = reactive({
	nickName: '', // 名字
	avatarUrl: ''
})

// 列表项图标配置（修正原拼写错误 ayth -> user）
const extraIcon1 = reactive({ color: '#666666', size: '22', type: 'user' })
const extraIcon2 = reactive({ color: '#666666', size: '22', type: 'cart' })
const extraIcon3 = reactive({ color: '#666666', size: '22', type: 'chatboxes' })
const extraIcon4 = reactive({ color: '#666666', size: '22', type: 'email' })
const extraIcon5 = reactive({ color: '#666666', size: '22', type: 'gift' })

// 弹窗控制
const show = ref(false)
const close = () => { show.value = false }

// 选择头像回调
const onChooseavatar = (e) => {
	usersInfo.avatarUrl = e.detail.avatarUrl
}

// 输入昵称回调
const changeName = (e) => {
	usersInfo.nickName = e.detail.value
}

// 提交用户信息
const userSumbit = async () => {
	if (!usersInfo.avatarUrl || !usersInfo.nickName) {
		uni.showToast({ title: '请完善头像和昵称', icon: 'none' })
		return
	}
	// 调用后端更新接口
	const res = await updateUserInfo(usersInfo)
	if (res.code === 1) {
		uni.setStorageSync('usersInfo', JSON.stringify(usersInfo))
		show.value = false;
		uni.showToast({ title: '保存成功', icon: 'success' })
	} else {
		uni.showToast({ title: res.msg || '保存失败', icon: 'none' })
	}
}
// 登录/授权逻辑
const setFun = () => {
	uni.showModal({
		title: '温馨提示',
		content: '亲，授权微信登陆后才能正常使用小程序',
		success: async (res) => {
			if (res.confirm) {
				uni.login({
					success: async (data) => {
						// 调用登录接口获取token
						const res = await login(data.code)
						if (res.code === 1) {
							uni.setStorageSync('token', res.data)
							// 根据token获取用户信息
							const userRes = await getUserInfo()
							if (userRes.code === 1) {
								usersInfo.avatarUrl = userRes.data.avatarUrl
								usersInfo.nickName = userRes.data.nickName
								show.value = true
							}
						} else {
							uni.showToast({ title: res.msg, icon: 'none' })
						}
					},
					fail: () => {
						uni.showToast({ title: '登录失败，请重试', icon: 'none' })
					}
				})
			}
		}
	})
}

// 页面加载时初始化用户信息
onLoad(async () => {
	const token = uni.getStorageSync('token')
	const localUser = uni.getStorageSync('usersInfo')
	if (token && !localUser) {
		const userRes = await getUserInfo()
		if (userRes.code === 1) {
			usersInfo.avatarUrl = userRes.data.avatarUrl
			usersInfo.nickName = userRes.data.nickName
		}
	} else if (token && localUser) {
		const { avatarUrl, nickName } = JSON.parse(localUser)
		usersInfo.avatarUrl = avatarUrl
		usersInfo.nickName = nickName
	}
})
</script>

<script>
export default {
	data() { return {}; }
}
</script>

<style lang="scss" scoped>
.content {
	height: 100vh;
	background-color: #f5f5f5;

	.topBox {
		width: 100%;
		position: relative;
		z-index: 1;
		overflow: hidden;
		padding: 40rpx 20rpx 0rpx;
		box-sizing: border-box;

		&::after {
			content: "";
			width: 140%;
			height: 200px;
			position: absolute;
			z-index: -1;
			top: 0;
			left: -20%;
			background-color: #0af;
			border-radius: 0 0 50% 50%;
		}

		.setbox {
			display: flex;
			justify-content: space-between;
			align-items: center;

			.set-left {
				width: 18%;
				display: flex;
				justify-content: space-between;
				align-items: center;
			}

			.txt {
				color: #fff;
				font-size: 30rpx;
			}
		}

		.users {
			margin-top: 35rpx;
			padding: 30rpx;
			box-sizing: border-box;
			height: 300rpx;
			background-color: #fff;
			box-shadow: 1px 10rpx 20rpx #ececec;
			border-radius: 16rpx;

			.u-top {
				display: flex;
				align-items: center;
				justify-content: flex-start;
				margin-bottom: 30rpx;

				image {
					width: 100rpx;
					height: 100rpx;
					border-radius: 50%;
					margin-right: 20rpx;
				}

				.tit {
					font-size: 30rpx;
					font-weight: 700;
					color: #333;
				}
			}

			.u-bottom {
				display: flex;
				justify-content: space-around;
				align-items: center;

				.u-item {
					text-align: center;

					.u-tit {
						color: #757575;
						font-size: 26rpx;
						margin-top: 10rpx;
					}

					.num {
						color: #000;
						font-weight: 700;
						font-size: 33rpx;
					}
				}
			}
		}
	}

	.popup {
		padding: 20rpx;
		border-radius: 20rpx 20rpx 0 0;

		.title {
			margin-bottom: 20rpx;
			font-size: 40rpx;
			text-align: center;
		}

		.flex {
			display: flex;
			justify-content: flex-start;
			align-items: center;
			border-bottom: 1px solid #f5f5f5;
			padding: 24rpx 0;
		}

		image {
			width: 70rpx;
			height: 70rpx;
		}

		.avatar-warpper {
			border: none;
			border-radius: 10rpx;
			width: 70rpx;
			height: 70rpx;
			margin-left: 20rpx;
			padding: 0;
		}

		.label {
			font-size: 28rpx;
			color: #333;
			width: 180rpx;
		}

		input {
			flex: 1;
			font-size: 28rpx;
			padding: 10rpx 0;
		}

		button {
			margin-top: 40rpx;
			height: 80rpx;
			line-height: 80rpx;
			font-size: 30rpx;
		}
	}

	.listBox {
		height: 200rpx;
		margin: -10rpx auto 0;
		padding: 20rpx;
		box-sizing: border-box;
		border-radius: 12rpx;

		.lists {
			background-color: #fff;
			border-radius: 12rpx;
			box-shadow: 0 2rpx 10rpx #eee;
		}
	}
}
</style>