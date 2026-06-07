<template>
  <div class="preference-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>消息偏好设置</span>
          <el-tag v-if="inDoNotDisturb" type="warning" size="small">
            <el-icon><Moon /></el-icon>
            免打扰中
          </el-tag>
        </div>
      </template>

      <el-form :model="formData" label-width="180px" class="preference-form">
        <el-divider content-position="left">通知显示设置</el-divider>

        <el-form-item label="显示的通知类型">
          <el-checkbox-group v-model="selectedEnabledTypes">
            <el-checkbox label="HEALTH_WARNING">健康预警</el-checkbox>
            <el-checkbox label="SYSTEM">系统通知</el-checkbox>
            <el-checkbox label="ANNOUNCEMENT">公告通知</el-checkbox>
            <el-checkbox label="NURSING">护理提醒</el-checkbox>
            <el-checkbox label="SHIFT_HANDOVER">交接班提醒</el-checkbox>
          </el-checkbox-group>
          <div class="form-tip">不勾选任何类型表示显示所有类型的消息</div>
        </el-form-item>

        <el-divider content-position="left">优先级提醒设置</el-divider>

        <el-form-item label="高优先级提醒类型">
          <el-checkbox-group v-model="selectedHighPriorityTypes">
            <el-checkbox label="HEALTH_WARNING">健康预警</el-checkbox>
            <el-checkbox label="SYSTEM">系统通知</el-checkbox>
            <el-checkbox label="ANNOUNCEMENT">公告通知</el-checkbox>
            <el-checkbox label="NURSING">护理提醒</el-checkbox>
            <el-checkbox label="SHIFT_HANDOVER">交接班提醒</el-checkbox>
          </el-checkbox-group>
          <div class="form-tip">选中的类型将在消息列表中置顶显示，并在角标中优先统计</div>
        </el-form-item>

        <el-divider content-position="left">免打扰设置</el-divider>

        <el-form-item label="启用免打扰">
          <el-switch v-model="formData.doNotDisturbEnabled" active-text="开启" inactive-text="关闭" />
          <div class="form-tip">开启后，在设置时段内将不会有声音提醒，角标显示可单独配置</div>
        </el-form-item>

        <el-form-item label="免打扰时段" v-if="formData.doNotDisturbEnabled">
          <el-time-picker
            v-model="dndStartTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="开始时间"
            style="width: 140px;"
          />
          <span style="margin: 0 10px;">至</span>
          <el-time-picker
            v-model="dndEndTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="结束时间"
            style="width: 140px;"
          />
          <div class="form-tip">支持跨天设置，例如 22:00 至次日 08:00</div>
        </el-form-item>

        <el-form-item label="免打扰时显示角标" v-if="formData.doNotDisturbEnabled">
          <el-switch v-model="formData.showBadgeInDnd" active-text="显示" inactive-text="隐藏" />
          <div class="form-tip">开启后，免打扰期间仍会显示未读消息角标</div>
        </el-form-item>

        <el-divider content-position="left">其他设置</el-divider>

        <el-form-item label="声音提醒">
          <el-switch v-model="formData.soundEnabled" active-text="开启" inactive-text="关闭" />
          <div class="form-tip">开启后，收到新通知时会播放提示音（免打扰时段除外）</div>
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
        <span>配置说明</span>
      </template>
      <div class="rule-description">
        <h4>配置字段说明：</h4>
        <ul>
          <li><strong>显示的通知类型</strong>：选择哪些类型的通知显示在消息中心，留空表示全部显示</li>
          <li><strong>高优先级提醒类型</strong>：选中的类型会在列表中置顶，并优先统计未读数</li>
          <li><strong>启用免打扰</strong>：在指定时段内关闭声音提醒</li>
          <li><strong>免打扰时段</strong>：设置免打扰的开始和结束时间，支持跨天</li>
          <li><strong>免打扰时显示角标</strong>：免打扰期间是否仍显示未读消息数量</li>
          <li><strong>声音提醒</strong>：新消息到达时是否播放提示音</li>
        </ul>
        <h4>默认策略：</h4>
        <ul>
          <li>默认显示所有类型的通知消息</li>
          <li>默认健康预警（HEALTH_WARNING）为高优先级</li>
          <li>默认关闭免打扰功能</li>
          <li>默认免打扰时段为 22:00 至 08:00</li>
          <li>默认免打扰期间不显示角标</li>
          <li>默认开启声音提醒</li>
        </ul>
        <h4>生效时机：</h4>
        <ul>
          <li>配置保存后立即生效</li>
          <li>下次加载通知列表和未读数时自动应用新配置</li>
          <li>已存在的历史通知不受影响，仅影响查询结果的展示和排序</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const formData = ref({
  enabledTypes: '',
  highPriorityTypes: '',
  doNotDisturbEnabled: false,
  doNotDisturbStart: '22:00',
  doNotDisturbEnd: '08:00',
  showBadgeInDnd: false,
  soundEnabled: true
})

const selectedEnabledTypes = ref([])
const selectedHighPriorityTypes = ref([])
const dndStartTime = ref('22:00')
const dndEndTime = ref('08:00')
const saving = ref(false)
const inDoNotDisturb = ref(false)

const loadPreference = async () => {
  try {
    const res = await request.get('/notification-preference/status')
    if (res.data) {
      inDoNotDisturb.value = res.data.inDoNotDisturb || false
      const pref = res.data.preference
      if (pref) {
        formData.value = {
          enabledTypes: pref.enabledTypes || '',
          highPriorityTypes: pref.highPriorityTypes || '',
          doNotDisturbEnabled: pref.doNotDisturbEnabled ?? false,
          doNotDisturbStart: pref.doNotDisturbStart || '22:00',
          doNotDisturbEnd: pref.doNotDisturbEnd || '08:00',
          showBadgeInDnd: pref.showBadgeInDnd ?? false,
          soundEnabled: pref.soundEnabled ?? true
        }
        if (formData.value.enabledTypes) {
          selectedEnabledTypes.value = formData.value.enabledTypes.split(',')
        } else {
          selectedEnabledTypes.value = []
        }
        if (formData.value.highPriorityTypes) {
          selectedHighPriorityTypes.value = formData.value.highPriorityTypes.split(',')
        } else {
          selectedHighPriorityTypes.value = []
        }
        dndStartTime.value = formData.value.doNotDisturbStart
        dndEndTime.value = formData.value.doNotDisturbEnd
      }
    }
  } catch (e) {
    console.error('加载消息偏好失败', e)
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const data = {
      ...formData.value,
      enabledTypes: selectedEnabledTypes.value.join(','),
      highPriorityTypes: selectedHighPriorityTypes.value.join(','),
      doNotDisturbStart: dndStartTime.value,
      doNotDisturbEnd: dndEndTime.value
    }
    await request.put('/notification-preference', data)
    ElMessage.success('配置保存成功')
    await loadPreference()
  } catch (e) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const handleReset = () => {
  formData.value = {
    enabledTypes: '',
    highPriorityTypes: 'HEALTH_WARNING',
    doNotDisturbEnabled: false,
    doNotDisturbStart: '22:00',
    doNotDisturbEnd: '08:00',
    showBadgeInDnd: false,
    soundEnabled: true
  }
  selectedEnabledTypes.value = []
  selectedHighPriorityTypes.value = ['HEALTH_WARNING']
  dndStartTime.value = '22:00'
  dndEndTime.value = '08:00'
}

onMounted(() => {
  loadPreference()
})
</script>

<style scoped>
.preference-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.preference-form {
  max-width: 700px;
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
