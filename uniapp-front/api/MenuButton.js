//公用的获取胶囊按钮数据
export const MenuButton=function(){
	let menuButtonInfo=uni.getStorageSync('MenuButton')
	const top =menuButtonInfo.top+'px'
	const height = menuButtonInfo.height +'px'
	const left = menuButtonInfo.left+'px'
	const right = menuButtonInfo.right +'px'
	const width = menuButtonInfo.width +'px'
	const seViewHeight =menuButtonInfo.top+menuButtonInfo.height+'px'
	return{
		top,
		height,
		left,
		right,
		seViewHeight,
		width
	}
}