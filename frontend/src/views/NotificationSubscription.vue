<template>
  <div class="subscription-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通知订阅规则配置</span>
          <el-tag :type="formData.enabled ? 'success' : 'info'" size="small">
            {{ formData.enabled ? '规则已启用' : '规则已禁用' }}
          </el-tag>
        </div>
      </template>

      <el-form :model="formData" label-width="180px" class="subscription-form">
        <el-form-item label="启用订阅规则">
          <el-switch v-model="formData.enabled" active-text="启用" inactive-text="禁用" />
          <div class="form-tip">启用后，系统将根据您配置的规则过滤通知消息</div>
        </el-form-item>

        <el-form-item label="订阅消息类型">
          <el-checkbox-group v-model="selectedTypes">
            <el-checkbox label="HEALTH_WARNING">健康预警</el-checkbox>
            <el-checkbox label="SYSTEM">系统通知</el-checkbox>
            <el-checkbox label="ANNOUNCEMENT">公告通知</el-checkbox>
            <el-checkbox label="NURSING">护理提醒</el-checkbox>
          </el-checkbox-group>
          <div class="form-tip">不勾选任何类型表示接收所有类型的消息</div>
        </el-form-item>

        <el-form-item label="仅接收异常类消息">
          <el-switch v-model="formData.onlyAbnormal" />
          <div class="form-tip">开启后，仅接收包含 WARNING 的预警类通知</div>
        </el-form-item>

        <el-form-item label="仅接收关注老人通知">
          <el-switch v-model="formData.onlyFollowedElderly" />
          <div class="form-tip">开启后，仅接收与您关注的老人相关的通知</div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">
            保存配置
          </el-button>
          <el-button @click="handleReset">重置为默认</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span>规则说明</span>
      </template>
      <div class="rule-description">
        <h4>配置字段说明：</h4>
        <ul>
          <li><strong>启用订阅规则</strong>：总开关，关闭后将接收所有通知，不受其他规则限制</li>
          <li><strong>订阅消息类型</strong>：按消息类型过滤，支持多选。留空表示接收所有类型</li>
          <li><strong>仅接收异常类消息</strong>：只接收预警类（包含 WARNING）通知</li>
          <li><strong>仅接收关注老人通知</strong>：只接收与您已关注老人相关的通知</li>
        </ul>
        <h4>默认行为：</h4>
        <ul>
          <li>未配置任何规则时，默认接收所有通知消息</li>
          <li>多个规则条件同时生效时，取交集（AND 关系）</li>
        </ul>
        <h4>生效时机：</h4>
        <ul>
          <li>规则保存后立即生效，下次加载通知列表和未读数时自动应用</li>
          <li>已存在的历史通知不受影响，仅影响查询结果的展示</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const formData = ref({
  enabled: true,
  notificationTypes: '',
  onlyAbnormal: false,
  onlyFollowedElderly: false
})

const selectedTypes = ref([])
const saving = ref(false)

const loadSubscription = async () => {
  try {
    const res = await request.get('/notification-subscription')
    if (res.data) {
      formData.value = {
        enabled: res.data.enabled ?? true,
        notificationTypes: res.data.notificationTypes ?? '',
        onlyAbnormal: res.data.onlyAbnormal ?? false,
        onlyFollowedElderly: res.data.onlyFollowedElderly ?? false
      }
      if (formData.value.notificationTypes) {
        selectedTypes.value = formData.value.notificationTypes.split(',')
      } else {
        selectedTypes.value = []
      }
    }
  } catch (e) {
    console.error('加载订阅规则失败', e)
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const data = {
      ...formData.value,
      notificationTypes: selectedTypes.value.join(',')
    }
    await request.post('/notification-subscription', data)
    ElMessage.success('配置保存成功')
    await loadSubscription()
  } catch (e) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  formData.value = {
    enabled: true,
    notificationTypes: '',
    onlyAbnormal: false,
    onlyFollowedElderly: false
  }
  selectedTypes.value = []
}

onMounted(() => {
  loadSubscription()
})
</script>

<style scoped>
.subscription-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.subscription-form {
  max-width: 600px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.rule-description {
  line-height: 1.8;
  color: #606266;
}

.rule-description h4 {
  margin: 15px 0 10px 0;
  color: #303133;
}

.rule-description ul {
  margin: 0 0 10px 20px;
  padding: 0;
}

.rule-description li {
  margin-bottom: 5px;
}
</style>
