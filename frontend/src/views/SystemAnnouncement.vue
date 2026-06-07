<template>
  <div class="announcement-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统公告</span>
          <div class="header-actions">
            <el-badge :value="unreadCount" class="item" style="margin-right: 15px;">
              <span>未读公告</span>
            </el-badge>
            <el-button v-if="unreadCount > 0" type="primary" size="small" @click="handleMarkAllRead">
              全部已读
            </el-button>
            <el-button type="default" size="small" @click="loadAnnouncements" style="margin-left: 10px;">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="announcement-list">
        <div 
          v-for="item in announcementList" 
          :key="item.id" 
          class="announcement-item"
          :class="{ 'unread': !item.isRead }"
          @click="handleViewDetail(item)"
        >
          <div class="announcement-left">
            <div class="announcement-tags">
              <el-tag v-if="item.isPinned" type="danger" size="small" effect="dark">置顶</el-tag>
            </div>
            <div class="announcement-content">
              <div class="announcement-title">
                <span>{{ item.title }}</span>
              </div>
              <div class="announcement-summary">{{ item.content }}</div>
              <div class="announcement-meta">
                <span class="publisher">{{ item.publisherName }}</span>
                <span class="time">{{ formatTime(item.publishStartTime) }}</span>
              </div>
            </div>
          </div>
          <div class="announcement-status">
            <el-dot v-if="!item.isRead" class="unread-dot" />
          </div>
        </div>
        
        <el-empty v-if="announcementList.length === 0" description="暂无系统公告" />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" :title="currentAnnouncement?.title" width="600px">
      <div v-if="currentAnnouncement" class="announcement-detail">
        <div class="detail-meta">
          <el-tag v-if="currentAnnouncement.isPinned" type="danger" size="small" effect="dark">置顶</el-tag>
          <span class="publisher">发布人：{{ currentAnnouncement.publisherName }}</span>
          <span class="time">发布时间：{{ formatTime(currentAnnouncement.publishStartTime) }}</span>
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
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const announcementList = ref([])
const unreadCount = ref(0)
const detailVisible = ref(false)
const currentAnnouncement = ref(null)

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

const loadAnnouncements = async () => {
  const res = await request.get('/system-announcement/list')
  announcementList.value = res.data || []
}

const loadUnreadCount = async () => {
  const res = await request.get('/system-announcement/unread-count')
  unreadCount.value = res.data?.unreadCount || 0
}

const handleViewDetail = async (item) => {
  if (!item.isRead) {
    await request.put(`/system-announcement/read/${item.id}`)
    item.isRead = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  const res = await request.get(`/system-announcement/detail/${item.id}`)
  currentAnnouncement.value = res.data
  detailVisible.value = true
}

const handleMarkAllRead = async () => {
  await request.put('/system-announcement/read-all')
  ElMessage.success('已全部标记为已读')
  announcementList.value.forEach(item => {
    item.isRead = true
  })
  unreadCount.value = 0
}

onMounted(() => {
  loadAnnouncements()
  loadUnreadCount()
})
</script>

<style scoped>
.announcement-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.header-actions {
  display: flex;
  align-items: center;
}

.announcement-list {
  max-height: 600px;
  overflow-y: auto;
}

.announcement-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 15px;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.announcement-item:hover {
  background-color: #F5F7FA;
}

.announcement-item.unread {
  background-color: #ECF5FF;
}

.announcement-item.unread:hover {
  background-color: #D9ECFF;
}

.announcement-left {
  display: flex;
  align-items: flex-start;
  flex: 1;
  min-width: 0;
}

.announcement-tags {
  margin-right: 12px;
  padding-top: 2px;
}

.announcement-content {
  flex: 1;
  min-width: 0;
}

.announcement-title {
  margin-bottom: 8px;
}

.announcement-title span {
  font-weight: bold;
  color: #303133;
  font-size: 15px;
}

.announcement-summary {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 12px;
}

.announcement-status {
  display: flex;
  align-items: center;
  margin-left: 10px;
  padding-top: 5px;
}

.unread-dot {
  background-color: #F56C6C;
}

.announcement-detail {
  padding: 10px 0;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
  margin-bottom: 20px;
  color: #909399;
  font-size: 13px;
}

.detail-content {
  line-height: 1.8;
  color: #303133;
  font-size: 14px;
}
</style>
