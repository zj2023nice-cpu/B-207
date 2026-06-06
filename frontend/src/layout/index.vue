<template>
  <div class="layout-container">
    <el-container style="height: 100vh">
      <el-aside width="200px">
        <el-menu
          router
          :default-active="$route.path"
          class="el-menu-vertical"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <div class="logo">智慧养老系统</div>
          <el-menu-item index="/elderly">
            <el-icon><User /></el-icon>
            <span>老人管理</span>
          </el-menu-item>
          <el-menu-item index="/elderly-tag">
            <el-icon><PriceTag /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
          <el-menu-item index="/health">
            <el-icon><Monitor /></el-icon>
            <span>健康监测</span>
          </el-menu-item>
          <el-menu-item index="/warning">
            <el-icon><Warning /></el-icon>
            <span>健康预警</span>
          </el-menu-item>
          <el-menu-item index="/threshold">
            <el-icon><Setting /></el-icon>
            <span>阈值配置</span>
          </el-menu-item>
          <el-menu-item index="/notification">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
              <el-icon><Bell /></el-icon>
              <span>通知消息</span>
            </el-badge>
          </el-menu-item>
          <el-menu-item index="/operation-log">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item index="/nursing-observation">
            <el-icon><Notebook /></el-icon>
            <span>护理观察记录</span>
          </el-menu-item>
          <el-menu-item index="/visitor-visit">
            <el-icon><UserFilled /></el-icon>
            <span>访客来访登记</span>
          </el-menu-item>
          <el-menu-item index="/shift-handover">
            <el-icon><Tickets /></el-icon>
            <span>交接班记录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <div class="header-left">
            <span>{{ $route.meta.title }}</span>
          </div>
          <div class="header-right">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="header-notification" @click="goToNotification">
              <el-icon class="notification-icon"><Bell /></el-icon>
            </el-badge>
            <el-dropdown @command="handleCommand">
              <span class="el-dropdown-link">
                {{ username }}<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const username = ref('管理员')
const unreadCount = ref(0)
let timer = null

const loadUnreadCount = async () => {
  try {
    const res = await request.get('/notification/count')
    unreadCount.value = res.data?.unreadCount || 0
  } catch {
    unreadCount.value = 0
  }
}

const goToNotification = () => {
  router.push('/notification')
}

onMounted(() => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    username.value = user.username
  }
  
  loadUnreadCount()
  timer = setInterval(loadUnreadCount, 30000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('user')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}
.el-menu-vertical {
  height: 100%;
  border-right: none;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: white;
  font-weight: bold;
  font-size: 18px;
  background-color: #2b2f3a;
}
.el-header {
  background-color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e6e6e6;
}
.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
}

.notification-badge {
  width: 100%;
}

.notification-badge :deep(.el-badge__content) {
  top: 8px;
  right: 8px;
}

.header-notification {
  margin-right: 20px;
  cursor: pointer;
}

.notification-icon {
  font-size: 20px;
  color: #606266;
}

.notification-icon:hover {
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
}
</style>
