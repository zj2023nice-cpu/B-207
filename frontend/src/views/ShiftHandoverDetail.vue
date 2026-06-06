<template>
  <el-dialog v-model="dialogVisible" title="交接班记录详情" width="900px">
    <div v-loading="loading" class="detail-container">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="交班人">
          {{ detail?.handoverPerson || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="接班人">
          {{ detail?.takeoverPerson || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="交接时间" :span="2">
          {{ detail?.handoverTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="重点关注老人" :span="2">
          <div style="white-space: pre-wrap;">{{ detail?.keyElderly || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="待跟进预警摘要" :span="2">
          <div style="white-space: pre-wrap;">{{ detail?.pendingWarningSummary || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          <div style="white-space: pre-wrap;">{{ detail?.remarks || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ detail?.createdAt || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ detail?.updatedAt || '-' }}
        </el-descriptions-item>
      </el-descriptions>

      <div v-if="detail?.relatedWarnings?.length > 0" style="margin-top: 24px;">
        <h3 style="margin-bottom: 16px; font-size: 16px; font-weight: 500;">关联预警记录</h3>
        <el-table :data="detail.relatedWarnings" border>
          <el-table-column prop="elderlyName" label="老人姓名" width="120" />
          <el-table-column label="预警级别" width="100">
            <template #default="scope">
              <el-tag :type="getWarningLevelType(scope.row.warningLevel)" size="small">
                {{ scope.row.warningLevel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="indicatorType" label="指标类型" width="120" />
          <el-table-column prop="warningMessage" label="预警信息" min-width="200" />
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="预警时间" width="180" />
        </el-table>
      </div>
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import request from '../utils/request'

const props = defineProps({
  visible: Boolean,
  recordId: [Number, String]
})

const emit = defineEmits(['update:visible'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const loading = ref(false)
const detail = ref(null)

const getWarningLevelType = (level) => {
  const map = {
    HIGH: 'danger',
    MEDIUM: 'warning',
    LOW: 'info'
  }
  return map[level] || 'info'
}

const getStatusType = (status) => {
  const map = {
    PENDING: 'danger',
    READ: 'warning',
    HANDLED: 'success'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待处理',
    READ: '已读',
    HANDLED: '已处理'
  }
  return map[status] || status
}

const loadDetail = async () => {
  if (!props.recordId) return

  loading.value = true
  try {
    const res = await request.get(`/shift-handover/detail/${props.recordId}`)
    detail.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

watch(() => props.visible, (val) => {
  if (val && props.recordId) {
    loadDetail()
  } else {
    detail.value = null
  }
})

watch(() => props.recordId, () => {
  if (props.visible) {
    loadDetail()
  }
})
</script>

<style scoped>
.detail-container {
  padding: 10px 0;
}
</style>
