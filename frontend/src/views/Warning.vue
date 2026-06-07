<template>
  <div class="warning-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>健康预警记录</span>
          <div class="header-actions">
            <el-select v-model="filterStatus" placeholder="筛选状态" @change="loadWarnings" clearable style="width: 120px; margin-right: 10px;">
              <el-option label="待处理" value="PENDING" />
              <el-option label="已读" value="READ" />
              <el-option label="已处理" value="HANDLED" />
            </el-select>
            <el-button type="primary" @click="loadWarnings" style="margin-right: 10px;">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="primary" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table :data="warningList" border style="width: 100%" v-loading="loading" max-height="500">
        <el-table-column prop="elderlyName" label="老人姓名" width="100" />
        <el-table-column prop="indicatorType" label="指标类型" width="100">
          <template #default="scope">
            <span>{{ getIndicatorName(scope.row.indicatorType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualValue" label="实际值" width="100">
          <template #default="scope">
            <span class="abnormal">{{ scope.row.actualValue }}{{ getIndicatorUnit(scope.row.indicatorType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="thresholdValue" label="阈值" width="100">
          <template #default="scope">
            <span>{{ scope.row.thresholdValue }}{{ getIndicatorUnit(scope.row.indicatorType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="warningLevel" label="级别" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.warningLevel === 'HIGH' ? 'danger' : 'warning'" size="small">
              {{ scope.row.warningLevel === 'HIGH' ? '偏高' : '偏低' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warningMessage" label="预警内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="healthRecordId" label="关联记录" width="80">
          <template #default="scope">
            <span v-if="scope.row.healthRecordId">#{{ scope.row.healthRecordId }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'PENDING'" 
              type="primary" 
              size="small" 
              @click="handleMarkAsRead(scope.row)"
            >
              标记已读
            </el-button>
            <el-button 
              v-if="scope.row.status === 'PENDING' || scope.row.status === 'READ'" 
              type="success" 
              size="small" 
              @click="showHandleDialog(scope.row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="warningList.length === 0 && !loading" description="暂无预警记录" />
    </el-card>

    <el-dialog v-model="handleDialogVisible" title="处理预警" width="500px">
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="预警信息">
          <el-input :value="currentWarning?.warningMessage" disabled />
        </el-form-item>
        <el-form-item label="处理人">
          <el-input v-model="handleForm.handledBy" placeholder="请输入处理人姓名" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input 
            v-model="handleForm.remark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入处理备注" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()

const loading = ref(false)
const filterStatus = ref(null)
const warningList = ref([])
const handleDialogVisible = ref(false)
const currentWarning = ref(null)
const handleForm = ref({
  handledBy: '',
  remark: ''
})

const indicatorNames = {
  temperature: '体温',
  systolicPressure: '收缩压',
  diastolicPressure: '舒张压',
  heartRate: '心率',
  bloodOxygen: '血氧',
  bloodSugar: '血糖'
}

const indicatorUnits = {
  temperature: '℃',
  systolicPressure: 'mmHg',
  diastolicPressure: 'mmHg',
  heartRate: '次/分',
  bloodOxygen: '%',
  bloodSugar: 'mmol/L'
}

const statusTypes = {
  PENDING: 'danger',
  READ: 'warning',
  HANDLED: 'success'
}

const statusNames = {
  PENDING: '待处理',
  READ: '已读',
  HANDLED: '已处理'
}

const getIndicatorName = (type) => indicatorNames[type] || type
const getIndicatorUnit = (type) => indicatorUnits[type] || ''
const getStatusType = (status) => statusTypes[status] || 'info'
const getStatusName = (status) => statusNames[status] || status

const loadWarnings = async () => {
  loading.value = true
  try {
    const res = await request.get('/warning/record/list', {
      params: {
        status: filterStatus.value
      }
    })
    warningList.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleMarkAsRead = async (row) => {
  await request.put(`/warning/record/read/${row.id}`)
  ElMessage.success('已标记为已读')
  loadWarnings()
}

const showHandleDialog = (row) => {
  currentWarning.value = row
  handleForm.value = {
    handledBy: '',
    remark: ''
  }
  handleDialogVisible.value = true
}

const submitHandle = async () => {
  if (!handleForm.value.handledBy) {
    ElMessage.warning('请输入处理人')
    return
  }
  
  await request.put(`/warning/record/handle/${currentWarning.value.id}`, {
    handledBy: handleForm.value.handledBy,
    remark: handleForm.value.remark
  })
  
  ElMessage.success('处理成功')
  handleDialogVisible.value = false
  loadWarnings()
}

const handleExport = async () => {
  try {
    const exportParams = JSON.stringify({
      status: filterStatus.value
    })
    let rangeDesc = '全部预警记录'
    if (filterStatus.value) {
      rangeDesc = `状态: ${getStatusName(filterStatus.value)}`
    }
    const res = await request.post('/export-task/create', {
      exportType: 'WARNING_RECORD',
      exportParams: exportParams,
      exportRangeDesc: rangeDesc,
      taskName: '预警记录导出'
    })
    ElMessage.success('导出任务已创建，请在导出任务中心查看进度')
    router.push('/export-task')
  } catch (error) {
    console.error(error)
    ElMessage.error('创建导出任务失败')
  }
}

onMounted(() => {
  loadWarnings()
})
</script>

<style scoped>
.warning-container {
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

.abnormal {
  color: #F56C6C;
  font-weight: bold;
}

.text-muted {
  color: #909399;
}
</style>
