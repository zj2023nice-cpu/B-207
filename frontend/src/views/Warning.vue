<template>
  <div class="warning-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>健康预警记录</span>
          <div class="header-actions">
            <el-select v-model="filterStatus" placeholder="筛选状态" @change="loadWarnings" clearable style="width: 140px; margin-right: 10px;">
              <el-option v-for="s in allStatuses" :key="s.code" :label="s.name" :value="s.code" />
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
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="canDoAction(scope.row.status, 'READ')" 
              type="primary" 
              size="small" 
              @click="handleMarkAsRead(scope.row)"
            >
              标记已读
            </el-button>
            <el-button 
              v-if="canDoAction(scope.row.status, 'HANDLED')" 
              type="success" 
              size="small" 
              @click="showActionDialog(scope.row, 'HANDLED')"
            >
              处理
            </el-button>
            <el-button 
              v-if="canDoAction(scope.row.status, 'IGNORED')" 
              type="info" 
              size="small" 
              @click="showActionDialog(scope.row, 'IGNORED')"
            >
              忽略
            </el-button>
            <el-button 
              v-if="canDoAction(scope.row.status, 'ESCALATED')" 
              type="warning" 
              size="small" 
              @click="showActionDialog(scope.row, 'ESCALATED')"
            >
              升级
            </el-button>
            <el-button 
              v-if="canDoAction(scope.row.status, 'REOPENED')" 
              type="danger" 
              size="small" 
              @click="showActionDialog(scope.row, 'REOPENED')"
            >
              重开
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              link
              @click="showCreateFollowupTask(scope.row)"
            >
              创建任务
            </el-button>
            <el-button 
              type="primary" 
              size="small" 
              link
              @click="showDetailDialog(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="warningList.length === 0 && !loading" description="暂无预警记录" />
    </el-card>

    <el-dialog v-model="actionDialogVisible" :title="getActionTitle(currentAction)" width="500px">
      <el-form :model="actionForm" label-width="80px">
        <el-form-item label="预警信息">
          <el-input :value="currentWarning?.warningMessage" disabled />
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="getStatusType(currentWarning?.status)" size="small">
            {{ getStatusName(currentWarning?.status) }}
          </el-tag>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          <el-tag :type="getStatusType(currentAction)" size="small">
            {{ getStatusName(currentAction) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="actionForm.operator" placeholder="请输入操作人姓名" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="actionForm.remark" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入备注信息" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAction">确认{{ getActionTitle(currentAction) }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="预警详情" width="700px">
      <div v-if="detailData" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="老人姓名">{{ detailData.elderlyName }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusType(detailData.status)" size="small">
              {{ detailData.statusName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="指标类型">{{ getIndicatorName(detailData.indicatorType) }}</el-descriptions-item>
          <el-descriptions-item label="预警级别">
            <el-tag :type="detailData.warningLevel === 'HIGH' ? 'danger' : 'warning'" size="small">
              {{ detailData.warningLevel === 'HIGH' ? '偏高' : '偏低' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="实际值">
            <span class="abnormal">{{ detailData.actualValue }}{{ getIndicatorUnit(detailData.indicatorType) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="阈值">
            {{ detailData.thresholdValue }}{{ getIndicatorUnit(detailData.indicatorType) }}
          </el-descriptions-item>
          <el-descriptions-item label="预警内容" :span="2">{{ detailData.warningMessage }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ detailData.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ detailData.handledAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ detailData.handledBy || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理备注">{{ detailData.handleRemark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="timeline-section">
          <h4>处理时间线</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in detailData.timeline"
              :key="item.id"
              :timestamp="item.createdAt"
              :type="getTimelineType(item.actionType)"
            >
              <div class="timeline-item-content">
                <div class="timeline-action">
                  <strong>{{ getActionName(item.actionType) }}</strong>
                  <span v-if="item.fromStatus" class="status-transition">
                    <el-tag size="small" :type="getStatusType(item.fromStatus)">{{ getStatusName(item.fromStatus) }}</el-tag>
                    <el-icon><ArrowRight /></el-icon>
                    <el-tag size="small" :type="getStatusType(item.toStatus)">{{ getStatusName(item.toStatus) }}</el-tag>
                  </span>
                </div>
                <div class="timeline-meta">
                  <span v-if="item.operator" class="operator">操作人：{{ item.operator }}</span>
                </div>
                <div v-if="item.remark" class="timeline-remark">{{ item.remark }}</div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <div class="detail-actions">
          <span class="allowed-actions-label">可执行操作：</span>
          <el-button 
            v-for="action in detailData.allowedActions" 
            :key="action"
            :type="getActionButtonType(action)"
            size="small"
            @click="showActionDialogFromDetail(action)"
          >
            {{ getActionNameByStatus(action) }}
          </el-button>
          <el-button type="success" size="small" @click="showCreateFollowupTask(detailData)">
            创建跟进任务
          </el-button>
          <el-button v-if="detailData.allowedActions.length === 0" size="small" disabled>
            无可用操作
          </el-button>
        </div>

        <div v-if="followupTasks.length > 0" class="followup-tasks-section">
          <h4>跟进任务</h4>
          <el-table :data="followupTasks" border size="small">
            <el-table-column prop="title" label="任务标题" min-width="150" show-overflow-tooltip />
            <el-table-column prop="priority" label="优先级" width="80">
              <template #default="scope">
                <el-tag :type="getFollowupPriorityTagType(scope.row.priority)" size="small">
                  {{ getFollowupPriorityName(scope.row.priority) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="scope">
                <el-tag :type="getFollowupStatusType(scope.row.status)" size="small">
                  {{ getFollowupStatusName(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="assigneeName" label="负责人" width="90" />
            <el-table-column prop="deadline" label="截止时间" width="160" />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" link @click="goToFollowupTask(scope.row)">
                  查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="createFollowupTaskVisible" title="创建跟进任务" width="600px">
      <el-form :model="followupTaskForm" label-width="100px">
        <el-form-item label="关联预警">
          <el-input :value="`#${detailData?.id || currentWarning?.id} - ${detailData?.warningMessage || currentWarning?.warningMessage}`" disabled />
        </el-form-item>
        <el-form-item label="任务标题" required>
          <el-input v-model="followupTaskForm.title" placeholder="请输入任务标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="followupTaskForm.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="followupTaskForm.assigneeName" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="截止时间" required>
          <el-date-picker
            v-model="followupTaskForm.deadline"
            type="datetime"
            placeholder="选择截止时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="followupTaskForm.priority">
            <el-radio value="HIGH">高</el-radio>
            <el-radio value="MEDIUM">中</el-radio>
            <el-radio value="LOW">低</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createFollowupTaskVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFollowupTask">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const filterStatus = ref(null)
const warningList = ref([])
const allStatuses = ref([])

const actionDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentWarning = ref(null)
const currentAction = ref('')
const detailData = ref(null)
const lastFocusedWarningId = ref(null)

const actionForm = ref({
  operator: '',
  remark: ''
})

const followupTasks = ref([])
const createFollowupTaskVisible = ref(false)
const followupTaskForm = ref({
  warningRecordId: null,
  title: '',
  description: '',
  assigneeName: '',
  deadline: null,
  priority: 'MEDIUM'
})

const followupStatusTypes = {
  PENDING: 'danger',
  IN_PROGRESS: 'warning',
  COMPLETED: 'success',
  OVERDUE: 'danger',
  CANCELLED: 'info'
}

const followupStatusNames = {
  PENDING: '待处理',
  IN_PROGRESS: '处理中',
  COMPLETED: '已完成',
  OVERDUE: '已逾期',
  CANCELLED: '已取消'
}

const followupPriorityNames = {
  HIGH: '高',
  MEDIUM: '中',
  LOW: '低'
}

const followupPriorityTagTypes = {
  HIGH: 'danger',
  MEDIUM: 'warning',
  LOW: 'info'
}

const getFollowupStatusType = (status) => followupStatusTypes[status] || 'info'
const getFollowupStatusName = (status) => followupStatusNames[status] || status
const getFollowupPriorityName = (priority) => followupPriorityNames[priority] || priority
const getFollowupPriorityTagType = (priority) => followupPriorityTagTypes[priority] || 'info'

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
  HANDLED: 'success',
  IGNORED: 'info',
  REOPENED: 'danger',
  ESCALATED: 'warning'
}

const statusNames = {
  PENDING: '待处理',
  READ: '已读',
  HANDLED: '已处理',
  IGNORED: '已忽略',
  REOPENED: '重新打开',
  ESCALATED: '升级处理'
}

const actionTitles = {
  READ: '标记已读',
  HANDLED: '处理',
  IGNORED: '忽略',
  ESCALATED: '升级处理',
  REOPENED: '重新打开',
  PENDING: '重置为待处理'
}

const actionNames = {
  CREATE: '创建预警',
  READ: '标记已读',
  HANDLE: '处理预警',
  IGNORE: '忽略预警',
  REOPEN: '重新打开',
  ESCALATE: '升级处理',
  UPDATE: '更新状态'
}

const allowedTransitions = {
  PENDING: ['READ', 'HANDLED', 'IGNORED', 'ESCALATED'],
  READ: ['HANDLED', 'IGNORED', 'ESCALATED', 'PENDING'],
  HANDLED: ['REOPENED'],
  IGNORED: ['REOPENED'],
  REOPENED: ['READ', 'HANDLED', 'IGNORED', 'ESCALATED'],
  ESCALATED: ['HANDLED', 'READ']
}

const getIndicatorName = (type) => indicatorNames[type] || type
const getIndicatorUnit = (type) => indicatorUnits[type] || ''
const getStatusType = (status) => statusTypes[status] || 'info'
const getStatusName = (status) => statusNames[status] || status
const getActionTitle = (action) => actionTitles[action] || '操作'
const getActionName = (actionType) => actionNames[actionType] || actionType

const getActionNameByStatus = (status) => {
  const map = {
    READ: '标记已读',
    HANDLED: '处理',
    IGNORED: '忽略',
    ESCALATED: '升级处理',
    REOPENED: '重新打开',
    PENDING: '重置为待处理'
  }
  return map[status] || status
}

const getActionButtonType = (status) => {
  const map = {
    READ: 'primary',
    HANDLED: 'success',
    IGNORED: 'info',
    ESCALATED: 'warning',
    REOPENED: 'danger',
    PENDING: 'primary'
  }
  return map[status] || 'primary'
}

const getTimelineActionColor = (actionType) => {
  const map = {
    CREATE: '#409EFF',
    READ: '#E6A23C',
    HANDLE: '#67C23A',
    IGNORE: '#909399',
    REOPEN: '#F56C6C',
    ESCALATE: '#E6A23C',
    UPDATE: '#409EFF'
  }
  return map[actionType] || '#409EFF'
}

const getTimelineType = (actionType) => {
  const map = {
    CREATE: 'primary',
    READ: 'warning',
    HANDLE: 'success',
    IGNORE: 'info',
    REOPEN: 'danger',
    ESCALATE: 'warning',
    UPDATE: 'primary'
  }
  return map[actionType] || 'primary'
}

const canDoAction = (currentStatus, targetStatus) => {
  const allowed = allowedTransitions[currentStatus] || []
  return allowed.includes(targetStatus)
}

const loadAllStatuses = async () => {
  try {
    const res = await request.get('/warning/record/statuses')
    allStatuses.value = res.data || []
  } catch (e) {
    allStatuses.value = [
      { code: 'PENDING', name: '待处理' },
      { code: 'READ', name: '已读' },
      { code: 'HANDLED', name: '已处理' },
      { code: 'IGNORED', name: '已忽略' },
      { code: 'REOPENED', name: '重新打开' },
      { code: 'ESCALATED', name: '升级处理' }
    ]
  }
}

const getRouteWarningId = () => {
  const warningId = Number(route.query.id)
  return Number.isFinite(warningId) && warningId > 0 ? warningId : null
}

const focusRouteWarning = async () => {
  const warningId = getRouteWarningId()
  if (!warningId || lastFocusedWarningId.value === warningId) {
    return
  }

  let targetRow = warningList.value.find(item => item.id === warningId)
  if (!targetRow && filterStatus.value) {
    filterStatus.value = null
    await loadWarnings()
    targetRow = warningList.value.find(item => item.id === warningId)
  }

  if (targetRow) {
    await showDetailDialog(targetRow)
    lastFocusedWarningId.value = warningId
  } else {
    lastFocusedWarningId.value = warningId
    ElMessage.warning('未找到对应预警记录，可能已不可见或状态已变化')
  }
}

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

const showActionDialog = (row, action) => {
  currentWarning.value = row
  currentAction.value = action
  actionForm.value = {
    operator: '',
    remark: ''
  }
  actionDialogVisible.value = true
}

const showActionDialogFromDetail = (action) => {
  detailDialogVisible.value = false
  showActionDialog(detailData, action)
}

const submitAction = async () => {
  if (currentAction.value !== 'READ' && !actionForm.value.operator) {
    ElMessage.warning('请输入操作人')
    return
  }
  
  const warningId = currentWarning.value?.id || detailData.value?.id
  if (!warningId) {
    ElMessage.error('无法获取预警ID')
    return
  }
  
  try {
    await request.put(`/warning/record/transition/${warningId}`, {
      targetStatus: currentAction.value,
      operator: actionForm.value.operator,
      remark: actionForm.value.remark
    })
    
    ElMessage.success('操作成功')
    actionDialogVisible.value = false
    loadWarnings()
    if (detailData.value && detailData.value.id === warningId) {
      showDetailDialog({ id: warningId })
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const showDetailDialog = async (row) => {
  try {
    const res = await request.get(`/warning/record/${row.id}`)
    detailData.value = res.data
    detailDialogVisible.value = true
    loadFollowupTasks(row.id)
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

const loadFollowupTasks = async (warningRecordId) => {
  try {
    const res = await request.get('/warning/followup-task/list', {
      params: { warningRecordId }
    })
    followupTasks.value = res.data || []
  } catch (e) {
    followupTasks.value = []
  }
}

const showCreateFollowupTask = (row) => {
  const warningId = row?.id || detailData.value?.id
  followupTaskForm.value = {
    warningRecordId: warningId,
    title: '',
    description: '',
    assigneeName: '',
    deadline: null,
    priority: 'MEDIUM'
  }
  createFollowupTaskVisible.value = true
}

const submitFollowupTask = async () => {
  if (!followupTaskForm.value.title.trim()) {
    ElMessage.warning('请输入任务标题')
    return
  }
  if (!followupTaskForm.value.deadline) {
    ElMessage.warning('请选择截止时间')
    return
  }

  try {
    await request.post('/warning/followup-task/create', followupTaskForm.value)
    ElMessage.success('任务创建成功')
    createFollowupTaskVisible.value = false
    if (detailData.value) {
      loadFollowupTasks(detailData.value.id)
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  }
}

const goToFollowupTask = (task) => {
  router.push(`/warning/followup-task?id=${task.id}`)
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

watch(() => route.query.id, async () => {
  lastFocusedWarningId.value = null
  if (getRouteWarningId()) {
    await focusRouteWarning()
  }
})

onMounted(async () => {
  await loadAllStatuses()
  await loadWarnings()
  await focusRouteWarning()
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

.arrow-icon {
  margin: 0 8px;
  color: #909399;
}

.detail-content {
  padding: 10px 0;
}

.timeline-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.timeline-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.timeline-item-content {
  padding: 5px 0;
}

.timeline-action {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.status-transition {
  display: flex;
  align-items: center;
  gap: 5px;
}

.timeline-meta {
  color: #909399;
  font-size: 13px;
  margin-bottom: 3px;
}

.timeline-remark {
  color: #606266;
  font-size: 13px;
  background: #f5f7fa;
  padding: 8px 10px;
  border-radius: 4px;
  margin-top: 5px;
}

.detail-actions {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 10px;
}

.allowed-actions-label {
  color: #606266;
  font-size: 14px;
}

.followup-tasks-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.followup-tasks-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
}
</style>
