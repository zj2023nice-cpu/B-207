<template>
  <div class="export-task-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据导出任务中心</span>
          <div class="header-actions">
            <el-button type="primary" @click="loadData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-alert
        title="说明"
        type="info"
        :closable="false"
        style="margin-bottom: 16px;"
      >
        <p>导出任务会在后台异步处理，您可以在此页面查看任务状态，处理完成后可下载文件。</p>
        <p>文件将保留7天，请及时下载。</p>
      </el-alert>

      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="taskName" label="任务名称" min-width="150" />
        <el-table-column prop="exportTypeDesc" label="导出类型" width="120" />
        <el-table-column prop="exportRangeDesc" label="导出范围" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ scope.row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="导出数量" width="100">
          <template #default="scope">
            {{ scope.row.totalCount || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="文件大小" width="100">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="创建人" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="completedAt" label="完成时间" width="160">
          <template #default="scope">
            {{ formatDateTime(scope.row.completedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              :disabled="!scope.row.canDownload"
              @click="handleDownload(scope.row)"
            >
              下载
            </el-button>
            <el-button
              size="small"
              type="warning"
              :disabled="!scope.row.canRetry"
              @click="handleRetry(scope.row)"
            >
              重试
            </el-button>
            <el-button
              size="small"
              type="danger"
              :disabled="scope.row.status !== 'PENDING'"
              @click="handleCancel(scope.row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
let refreshTimer = null

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/export-task/list')
    tableData.value = res.data || []
  } catch (error) {
    console.error(error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const map = {
    PENDING: 'info',
    PROCESSING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    CANCELLED: 'info'
  }
  return map[status] || 'info'
}

const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').substring(0, 19)
}

const handleDownload = (row) => {
  const token = localStorage.getItem('token')
  const url = `/api/export-task/download/${row.id}`
  const link = document.createElement('a')
  link.href = url
  link.download = row.fileName || 'export.xlsx'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success('开始下载')
}

const handleRetry = (row) => {
  ElMessageBox.confirm('确定要重试此任务吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await request.post(`/export-task/retry/${row.id}`)
      ElMessage.success('已发起重试')
      loadData()
    } catch (error) {
      console.error(error)
      ElMessage.error('重试失败')
    }
  })
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消此任务吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await request.post(`/export-task/cancel/${row.id}`)
      ElMessage.success('已取消')
      loadData()
    } catch (error) {
      console.error(error)
      ElMessage.error('取消失败')
    }
  })
}

onMounted(() => {
  loadData()
  refreshTimer = setInterval(() => {
    const hasProcessing = tableData.value.some(item => 
      item.status === 'PENDING' || item.status === 'PROCESSING'
    )
    if (hasProcessing) {
      loadData()
    }
  }, 5000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}
</style>
