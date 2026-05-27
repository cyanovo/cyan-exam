<template>
  <div class="app-container exam-app">
    <!-- 搜索区域 -->
    <div class="search-bar">
      <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
        <el-form-item label="题目分类" prop="category">
          <el-select v-model="queryParams.category" placeholder="请选择" clearable>
            <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="难易程度" prop="difficulty">
          <el-select v-model="queryParams.difficulty" placeholder="请选择" clearable>
            <el-option v-for="i in 5" :key="i" :label="i + '星'" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目类型" prop="questionType">
          <el-select v-model="queryParams.questionType" placeholder="请选择" clearable>
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-row :gutter="10">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['question:question:add']">新增</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['question:question:edit']">修改</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['question:question:remove']">删除</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['question:question:export']">导出</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['question:question:import']">导入</el-button>
        </el-col>
        <el-col :span="1.5">
          <!-- AI 导入按钮 – 炫彩光效 -->
          <el-button class="ai-import-btn" plain icon="Upload" @click="handlePasteImport" v-hasPermi="['question:question:import']">
            AI 导入
          </el-button>
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>
    </div>

    <!-- 表格 -->
    <div class="table-wrap">
      <el-table v-loading="loading" :data="questionList" @selection-change="handleSelectionChange" stripe border header-cell-class-name="table-header">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="ID" align="center" prop="questionId" width="80" />
        <el-table-column label="分类" align="center" prop="category" width="100" />
        <el-table-column label="难度" align="center" width="120">
          <template #default="{ row }">
            <el-rate v-model="row.difficulty" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column label="题型" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.questionType)" effect="light">{{ typeLabel(row.questionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="题干" prop="title" show-overflow-tooltip min-width="200" />
        <el-table-column label="答案" align="center" width="120" prop="answer" />
        <el-table-column label="状态" align="center" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="0" :inactive-value="1" disabled inline-prompt active-text="启" inactive-text="停" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="150" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['question:question:edit']">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['question:question:remove']">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total>0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
    </div>

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="750px" append-to-body destroy-on-close class="modern-dialog">
      <el-form ref="questionRef" :model="form" :rules="rules" label-width="100px" class="exam-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="题目分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择" style="width:100%">
                <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="难易程度" prop="difficulty">
              <el-rate v-model="form.difficulty" :max="5" show-score />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="题目类型" prop="questionType">
          <el-radio-group v-model="form.questionType" @change="onTypeChange">
            <el-radio-button v-for="item in typeOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="题干" prop="title">
          <el-input v-model="form.title" type="textarea" :rows="3" placeholder="请输入题干" class="limited-textarea" />
        </el-form-item>

        <!-- 选项编辑区 -->
        <el-form-item v-if="isChoiceOrJudge" label="选项" prop="subTitle">
          <div class="option-list">
            <div v-for="(opt, index) in optionList" :key="index" class="option-item">
              <el-input v-model="opt.label" style="width:60px" disabled />
              <el-input v-model="opt.content" placeholder="选项内容" style="width:320px; margin:0 10px" />
              <el-checkbox v-if="form.questionType === 'MULTI_CHOICE'" v-model="opt.isAnswer" :true-label="1" :false-label="0" style="margin-right:10px">正确</el-checkbox>
              <el-radio v-else v-model="singleCorrect" :label="opt.label" @change="onSingleAnswerChange(index)" style="margin-right:10px">正确</el-radio>
              <el-button icon="Delete" circle size="small" @click="removeOption(index)" :disabled="optionList.length <= 2 && form.questionType === 'JUDGE'" />
            </div>
            <el-button type="primary" text @click="addOption" v-if="form.questionType !== 'JUDGE'">+ 添加选项</el-button>
          </div>
        </el-form-item>

        <!-- 填空/简答答案 -->
        <el-form-item v-if="isFillOrShort" label="答案" prop="answer">
          <el-input v-model="form.answer" type="textarea" class="limited-textarea" :placeholder="form.questionType === 'FILL_BLANK' ? '多个填空答案请用英文分号(;)分隔' : '请输入参考答案'" />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" class="limited-textarea" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancel">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导入对话框 -->
    <el-dialog title="题库导入" v-model="importOpen" width="400px" append-to-body class="modern-dialog">
      <el-upload ref="uploadRef" :action="uploadUrl" :headers="uploadHeaders" :before-upload="beforeUpload" :on-success="handleImportSuccess" :auto-upload="false" :limit="1" accept=".xlsx, .xls" drag>
        <el-icon><Upload /></el-icon>
        <div class="el-upload__text">将Excel文件拖到此处，或 <em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">
            仅允许 .xlsx / .xls 文件，请先
            <el-button type="text" @click="downloadTemplate">下载模板</el-button>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitUpload">开始上传</el-button>
      </template>
    </el-dialog>

    <!-- AI粘贴导入对话框 -->
    <el-dialog title="AI 智能导入" v-model="pasteOpen" width="700px" append-to-body class="modern-dialog">
      <el-alert title="操作指南" type="info" :closable="false" show-icon class="step-alert">
        <template #default>
          <div class="steps">
            <p>① 点击“复制提示词”，将提示词与您的试题一起发送给 AI</p>
            <p>② 将 AI 生成的 JSON 数组粘贴到下方文本框</p>
            <p>③ 点击“导入”完成批量录入</p>
            <p class="ai-link">推荐使用 <a href="https://chat.deepseek.com/" target="_blank">DeepSeek Chat</a></p>
          </div>
        </template>
      </el-alert>
      <div class="prompt-actions">
        <el-button type="primary" @click="copyPrompt">复制提示词</el-button>
        <el-popover placement="bottom" :width="500" trigger="click">
          <template #reference>
            <el-button link type="primary">查看提示词</el-button>
          </template>
          <div class="prompt-content">{{ promptTemplate }}</div>
        </el-popover>
      </div>
      <el-form>
        <el-form-item label="JSON 数据">
          <el-input v-model="jsonText" type="textarea" :rows="12" placeholder="请将AI生成的JSON数组粘贴到这里" class="limited-textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pasteOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitJsonImport">导 入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Question">
