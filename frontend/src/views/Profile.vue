<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人资料与安全设置</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="基本资料" name="profile">
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
          >
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="展示名称" prop="displayName">
              <el-input v-model="profileForm.displayName" placeholder="请输入展示名称" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入联系电话" maxlength="11" />
            </el-form-item>
            <el-form-item label="用户角色">
              <el-tag :type="profileForm.role === 'admin' ? 'danger' : 'primary'">
                {{ profileForm.role === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </el-form-item>
            <el-form-item label="最近登录">
              <span>{{ formatDateTime(profileForm.lastLoginTime) }}</span>
            </el-form-item>
            <el-form-item label="注册时间">
              <span>{{ formatDateTime(profileForm.createdAt) }}</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile" :loading="profileLoading">
                保存修改
              </el-button>
              <el-button @click="loadProfile">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="password-form"
          >
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
                确认修改
              </el-button>
              <el-button @click="resetPasswordForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="登录会话管理" name="sessions">
          <div class="sessions-container">
            <div class="sessions-header">
              <span>最近登录记录与活跃会话</span>
              <el-button type="primary" size="small" @click="loadSessions" :icon="Refresh">刷新</el-button>
            </div>
            <el-table :data="sessionsList" v-loading="sessionsLoading" stripe>
              <el-table-column prop="loginTime" label="登录时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.loginTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="loginIp" label="来源IP" width="140" />
              <el-table-column prop="os" label="操作系统" width="120" />
              <el-table-column prop="browser" label="浏览器" width="150" />
              <el-table-column prop="deviceType" label="设备类型" width="100">
                <template #default="{ row }">
                  {{ getDeviceTypeLabel(row.deviceType) }}
                </template>
              </el-table-column>
              <el-table-column prop="lastActiveTime" label="最后活跃" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.lastActiveTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" size="small">
                    {{ getStatusLabel(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="当前会话" width="100">
                <template #default="{ row }">
                  <el-tag v-if="row.isCurrent" type="success" size="small">当前</el-tag>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button
                    v-if="!row.isCurrent && row.status === 'ACTIVE'"
                    type="danger"
                    size="small"
                    @click="handleInvalidateSession(row)"
                  >
                    使失效
                  </el-button>
                  <span v-else>-</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('profile')
const profileLoading = ref(false)
const passwordLoading = ref(false)
const sessionsLoading = ref(false)
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const sessionsList = ref([])

const profileForm = ref({
  username: '',
  displayName: '',
  phone: '',
  role: '',
  lastLoginTime: '',
  createdAt: ''
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const profileRules = {
  displayName: [
    { max: 100, message: '展示名称长度不能超过100个字符', trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const loadProfile = async () => {
  try {
    const res = await request.get('/users/profile')
    if (res.data) {
      profileForm.value = {
        username: res.data.username || '',
        displayName: res.data.displayName || '',
        phone: res.data.phone || '',
        role: res.data.role || 'user',
        lastLoginTime: res.data.lastLoginTime || '',
        createdAt: res.data.createdAt || ''
      }
    }
  } catch (error) {
    console.error('加载个人资料失败:', error)
    ElMessage.error('加载个人资料失败')
  }
}

const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return
  try {
    await profileFormRef.value.validate()
  } catch (error) {
    return
  }

  profileLoading.value = true
  try {
    await request.put('/users/profile', {
      displayName: profileForm.value.displayName,
      phone: profileForm.value.phone
    })
    ElMessage.success('资料更新成功')
    const userStr = localStorage.getItem('user')
    if (userStr) {
      const user = JSON.parse(userStr)
      user.displayName = profileForm.value.displayName
      user.phone = profileForm.value.phone
      localStorage.setItem('user', JSON.stringify(user))
    }
  } catch (error) {
    console.error('更新资料失败:', error)
    ElMessage.error(error.message || '更新资料失败')
  } finally {
    profileLoading.value = false
  }
}

const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
  } catch (error) {
    return
  }

  if (passwordForm.value.oldPassword === passwordForm.value.newPassword) {
    ElMessage.warning('新密码不能与旧密码相同')
    return
  }

  try {
    await ElMessageBox.confirm('确定要修改密码吗？修改成功后需要重新登录。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  passwordLoading.value = true
  try {
    await request.put('/users/password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
      confirmPassword: passwordForm.value.confirmPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    localStorage.removeItem('user')
    router.push('/login')
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    passwordLoading.value = false
  }
}

const getDeviceTypeLabel = (type) => {
  const typeMap = {
    'COMPUTER': '电脑',
    'MOBILE': '手机',
    'TABLET': '平板',
    'UNKNOWN': '未知'
  }
  return typeMap[type] || type || '未知'
}

const getStatusLabel = (status) => {
  const statusMap = {
    'ACTIVE': '活跃',
    'EXPIRED': '已过期',
    'INVALIDATED': '已失效',
    'LOGOUT': '已退出'
  }
  return statusMap[status] || status || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    'ACTIVE': 'success',
    'EXPIRED': 'info',
    'INVALIDATED': 'danger',
    'LOGOUT': 'warning'
  }
  return typeMap[status] || 'info'
}

const loadSessions = async () => {
  sessionsLoading.value = true
  try {
    const res = await request.get('/user-sessions/my-sessions')
    if (res.data) {
      sessionsList.value = res.data
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
    ElMessage.error('加载会话列表失败')
  } finally {
    sessionsLoading.value = false
  }
}

const handleInvalidateSession = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要使该会话失效吗？该设备将需要重新登录。\n登录IP: ${row.loginIp}\n浏览器: ${row.browser}`,
      '确认使会话失效',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  try {
    await request.post(`/user-sessions/invalidate/${row.id}`)
    ElMessage.success('会话已失效')
    loadSessions()
  } catch (error) {
    console.error('使会话失效失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

onMounted(() => {
  loadProfile()
  loadSessions()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.profile-form,
.password-form {
  max-width: 500px;
  padding: 20px 0;
}

.sessions-container {
  padding: 20px 0;
}

.sessions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}
</style>
