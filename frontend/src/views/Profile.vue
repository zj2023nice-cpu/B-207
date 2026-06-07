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
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const activeTab = ref('profile')
const profileLoading = ref(false)
const passwordLoading = ref(false)
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

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

onMounted(() => {
  loadProfile()
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
</style>
