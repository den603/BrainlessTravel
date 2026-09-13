<template>
	<view class="detail">
		<up-navbar title=""  bg-color="#00000000" :autoBack="true" left-icon-color="#fff"/>
		<view class="d-con">
			<image :src="resolveImage(details.dt.img)" mode="aspectFill"></image>
			<view class="d-content">
				<view class="j-con">
					<view class="tit" style="display:flex;">
						<text style="margin-right: 7rpx; font-size: 17px;">{{details.dt.title}}</text>
						<up-tag text="5A级景区" size="mini" shape="circle"></up-tag>
					</view>
					<view class="jj">
						<view style="font-size: 14px;font-weight: 700;">景区介绍</view>
						<view class="nr">{{details.dt.introduce}}</view>
					</view>
					<view class="j-con">
						<view class="jj">
							<text style="font-weight: 700;font-size: 14px;">开放时间：</text>
							<text class="nr">{{details.dt.times}}</text>
						</view>
					</view>
					<view class="j-con ls">
						<view class="tit" style="font-size: 34rpx;">游玩推荐</view>
						<view class="jj tj-list">
							<view class="item" v-for="(item,index) in projectList " :key="index"  @click="goLine(item)">
								<image :src="resolveImage(item.url)" mode="aspectFill"></image>
								<view class="topFiex">
									{{item.tag}}
								</view>
								<view class="infos">
									<view class="tit">{{item.title}}</view>
									<view class="desc">
										<up-icon name="map" color="#9c9c9c" size='16'></up-icon>
										<view class="text">{{item.description}}</view>
									</view>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import{onLoad} from '@dcloudio/uni-app'
import { ref,reactive } from 'vue'
import {detailProject} from '../../api/api.js'
// 图片地址解析：数据库只存相对路径，这里拼成完整的 MinIO 地址
import { resolveImage } from '../../api/config.js'

const details = reactive({
	dt:''
})

const projectList =ref([])

const goLine=(item)=>{
  uni.navigateTo({
    url: `/pages/line/line?id=${item.id}` 
  })
}

onLoad((opt)=>{
	// 1. 解析首页跳转传入的景区数据（含ID）
	const scenicData = JSON.parse(decodeURIComponent(opt.item));
	details.dt = scenicData;
	console.log('当前景区数据：', scenicData); // 确认id字段存在
	
	// 2. 校验并传递景区ID调用接口
	if (scenicData.id) {
		detailProject(scenicData.id) // 传入景区ID
			.then(res=>{
				// 适配后端Result格式
				projectList.value = res.data || []; 
				console.log('游玩推荐数据：', projectList.value);
			})
			.catch(err => { // 增加错误捕获
				console.error('获取游玩推荐失败：', err);
				uni.showToast({ title: '加载游玩推荐失败', icon: 'none' });
			});
	} else {
		console.error('未获取到景区ID');
		uni.showToast({ title: '景区ID缺失', icon: 'none' });
	}
})
</script>
<style lang="scss">

	.detail{
		background-color: #f5f5f5;
		.d-con{
			image{
				
				width: 100%;
				height: 700rpx;
			}
		}
		.d-content{
			width: 100%;
			height: 700rpx;
			margin-top: -40rpx;
			background-color: #fff;
			padding: 35rpx 30rpx;
			box-sizing: border-box;
			border-radius: 0 30rpx;
			position: relative;
			z-index: 9;
		}
		.j-con{
			margin-bottom: 30rpx;
			.tit{
				font-size: 36rpx;
				font-weight: 700;
				color:#111;
				margin-bottom: 30rpx;
			}
			.jj{
				.nr{
					font-size: 26rpx;
					color:#8a8a8a;
					line-height: 40rpx;
				}
			}
			.tj-list{
				display: flex;
				flex-wrap: wrap;
				justify-content: space-between;
				.item{
					position: relative;
					width: 48%;
					margin-bottom: 20rpx;
					box-shadow: 1px 2px 3px #e5e5e5;
					// border-radius: 0 20rpx;
					border-top-right-radius: 20rpx;
					border-top-left-radius: 20rpx;
					overflow: hidden;
					.topFiex{
						position: absolute;
						top:0;
						left:0;
						border-top-left-radius:20rpx;
						border-bottom-right-radius:20rpx;
						background-color:#ffaa7f;
						color: #fff;
						text-align:center;
						box-sizing:border-box;
						}
						image{
							width: 100%;
							height:200rpx;
							
						}
						.infos{
							padding: 10rpx 15rpx;
							.tit{
								font-size: 28rpx;
								font-weight: 700;
								margin-bottom: 15rpx;
								color: #111;
								text-overflow: ellipsis;
							}
							.desc{
								display: flex;
								justify-content: flex-start;
								align-items: center;
								.text{
									font-size: 26rpx;
									color: #8a8a8a;
								}
								
							}
						}
					
				}
			}
		}
	}
</style>
