export default {
  data() {
    return {
      travelers: [], // 出行人列表
      selectedTravelers: [], // 选中的出行人
      travelerDialog: false, // 选择出行人弹窗
      newTraveler: { // 新增出行人表单
        name: '',
        age: '',
        phone: '',
        idCard: ''
      },
      addTravelerDialog: false // 新增出行人弹窗
    }
  },
  onLoad() {
    // 加载本地存储的出行人
    this.travelers = uni.getStorageSync('travelers') || []
  },
  methods: {
    // 打开选择出行人弹窗
    openTravelerDialog() {
      this.travelerDialog = true
      // 重置选中状态
      this.selectedTravelers = []
    },
    // 打开新增出行人弹窗
    openAddTravelerDialog() {
      this.addTravelerDialog = true
      this.newTraveler = { name: '', age: '', phone: '', idCard: '' }
    },
    // 新增出行人
    addTraveler() {
      if (!this.newTraveler.name || !this.newTraveler.phone) {
        uni.showToast({ title: '姓名和手机号不能为空', icon: 'none' })
        return
      }
      const newTraveler = {
        id: Date.now(), // 临时ID，后端对接后替换为数据库ID
        ...this.newTraveler
      }
      this.travelers.push(newTraveler)
      uni.setStorageSync('travelers', this.travelers)
      this.addTravelerDialog = false
      uni.showToast({ title: '新增出行人成功', icon: 'success' })
    },
    // 选择/取消出行人
    toggleTraveler(traveler) {
      const index = this.selectedTravelers.findIndex(t => t.id === traveler.id)
      if (index > -1) {
        this.selectedTravelers.splice(index, 1)
      } else {
        this.selectedTravelers.push(traveler)
      }
    },
    // 删除出行人
    deleteTraveler(id) {
      uni.showModal({
        title: '提示',
        content: '确定删除该出行人吗？',
        success: (res) => {
          if (res.confirm) {
            this.travelers = this.travelers.filter(t => t.id !== id)
            uni.setStorageSync('travelers', this.travelers)
            uni.showToast({ title: '删除成功', icon: 'success' })
          }
        }
      })
    }
  }
}