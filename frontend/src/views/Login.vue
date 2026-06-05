<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2 style="text-align: center; margin: 0">智慧养老系统登录</h2>
      </template>
      <el-form :model="loginForm" label-width="0">
        <el-form-item>
          <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item v-if="showCaptcha">
          <div style="display: flex; gap: 10px">
            <el-input v-model="loginForm.captchaCode" placeholder="验证码" prefix-icon="Key" style="flex: 1" />
            <img
              :src="captchaImage"
              alt="验证码"
              class="captcha-image"
              @click="refreshCaptcha"
              title="点击刷新验证码"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" @click="handleLogin" :loading="loading">登录</el-button>
        </el-form-item>
        <el-form-item>
           <el-link type="info" @click="showRegister = true">没有账号？点击注册</el-link>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="showRegister" title="用户注册" width="400px">
      <el-form :model="registerForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" @click="handleRegister">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const showRegister = ref(false)
const showCaptcha = ref(false)
const captchaImage = ref('')
const captchaUuid = ref('')

const loginForm = ref({
  username: '',
  password: '',
  captchaCode: ''
})

const registerForm = ref({
  username: '',
  password: ''
})

const refreshCaptcha = async () => {
  try {
    const res = await request.get('/users/captcha')
    captchaImage.value = res.data.image
    captchaUuid.value = res.data.uuid
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败')
  }
}

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  if (showCaptcha.value && !loginForm.value.captchaCode) {
    ElMessage.warning('请输入验证码')
    return
  }

  loading.value = true
  try {
    const loginData = {
      username: loginForm.value.username,
      password: loginForm.value.password
    }

    if (showCaptcha.value) {
      loginData.captchaUuid = captchaUuid.value
      loginData.captchaCode = loginForm.value.captchaCode
    }

    const res = await request.post('/users/login', loginData)
    localStorage.setItem('user', JSON.stringify(res.data))
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error(error)
    const errorMessage = error.message || '登录失败'
    console.log('登录失败，错误消息:', errorMessage)
    
    const needsCaptcha = errorMessage.includes('验证码') || errorMessage.includes('请输入验证码')
    
    if (needsCaptcha) {
      console.log('需要显示验证码框')
      if (!showCaptcha.value) {
        showCaptcha.value = true
      }
      await refreshCaptcha()
      console.log('验证码图片加载完成:', captchaImage.value ? '成功' : '失败')
    } else if (showCaptcha.value) {
      await refreshCaptcha()
    }
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
    if (!registerForm.value.username || !registerForm.value.username.trim()) {
        ElMessage.warning('注册用户名不能为空')
        return
    }
    if (!registerForm.value.password || !registerForm.value.password.trim()) {
        ElMessage.warning('注册密码不能为空')
        return
    }
    try {
        await request.post('/users/register', registerForm.value)
        ElMessage.success('注册成功，请登录')
        showRegister.value = false
    } catch (error) {
        console.error(error)
    }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}
.captcha-image {
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
</style>