import { listQuestion, getQuestion, delQuestion, addQuestion, updateQuestion, importJson } from "@/api/question/question"
import { getCurrentInstance, reactive, ref, toRefs, computed, nextTick } from "vue"
import { getToken } from "@/utils/auth"
import request from '@/utils/request'
import { Upload } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

const importOpen = ref(false)
const uploadRef = ref(null)
const questionList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")

const data = reactive({
  form: {
    questionId: null,
    category: "",
    difficulty: 1,
    questionType: "SINGLE_CHOICE",
    title: "",
    subTitle: "",
    answer: "",
    status: 0,
    remark: ""
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    category: undefined,
    difficulty: undefined,
    questionType: undefined
  },
  rules: {
    category: [{ required: true, message: "题目分类不能为空", trigger: "change" }],
    difficulty: [{ required: true, message: "难易程度不能为空", trigger: "change" }],
    questionType: [{ required: true, message: "题目类型不能为空", trigger: "change" }],
    title: [{ required: true, message: "题干不能为空", trigger: "blur" }],
    answer: [{
      validator: (rule, value, callback) => {
        if (isChoiceOrJudge.value) callback()
        else if (!value || value.trim() === "") callback(new Error("答案不能为空"))
        else callback()
      },
      trigger: "blur"
    }]
  }
})

const pasteOpen = ref(false)
const jsonText = ref('')

const promptTemplate = `请将以下试题内容转换为 JSON 数组格式（严格使用下方字段名和格式），每个题目一个对象：
{
  "category": "题目分类 (JAVA/PYTHON/C/HTML)",
  "difficulty": 难易程度 (1-5整数),
  "questionType": "题目类型 (SINGLE_CHOICE/MULTI_CHOICE/JUDGE/FILL_BLANK/SHORT_ANSWER)",
  "title": "题干内容",
  "subTitle": "选择题/判断题：选项格式为 'A: 内容; B: 内容 | 正确答案标签(多选用逗号分隔)'，如 'A: 编译型语言; B: 解释型语言; C: 编译与解释结合; D: 以上都对 | C'。填空题和简答题留空。",
  "answer": "选择题：选项标签；多选题：标签用逗号连接；判断题：'对'或'错'；填空题：答案用英文分号分隔；简答题：参考答案文本"
}
请直接输出 JSON 数组，不要包含 markdown 标记或任何说明文字。示例：
[
  {
    "category": "JAVA",
    "difficulty": 3,
    "questionType": "SINGLE_CHOICE",
    "title": "Java 属于什么类型的语言？",
    "subTitle": "A: 编译型语言; B: 解释型语言; C: 编译与解释结合; D: 以上都对 | C",
    "answer": "C"
  }
]
试题内容如下：`

const { queryParams, form, rules } = toRefs(data)

const categoryOptions = [
  { label: "Java", value: "JAVA" },
  { label: "Python", value: "PYTHON" },
  { label: "C语言", value: "C" },
  { label: "HTML", value: "HTML" }
]

const typeOptions = [
  { label: "单选题", value: "SINGLE_CHOICE" },
  { label: "多选题", value: "MULTI_CHOICE" },
  { label: "判断题", value: "JUDGE" },
  { label: "填空题", value: "FILL_BLANK" },
  { label: "简答题", value: "SHORT_ANSWER" }
]

