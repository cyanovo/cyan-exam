<template>
  <div class="grading-student-container">
    <div class="page-card">
      <h3 class="page-title">考试：{{ examName }}</h3>
      <el-table :data="studentList" v-loading="loading" class="student-table">
        <el-table-column label="学生姓名" prop="studentName" />
        <el-table-column label="已答题数/总题数">
          <template #default="{ row }">
            <span class="progress-text">{{ row.answered }} / {{ row.total }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已批改题数" prop="graded" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button link type="primary"
              @click="$router.push(`/exam/grading/detail/${$route.params.examId}/${scope.row.userId}`)">批改</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const examId = route.params.examId
const examName = ref('')
const studentList = ref([])
const loading = ref(false)

function fetchStudents() {
  loading.value = true
  request.get(`/exam/grading/students/${examId}`).then(res => {
    studentList.value = res.data || []
    examName.value = '考试' + examId
    loading.value = false
  })
}

onMounted(() => fetchStudents())
</script>

<style scoped>
.grading-student-container {
  padding: 20px;
}

.page-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 24px rgba(0, 137, 123, 0.06);
  border: 1px solid rgba(0, 137, 123, 0.08);
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #00695c;
  margin: 0 0 20px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(0, 137, 123, 0.1);
}

.progress-text {
  font-weight: 600;
  color: #00897b;
}

:deep(.student-table) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.student-table th.el-table__cell) {
  background-color: #e0f2f1 !important;
  color: #00695c !important;
  font-weight: 600;
}

:deep(.el-button--primary) {
  border-radius: 10px;
}
</style>