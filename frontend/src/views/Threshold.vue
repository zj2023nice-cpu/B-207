<template>
  <div class="threshold-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预警阈值配置</span>
          <div class="header-actions">
            <el-select v-model="selectedElderlyId" placeholder="选择老人查看阈值" @change="loadThresholds" clearable style="width: 180px; margin-right: 10px;">
              <el-option label="系统默认阈值" :value="null" />
              <el-option v-for="item in elderlyList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <el-button type="primary" @click="loadThresholds">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <el-alert 
        v-if="selectedElderlyId === null" 
        type="info" 
        :closable="false" 
        style="margin-bottom: 20px;"
      >
        <template #title>
          当前显示的是系统默认阈值，所有老人默认使用此配置。
          可以为特定老人设置自定义阈值，自定义阈值优先级更高。
        </template>
      </el-alert>
      
      <el-alert 
        v-else 
        type="warning" 
        :closable="false" 
        style="margin-bottom: 20px;"
      >
        <template #title>
          当前配置的是老人 <strong>{{ getElderlyName(selectedElderlyId) }}</strong> 的自定义阈值。
          未配置的指标将使用系统默认阈值。
        </template>
      </el-alert>

      <el-table :data="thresholdList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="indicatorType" label="指标类型" width="120">
          <template #default="scope">
            <span class="indicator-name">{{ getIndicatorName(scope.row.indicatorType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="高阈值" width="200">
          <template #default="scope">
            <div class="threshold-input-group">
              <el-input-number 
                v-model="scope.row.highThreshold" 
                :precision="getPrecision(scope.row.indicatorType)"
                :step="getStep(scope.row.indicatorType)"
                :min="getMinValue(scope.row.indicatorType)"
                :max="getMaxValue(scope.row.indicatorType)"
                placeholder="不限制"
                :disabled="!scope.row.enabled"
                style="width: 140px;"
              />
              <span class="unit">{{ getIndicatorUnit(scope.row.indicatorType) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="低阈值" width="200">
          <template #default="scope">
            <div class="threshold-input-group">
              <el-input-number 
                v-model="scope.row.lowThreshold" 
                :precision="getPrecision(scope.row.indicatorType)"
                :step="getStep(scope.row.indicatorType)"
                :min="getMinValue(scope.row.indicatorType)"
                :max="getMaxValue(scope.row.indicatorType)"
                placeholder="不限制"
                :disabled="!scope.row.enabled"
                style="width: 140px;"
              />
              <span class="unit">{{ getIndicatorUnit(scope.row.indicatorType) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="启用" width="100">
          <template #default="scope">
            <el-switch v-model="scope.row.enabled" active-text="是" inactive-text="否" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="saveThreshold(scope.row)">
              保存
            </el-button>
            <el-button 
              v-if="selectedElderlyId !== null" 
              type="danger" 
              size="small" 
              @click="deleteThreshold(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-divider content-position="left">系统默认阈值参考</el-divider>
      
      <el-descriptions :column="3" border>
        <el-descriptions-item label="体温">
          > 37.3℃ 为异常
        </el-descriptions-item>
        <el-descriptions-item label="收缩压">
          > 140 或 < 90 mmHg 为异常
        </el-descriptions-item>
        <el-descriptions-item label="舒张压">
          > 90 或 < 60 mmHg 为异常
        </el-descriptions-item>
        <el-descriptions-item label="心率">
          > 100 或 < 60 次/分 为异常
        </el-descriptions-item>
        <el-descriptions-item label="血氧">
          < 95% 为异常
        </el-descriptions-item>
        <el-descriptions-item label="血糖">
          > 6.1 或 < 3.9 mmol/L 为异常
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const selectedElderlyId = ref(null)
const elderlyList = ref([])
const thresholdList = ref([])

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

const indicatorConfig = {
  temperature: { precision: 1, step: 0.1, min: 30, max: 45 },
  systolicPressure: { precision: 0, step: 1, min: 50, max: 200 },
  diastolicPressure: { precision: 0, step: 1, min: 30, max: 120 },
  heartRate: { precision: 0, step: 1, min: 30, max: 200 },
  bloodOxygen: { precision: 0, step: 1, min: 70, max: 100 },
  bloodSugar: { precision: 1, step: 0.1, min: 2, max: 20 }
}

const getIndicatorName = (type) => indicatorNames[type] || type
const getIndicatorUnit = (type) => indicatorUnits[type] || ''
const getPrecision = (type) => indicatorConfig[type]?.precision || 0
const getStep = (type) => indicatorConfig[type]?.step || 1
const getMinValue = (type) => indicatorConfig[type]?.min || 0
const getMaxValue = (type) => indicatorConfig[type]?.max || 1000

const getElderlyName = (id) => {
  const elderly = elderlyList.value.find(e => e.id === id)
  return elderly ? elderly.name : ''
}

const loadElderly = async () => {
  const res = await request.get('/elderly/list')
  elderlyList.value = res.data || []
}

const loadThresholds = async () => {
  loading.value = true
  try {
    if (selectedElderlyId.value === null) {
      const res = await request.get('/warning/threshold/system')
      thresholdList.value = res.data || []
    } else {
      const effectiveRes = await request.get(`/warning/threshold/effective/${selectedElderlyId.value}`)
      const effectiveData = effectiveRes.data || {}
      
      const personalRes = await request.get(`/warning/threshold/elderly/${selectedElderlyId.value}`)
      const personalData = personalRes.data || []
      
      const personalMap = new Map()
      personalData.forEach(t => personalMap.set(t.indicatorType, t))
      
      const allIndicators = ['temperature', 'systolicPressure', 'diastolicPressure', 'heartRate', 'bloodOxygen', 'bloodSugar']
      thresholdList.value = allIndicators.map(type => {
        if (personalMap.has(type)) {
          return personalMap.get(type)
        }
        const effective = effectiveData[type]
        return {
          id: null,
          elderlyId: selectedElderlyId.value,
          indicatorType: type,
          highThreshold: effective?.high || null,
          lowThreshold: effective?.low || null,
          enabled: true,
          isNew: true
        }
      })
    }
  } finally {
    loading.value = false
  }
}

const saveThreshold = async (row) => {
  const data = {
    id: row.id || null,
    elderlyId: selectedElderlyId.value,
    indicatorType: row.indicatorType,
    highThreshold: row.highThreshold,
    lowThreshold: row.lowThreshold,
    enabled: row.enabled
  }
  
  await request.post('/warning/threshold/save', data)
  ElMessage.success('保存成功')
  loadThresholds()
}

const deleteThreshold = async (row) => {
  if (!row.id) {
    ElMessage.warning('该阈值尚未保存，无需删除')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除此阈值配置吗？删除后将使用系统默认阈值。', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/warning/threshold/${row.id}`)
    ElMessage.success('删除成功')
    loadThresholds()
  } catch {
  }
}

onMounted(() => {
  loadElderly()
  loadThresholds()
})
</script>

<style scoped>
.threshold-container {
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

.indicator-name {
  font-weight: bold;
  color: #409EFF;
}

.threshold-input-group {
  display: flex;
  align-items: center;
}

.unit {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
