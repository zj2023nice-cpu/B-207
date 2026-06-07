<template>
  <div class="notification-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>通知消息</span>
            <el-tag
              v-if="inDoNotDisturb"
              type="warning"
              size="small"
              class="status-tag"
            >
              <el-icon><Moon /></el-icon>
              免打扰中
            </el-tag>
            <el-tag
              :type="preferenceEnabled ? 'success' : 'info'"
              size="small"
              class="status-tag"
              @click="goToPreference"
              style="cursor: pointer;"
            >
              {{ preferenceEnabled ? '偏好已生效' : '偏好未配置' }}
            </el-tag>
          </div>
          <div class="header-actions">
            <div class="unread-summary" v-if="unreadCount > 0">
              <el-badge :value="highPriorityUnread" :hidden="highPriorityUnread === 0" type="danger" class="badge-item">
                <span class="summary-text">高优未读</span>
              </el-badge>
              <el-badge :value="normalUnread" :hidden="normalUnread === 0" type="primary" class="badge-item">
                <span class="summary-text">普通未读</span>
              </el-badge>
            </div>
            <el-button v-if="unreadCount > 0" type="primary" size="small" @click="handleMarkAllRead">
              全部已读
            </el-button>
            <el-button type="default" size="small" @click="loadNotifications" style="margin-left: 10px;">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <div class="preference-info" v-if="preferenceEnabled">
        <el-alert
          :title="preferenceInfoText"
          type="success"
          :closable="false"
          show-icon
          style="margin-bottom: 15px;"
        >
          <template #default>
            <span>消息偏好已生效，点击 <el-link type="primary" @click="goToPreference">这里</el-link> 可修改配置</span>
          </template>
        </el-alert>
      </div>

      <div class="notification-list">
        <div
          v-for="item in notificationList"
          :key="item.id"
          class="notification-item"
          :class="{ 
            'unread': item.status === 'UNREAD',
            'high-priority': item.highPriority 
          }"
          @click="handleRead(item)"
        >
          <div class="notification-icon">
            <el-icon v-if="item.notificationType === 'HEALTH_WARNING'" class="warning-icon"><Warning /></el-icon>
            <el-icon v-else class="info-icon"><InfoFilled /></el-icon>
          </div>
          <div class="notification-content">
            <div class="notification-title">
              <span>{{ item.title }}</span>
              <el-tag v-if="item.highPriority" type="danger" size="small" effect="dark">高优先级</el-tag>
              <el-tag v-if="item.elderlyName" size="small" type="info">{{ item.elderlyName }}</el-tag>
            </div>
            <div class="notification-desc">{{ item.content }}</div>
            <div class="notification-time">{{ item.createdAt }}</div>
          </div>
          <div class="notification-status">
            <el-dot v-if="item.status === 'UNREAD'" class="unread-dot" :class="{ 'high-priority-dot': item.highPriority }" />
          </div>
        </div>

        <el-empty v-if="notificationList.length === 0" description="暂无通知消息" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const notificationList = ref([])
const unreadCount = ref(0)
const highPriorityUnread = ref(0)
const normalUnread = ref(0)
const preferenceEnabled = ref(false)
const inDoNotDisturb = ref(false)
const preference = ref(null)

const preferenceInfoText = computed(() => {
  if (!preference.value) return '消息偏好已启用'
  const parts = []
  if (preference.value.enabledTypes) {
    parts.push('已选择显示类型')
  }
  if (preference.value.highPriorityTypes) {
    parts.push('已设置高优先级')
  }
  if (preference.value.doNotDisturbEnabled) {
    parts.push('已开启免打扰')
  }
  return parts.length > 0 ? `当前配置：${parts.join('、')}` : '消息偏好已启用'
})

const loadNotifications = async () => {
  const res = await request.get('/notification/list/with-preference')
  notificationList.value = res.data || []
}

const loadUnreadCount = async () => {
  const res = await request.get('/notification/count/with-preference')
  unreadCount.value = res.data?.totalUnread || 0
  highPriorityUnread.value = res.data?.highPriorityUnread || 0
  normalUnread.value = res.data?.normalUnread || 0
  inDoNotDisturb.value = res.data?.inDoNotDisturb || false
  preferenceEnabled.value = !!res.data?.preference
  preference.value = res.data?.preference || null
}

const reloadPageState = async () => {
  await Promise.all([loadNotifications(), loadUnreadCount()])
}

const handleRead = async (item) => {
  if (item.status === 'UNREAD') {
    await request.put(`/notification/read/${item.id}`)
    await reloadPageState()
  }
}

const handleMarkAllRead = async () => {
  await request.put('/notification/read-all/with-preference')
  ElMessage.success('已全部标记为已读')
  await reloadPageState()
}

const goToPreference = () => {
  router.push('/notification-preference')
}

onMounted(() => {
  reloadPageState()
})
</script>

<style scoped>
.notification-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-tag {
  margin-left: 5px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.unread-summary {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-right: 15px;
}

.badge-item {
  display: flex;
  align-items: center;
}

.summary-text {
  font-size: 13px;
  color: #606266;
  font-weight: normal;
}

.notification-list {
  max-height: 600px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 15px;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #F5F7FA;
}

.notification-item.unread {
  background-color: #ECF5FF;
}

.notification-item.unread:hover {
  background-color: #D9ECFF;
}

.notification-item.high-priority.unread {
  background-color: #FEF0F0;
  border-left: 3px solid #F56C6C;
}

.notification-item.high-priority.unread:hover {
  background-color: #FDE2E2;
}

.notification-icon {
  margin-right: 15px;
  padding-top: 3px;
}

.warning-icon {
  font-size: 24px;
  color: #F56C6C;
}

.info-icon {
  font-size: 24px;
  color: #409EFF;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  gap: 8px;
}

.notification-title span {
  font-weight: bold;
  color: #303133;
}

.notification-desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 5px;
  word-break: break-all;
}

.notification-time {
  color: #909399;
  font-size: 12px;
}

.notification-status {
  display: flex;
  align-items: center;
  margin-left: 10px;
}

.unread-dot {
  background-color: #F56C6C;
}

.high-priority-dot {
  width: 12px;
  height: 12px;
  background-color: #F56C6C;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.7);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(245, 108, 108, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0);
  }
}
</style>
