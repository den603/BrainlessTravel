<template>
  <view class="qa-container">
    <!-- 1. 参数选择页面 -->
    <view v-if="currentStep === 'select'" class="step-select">
      <view class="title">旅游景点知识问答</view>
      
      <!-- 景点名称输入 -->
      <view class="form-item">
        <view class="label">景点名称</view>
        <input class="input" v-model="params.scenicName" placeholder="请输入景点名称（如：故宫）" />
      </view>

      <!-- 题数选择 -->
      <view class="form-item">
        <view class="label">题目数量</view>
        <view class="options">
          <view 
            v-for="count in [5, 10, 15, 20]" 
            :key="count"
            class="option"
            :class="{ active: params.questionCount === count }"
            @click="params.questionCount = count"
          >
            {{ count }}题
          </view>
        </view>
      </view>

      <!-- 选项数选择 -->
      <view class="form-item">
        <view class="label">选项数量</view>
        <view class="options">
          <view 
            v-for="count in [2, 3, 4]" 
            :key="count"
            class="option"
            :class="{ active: params.optionCount === count }"
            @click="params.optionCount = count"
          >
            {{ count }}个
          </view>
        </view>
      </view>

      <!-- 难度选择 -->
      <view class="form-item">
        <view class="label">难度</view>
        <view class="options">
          <view 
            v-for="diff in ['简单', '中等', '困难', '噩梦']" 
            :key="diff"
            class="option"
            :class="{ active: params.difficulty === diff }"
            @click="params.difficulty = diff"
          >
            {{ diff }}
          </view>
        </view>
      </view>

      <!-- 生成按钮 -->
      <button class="btn-generate" :disabled="loading" @click="generateQuestions">
        {{ loading ? '生成中...' : '开始答题' }}
      </button>
    </view>

    <!-- 2. 答题页面 -->
    <view v-if="currentStep === 'answer'" class="step-answer">
      <!-- 进度条 -->
      <view class="progress-bar">
        <view class="progress-text">第 {{ currentIndex + 1 }}/{{ questions.length }} 题</view>
        <view class="progress-track">
          <view class="progress-fill" :style="{ width: ((currentIndex + 1) / questions.length * 100) + '%' }"></view>
        </view>
      </view>

      <!-- 题目内容 -->
      <view class="question-card">
        <view class="question-content">{{ currentQuestion.content }}</view>
        
        <!-- 选项列表 -->
        <view class="answer-options">
          <view 
            v-for="(option, index) in currentQuestion.options" 
            :key="index"
            class="answer-option"
            :class="{ selected: userAnswers[currentQuestion.questionId] === option }"
            @click="selectAnswer(option)"
          >
            <view class="option-letter">{{ String.fromCharCode(65 + index) }}</view>
            <view class="option-text">{{ option }}</view>
          </view>
        </view>
      </view>

      <!-- 下一题/提交按钮 -->
      <button 
        class="btn-next" 
        :disabled="!userAnswers[currentQuestion.questionId]"
        @click="nextQuestion"
      >
        {{ currentIndex === questions.length - 1 ? '提交答案' : '下一题' }}
      </button>
    </view>

    <!-- 3. 结果页面 -->
    <view v-if="currentStep === 'result'" class="step-result">
      <view class="result-card">
        <view class="result-score">{{ result.score }}<text class="score-unit">分</text></view>
        <view class="result-name">{{ result.evaluateName }}</view>
        <view class="result-desc">{{ result.evaluateDesc }}</view>
        
        <view class="result-info">
          <view class="info-item">
            <text class="info-label">测评景点</text>
            <text class="info-value">{{ result.scenicName }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">答题人</text>
            <text class="info-value">{{ result.answerer }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">答题时间</text>
            <text class="info-value">{{ result.answerTime }}</text>
          </view>
        </view>
      </view>

      <button class="btn-restart" @click="restart">再玩一次</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { request } from '@/api/question.js' 

// 当前步骤：select-参数选择，answer-答题，result-结果
const currentStep = ref('select')
const loading = ref(false)

// 参数选择
const params = ref({
  scenicName: '',
  questionCount: 10,
  optionCount: 4,
  difficulty: '中等'
})

// 题目列表
const questions = ref([])
const currentIndex = ref(0)
const userAnswers = ref({}) // 存储用户答案：{ questionId: userAnswer }

// 结果数据
const result = ref({})

// 当前题目
const currentQuestion = computed(() => questions.value[currentIndex.value] || {})

// 1. 生成题目
const generateQuestions = async () => {
  if (!params.value.scenicName) {
    uni.showToast({ title: '请输入景点名称', icon: 'none' })
    return
  }

  loading.value = true
  try {
    // 调用后端生成题目接口
    const res = await request({
      url: '/qa/generate',
      method: 'POST',
      data: params.value
    })

    if (res.code === 1) {
      questions.value = res.data
      currentIndex.value = 0
      userAnswers.value = {}
      currentStep.value = 'answer'
    } else {
      uni.showToast({ title: res.msg || '生成题目失败', icon: 'none' })
    }
  } catch (error) {
    uni.showToast({ title: '网络错误，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 2. 选择答案
const selectAnswer = (option) => {
  userAnswers.value[currentQuestion.value.questionId] = option
}

// 3. 下一题/提交
const nextQuestion = async () => {
  if (currentIndex.value < questions.value.length - 1) {
    // 下一题
    currentIndex.value++
  } else {
    // 提交答案
    await submitAnswers()
  }
}

// 4. 提交答案
const submitAnswers = async () => {
  loading.value = true
  try {
    // 构造提交数据
    const submitData = {
      scenicName: params.value.scenicName,
      answers: questions.value.map(q => ({
        questionId: q.questionId,
        userAnswer: userAnswers.value[q.questionId] || ''
      }))
    }

    // 调用后端提交接口
    const res = await request({
      url: '/qa/submit',
      method: 'POST',
      data: submitData
    })

    if (res.code === 1) {
      result.value = res.data
      currentStep.value = 'result'
    } else {
      uni.showToast({ title: res.msg || '提交失败', icon: 'none' })
    }
  } catch (error) {
    uni.showToast({ title: '网络错误，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 5. 重新开始
const restart = () => {
  currentStep.value = 'select'
  params.value = {
    scenicName: '',
    questionCount: 10,
    optionCount: 4,
    difficulty: '中等'
  }
  questions.value = []
  currentIndex.value = 0
  userAnswers.value = {}
  result.value = {}
}
</script>

<style scoped>
.qa-container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
  box-sizing: border-box;
}

/* 1. 参数选择页面 */
.step-select {
  max-width: 600px;
  margin: 0 auto;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 40px;
}

.form-item {
  margin-bottom: 30px;
}

.label {
  font-size: 16px;
  color: #333;
  margin-bottom: 12px;
  font-weight: 500;
}

.input {
  width: 100%;
  height: 48px;
  background-color: #fff;
  border-radius: 8px;
  padding: 0 16px;
  font-size: 15px;
  color: #333;
  box-sizing: border-box;
}

.options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.option {
  flex: 1;
  min-width: 80px;
  height: 44px;
  background-color: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  color: #666;
  border: 1px solid #e0e0e0;
  transition: all 0.3s;
}

.option.active {
  background-color: #409eff;
  color: #fff;
  border-color: #409eff;
}

.btn-generate {
  width: 100%;
  height: 52px;
  background-color: #409eff;
  color: #fff;
  border-radius: 26px;
  font-size: 17px;
  font-weight: bold;
  margin-top: 20px;
  border: none;
}

.btn-generate:disabled {
  background-color: #a0cfff;
}

/* 2. 答题页面 */
.step-answer {
  max-width: 600px;
  margin: 0 auto;
}

.progress-bar {
  margin-bottom: 30px;
}

.progress-text {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  text-align: right;
}

.progress-track {
  width: 100%;
  height: 6px;
  background-color: #e0e0e0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #409eff;
  border-radius: 3px;
  transition: width 0.3s;
}

.question-card {
  background-color: #fff;
  border-radius: 16px;
  padding: 30px 24px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.question-content {
  font-size: 18px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 30px;
  font-weight: 500;
}

.answer-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.answer-option {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 12px;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.answer-option.selected {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.option-letter {
  width: 32px;
  height: 32px;
  background-color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  color: #666;
  margin-right: 16px;
  font-weight: bold;
}

.answer-option.selected .option-letter {
  background-color: #409eff;
  color: #fff;
}

.option-text {
  flex: 1;
  font-size: 15px;
  color: #333;
  line-height: 1.5;
}

.btn-next {
  width: 100%;
  height: 52px;
  background-color: #409eff;
  color: #fff;
  border-radius: 26px;
  font-size: 17px;
  font-weight: bold;
  border: none;
}

.btn-next:disabled {
  background-color: #a0cfff;
}

/* 3. 结果页面 */
.step-result {
  max-width: 600px;
  margin: 0 auto;
}

.result-card {
  background-color: #fff;
  border-radius: 20px;
  padding: 40px 30px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  text-align: center;
}

.result-score {
  font-size: 64px;
  font-weight: bold;
  color: #409eff;
  line-height: 1;
  margin-bottom: 12px;
}

.score-unit {
  font-size: 24px;
  color: #999;
  margin-left: 4px;
}

.result-name {
  font-size: 22px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.result-desc {
  font-size: 15px;
  color: #666;
  line-height: 1.8;
  margin-bottom: 30px;
  padding: 0 20px;
}

.result-info {
  border-top: 1px solid #f0f0f0;
  padding-top: 24px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.info-label {
  font-size: 14px;
  color: #999;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.btn-restart {
  width: 100%;
  height: 52px;
  background-color: #fff;
  color: #409eff;
  border-radius: 26px;
  font-size: 17px;
  font-weight: bold;
  border: 2px solid #409eff;
}
</style>