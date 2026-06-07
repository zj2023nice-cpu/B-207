<template>
  <div class="announcement-admin-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              发布公告
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PUBLISHED'" type="success" size="small">已发布</el-tag>
            <el-tag v-else type="info" size="small">草稿</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isPinned" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isPinned" type="danger" size="small">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishStartTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.publishStartTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="publishEndTime" label="过期时间" width="180">
          <template #default="{ row }">
            {{ row.publishEndTime ? formatTime(row.publishEndTime) : '永久有效' }}
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end;"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="formVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="650px">
      <el-form :model="form" ref="formRef" :rules="rules" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="公告正文" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告正文内容"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="是否置顶" prop="isPinned">
          <el-switch v-model="form.isPinned" active-text="置顶" inactive-text="不置顶" />
        </el-form-item>
        <el-form-item label="发布状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="PUBLISHED">立即发布</el-radio>
            <el-radio value="DRAFT">存为草稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发布时间" prop="publishStartTime">
          <el-date-picker
            v-model="form.publishStartTime"
            type="datetime"
            placeholder="选择发布开始时间"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="form.publishEndTime"
            type="datetime"
            placeholder="选择发布结束时间（不选则永久有效）"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="公告详情" width="600px">
      <div v-if="currentAnnouncement" class="announcement-detail">
        <div class="detail-title">{{ currentAnnouncement.title }}</div>
        <div class="detail-meta">
          <el-tag v-if="currentAnnouncement.isPinned" type="danger" size="small" effect="dark">置顶</el-tag>
          <el-tag v-if="currentAnnouncement.status === 'PUBLISHED'" type="success" size="small">已发布</el-tag>
          <el-tag v-else type="info" size="small">草稿</el-tag>
          <span class="publisher">发布人：{{ currentAnnouncement.publisherName }}</span>
        </div>
        <div class="detail-times">
          <div>发布时间：{{ formatTime(currentAnnouncement.publishStartTime) }}</div>
          <div>过期时间：{{ currentAnnouncement.publishEndTime ? formatTime(currentAnnouncement.publishEndTime) : '永久有效' }}</div>
        </div>
        <div class="detail-content">
          <div v-html="currentAnnouncement.content"></div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const formVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const currentAnnouncement = ref(null)
const formRef = ref(null)

const form = reactive({
  id: null,
  title: '',
  content: '',
  isPinned: false,
  status: 'PUBLISHED',
  publishStartTime: '',
  publishEndTime: ''
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告正文', trigger: 'blur' }],
  publishStartTime: [{ required: true, message: '请选择发布时间', trigger: 'change' }]
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/system-announcement/admin/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.id = null
  form.title = ''
  form.content = ''
  form.isPinned = false
  form.status = 'PUBLISHED'
  form.publishStartTime = new Date()
  form.publishEndTime = ''
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  formVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.title = row.title
  form.content = row.content
  form.isPinned = row.isPinned
  form.status = row.status
  form.publishStartTime = row.publishStartTime
  form.publishEndTime = row.publishEndTime
  formVisible.value = true
}

const handleView = async (row) => {
  const res = await request.get(`/system-announcement/admin/${row.id}`)
  currentAnnouncement.value = res.data
  detailVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该公告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/system-announcement/admin/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (isEdit.value) {
        await request.put('/system-announcement/admin', form)
        ElMessage.success('更新成功')
      } else {
        await request.post('/system-announcement/admin', form)
        ElMessage.success('发布成功')
      }
      formVisible.value = false
      loadData()
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.announcement-detail {
  padding: 10px 0;
}

.detail-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 15px;
  text-align: center;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
  margin-bottom: 15px;
  color: #909399;
  font-size: 13px;
}

.detail-times {
  color: #909399;
  font-size: 13px;
  margin-bottom: 20px;
  line-height: 1.8;
}

.detail-content {
  line-height: 1.8;
  color: #303133;
  font-size: 14px;
}
</style>
