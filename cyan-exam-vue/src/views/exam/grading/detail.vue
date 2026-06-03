<template>
  <div class="grading-detail">
    <h3>{{ examName }} - 学生：{{ studentName }}</h3>

    <!-- 分值设置与 AI 批改区域 -->
    <div class="ai-grading-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <label class="setting-label">单选题分值</label>
          <el-input-number v-model="scoreSettings.singleScore" :min="1" :max="100" size="small" />
        </el-col>
        <el-col :span="6">
          <label class="setting-label">多选题分值</label>
          <el-input-number v-model="scoreSettings.multiScore" :min="1" :max="100" size="small" />
        </el-col>
        <el-col :span="6">
          <label class="setting-label">判断题分值</label>
          <el-input-number v-model="scoreSettings.judgeScore" :min="1" :max="100" size="small" />
        </el-col>
        <el-col :span="6">
          <label class="setting-label">主观题分值</label>
          <el-input-number v-model="scoreSettings.subjectiveMax" :min="1" :max="100" size="small" />
        </el-col>
      </el-row>

      <el-divider />

      <div class="ai-api-row">
        <el-input v-model="aiKey" placeholder="DeepSeek API Key (sk-...)" show-password style="width: 400px;">
          <template #append>
            <el-button @click="saveAiKey">保存密钥</el-button>
          </template>
        </el-input>
        <span style="margin-left: 10px; color: #64748b; font-size: 13px;">
          密钥仅保存在当前浏览器，不会上传至服务器。
        </span>
      </div>

      <div class="ai-standard-area">
        <label class="setting-label">批改标准（评分要点）</label>
        <el-input
          v-model="gradingStandard"
          type="textarea"
          :rows="3"
          placeholder="例如：概念解释2分，逻辑清晰3分；编程题：语法正确3分，思路完整2分..."
        />
      </div>

      <div class="ai-actions">
        <el-button type="primary" @click="autoGradingByAI" :loading="aiGrading">
          <el-icon v-if="!aiGrading"><MagicStick /></el-icon>
          {{ aiGrading ? 'AI 正在批改...' : 'AI 智能批改' }}
        </el-button>
        <el-button @click="applyAutoGrading">仅自动批改选择/判断</el-button>
      </div>
    </div>

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
            <el-input-number v-model="q.tempScore" :min="0" :max="q.maxScore" size="small" />
            <span v-if="q.autoGradable && q.studentAnswer === q.referenceAnswer" style="color:green; margin-left:10px">✓ 自动正确</span>
            <span v-else-if="q.autoGradable && q.studentAnswer !== q.referenceAnswer" style="color:red; margin-left:10px">✗ 自动错误</span>
          </div>
        </div>
        <el-button type="primary" @click="submitScores">提交批改</el-button>
      </el-col>
      <el-col :span="8">
        <div class="score-panel">
          <p>总分：{{ totalScore }} / {{ totalMaxScore }}</p>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { MagicStick } from '@element-plus/icons-vue'

const route = useRoute()
const examId = route.params.examId
const userId = route.params.userId
const examName = ref('')
const studentName = ref('')
const questions = ref([])

// 分值设置
const scoreSettings = ref({
  singleScore: 5,
  multiScore: 5,
  judgeScore: 5,
  subjectiveMax: 10
})

// AI 相关
const aiKey = ref(localStorage.getItem('deepseek_api_key') || '')
const gradingStandard = ref('')
const aiGrading = ref(false)

// 总分统计
const totalScore = computed(() => {
  return questions.value.reduce((sum, q) => sum + (q.tempScore || 0), 0)
})
const totalMaxScore = computed(() => {
  return questions.value.reduce((sum, q) => sum + (q.maxScore || 0), 0)
})

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

// 根据题型获取满分
function getMaxScore(type) {
  switch (type) {
    case 'SINGLE_CHOICE': return scoreSettings.value.singleScore
    case 'MULTI_CHOICE': return scoreSettings.value.multiScore
    case 'JUDGE': return scoreSettings.value.judgeScore
    default: return scoreSettings.value.subjectiveMax
  }
}

// 判断题答案标准化
function normalizeJudgeAnswer(answer) {
  if (!answer) return '';
  const map = {
    'T': '对', 't': '对', 'true': '对', 'True': '对',
    'F': '错', 'f': '错', 'false': '错', 'False': '错',
    '对': '对', '错': '错'
  };
  return map[answer] || answer;
}

