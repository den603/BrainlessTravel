<script setup>
	// export default {
	// 	onLaunch: function() {
	// 		console.log('App Launch')
	// 	},
	// 	onShow: function() {
	// 		console.log('App Show')
	// 	},
	// 	onHide: function() {
	// 		console.log('App Hide')
	// 	}
	// }
	import {onLaunch} from '@dcloudio/uni-app'
	
	onLaunch(()=>{
		
		//获取右上角胶囊按钮坐标
		let menuButtonInfo =uni.getStorageSync('MenuButton')
		if(!menuButtonInfo){
			const menuButtonInfo =uni.getMenuButtonBoundingClientRect();
			console.log(menuButtonInfo)
			uni.setStorageSync('MenuButton',menuButtonInfo)
		}
		wx.cloud.init({
			env:'cloud1-2grkxba272e05118'
		})
	})
</script>

<style lang="scss">
	/* 注意要写在第一行，注意不能引入至uni.scss，同时给style标签加入lang="scss"属性 */
	@import "@/uni_modules/uview-plus/index.scss";
</style>
<style>
	/*每个页面公共css */
</style>
