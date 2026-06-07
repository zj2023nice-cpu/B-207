<template>
  <div class="workbench-container">
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="4">
        <el-card class="stat-card total-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="28"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCount || 0 }}</div>
              <div class="stat-label">全部待办</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="4">
        <el-card class="stat-card high-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.highPriorityCount || 0 }}</div>
              <div class="stat-label">高优先级</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="4">
        <el-card class="stat-card warning-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="28"><BellFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.warningCount || 0 }}</div>
              <div class="stat-label">预警事项</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="4">
        <el-card class="stat-card notification-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="28"><Message /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.notificationCount || 0 }}</div>
              <div class="stat-label">通知事项</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="8" :lg="8" :xl="4">
        <el-card class="stat-card health-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="28"><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.healthRecordCount || 0 }}</div>
              <div class="stat-label">异常健康记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <span>筛选条件</span>
          <div class="header-actions">
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="loadWorkbenchData">刷新</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" class="filter-form">
        <el-form-item label="事项类型">
          <el-select v-model="filters.itemTypes" multiple clearable collapse-tags placeholder="全部类型" style="width: 220px;">
            <el-option
              v-for="item in filterOptions.itemTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="filters.priorities" multiple clearable collapse-tags placeholder="全部优先级" style="width: 220px;">
            <el-option
              v-for="item in filterOptions.priorities"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="老人">
          <el-select v-model="filters.elderlyIds" multiple clearable collapse-tags placeholder="全部老人" style="width: 260px;">
            <el-option
              v-for="item in elderlyOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 360px;"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>待办列表</span>
          <span class="list-total">当前 {{ items.length }} 条</span>
        </div>
      </template>

      <el-table :data="items" border v-loading="loading" empty-text="暂无待办事项">
        <el-table-column label="事项类型" width="120">
          <template #default="scope">
            <el-tag :type="getItemTypeTagType(scope.row.itemType)">
              {{ scope.row.itemTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="elderlyName" label="老人姓名" width="120">
          <template #default="scope">
            {{ scope.row.elderlyName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
        <el-table-column label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityTagType(scope.row.priority)">{{ scope.row.priorityName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="状态" width="120" />
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="快速处理" min-width="260" fixed="right">
          <template #default="scope">
            <div class="action-list">
              <el-button
                v-for="action in scope.row.quickActions || []"
                :key="action.actionKey"
                size="small"
                :type="getActionButtonType(action.actionType)"
                @click="handleQuickAction(scope.row, action)"
              >
                {{ action.actionName }}
              </el-button>
              <el-button v-if="!scope.row.quickActions || scope.row.quickActions.length === 0" size="small" type="primary" @click="openItem(scope.row)">
                查看详情
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const stats = ref({
  totalCount: 0,
  highPriorityCount: 0,
  warningCount: 0,
  notificationCount: 0,
  healthRecordCount: 0
})
const filterOptions = ref({
  itemTypes: [],
  priorities: []
})
const elderlyOptions = ref([])
const items = ref([])
const filters = ref({
  itemTypes: [],
  priorities: [],
  elderlyIds: [],
  dateRange: []
})

const buildQueryPayload = () => {
  const payload = {
    itemTypes: filters.value.itemTypes?.length ? filters.value.itemTypes : null,
    priorities: filters.value.priorities?.length ? filters.value.priorities : null,
    elderlyIds: filters.value.elderlyIds?.length ? filters.value.elderlyIds : null,
    pageNum: 1,
    pageSize: 50
  }

  if (filters.value.dateRange?.length === 2) {
    payload.startTime = filters.value.dateRange[0]
    payload.endTime = filters.value.dateRange[1]
  }

  return payload
}

const loadStats = async () => {
  const res = await request.get('/workbench/stats')
  stats.value = res.data || stats.value
}

const loadFilterOptions = async () => {
  const [optionsRes, elderlyRes] = await Promise.all([
    request.get('/workbench/filter-options'),
    request.get('/elderly/list')
  ])
  filterOptions.value = optionsRes.data || { itemTypes: [], priorities: [] }
  elderlyOptions.value = elderlyRes.data || []
}

const loadItems = async () => {
  const res = await request.post('/workbench/items', buildQueryPayload())
  items.value = res.data || []
}

const loadWorkbenchData = async () => {
  loading.value = true
  try {
    await Promise.all([loadStats(), loadItems()])
  } catch (error) {
    console.error(error)
    ElMessage.error('加载工作台数据失败')
  } finally {
    loading.value = false
  }
}

const handleReset = async () => {
  filters.value = {
    itemTypes: [],
    priorities: [],
    elderlyIds: [],
    dateRange: []
  }
  await loadWorkbenchData()
}

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

const getPriorityTagType = (priority) => {
  const map = {
    HIGH: 'danger',
    MEDIUM: 'warning',
    LOW: 'info'
  }
  return map[priority] || 'info'
}

const getItemTypeTagType = (itemType) => {
  const map = {
    WARNING: 'danger',
    NOTIFICATION: 'primary',
    HEALTH_RECORD: 'warning'
  }
  return map[itemType] || 'info'
}

const getActionButtonType = (actionType) => {
  const map = {
    PRIMARY: 'primary',
    SUCCESS: 'success',
    INFO: 'info',
    WARNING: 'warning',
    DANGER: 'danger',
    DEFAULT: 'default'
  }
  return map[actionType] || 'primary'
}

const openItem = (item) => {
  if (item.detailUrl) {
    router.push(item.detailUrl)
  } else if (item.moduleUrl) {
    router.push(item.moduleUrl)
  }
}

const executeAction = async (item, action) => {
  if (action.method === 'ROUTE') {
    router.push(action.apiUrl || item.detailUrl || item.moduleUrl)
    return
  }

  if (action.method === 'PUT') {
    await request.put(action.apiUrl)
    ElMessage.success(action.actionName + '成功')
    await loadWorkbenchData()
    return
  }

  if (action.method === 'POST') {
    await request.post(action.apiUrl)
    ElMessage.success(action.actionName + '成功')
    await loadWorkbenchData()
    return
  }

  openItem(item)
}

const handleQuickAction = async (item, action) => {
  try {
    if (action.requireConfirm) {
      await ElMessageBox.confirm(action.confirmText || '确认执行该操作吗？', '提示', { type: 'warning' })
    }
    await executeAction(item, action)
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    console.error(error)
    ElMessage.error(action.actionName + '失败')
  }
}

onMounted(async () => {
  try {
    await loadFilterOptions()
    await loadWorkbenchData()
  } catch (error) {
    console.error(error)
    ElMessage.error('初始化工作台失败')
  }
})
</script>

<style scoped>
.workbench-container {
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
  gap: 16px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.total-card .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.high-card .stat-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.warning-card .stat-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.notification-card .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.health-card .stat-icon {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-value {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  color: #909399;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-card,
.list-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
}

.list-total {
  color: #909399;
  font-size: 13px;
}

.action-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