function initQuestions(data) {
  questions.value = (data.questions || []).map(q => {
    const max = getMaxScore(q.type);
    // 判断题答案标准化
    if (q.type === 'JUDGE') {
      q.referenceAnswer = normalizeJudgeAnswer(q.referenceAnswer);
      q.studentAnswer = normalizeJudgeAnswer(q.studentAnswer);
    }
    // 自动批改初始分数
    let tempScore = q.score !== null ? q.score : null;
    if (tempScore === null && q.autoGradable) {
      tempScore = (q.studentAnswer === q.referenceAnswer) ? max : 0;
    } else if (tempScore === null) {
      tempScore = 0;
    }
    return { ...q, maxScore: max, tempScore };
  });
}

function fetchDetail() {
  request.get(`/exam/grading/detail/${examId}/${userId}`).then(res => {
    const data = res.data
    examName.value = data.examName || ''
    studentName.value = data.studentName || '未知'
    initQuestions(data)
  })
}

// 保存 API Key
function saveAiKey() {
  localStorage.setItem('deepseek_api_key', aiKey.value)
  alert('密钥已保存')
}

// 仅自动批改选择/判断题
function applyAutoGrading() {
  questions.value.forEach(q => {
    if (q.autoGradable) {
      const max = getMaxScore(q.type)
      q.tempScore = (q.studentAnswer === q.referenceAnswer) ? max : 0
    }
  })
}

// AI 批改
async function autoGradingByAI() {
  // 收集主观题数据
  const subjectiveQuestions = questions.value.filter(q => !q.autoGradable && q.studentAnswer)
  if (subjectiveQuestions.length === 0) {
    alert('没有需要 AI 批改的主观题')
    return
  }
  if (!aiKey.value) {
    alert('请填写 DeepSeek API Key')
    return
  }

  aiGrading.value = true
  try {
    // 构建 Prompt
    const questionList = subjectiveQuestions.map(q => ({
      questionId: q.questionId,
      title: q.title,
      referenceAnswer: q.referenceAnswer,
      studentAnswer: q.studentAnswer
    }))

    const systemPrompt = `你是一个专业的阅卷老师。请根据以下批改标准，对每一道主观题的学生答案进行评分。
批改标准：${gradingStandard.value || '无特殊标准，请根据答案正确性酌情给分'}

每道题的满分是 ${scoreSettings.value.subjectiveMax} 分。
请返回 JSON 数组，格式如下（不要包含 markdown 标记）：
[
  { "questionId": 1, "score": 8 },
  { "questionId": 2, "score": 10 }
]`

    const userMessage = JSON.stringify(questionList, null, 2)

    const response = await fetch('https://api.deepseek.com/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${aiKey.value}`
      },
      body: JSON.stringify({
        model: 'deepseek-chat',
        messages: [
          { role: 'system', content: systemPrompt },
          { role: 'user', content: userMessage }
        ],
        temperature: 0.1
      })
    })

    const data = await response.json()
    if (!response.ok) throw new Error(data.error?.message || '调用失败')

    const content = data.choices?.[0]?.message?.content || ''
    // 提取 JSON 数组
    const jsonMatch = content.match(/\[[\s\S]*\]/)
    if (!jsonMatch) throw new Error('AI 未返回有效 JSON')

    const scores = JSON.parse(jsonMatch[0])
    // 更新主观题分数
    scores.forEach(item => {
      const q = questions.value.find(q => q.questionId === item.questionId)
      if (q && !q.autoGradable) {
        q.tempScore = Math.min(item.score, q.maxScore) // 防止超过满分
      }
    })
    alert('AI 批改完成')
  } catch (e) {
    console.error(e)
    alert('AI 批改失败：' + e.message)
  } finally {
    aiGrading.value = false
  }
}

// 提交批改
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

/* AI 批改设置卡片 */
.ai-grading-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  border: 1px solid rgba(0, 137, 123, 0.12);
}

.setting-label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
}

.ai-api-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.ai-standard-area {
  margin-bottom: 16px;
}

.ai-actions {
  display: flex;
  gap: 12px;
}

/* 题目卡片 */
.question-block {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
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