const typeLabel = (type) => {
  const map = { SINGLE_CHOICE: "单选题", MULTI_CHOICE: "多选题", JUDGE: "判断题", FILL_BLANK: "填空题", SHORT_ANSWER: "简答题" }
  return map[type] || type
}
const typeTag = (type) => {
  const map = { SINGLE_CHOICE: "", MULTI_CHOICE: "success", JUDGE: "warning", FILL_BLANK: "info", SHORT_ANSWER: "danger" }
  return map[type] || ""
}

const optionList = ref([])
const singleCorrect = ref("")
const isChoiceOrJudge = computed(() => ["SINGLE_CHOICE", "MULTI_CHOICE", "JUDGE"].includes(form.value.questionType))
const isFillOrShort = computed(() => ["FILL_BLANK", "SHORT_ANSWER"].includes(form.value.questionType))

function initOptionsForType() {
  if (form.value.questionType === "JUDGE") {
    optionList.value = [
      { label: "A", content: "对", isAnswer: 0 },
      { label: "B", content: "错", isAnswer: 0 }
    ]
  } else if (["SINGLE_CHOICE", "MULTI_CHOICE"].includes(form.value.questionType)) {
    optionList.value = [
      { label: "A", content: "", isAnswer: 0 },
      { label: "B", content: "", isAnswer: 0 },
      { label: "C", content: "", isAnswer: 0 },
      { label: "D", content: "", isAnswer: 0 }
    ]
  } else {
    optionList.value = []
  }
  singleCorrect.value = ""
}

function onTypeChange() {
  if (["SINGLE_CHOICE", "MULTI_CHOICE", "JUDGE"].includes(form.value.questionType)) {
    if (form.value.subTitle) {
      try {
        const parsed = JSON.parse(form.value.subTitle)
        if (Array.isArray(parsed) && parsed.length) {
          optionList.value = parsed
          const correctOpt = parsed.find(o => o.isAnswer == 1)
          singleCorrect.value = correctOpt ? correctOpt.label : ""
          return
        }
      } catch (e) {}
    }
    initOptionsForType()
  } else {
    optionList.value = []
    singleCorrect.value = ""
    form.value.subTitle = ""
  }
}

function addOption() {
  const labels = "ABCDEFGH"
  const nextLabel = labels[optionList.value.length] || String(optionList.value.length + 1)
  optionList.value.push({ label: nextLabel, content: "", isAnswer: 0 })
}

function removeOption(index) {
  optionList.value.splice(index, 1)
  if (singleCorrect.value === optionList.value[index]?.label) singleCorrect.value = ""
}

function onSingleAnswerChange(index) {
  optionList.value.forEach((opt, i) => opt.isAnswer = i === index ? 1 : 0)
}

function syncAnswerFromOptions() {
  if (form.value.questionType === "SINGLE_CHOICE" || form.value.questionType === "JUDGE") {
    const correct = optionList.value.find(o => o.isAnswer == 1)
    form.value.answer = correct ? correct.label : ""
  } else if (form.value.questionType === "MULTI_CHOICE") {
    form.value.answer = optionList.value.filter(o => o.isAnswer == 1).map(o => o.label).join(",")
  }
}

