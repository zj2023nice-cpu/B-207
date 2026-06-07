<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="4">
        <el-card class="stat-card elderly-card" shadow="hover" @click="goToPage('/elderly')">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewData.elderlyTotal || 0 }}</div>
              <div class="stat-label">老人总数</div>
            </div>
          </div>
          <div class="stat-footer">
            <span>查看详情</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="4">
        <el-card class="stat-card health-card" shadow="hover" @click="goToPage('/health')">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewData.todayHealthRecords || 0 }}</div>
              <div class="stat-label">今日健康记录</div>
            </div>
          </div>
          <div class="stat-footer">
            <span>查看详情</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="4">
        <el-card class="stat-card abnormal-card" shadow="hover" @click="goToPage('/health')">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewData.recentAbnormalElderly || 0 }}</div>
              <div class="stat-label">最近异常人数</div>
            </div>
          </div>
          <div class="stat-footer">
            <span>查看详情</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="4">
        <el-card class="stat-card warning-card" shadow="hover" @click="goToPage('/warning')">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><BellFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewData.pendingWarnings || 0 }}</div>
              <div class="stat-label">待处理预警</div>
            </div>
          </div>
          <div class="stat-footer">
            <span>查看详情</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="4">
        <el-card class="stat-card notification-card" shadow="hover" @click="goToPage('/notification')">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><Message /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewData.unreadNotifications || 0 }}</div>
              <div class="stat-label">未读通知</div>
            </div>
          </div>
          <div class="stat-footer">
            <span>查看详情</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row class="activity-row">
      <el-col :span="24">
        <el-card class="activity-card" v-loading="loading">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon class="header-icon"><Clock /></el-icon>
                最近活动
              </span>
            </div>
          </template>

          <div v-if="!overviewData.recentActivities || overviewData.recentActivities.length === 0" class="empty-state">
            <el-empty description="暂无活动记录" />
          </div>

          <el-timeline v-else>
            <el-timeline-item
              v-for="(activity, index) in overviewData.recentActivities"
              :key="activity.id"
              :timestamp="formatTime(activity.operationTime)"
              :type="activity.success ? 'primary' : 'danger'"
              :icon="activity.success ? 'Check' : 'Close'"
            >
              <el-card shadow="never" class="activity-item">
                <div class="activity-header">
                  <span class="activity-user">{{ activity.username || '系统' }}</span>
                  <el-tag :type="activity.success ? 'success' : 'danger'" size="small">
                    {{ activity.success ? '成功' : '失败' }}
                  </el-tag>
                </div>
                <div class="activity-operation">{{ activity.operation }}</div>
                <div class="activity-desc" v-if="activity.description">
                  {{ activity.description }}
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const overviewData = ref({
  elderlyTotal: 0,
  todayHealthRecords: 0,
  recentAbnormalElderly: 0,
  pendingWarnings: 0,
  unreadNotifications: 0,
  recentActivities: []
})

const loadOverview = async () => {
  loading.value = true
  try {
    const res = await request.get('/dashboard/overview')
    if (res.data) {
      overviewData.value = res.data
    }
  } catch (e) {
    ElMessage.error('加载总览数据失败')
  } finally {
    loading.value = false
  }
}

const goToPage = (path) => {
  router.push(path)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

onMounted(() => {
  loadOverview()
})
</script>

<style scoped>
.dashboard-container {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  margin-bottom: 20px;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
}

.elderly-card .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.health-card .stat-icon {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.abnormal-card .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.warning-card .stat-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.notification-card .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
  color: #909399;
  font-size: 13px;
}

.stat-card:hover .stat-footer {
  color: #409eff;
}

.activity-row {
  margin-top: 0;
}

.activity-card {
  min-height: 400px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
}

.header-icon {
  margin-right: 8px;
  color: #409eff;
}

.activity-item {
  border: 1px solid #ebeef5;
  margin-bottom: 0;
}

.activity-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.activity-user {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.activity-operation {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.activity-desc {
  font-size: 13px;
  color: #909399;
}

.empty-state {
  padding: 60px 0;
}

:deep(.el-timeline-item__timestamp) {
  color: #909399;
  font-size: 12px;
}
</style>
