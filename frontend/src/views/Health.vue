<template>
  <div class="health-container">
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>录入健康数据</span>
            </div>
          </template>
          <el-form :model="healthForm" label-width="80px">
            <el-form-item label="老人">
              <el-select v-model="healthForm.elderlyId" placeholder="选择老人" style="width: 100%">
                <el-option v-for="item in elderlyList" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="收缩压">
              <el-input-number v-model="healthForm.systolicPressure" :min="50" :max="200" placeholder="mmHg" style="width: 100%" />
            </el-form-item>
            <el-form-item label="舒张压">
              <el-input-number v-model="healthForm.diastolicPressure" :min="30" :max="120" placeholder="mmHg" style="width: 100%" />
            </el-form-item>
            <el-form-item label="体温">
              <el-input-number v-model="healthForm.temperature" :precision="1" :step="0.1" :min="30" :max="45" style="width: 100%" />
            </el-form-item>
            <el-form-item label="心率">
              <el-input-number v-model="healthForm.heartRate" :min="30" :max="200" placeholder="次/分钟" style="width: 100%" />
            </el-form-item>
            <el-form-item label="血氧">
              <el-input-number v-model="healthForm.bloodOxygen" :min="70" :max="100" placeholder="%" style="width: 100%" />
            </el-form-item>
            <el-form-item label="血糖">
              <el-input-number v-model="healthForm.bloodSugar" :precision="1" :step="0.1" :min="2" :max="20" placeholder="mmol/L" style="width: 100%" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSave" style="width: 100%">保存数据</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>健康趋势图</span>
              <div class="chart-controls">
                <el-select v-model="selectedElderlyId" placeholder="选择老人查看趋势" @change="loadTrendData" style="width: 150px; margin-right: 10px;">
                  <el-option v-for="item in elderlyList" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
                <el-select v-model="selectedIndicator" @change="updateChart" style="width: 120px;">
                  <el-option label="体温" value="temperature" />
                  <el-option label="血压" value="bloodPressure" />
                  <el-option label="心率" value="heartRate" />
                  <el-option label="血氧" value="bloodOxygen" />
                  <el-option label="血糖" value="bloodSugar" />
                </el-select>
              </div>
            </div>
          </template>
          <div ref="chartRef" class="chart-container" v-loading="chartLoading"></div>
          <el-empty v-if="!hasTrendData" description="请选择老人查看健康趋势" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>历史健康记录</span>
              <div class="header-filters">
                <el-select v-model="filterElderlyId" placeholder="按老人筛选" clearable @change="loadHistory" style="width: 150px; margin-right: 10px;">
                  <el-option v-for="item in elderlyList" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
                <el-select v-model="filterIsAbnormal" placeholder="按状态筛选" clearable @change="loadHistory" style="width: 120px; margin-right: 10px;">
                  <el-option label="异常" :value="true" />
                  <el-option label="正常" :value="false" />
                </el-select>
                <el-button type="primary" size="small" @click="handleExport">
                  <el-icon><Download /></el-icon>
                  导出数据
                </el-button>
              </div>
            </div>
          </template>
          <el-table :data="historyData" border style="width: 100%" v-loading="loading" max-height="400" row-key="id">
            <el-table-column prop="elderlyName" label="姓名" width="100" />
            <el-table-column label="血压" width="140">
              <template #default="scope">
                <span :class="{ 'abnormal': isBloodPressureAbnormal(scope.row), 'search-target-cell': scope.row.id === highlightedRecordId }">
                  {{ scope.row.systolicPressure }}/{{ scope.row.diastolicPressure }} mmHg
                </span>
              </template>
            </el-table-column>
            <el-table-column label="体温" width="100">
              <template #default="scope">
                <span :class="{ 'abnormal': scope.row.temperature > 37.3 }">
                  {{ scope.row.temperature }} ℃
                </span>
              </template>
            </el-table-column>
            <el-table-column label="心率" width="100">
              <template #default="scope">
                <span :class="{ 'abnormal': isHeartRateAbnormal(scope.row.heartRate) }">
                  {{ scope.row.heartRate }} 次/分
                </span>
              </template>
            </el-table-column>
            <el-table-column label="血氧" width="100">
              <template #default="scope">
                <span :class="{ 'abnormal': scope.row.bloodOxygen < 95 }">
                  {{ scope.row.bloodOxygen }} %
                </span>
              </template>
            </el-table-column>
            <el-table-column label="血糖" width="120">
              <template #default="scope">
                <span :class="{ 'abnormal': isBloodSugarAbnormal(scope.row.bloodSugar) }">
                  {{ scope.row.bloodSugar }} mmol/L
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="scope">
                <el-tag v-if="scope.row.isAbnormal" type="danger" size="small">异常</el-tag>
                <el-tag v-else type="success" size="small">正常</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="异常原因" min-width="150" show-overflow-tooltip>
              <template #default="scope">
                <span class="abnormal-reason">{{ scope.row.abnormalReason || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="checkTime" label="检测时间" width="180" />
            <el-table-column label="更正标记" width="100">
              <template #default="scope">
                <el-tag v-if="scope.row.corrected" type="warning" size="small">已更正</el-tag>
                <el-tag v-else type="info" size="small">原始</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="openCorrectionDialog(scope.row)">更正</el-button>
                <el-button type="info" size="small" @click="openCorrectionHistory(scope.row)">历史</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="correctionDialogVisible" title="更正健康记录" width="600px">
      <el-form :model="correctionForm" label-width="100px">
        <el-form-item label="老人">
          <el-input :value="currentCorrectionElderlyName" disabled />
        </el-form-item>
        <el-form-item label="收缩压">
          <el-input-number v-model="correctionForm.systolicPressure" :min="50" :max="200" placeholder="mmHg" style="width: 100%" />
        </el-form-item>
        <el-form-item label="舒张压">
          <el-input-number v-model="correctionForm.diastolicPressure" :min="30" :max="120" placeholder="mmHg" style="width: 100%" />
        </el-form-item>
        <el-form-item label="体温">
          <el-input-number v-model="correctionForm.temperature" :precision="1" :step="0.1" :min="30" :max="45" style="width: 100%" />
        </el-form-item>
        <el-form-item label="心率">
          <el-input-number v-model="correctionForm.heartRate" :min="30" :max="200" placeholder="次/分钟" style="width: 100%" />
        </el-form-item>
        <el-form-item label="血氧">
          <el-input-number v-model="correctionForm.bloodOxygen" :min="70" :max="100" placeholder="%" style="width: 100%" />
        </el-form-item>
        <el-form-item label="血糖">
          <el-input-number v-model="correctionForm.bloodSugar" :precision="1" :step="0.1" :min="2" :max="20" placeholder="mmol/L" style="width: 100%" />
        </el-form-item>
        <el-form-item label="更正原因" required>
          <el-input v-model="correctionForm.correctionReason" type="textarea" :rows="3" placeholder="请输入更正原因" />
        </el-form-item>
        <el-form-item label="更正备注">
          <el-input v-model="correctionForm.correctionRemark" type="textarea" :rows="2" placeholder="请输入更正备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="correctionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCorrection" :loading="correctionSubmitting">确认更正</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="historyDialogVisible" title="更正历史记录" width="900px">
      <el-table :data="correctionHistory" border v-loading="historyLoading">
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="correctedBy" label="操作人" width="100" />
        <el-table-column prop="correctedAt" label="更正时间" width="180" />
        <el-table-column prop="correctionReason" label="更正原因" min-width="150" show-overflow-tooltip />
        <el-table-column label="修改前" width="150">
          <template #default="scope">
            <div class="history-data">
              <span v-if="scope.row.beforeSystolicPressure">血压: {{ scope.row.beforeSystolicPressure }}/{{ scope.row.beforeDiastolicPressure }}</span>
              <span v-if="scope.row.beforeTemperature">体温: {{ scope.row.beforeTemperature }}℃</span>
              <span v-if="scope.row.beforeHeartRate">心率: {{ scope.row.beforeHeartRate }}次/分</span>
              <span v-if="scope.row.beforeBloodOxygen">血氧: {{ scope.row.beforeBloodOxygen }}%</span>
              <span v-if="scope.row.beforeBloodSugar">血糖: {{ scope.row.beforeBloodSugar }}mmol/L</span>
              <el-tag v-if="scope.row.beforeIsAbnormal" type="danger" size="small" style="margin-top: 4px;">异常</el-tag>
              <el-tag v-else type="success" size="small" style="margin-top: 4px;">正常</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="修改后" width="150">
          <template #default="scope">
            <div class="history-data">
              <span v-if="scope.row.afterSystolicPressure">血压: {{ scope.row.afterSystolicPressure }}/{{ scope.row.afterDiastolicPressure }}</span>
              <span v-if="scope.row.afterTemperature">体温: {{ scope.row.afterTemperature }}℃</span>
              <span v-if="scope.row.afterHeartRate">心率: {{ scope.row.afterHeartRate }}次/分</span>
              <span v-if="scope.row.afterBloodOxygen">血氧: {{ scope.row.afterBloodOxygen }}%</span>
              <span v-if="scope.row.afterBloodSugar">血糖: {{ scope.row.afterBloodSugar }}mmol/L</span>
              <el-tag v-if="scope.row.afterIsAbnormal" type="danger" size="small" style="margin-top: 4px;">异常</el-tag>
              <el-tag v-else type="success" size="small" style="margin-top: 4px;">正常</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'EFFECTIVE'" type="success" size="small">已生效</el-tag>
            <el-tag v-else type="info" size="small">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const chartLoading = ref(false)
const elderlyList = ref([])
const historyData = ref([])
const chartRef = ref(null)
const filterElderlyId = ref(null)
const filterIsAbnormal = ref(null)
const highlightedRecordId = ref(null)
let chartInstance = null

const correctionDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const correctionSubmitting = ref(false)
const historyLoading = ref(false)
const currentCorrectionRecord = ref(null)
const currentCorrectionElderlyName = ref('')
const correctionHistory = ref([])
const correctionForm = ref({
  healthRecordId: null,
  systolicPressure: null,
  diastolicPressure: null,
  temperature: null,
  heartRate: null,
  bloodOxygen: null,
  bloodSugar: null,
  correctionReason: '',
  correctionRemark: ''
})

const healthForm = ref({
  elderlyId: null,
  systolicPressure: 120,
  diastolicPressure: 80,
  temperature: 36.5,
  heartRate: 75,
  bloodOxygen: 98,
  bloodSugar: 5.5
})

const selectedElderlyId = ref(null)
const selectedIndicator = ref('temperature')
const trendData = ref(null)

const hasTrendData = computed(() => {
  return trendData.value && trendData.value.times && trendData.value.times.length > 0
})

const isBloodPressureAbnormal = (row) => {
  if (!row.systolicPressure || !row.diastolicPressure) return false
  return row.systolicPressure > 140 || row.systolicPressure < 90 || 
         row.diastolicPressure > 90 || row.diastolicPressure < 60
}

const isHeartRateAbnormal = (heartRate) => {
  if (!heartRate) return false
  return heartRate > 100 || heartRate < 60
}

const isBloodSugarAbnormal = (bloodSugar) => {
  if (!bloodSugar) return false
  return bloodSugar > 6.1 || bloodSugar < 3.9
}

const getRouteNumber = (value) => {
  const num = Number(value)
  return Number.isFinite(num) && num > 0 ? num : null
}

const getRouteElderlyId = () => getRouteNumber(route.query.elderlyId)
const getRouteRecordId = () => getRouteNumber(route.query.recordId)

const focusRouteRecord = async () => {
  const recordId = getRouteRecordId()
  highlightedRecordId.value = recordId
  if (!recordId) {
    return
  }

  const targetRow = historyData.value.find(row => row.id === recordId)
  if (!targetRow) {
    return
  }

  await nextTick()
  const targetCell = document.querySelector('.search-target-cell')
  targetCell?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const loadElderly = async () => {
  const res = await request.get('/elderly/list')
  elderlyList.value = res.data
}

const loadHistory = async () => {
  loading.value = true
  try {
    const res = await request.get('/health/history', {
      params: {
        elderlyId: filterElderlyId.value,
        isAbnormal: filterIsAbnormal.value
      }
    })
    historyData.value = res.data || []
    await focusRouteRecord()
  } finally {
    loading.value = false
  }
}

const loadTrendData = async () => {
  if (!selectedElderlyId.value) {
    trendData.value = null
    return
  }
  chartLoading.value = true
  try {
    const res = await request.get(`/health/trend/${selectedElderlyId.value}`)
    trendData.value = res.data
    await nextTick()
    updateChart()
  } finally {
    chartLoading.value = false
  }
}

const applyRouteTarget = async () => {
  const elderlyId = getRouteElderlyId()
  highlightedRecordId.value = getRouteRecordId()

  if (elderlyId) {
    filterElderlyId.value = elderlyId
    selectedElderlyId.value = elderlyId
    await Promise.all([loadHistory(), loadTrendData()])
    return
  }

  selectedElderlyId.value = null
  highlightedRecordId.value = null
  await loadHistory()
}

const initChart = () => {
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    window.addEventListener('resize', handleResize)
  }
}

const handleResize = () => {
  chartInstance && chartInstance.resize()
}

const updateChart = () => {
  if (!chartInstance || !trendData.value) return

  const times = trendData.value.times || []
  let seriesData = []
  let title = ''
  let unit = ''
  let normalRange = {}

  switch (selectedIndicator.value) {
    case 'temperature':
      seriesData = trendData.value.temperatures || []
      title = '体温趋势'
      unit = '℃'
      normalRange = { min: 36.0, max: 37.3 }
      break
    case 'bloodPressure':
      title = '血压趋势'
      unit = 'mmHg'
      normalRange = { min: 60, max: 140 }
      break
    case 'heartRate':
      seriesData = trendData.value.heartRates || []
      title = '心率趋势'
      unit = '次/分'
      normalRange = { min: 60, max: 100 }
      break
    case 'bloodOxygen':
      seriesData = trendData.value.bloodOxygens || []
      title = '血氧饱和度趋势'
      unit = '%'
      normalRange = { min: 95, max: 100 }
      break
    case 'bloodSugar':
      seriesData = trendData.value.bloodSugars || []
      title = '血糖趋势'
      unit = 'mmol/L'
      normalRange = { min: 3.9, max: 6.1 }
      break
  }

  let option = {
    title: {
      text: title,
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: selectedIndicator.value === 'bloodPressure' ? ['收缩压', '舒张压'] : ['数值'],
      bottom: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: times
    },
    yAxis: {
      type: 'value',
      name: unit
    },
    series: []
  }

  if (selectedIndicator.value === 'bloodPressure') {
    option.series = [
      {
        name: '收缩压',
        type: 'line',
        smooth: true,
        data: trendData.value.systolicPressures || [],
        markLine: {
          data: [
            { yAxis: 140, name: '高压上限', lineStyle: { color: '#F56C6C', type: 'dashed' } },
            { yAxis: 90, name: '高压下限', lineStyle: { color: '#409EFF', type: 'dashed' } }
          ]
        }
      },
      {
        name: '舒张压',
        type: 'line',
        smooth: true,
        data: trendData.value.diastolicPressures || [],
        markLine: {
          data: [
            { yAxis: 90, name: '低压上限', lineStyle: { color: '#F56C6C', type: 'dashed' } },
            { yAxis: 60, name: '低压下限', lineStyle: { color: '#409EFF', type: 'dashed' } }
          ]
        }
      }
    ]
  } else {
    let markLineData = []
    if (selectedIndicator.value === 'temperature') {
      markLineData.push({ yAxis: 37.3, name: '异常值', lineStyle: { color: '#F56C6C', type: 'dashed' } })
    } else if (selectedIndicator.value === 'bloodOxygen') {
      markLineData.push({ yAxis: 95, name: '下限', lineStyle: { color: '#F56C6C', type: 'dashed' } })
    } else {
      markLineData.push(
        { yAxis: normalRange.max, name: '上限', lineStyle: { color: '#F56C6C', type: 'dashed' } },
        { yAxis: normalRange.min, name: '下限', lineStyle: { color: '#409EFF', type: 'dashed' } }
      )
    }

    option.series = [
      {
        name: '数值',
        type: 'line',
        smooth: true,
        data: seriesData,
        areaStyle: {
          opacity: 0.3
        },
        markLine: {
          data: markLineData
        }
      }
    ]
  }

  chartInstance.setOption(option)
}

const handleSave = async () => {
  if (!healthForm.value.elderlyId) {
    ElMessage.warning('请选择老人')
    return
  }

  await request.post('/health/add', healthForm.value)

  const hasAbnormal = checkHasAbnormal()
  if (hasAbnormal) {
    ElMessage.error('警告：存在异常指标！已自动标记。')
  } else {
    ElMessage.success('保存成功')
  }

  await loadHistory()
  if (selectedElderlyId.value === healthForm.value.elderlyId) {
    await loadTrendData()
  }
}

const checkHasAbnormal = () => {
  const form = healthForm.value
  if (form.temperature > 37.3) return true
  if (form.systolicPressure > 140 || form.systolicPressure < 90) return true
  if (form.diastolicPressure > 90 || form.diastolicPressure < 60) return true
  if (form.heartRate > 100 || form.heartRate < 60) return true
  if (form.bloodOxygen < 95) return true
  if (form.bloodSugar > 6.1 || form.bloodSugar < 3.9) return true
  return false
}

watch(() => [route.query.elderlyId, route.query.recordId], async () => {
  await applyRouteTarget()
})

onMounted(async () => {
  await loadElderly()
  initChart()
  await applyRouteTarget()
})

const handleExport = async () => {
  try {
    const exportParams = JSON.stringify({
      elderlyId: filterElderlyId.value,
      isAbnormal: filterIsAbnormal.value
    })
    let rangeDesc = '全部健康记录'
    const descParts = []
    if (filterElderlyId.value) {
      const elderly = elderlyList.value.find(e => e.id === filterElderlyId.value)
      descParts.push(elderly ? `老人: ${elderly.name}` : `老人ID: ${filterElderlyId.value}`)
    }
    if (filterIsAbnormal.value !== null && filterIsAbnormal.value !== undefined) {
      descParts.push(`状态: ${filterIsAbnormal.value ? '异常' : '正常'}`)
    }
    if (descParts.length > 0) {
      rangeDesc = descParts.join('，')
    }
    const res = await request.post('/export-task/create', {
      exportType: 'HEALTH_RECORD',
      exportParams: exportParams,
      exportRangeDesc: rangeDesc,
      taskName: '健康记录导出'
    })
    ElMessage.success('导出任务已创建，请在导出任务中心查看进度')
    router.push('/export-task')
  } catch (error) {
    console.error(error)
    ElMessage.error('创建导出任务失败')
  }
}

const openCorrectionDialog = (row) => {
  currentCorrectionRecord.value = row
  currentCorrectionElderlyName.value = row.elderlyName
  correctionForm.value = {
    healthRecordId: row.id,
    systolicPressure: row.systolicPressure,
    diastolicPressure: row.diastolicPressure,
    temperature: row.temperature,
    heartRate: row.heartRate,
    bloodOxygen: row.bloodOxygen,
    bloodSugar: row.bloodSugar,
    correctionReason: '',
    correctionRemark: ''
  }
  correctionDialogVisible.value = true
}

const submitCorrection = async () => {
  if (!correctionForm.value.correctionReason) {
    ElMessage.warning('请输入更正原因')
    return
  }
  
  correctionSubmitting.value = true
  try {
    await request.post('/health/correction/create', correctionForm.value)
    ElMessage.success('更正成功，异常判断和预警已重新计算')
    correctionDialogVisible.value = false
    await loadHistory()
    if (selectedElderlyId.value === currentCorrectionRecord.value.elderlyId) {
      await loadTrendData()
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('更正失败：' + (error.response?.data?.message || error.message))
  } finally {
    correctionSubmitting.value = false
  }
}

const openCorrectionHistory = async (row) => {
  historyDialogVisible.value = true
  historyLoading.value = true
  try {
    const res = await request.get(`/health/correction/list/${row.id}`)
    correctionHistory.value = res.data || []
  } catch (error) {
    console.error(error)
    ElMessage.error('加载更正历史失败')
  } finally {
    historyLoading.value = false
  }
}

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance && chartInstance.dispose()
})
</script>

<style scoped>
.health-container {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.header-filters {
  display: flex;
  align-items: center;
}

.chart-controls {
  display: flex;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.abnormal {
  color: #F56C6C;
  font-weight: bold;
}

.abnormal-reason {
  color: #909399;
  font-size: 12px;
}

.search-target-cell {
  background: #fff3cd;
  padding: 2px 6px;
  border-radius: 4px;
  box-shadow: 0 0 0 1px rgba(230, 162, 60, 0.35);
}

.history-data {
  display: flex;
  flex-direction: column;
  font-size: 12px;
  line-height: 1.6;
}

.history-data span {
  color: #606266;
}

:deep(.el-card__body) {
  padding: 15px;
}

:deep(.el-form-item) {
  margin-bottom: 15px;
}
</style>