function getList() {
  loading.value = true
  listQuestion(queryParams.value).then(response => {
    questionList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    questionId: null,
    category: "",
    difficulty: 1,
    questionType: "SINGLE_CHOICE",
    title: "",
    subTitle: "",
    answer: "",
    status: 0,
    remark: ""
  }
  optionList.value = []
  singleCorrect.value = ""
  proxy.resetForm("questionRef")
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.questionId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "添加题库"
  nextTick(() => onTypeChange())
}

function handleUpdate(row) {
  reset()
  const _questionId = row.questionId || ids.value
  getQuestion(_questionId).then(response => {
    form.value = response.data
    if (["SINGLE_CHOICE", "MULTI_CHOICE", "JUDGE"].includes(form.value.questionType)) {
      try {
        optionList.value = form.value.subTitle ? JSON.parse(form.value.subTitle) : []
        if (!optionList.value.length) initOptionsForType()
      } catch (e) { initOptionsForType() }
      const correctOpt = optionList.value.find(o => o.isAnswer == 1)
      singleCorrect.value = correctOpt ? correctOpt.label : ""
    } else {
      optionList.value = []
    }
    open.value = true
    title.value = "修改题库"
  })
}

function submitForm() {
  proxy.$refs["questionRef"].validate(valid => {
    if (!valid) return
    if (isChoiceOrJudge.value) {
      form.value.subTitle = JSON.stringify(optionList.value)
      syncAnswerFromOptions()
    }
    if (form.value.questionId != null) {
      updateQuestion(form.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        open.value = false
        getList()
      })
    } else {
      addQuestion(form.value).then(() => {
        proxy.$modal.msgSuccess("新增成功")
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  const _questionIds = row.questionId || ids.value
  proxy.$modal.confirm('是否确认删除题库编号为"' + _questionIds + '"的数据项？').then(function () {
    return delQuestion(_questionIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function handleExport() {
  request({
    url: '/question/question/export',
    method: 'get',
    params: { ...queryParams.value, _t: Date.now() },
    responseType: 'blob'
  }).then(blob => {
    const url = window.URL.createObjectURL(blob instanceof Blob ? blob : blob.data)
    const link = document.createElement('a')
    link.href = url
    link.download = '题库数据.xlsx'
    link.click()
    URL.revokeObjectURL(url)
  }).catch(() => {
    proxy.$modal.msgError('导出失败')
  })
}

const uploadUrl = import.meta.env.VITE_APP_BASE_API + "/question/question/import"
const uploadHeaders = { Authorization: "Bearer " + getToken() }

function handleImport() {
  importOpen.value = true
}

function beforeUpload(file) {
  const isExcel = file.type === "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
                  file.type === "application/vnd.ms-excel"
  if (!isExcel) {
    proxy.$modal.msgError("只能上传 Excel 文件！")
    return false
  }
  return true
}

function submitUpload() {
  uploadRef.value.submit()
}

function handleImportSuccess(res) {
  if (res.code === 200) {
    proxy.$modal.msgSuccess("导入成功")
    importOpen.value = false
    getList()
  } else {
    proxy.$modal.msgError(res.msg || "导入失败")
  }
}

function downloadTemplate() {
  request({
    url: '/question/question/importTemplate',
    method: 'get',
    responseType: 'blob'
  }).then(blob => {
    const url = window.URL.createObjectURL(blob instanceof Blob ? blob : blob.data)
    const link = document.createElement('a')
    link.href = url
    link.download = '题库导入模板.xlsx'
    link.click()
    URL.revokeObjectURL(url)
  }).catch(() => {
    proxy.$modal.msgError('模板下载失败')
  })
}

function handlePasteImport() {
  pasteOpen.value = true
  jsonText.value = ''
}

function copyPrompt() {
  navigator.clipboard.writeText(promptTemplate).then(() => {
    proxy.$modal.msgSuccess("提示词已复制到剪贴板")
  }).catch(() => {
    proxy.$modal.msgError("复制失败，请手动复制")
  })
}

function submitJsonImport() {
  if (!jsonText.value.trim()) {
    proxy.$modal.msgWarning("请粘贴 JSON 数据")
    return
  }
  importJson({ json: jsonText.value }).then(res => {
    if (res.code === 200) {
      proxy.$modal.msgSuccess("导入成功")
      pasteOpen.value = false
      getList()
    } else {
      proxy.$modal.msgError(res.msg)
    }
  }).catch(() => {
    proxy.$modal.msgError("导入请求失败")
  })
}

getList()
</script>

<style scoped>
.exam-app {
  padding: 0;
}

.search-bar,
.toolbar,
.table-wrap {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.table-wrap {
  padding-bottom: 10px;
}

:deep(.table-header) {
  background-color: #f8f9fc;
  font-weight: 600;
  color: #303133;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f8f9fc;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafbfd;
}

/* 统一文本框限高 */
:deep(.el-textarea__inner) {
  max-height: 150px !important;
  overflow-y: auto !important;
  resize: vertical;
}

.limited-textarea {
  width: 100%;
}

/* 选项列表 */
.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

/* 对话框圆角 */
:deep(.modern-dialog) {
  border-radius: 16px;
}

:deep(.modern-dialog .el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.modern-dialog .el-dialog__header) {
  background-color: #f5f7fb;
  padding: 20px 24px 0;
  border-bottom: none;
}

:deep(.modern-dialog .el-dialog__body) {
  padding: 20px 24px;
}

/* AI 导入按钮炫彩光效 */
.ai-import-btn {
  position: relative;
  z-index: 0;
  border: none;
  color: #fff;
  background: linear-gradient(45deg, #ff0080, #ff8c00, #40e0d0);
  background-size: 300% 300%;
  animation: gradientShift 4s ease infinite;
  box-shadow: 0 0 15px rgba(255, 0, 128, 0.6), 0 0 30px rgba(64, 224, 208, 0.4);
  transition: transform 0.2s ease;
}

.ai-import-btn:hover {
  transform: scale(1.03);
  box-shadow: 0 0 20px rgba(255, 0, 128, 0.8), 0 0 40px rgba(64, 224, 208, 0.6);
}

.ai-import-btn:active {
  transform: scale(0.98);
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 提示词操作区 */
.prompt-actions {
  margin: 15px 0;
  display: flex;
  gap: 10px;
  align-items: center;
}

.prompt-content {
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 13px;
}

.steps p {
  margin: 5px 0;
}

.ai-link {
  margin-top: 8px;
  color: #409EFF;
}

.ai-link a {
  color: #409EFF;
  font-weight: 500;
}

:deep(.el-button) {
  border-radius: 6px;
}
</style>