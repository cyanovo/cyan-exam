<template>
  <div class="app-container">
    <el-table :data="examList" v-loading="loading">
      <el-table-column label="考试名称" prop="examName" />
      <el-table-column label="开始时间" prop="startTime" width="180">
        <template #default="{ row }">
          <span>{{ parseTime(row.startTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" prop="endTime" width="180">
        <template #default="{ row }">
          <span>{{ parseTime(row.endTime, '{y}-{m}-{d} {h}:{i}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button link type="primary" @click="$router.push(`/exam/grading/students/${scope.row.examId}`)">批改</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const examList = ref([])
const loading = ref(false)

function fetchExams() {
  loading.value = true
  request.get('/exam/grading/exams').then(res => {
    examList.value = res.data || []
    loading.value = false
  })
}

onMounted(() => fetchExams())
</script>