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
.grading-detail {
  padding: 24px;
  max-width: 1200px;
}

.grading-detail h3 {
  font-size: 20px;
  font-weight: 700;
  color: #00695c;
  margin: 0 0 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(0, 137, 123, 0.12);
}

.question-block {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 137, 123, 0.1);
  border-radius: 16px;
  padding: 20px 24px;
  margin-bottom: 16px;
  transition: box-shadow 0.2s;
}
.question-block:hover {
  box-shadow: 0 4px 16px rgba(0, 137, 123, 0.08);
}

.question-block p {
  margin: 0 0 8px;
  color: #334155;
  line-height: 1.6;
}

.score-input {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid rgba(0, 137, 123, 0.08);
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #00695c;
}

.score-panel {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border: 1px solid rgba(0, 137, 123, 0.12);
  border-radius: 20px;
  padding: 24px;
  position: sticky;
  top: 20px;
  text-align: center;
}
.score-panel p {
  font-size: 20px;
  font-weight: 700;
  color: #00695c;
  margin: 0;
}

:deep(.el-button--primary) {
  border-radius: 12px;
  padding: 12px 28px;
  font-size: 15px;
  font-weight: 600;
  margin-top: 8px;
}

:deep(.el-input-number .el-input__inner) {
  border-radius: 10px;
}
</style>