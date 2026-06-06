<template>
  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑交接班记录' : '新建交接班记录'" width="800px">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="交班人" prop="handoverPerson">
            <el-input v-model="form.handoverPerson" placeholder="请输入交班人姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="接班人" prop="takeoverPerson">
            <el-input v-model="form.takeoverPerson" placeholder="请输入接班人姓名" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="交接时间" prop="handoverTime">
        <el-date-picker
          v-model="form.handoverTime"
          type="datetime"
          placeholder="选择交接时间"
          style="width: 100%"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="重点关注老人" prop="keyElderly">
        <el-input v-model="form.keyElderly" type="textarea" :rows="3" placeholder="请输入重点关注的老人信息，如姓名、房间号、特殊情况等" />
      </el-form-item>
      <el-form-item label="关联预警">
        <div style="margin-bottom: 10px;">
          <el-checkbox v-model="selectAll" @change="handleSelectAll" :indeterminate="isIndeterminate">
            全选待处理预警
          </el-checkbox>
        </div>
        <el-card style="max-height: 300px; overflow-y: auto;">
          <el-empty v-if="pendingWarnings.length === 0" description="暂无待处理预警" />
          <div v-else>
            <div v-for="warning in pendingWarnings" :key="warning.id" class="warning-item">
              <el-checkbox :value="warning.id" v-model="selectedWarningIds">
                <span class="warning-elderly">{{ warning.elderlyName || '未知老人' }}</span>
                <el-tag :type="getWarningLevelType(warning.warningLevel)" size="small" style="margin-left: 10px;">
                  {{ warning.warningLevel }}
                </el-tag>
                <span class="warning-message" style="margin-left: 10px;">{{ warning.warningMessage }}</span>
                <span class="warning-time" style="margin-left: 10px; color: #999; font-size: 12px;">
                  {{ warning.createdAt }}
                </span>
              </el-checkbox>
            </div>
          </div>
        </el-card>
      </el-form-item>
      <el-form-item label="待跟进预警摘要">
        <el-input 
          v-model="form.pendingWarningSummary" 
          type="textarea" 
          :rows="2" 
          placeholder="摘要会根据选择的预警自动生成，也可手动编辑"
        />
        <el-button size="small" type="primary" plain @click="generateSummary" style="margin-top: 8px;">
          自动生成摘要
        </el-button>
      </el-form-item>
      <el-form-item label="备注" prop="remarks">
        <el-input v-model="form.remarks" type="textarea" :rows="3" placeholder="请输入其他需要交接的备注信息" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSave" :loading="submitting">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: Boolean,
  record: Object
})

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formRef = ref(null)
const submitting = ref(false)
const pendingWarnings = ref([])
const selectedWarningIds = ref([])
const selectAll = ref(false)
const isIndeterminate = ref(false)

const isEdit = computed(() => !!props.record?.id)

const form = ref({
  handoverPerson: '',
  takeoverPerson: '',
  handoverTime: null,
  keyElderly: '',
  pendingWarningSummary: '',
  remarks: '',
  warningRecordIds: []
})

const rules = {
  handoverPerson: [
    { required: true, message: '请输入交班人', trigger: 'blur' }
  ],
  takeoverPerson: [
    { required: true, message: '请输入接班人', trigger: 'blur' }
  ],
  handoverTime: [
    { required: true, message: '请选择交接时间', trigger: 'change' }
  ]
}

const getWarningLevelType = (level) => {
  const map = {
    HIGH: 'danger',
    MEDIUM: 'warning',
    LOW: 'info'
  }
  return map[level] || 'info'
}

const loadPendingWarnings = async () => {
  try {
    const res = await request.get('/warning/record/pending')
    pendingWarnings.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const handleSelectAll = (val) => {
  if (val) {
    selectedWarningIds.value = pendingWarnings.value.map(w => w.id)
    isIndeterminate.value = false
  } else {
    selectedWarningIds.value = []
    isIndeterminate.value = false
  }
}

watch(selectedWarningIds, (val) => {
  if (val.length === 0) {
    selectAll.value = false
    isIndeterminate.value = false
  } else if (val.length === pendingWarnings.value.length) {
    selectAll.value = true
    isIndeterminate.value = false
  } else {
    selectAll.value = false
    isIndeterminate.value = true
  }
}, { deep: true })

const generateSummary = async () => {
  if (selectedWarningIds.value.length === 0) {
    form.value.pendingWarningSummary = ''
    return
  }
  try {
    const res = await request.post('/shift-handover/generate-summary', {
      warningIds: selectedWarningIds.value
    })
    form.value.pendingWarningSummary = res.data
  } catch (error) {
    console.error(error)
  }
}

const resetForm = () => {
  form.value = {
    handoverPerson: '',
    takeoverPerson: '',
    handoverTime: null,
    keyElderly: '',
    pendingWarningSummary: '',
    remarks: '',
    warningRecordIds: []
  }
  selectedWarningIds.value = []
  selectAll.value = false
  isIndeterminate.value = false
}

const fillForm = (record) => {
  form.value = {
    handoverPerson: record.handoverPerson || '',
    takeoverPerson: record.takeoverPerson || '',
    handoverTime: record.handoverTime || null,
    keyElderly: record.keyElderly || '',
    pendingWarningSummary: record.pendingWarningSummary || '',
    remarks: record.remarks || '',
    warningRecordIds: []
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    loadPendingWarnings()
    if (isEdit.value && props.record) {
      fillForm(props.record)
    } else {
      resetForm()
      const now = new Date()
      const formatted = now.getFullYear() + '-' +
        String(now.getMonth() + 1).padStart(2, '0') + '-' +
        String(now.getDate()).padStart(2, '0') + ' ' +
        String(now.getHours()).padStart(2, '0') + ':' +
        String(now.getMinutes()).padStart(2, '0') + ':' +
        String(now.getSeconds()).padStart(2, '0')
      form.value.handoverTime = formatted
    }
  }
})

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = {
      ...form.value,
      warningRecordIds: selectedWarningIds.value
    }

    if (isEdit.value) {
      await request.put(`/shift-handover/update/${props.record.id}`, data)
    } else {
      await request.post('/shift-handover/add', data)
    }

    ElMessage.success('操作成功')
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.warning-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.warning-item:last-child {
  border-bottom: none;
}

.warning-elderly {
  font-weight: 500;
  color: #303133;
}

.warning-message {
  color: #606266;
}
</style>
