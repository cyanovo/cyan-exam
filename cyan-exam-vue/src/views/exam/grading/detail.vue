<template>
  <div class="grading-detail">
    <h3>{{ examName }} - 学生：{{ studentName }}</h3>
    <el-row :gutter="20">
      <el-col :span="16">
        <div v-for="(q, idx) in questions" :key="q.questionId" class="question-block">
          <p><b>{{ idx + 1 }}. {{ typeLabel(q.type) }}：{{ q.title }}</b></p>
          <!-- 选项展示 -->
          <div v-if="q.options && q.options.length">
            <p v-for="opt in q.options" :key="opt.label">
              {{ opt.label }}. {{ opt.content }}
            </p>
          </div>
          <p>参考答案：<b style="color:green">{{ q.referenceAnswer }}</b></p>
          <p>学生答案：<b style="color:blue">{{ q.studentAnswer || '未作答' }}</b></p>
          <!-- 自动批改标记 -->
          <div class="score-input">
            得分：
            <el-input-number v-model="q.tempScore" :min="0" :max="maxScore" size="small" />
            <span v-if="q.autoGradable && q.studentAnswer === q.referenceAnswer" style="color:green; margin-left:10px">✓ 自动正确</span>
            <span v-else-if="q.autoGradable && q.studentAnswer !== q.referenceAnswer" style="color:red; margin-left:10px">✗ 自动错误</span>
          </div>
        </div>
        <el-button type="primary" @click="submitScores">提交批改</el-button>
      </el-col>
      <el-col :span="8">
        <div class="score-panel">
          <p>总分：{{ totalScore }} / {{ questions.length * maxScore }}</p>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const examId = route.params.examId
const userId = route.params.userId
const examName = ref('')
const studentName = ref('')
const questions = ref([])
const maxScore = ref(5)

const totalScore = computed(() => {
  return questions.value.reduce((sum, q) => sum + (q.tempScore || 0), 0)
})

// 题目类型中文映射
const typeLabel = (type) => {
  const map = {
    SINGLE_CHOICE: '单选题',
    MULTI_CHOICE: '多选题',
    JUDGE: '判断题',
    FILL_BLANK: '填空题',
    SHORT_ANSWER: '简答题'
  }
  return map[type] || type
}

function fetchDetail() {
  request.get(`/exam/grading/detail/${examId}/${userId}`).then(res => {
    const data = res.data
    examName.value = data.examName || ''
    studentName.value = data.studentName || '未知'
    questions.value = (data.questions || []).map(q => ({
      ...q,
      // 自动初始化分数：如果已有分数则保留，否则自动批改计算（单选/判断）
      tempScore: q.score !== null ? q.score : (q.autoGradable && q.studentAnswer === q.referenceAnswer ? maxScore.value : 0)
    }))
  })
}

function submitScores() {
  const scoreData = questions.value
    .filter(q => q.recordId != null)
    .map(q => ({
      recordId: q.recordId,
      score: q.tempScore
    }))
  request.post('/exam/grading/submitScore', scoreData).then(() => {
    alert('批改提交成功')
  })
}

onMounted(() => fetchDetail())
</script>

<style scoped>
.grading-detail { padding: 20px; }
.question-block { border: 1px solid #ddd; padding: 15px; margin-bottom: 15px; border-radius: 5px; }
.score-input { margin-top: 10px; }
.score-panel { background: #f5f7fa; padding: 20px; border-radius: 5px; position: sticky; top: 20px; }
</style>