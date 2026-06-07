<template>
  <div class="follow-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>重点关注老人清单</span>
          <div class="header-actions">
            <el-button type="primary" @click="refreshData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="follow-list">
        <el-empty v-if="!loading && tableData.length === 0" description="暂无关注的老人" />
        
        <el-row :gutter="20">
          <el-col :span="8" v-for="item in tableData" :key="item.elderlyId" style="margin-bottom: 20px;">
            <el-card shadow="hover" class="elderly-card">
              <div class="card-content">
                <div class="elderly-header">
                  <div class="elderly-info">
                    <div class="elderly-name">{{ item.name }}</div>
                    <div class="elderly-basic">
                      <span>{{ item.gender }}</span>
                      <span class="divider">|</span>
                      <span>{{ item.age }}岁</span>
                      <span class="divider">|</span>
                      <el-tag :type="getStatusType(item.status)" size="small">
                        {{ item.status || '正常' }}
                      </el-tag>
                    </div>
                  </div>
                  <el-button 
                    size="small" 
                    type="info" 
                    @click="handleUnfollow(item)"
                  >
                    取消关注
                  </el-button>
                </div>

                <el-divider style="margin: 12px 0;" />

                <div class="health-section">
                  <div class="section-title">最近健康记录</div>
                  <div v-if="item.lastCheckTime" class="health-content">
                    <div class="health-time">
                      检测时间：{{ formatDateTime(item.lastCheckTime) }}
                    </div>
                    <div class="health-summary">
                      <el-tag v-if="item.lastIsAbnormal" type="danger" size="small" style="margin-right: 8px;">
                        异常
                      </el-tag>
                      <span>{{ item.lastHealthSummary }}</span>
                    </div>
                  </div>
                  <div v-else class="health-empty">
                    暂无健康记录
                  </div>
                </div>

                <el-divider style="margin: 12px 0;" />

                <div class="stats-section">
                  <div class="stat-item warning" @click="goToWarning(item.elderlyId)">
                    <div class="stat-value">{{ item.pendingWarningCount || 0 }}</div>
                    <div class="stat-label">待处理预警</div>
                  </div>
                  <div class="stat-item notification" @click="goToNotification(item.elderlyId)">
                    <div class="stat-value">{{ item.unreadNotificationCount || 0 }}</div>
                    <div class="stat-label">未读通知</div>
                  </div>
                </div>

                <el-divider style="margin: 12px 0;" />

                <div class="action-section">
                  <el-button size="small" type="primary" @click="goToHealth(item.elderlyId)">
                    查看健康记录
                  </el-button>
                  <el-button size="small" type="success" @click="goToNursing(item.elderlyId)">
                    护理记录
                  </el-button>
                </div>

                <div class="follow-time">
                  关注时间：{{ formatDateTime(item.followTime) }}
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])

const getStatusType = (status) => {
  const statusMap = {
    '正常': 'success',
    '住院': 'warning',
    '外出': 'info',
    '失联': 'danger'
  }
  return statusMap[status] || 'info'
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  if (isNaN(date.getTime())) return '-'
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/elderly-follow/list')
    tableData.value = res.data || []
  } catch (error) {
    console.error(error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadData()
}

const handleUnfollow = async (item) => {
  ElMessageBox.confirm(`确定取消关注「${item.name}」吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.post(`/elderly-follow/unfollow/${item.elderlyId}`)
      ElMessage.success('已取消关注')
      loadData()
    } catch (error) {
      console.error(error)
      ElMessage.error('操作失败')
    }
  })
}

const goToHealth = (elderlyId) => {
  router.push({
    path: '/health',
    query: { elderlyId }
  })
}

const goToWarning = (elderlyId) => {
  router.push({
    path: '/warning',
    query: { elderlyId }
  })
}

const goToNotification = (elderlyId) => {
  router.push({
    path: '/notification',
    query: { elderlyId }
  })
}

const goToNursing = (elderlyId) => {
  router.push({
    path: '/nursing-observation',
    query: { elderlyId }
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
}

.follow-list {
  min-height: 400px;
}

.elderly-card {
  height: 100%;
}

.card-content {
  padding: 8px 0;
}

.elderly-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.elderly-name {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 6px;
}

.elderly-basic {
  font-size: 13px;
  color: #606266;
  display: flex;
  align-items: center;
}

.divider {
  margin: 0 8px;
  color: #c0c4cc;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.health-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.health-summary {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  word-break: break-all;
}

.health-empty {
  font-size: 13px;
  color: #c0c4cc;
}

.stats-section {
  display: flex;
  gap: 16px;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 12px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-item.warning {
  background-color: #fef0f0;
}

.stat-item.warning:hover {
  background-color: #fde2e2;
}

.stat-item.notification {
  background-color: #ecf5ff;
}

.stat-item.notification:hover {
  background-color: #d9ecff;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1.2;
}

.stat-item.warning .stat-value {
  color: #f56c6c;
}

.stat-item.notification .stat-value {
  color: #409eff;
}

.stat-label {
  font-size: 12px;
  color: #606266;
  margin-top: 4px;
}

.action-section {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.follow-time {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  text-align: right;
}
</style>
