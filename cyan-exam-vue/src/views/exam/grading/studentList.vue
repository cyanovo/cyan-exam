<template>
  <div class="app-container">
    <h3>考试：{{ examName }}</h3>
    <el-table :data="studentList" v-loading="loading">
      <el-table-column label="学生姓名" prop="studentName" />
      <el-table-column label="已答题数/总题数">
        <template #default="{ row }">
          {{ row.answered }} / {{ row.total }}
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
    // 可另外请求考试名称
    examName.value = '考试' + examId   // 可从其他接口获取
    loading.value = false
  })
}

onMounted(() => fetchStudents())
</script>