<template>
  <div class="followup-task-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon pending-icon">
              <el-icon :size="24"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingCount || 0 }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon in-progress-icon">
              <el-icon :size="24"><Loading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.inProgressCount || 0 }}</div>
              <div class="stat-label">处理中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon overdue-icon">
              <el-icon :size="24"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.overdueCount || 0 }}</div>
              <div class="stat-label">已逾期</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6" :lg="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon completed-icon">
              <el-icon :size="24"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.completedCount || 0 }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>跟进任务列表</span>
          <div class="header-actions">
            <el-select v-model="filterStatus" placeholder="筛选状态" @change="loadTasks" clearable style="width: 140px; margin-right: 10px;">
              <el-option v-for="s in allStatuses" :key="s.code" :label="s.name" :value="s.code" />
            </el-select>
            <el-select v-model="filterPriority" placeholder="筛选优先级" @change="loadTasks" clearable style="width: 140px; margin-right: 10px;">
              <el-option label="高" value="HIGH" />
              <el-option label="中" value="MEDIUM" />
              <el-option label="低" value="LOW" />
            </el-select>
            <el-switch v-model="onlyMyTasks" @change="loadTasks" active-text="我的任务" style="margin-right: 15px;" />
            <el-button type="primary" @click="loadTasks" style="margin-right: 10px;">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              新建任务
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="taskList" border style="width: 100%" v-loading="loading" max-height="550">
        <el-table-column prop="title" label="任务标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="elderlyName" label="老人姓名" width="100" />
        <el-table-column prop="warningIndicatorType" label="预警指标" width="100">
          <template #default="scope">
            <span>{{ getIndicatorName(scope.row.warningIndicatorType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="scope">
            <el-tag :type="getPriorityTagType(scope.row.priority)" size="small">
              {{ getPriorityName(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="100" />
        <el-table-column prop="deadline" label="截止时间" width="180">
          <template #default="scope">
            <span :class="{ 'overdue-text': isOverdue(scope.row) }">
              {{ formatDateTime(scope.row.deadline) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" link @click="showDetailDialog(scope.row)">
              详情
            </el-button>
            <el-button v-if="canStart(scope.row)" type="warning" size="small" link @click="handleStart(scope.row)">
              开始
            </el-button>
            <el-button v-if="canComplete(scope.row)" type="success" size="small" link @click="showCompleteDialog(scope.row)">
              完成
            </el-button>
            <el-button v-if="canEdit(scope.row)" type="primary" size="small" link @click="showEditDialog(scope.row)">
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="taskList.length === 0 && !loading" description="暂无任务" />
    </el-card>

    <el-dialog v-model="createDialogVisible" title="创建跟进任务" width="600px">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="关联预警" required>
          <el-select v-model="taskForm.warningRecordId" placeholder="请选择预警记录" filterable style="width: 100%;">
            <el-option
              v-for="warning in warningOptions"
              :key="warning.id"
              :label="`#${warning.id} - ${warning.elderlyName || ''} - ${warning.warningMessage || ''}`"
              :value="warning.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务标题" required>
          <el-input v-model="taskForm.title" placeholder="请输入任务标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="taskForm.assigneeName" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="截止时间" required>
          <el-date-picker
            v-model="taskForm.deadline"
            type="datetime"
            placeholder="选择截止时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="taskForm.priority">
            <el-radio value="HIGH">高</el-radio>
            <el-radio value="MEDIUM">中</el-radio>
            <el-radio value="LOW">低</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确认创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑任务" width="600px">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="任务标题" required>
          <el-input v-model="taskForm.title" placeholder="请输入任务标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="taskForm.assigneeName" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="截止时间" required>
          <el-date-picker
            v-model="taskForm.deadline"
            type="datetime"
            placeholder="选择截止时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="taskForm.priority">
            <el-radio value="HIGH">高</el-radio>
            <el-radio value="MEDIUM">中</el-radio>
            <el-radio value="LOW">低</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="taskForm.remark" type="textarea" :rows="2" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存修改</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="任务详情" width="700px">
      <div v-if="currentTask" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务标题" :span="2">{{ currentTask.title }}</el-descriptions-item>
          <el-descriptions-item label="老人姓名">{{ currentTask.elderlyName }}</el-descriptions-item>
          <el-descriptions-item label="预警指标">{{ getIndicatorName(currentTask.warningIndicatorType) }}</el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTagType(currentTask.priority)" size="small">
              {{ getPriorityName(currentTask.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentTask.status)" size="small">
              {{ getStatusName(currentTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">{{ currentTask.assigneeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentTask.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">
            <span :class="{ 'overdue-text': isOverdue(currentTask) }">
              {{ formatDateTime(currentTask.deadline) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ formatDateTime(currentTask.completedAt) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="任务描述" :span="2">{{ currentTask.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关联预警" :span="2">
            <el-button type="primary" link @click="goToWarning(currentTask.warningRecordId)">
              #{{ currentTask.warningRecordId }} - {{ currentTask.warningMessage }}
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="处理备注" :span="2">{{ currentTask.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-actions">
          <el-button v-if="canStart(currentTask)" type="warning" @click="handleStart(currentTask)">
            开始处理
          </el-button>
          <el-button v-if="canComplete(currentTask)" type="success" @click="showCompleteDialog(currentTask)">
            完成任务
          </el-button>
          <el-button v-if="canCancel(currentTask)" type="danger" @click="handleCancel(currentTask)">
            取消任务
          </el-button>
          <el-button v-if="canEdit(currentTask)" type="primary" @click="showEditDialog(currentTask)">
            编辑
          </el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="completeDialogVisible" title="完成任务" width="500px">
      <el-form label-width="80px">
        <el-form-item label="处理备注">
          <el-input v-model="completeForm.remark" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const filterStatus = ref(null)
const filterPriority = ref(null)
const onlyMyTasks = ref(false)
const taskList = ref([])
const allStatuses = ref([])
const warningOptions = ref([])
const currentTaskId = ref(null)

const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const currentTask = ref(null)

const taskForm = ref({
  warningRecordId: null,
  title: '',
  description: '',
  assigneeName: '',
  deadline: null,
  priority: 'MEDIUM'
})

const completeForm = ref({
  remark: ''
})

const stats = computed(() => {
  const result = {
    pendingCount: 0,
    inProgressCount: 0,
    overdueCount: 0,
    completedCount: 0
  }
  taskList.value.forEach(task => {
    if (task.status === 'PENDING') result.pendingCount++
    if (task.status === 'IN_PROGRESS') result.inProgressCount++
    if (task.status === 'OVERDUE') result.overdueCount++
    if (task.status === 'COMPLETED') result.completedCount++
  })
  return result
})

const indicatorNames = {
  temperature: '体温',
  systolicPressure: '收缩压',
  diastolicPressure: '舒张压',
  heartRate: '心率',
  bloodOxygen: '血氧',
  bloodSugar: '血糖'
}

const statusTypes = {
  PENDING: 'danger',
  IN_PROGRESS: 'warning',
  COMPLETED: 'success',
  OVERDUE: 'danger',
  CANCELLED: 'info'
}

const statusNames = {
  PENDING: '待处理',
  IN_PROGRESS: '处理中',
  COMPLETED: '已完成',
  OVERDUE: '已逾期',
  CANCELLED: '已取消'
}

const priorityNames = {
  HIGH: '高',
  MEDIUM: '中',
  LOW: '低'
}

const priorityTagTypes = {
  HIGH: 'danger',
  MEDIUM: 'warning',
  LOW: 'info'
}

const allowedTransitions = {
  PENDING: ['IN_PROGRESS', 'COMPLETED', 'CANCELLED'],
  IN_PROGRESS: ['COMPLETED', 'CANCELLED', 'PENDING'],
  COMPLETED: ['PENDING'],
  OVERDUE: ['IN_PROGRESS', 'COMPLETED', 'CANCELLED'],
  CANCELLED: ['PENDING']
}

const getIndicatorName = (type) => indicatorNames[type] || type
const getStatusType = (status) => statusTypes[status] || 'info'
const getStatusName = (status) => statusNames[status] || status
const getPriorityName = (priority) => priorityNames[priority] || priority
const getPriorityTagType = (priority) => priorityTagTypes[priority] || 'info'

const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const isOverdue = (task) => {
  if (!task || !task.deadline) return false
  return new Date(task.deadline) < new Date() &&
    ['PENDING', 'IN_PROGRESS'].includes(task.status)
}

const canStart = (task) => allowedTransitions[task.status]?.includes('IN_PROGRESS')
const canComplete = (task) => allowedTransitions[task.status]?.includes('COMPLETED')
const canCancel = (task) => allowedTransitions[task.status]?.includes('CANCELLED')
const canEdit = (task) => ['PENDING', 'IN_PROGRESS', 'OVERDUE'].includes(task.status)

const loadStatuses = async () => {
  try {
    const res = await request.get('/warning/followup-task/statuses')
    allStatuses.value = res.data || []
  } catch (e) {
    allStatuses.value = [
      { code: 'PENDING', name: '待处理' },
      { code: 'IN_PROGRESS', name: '处理中' },
      { code: 'COMPLETED', name: '已完成' },
      { code: 'OVERDUE', name: '已逾期' },
      { code: 'CANCELLED', name: '已取消' }
    ]
  }
}

const loadWarningOptions = async () => {
  try {
    const res = await request.get('/warning/record/list')
    warningOptions.value = res.data || []
  } catch (e) {
    warningOptions.value = []
  }
}

const loadTasks = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterStatus.value) params.status = filterStatus.value
    if (onlyMyTasks.value) params.myTasks = true

    let res = await request.get('/warning/followup-task/list', { params })
    let tasks = res.data || []

    if (filterPriority.value) {
      tasks = tasks.filter(t => t.priority === filterPriority.value)
    }

    taskList.value = tasks
  } catch (e) {
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  taskForm.value = {
    warningRecordId: route.query.warningId ? Number(route.query.warningId) : null,
    title: '',
    description: '',
    assigneeName: '',
    deadline: null,
    priority: 'MEDIUM'
  }
  createDialogVisible.value = true
}

const submitCreate = async () => {
  if (!taskForm.value.warningRecordId) {
    ElMessage.warning('请选择关联的预警记录')
    return
  }
  if (!taskForm.value.title.trim()) {
    ElMessage.warning('请输入任务标题')
    return
  }
  if (!taskForm.value.deadline) {
    ElMessage.warning('请选择截止时间')
    return
  }

  try {
    await request.post('/warning/followup-task/create', taskForm.value)
    ElMessage.success('任务创建成功')
    createDialogVisible.value = false
    loadTasks()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  }
}

const showEditDialog = (row) => {
  currentTaskId.value = row.id
  taskForm.value = {
    title: row.title,
    description: row.description || '',
    assigneeName: row.assigneeName || '',
    deadline: row.deadline,
    priority: row.priority,
    remark: row.remark || ''
  }
  detailDialogVisible.value = false
  editDialogVisible.value = true
}

const submitEdit = async () => {
  if (!taskForm.value.title.trim()) {
    ElMessage.warning('请输入任务标题')
    return
  }
  if (!taskForm.value.deadline) {
    ElMessage.warning('请选择截止时间')
    return
  }

  try {
    await request.put(`/warning/followup-task/${currentTaskId.value}`, taskForm.value)
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    loadTasks()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

const showDetailDialog = async (row) => {
  try {
    const res = await request.get(`/warning/followup-task/${row.id}`)
    currentTask.value = res.data
    detailDialogVisible.value = true
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

const handleStart = async (row) => {
  try {
    await request.put(`/warning/followup-task/start/${row.id}`)
    ElMessage.success('已开始处理')
    loadTasks()
    if (currentTask.value && currentTask.value.id === row.id) {
      showDetailDialog(row)
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const showCompleteDialog = (row) => {
  currentTaskId.value = row.id
  completeForm.value.remark = ''
  detailDialogVisible.value = false
  completeDialogVisible.value = true
}

const submitComplete = async () => {
  try {
    await request.put(`/warning/followup-task/complete/${currentTaskId.value}`, {
      remark: completeForm.value.remark
    })
    ElMessage.success('任务已完成')
    completeDialogVisible.value = false
    loadTasks()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确认取消该任务吗？', '提示', { type: 'warning' })
    await request.put(`/warning/followup-task/cancel/${row.id}`, { remark: '手动取消' })
    ElMessage.success('任务已取消')
    detailDialogVisible.value = false
    loadTasks()
  } catch (e) {
    if (e === 'cancel') return
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const goToWarning = (warningId) => {
  router.push(`/warning?id=${warningId}`)
}

onMounted(async () => {
  await loadStatuses()
  await loadWarningOptions()
  await loadTasks()

  if (route.query.id) {
    const task = taskList.value.find(t => t.id === Number(route.query.id))
    if (task) {
      showDetailDialog(task)
    }
  }
  if (route.query.warningId) {
    showCreateDialog()
  }
})
</script>

<style scoped>
.followup-task-container {
  padding: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.pending-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.in-progress-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
}

.overdue-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.completed-icon {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  color: #909399;
  margin-top: 2px;
  font-size: 13px;
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

.overdue-text {
  color: #f56c6c;
  font-weight: bold;
}

.detail-content {
  padding: 10px 0;
}

.detail-actions {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
  display: flex;
  gap: 10px;
}
</style>
