<template>
  <div class="notification-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>通知消息</span>
            <el-tag
              :type="subscriptionEnabled ? 'success' : 'info'"
              size="small"
              class="subscription-tag"
              @click="goToSubscription"
              style="cursor: pointer;"
            >
              {{ subscriptionEnabled ? '订阅规则已生效' : '订阅规则未启用' }}
            </el-tag>
          </div>
          <div class="header-actions">
            <el-badge :value="unreadCount" class="item" style="margin-right: 15px;">
              <span>未读消息</span>
            </el-badge>
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

      <div class="subscription-info" v-if="subscriptionEnabled">
        <el-alert
          :title="subscriptionInfoText"
          type="success"
          :closable="false"
          show-icon
          style="margin-bottom: 15px;"
        >
          <template #default>
            <span>订阅规则已生效，点击 <el-link type="primary" @click="goToSubscription">这里</el-link> 可修改配置</span>
          </template>
        </el-alert>
      </div>

      <div class="notification-list">
        <div
          v-for="item in notificationList"
          :key="item.id"
          class="notification-item"
          :class="{ 'unread': item.status === 'UNREAD' }"
          @click="handleRead(item)"
        >
          <div class="notification-icon">
            <el-icon v-if="item.notificationType === 'HEALTH_WARNING'" class="warning-icon"><Warning /></el-icon>
            <el-icon v-else class="info-icon"><InfoFilled /></el-icon>
          </div>
          <div class="notification-content">
            <div class="notification-title">
              <span>{{ item.title }}</span>
              <el-tag v-if="item.elderlyName" size="small" type="info">{{ item.elderlyName }}</el-tag>
            </div>
            <div class="notification-desc">{{ item.content }}</div>
            <div class="notification-time">{{ item.createdAt }}</div>
          </div>
          <div class="notification-status">
            <el-dot v-if="item.status === 'UNREAD'" class="unread-dot" />
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
const subscriptionEnabled = ref(false)
const subscription = ref(null)

const subscriptionInfoText = computed(() => {
  if (!subscription.value) return '订阅规则已启用'
  const parts = []
  if (subscription.value.notificationTypes) {
    parts.push('已选择消息类型')
  }
  if (subscription.value.onlyAbnormal) {
    parts.push('仅异常类消息')
  }
  if (subscription.value.onlyFollowedElderly) {
    parts.push('仅关注老人')
  }
  return parts.length > 0 ? `当前过滤条件：${parts.join('、')}` : '订阅规则已启用'
})

const loadNotifications = async () => {
  const res = await request.get('/notification/list/with-subscription')
  notificationList.value = res.data || []
}

const loadUnreadCount = async () => {
  const res = await request.get('/notification/count/with-subscription')
  unreadCount.value = res.data?.unreadCount || 0
  subscriptionEnabled.value = res.data?.subscriptionEnabled || false
  subscription.value = res.data?.subscription || null
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
  await request.put('/notification/read-all')
  ElMessage.success('已全部标记为已读')
  await reloadPageState()
}

const goToSubscription = () => {
  router.push('/notification-subscription')
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

.subscription-tag {
  margin-left: 10px;
}

.header-actions {
  display: flex;
  align-items: center;
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
}

.notification-title span {
  font-weight: bold;
  color: #303133;
  margin-right: 10px;
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
</style>
