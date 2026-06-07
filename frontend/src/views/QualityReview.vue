<template>
  <div class="quality-review-container">
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending || 0 }}</div>
              <div class="stat-label">待复核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon approved">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.approved || 0 }}</div>
              <div class="stat-label">确认有效</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon corrected">
              <el-icon><Edit /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.corrected || 0 }}</div>
              <div class="stat-label">已更正</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon ignored">
              <el-icon><Delete /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.ignored || 0 }}</div>
              <div class="stat-label">已忽略</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>质量复核列表</span>
          <div class="header-filters">
            <el-select v-model="filterElderlyId" placeholder="按老人筛选" clearable @change="loadReviews" style="width: 150px; margin-right: 10px;">
              <el-option v-for="item in elderlyList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
            <el-select v-model="filterReviewStatus" placeholder="按状态筛选" clearable @change="loadReviews" style="width: 120px; margin-right: 10px;">
              <el-option label="待复核" value="PENDING" />
              <el-option label="确认有效" value="APPROVED" />
              <el-option label="已更正" value="CORRECTED" />
              <el-option label="已忽略" value="IGNORED" />
            </el-select>
            <el-button type="primary" size="small" @click="loadStats">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      <el-table :data="reviewList" border style="width: 100%" v-loading="loading" max-height="500" row-key="id">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="elderlyName" label="姓名" width="100" />
        <el-table-column label="质量评分" width="100">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.qualityScore || 0" 
              :color="getScoreColor(scope.row.qualityScore)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column label="复核状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.reviewStatus)" size="small">
              {{ getStatusText(scope.row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="issuesSummary" label="问题摘要" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reviewerName" label="复核人" width="100" />
        <el-table-column prop="reviewTime" label="复核时间" width="180" />
        <el-table-column prop="createdAt" label="检测时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">查看</el-button>
            <el-button 
              v-if="scope.row.reviewStatus === 'PENDING'" 
              type="success" 
              size="small" 
              @click="openReviewDialog(scope.row)"
            >
              复核
            </el-button>
            <el-button type="info" size="small" @click="goToHealthRecord(scope.row)">
              <el-icon><Link /></el-icon>
              原记录
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="复核详情" width="900px">
      <div v-if="currentReview" v-loading="detailLoading">
        <el-descriptions :column="3" border style="margin-bottom: 20px;">
          <el-descriptions-item label="复核ID">{{ currentReview.id }}</el-descriptions-item>
          <el-descriptions-item label="老人姓名">{{ currentReview.elderlyName }}</el-descriptions-item>
          <el-descriptions-item label="复核状态">
            <el-tag :type="getStatusType(currentReview.reviewStatus)">
              {{ getStatusText(currentReview.reviewStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="质量评分">
            <el-progress 
              :percentage="currentReview.qualityScore || 0" 
              :color="getScoreColor(currentReview.qualityScore)"
              :stroke-width="8"
            />
          </el-descriptions-item>
          <el-descriptions-item label="复核人">{{ currentReview.reviewerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="复核时间">{{ currentReview.reviewTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="检测时间" :span="3">{{ currentReview.createdAt }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">检测到的问题</el-divider>
        <el-alert 
          :title="currentReview.issuesSummary || '无问题'" 
          type="warning" 
          :closable="false"
          style="margin-bottom: 20px;"
        />

        <el-divider content-position="left">原始健康记录</el-divider>
        <el-descriptions v-if="currentReview.healthRecord" :column="3" border>
          <el-descriptions-item label="血压">
            {{ currentReview.healthRecord.systolicPressure }}/{{ currentReview.healthRecord.diastolicPressure }} mmHg
          </el-descriptions-item>
          <el-descriptions-item label="体温">{{ currentReview.healthRecord.temperature }} ℃</el-descriptions-item>
          <el-descriptions-item label="心率">{{ currentReview.healthRecord.heartRate }} 次/分</el-descriptions-item>
          <el-descriptions-item label="血氧">{{ currentReview.healthRecord.bloodOxygen }} %</el-descriptions-item>
          <el-descriptions-item label="血糖">{{ currentReview.healthRecord.bloodSugar }} mmol/L</el-descriptions-item>
          <el-descriptions-item label="检测时间">{{ currentReview.healthRecord.checkTime }}</el-descriptions-item>
        </el-descriptions>

        <el-divider v-if="currentReview.reviewConclusion || currentReview.ignoreReason" content-position="left">复核信息</el-divider>
        <el-descriptions v-if="currentReview.reviewConclusion || currentReview.ignoreReason" :column="1" border>
          <el-descriptions-item v-if="currentReview.reviewConclusion" label="复核结论">
            {{ currentReview.reviewConclusion }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentReview.ignoreReason" label="忽略原因">
            {{ currentReview.ignoreReason }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
          v-if="currentReview && currentReview.reviewStatus === 'PENDING'" 
          type="primary" 
          @click="openReviewDialog(currentReview)"
        >
          去复核
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogVisible" title="质量复核" width="600px">
      <el-form :model="reviewForm" label-width="100px">
        <el-form-item label="复核结论" required>
          <el-radio-group v-model="reviewForm.reviewStatus">
            <el-radio value="APPROVED">确认有效</el-radio>
            <el-radio value="CORRECTED">已更正</el-radio>
            <el-radio value="IGNORED">忽略</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="复核结论" required>
          <el-input 
            v-model="reviewForm.reviewConclusion" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入复核结论说明"
          />
        </el-form-item>
        <el-form-item v-if="reviewForm.reviewStatus === 'IGNORED'" label="忽略原因" required>
          <el-input 
            v-model="reviewForm.ignoreReason" 
            type="textarea" 
            :rows="2" 
            placeholder="请输入忽略该问题的原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submitting">提交复核</el-button>
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
const detailLoading = ref(false)
const submitting = ref(false)
const elderlyList = ref([])
const reviewList = ref([])
const stats = ref({})
const filterElderlyId = ref(null)
const filterReviewStatus = ref(null)

const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const currentReview = ref(null)
const reviewForm = ref({
  reviewId: null,
  reviewStatus: 'APPROVED',
  reviewConclusion: '',
  ignoreReason: ''
})

const getStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    CORRECTED: 'primary',
    IGNORED: 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待复核',
    APPROVED: '确认有效',
    CORRECTED: '已更正',
    IGNORED: '已忽略'
  }
  return map[status] || status
}

const getScoreColor = (score) => {
  if (score >= 80) return '#67C23A'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

const loadElderly = async () => {
  try {
    const res = await request.get('/elderly/list')
    elderlyList.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const loadStats = async () => {
  try {
    const res = await request.get('/quality-review/stats')
    stats.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const loadReviews = async () => {
  loading.value = true
  try {
    const res = await request.get('/quality-review/list', {
      params: {
        elderlyId: filterElderlyId.value,
        reviewStatus: filterReviewStatus.value
      }
    })
    reviewList.value = res.data || []
  } catch (e) {
    console.error(e)
    ElMessage.error('加载复核列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row) => {
  detailLoading.value = true
  detailDialogVisible.value = true
  try {
    const res = await request.get(`/quality-review/detail/${row.id}`)
    currentReview.value = res.data
  } catch (e) {
    console.error(e)
    ElMessage.error('加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

const openReviewDialog = (row) => {
  reviewForm.value = {
    reviewId: row.id,
    reviewStatus: 'APPROVED',
    reviewConclusion: '',
    ignoreReason: ''
  }
  reviewDialogVisible.value = true
  detailDialogVisible.value = false
}

const submitReview = async () => {
  if (!reviewForm.value.reviewConclusion) {
    ElMessage.warning('请输入复核结论')
    return
  }
  if (reviewForm.value.reviewStatus === 'IGNORED' && !reviewForm.value.ignoreReason) {
    ElMessage.warning('请输入忽略原因')
    return
  }
  
  submitting.value = true
  try {
    await request.post('/quality-review/submit', reviewForm.value)
    ElMessage.success('复核提交成功')
    reviewDialogVisible.value = false
    await Promise.all([loadReviews(), loadStats()])
  } catch (e) {
    console.error(e)
    ElMessage.error('复核提交失败：' + (e.response?.data?.message || e.message))
  } finally {
    submitting.value = false
  }
}

const goToHealthRecord = (row) => {
  router.push({
    path: '/health',
    query: {
      elderlyId: row.elderlyId,
      recordId: row.healthRecordId
    }
  })
}

onMounted(async () => {
  await Promise.all([loadElderly(), loadStats(), loadReviews()])
})
</script>

<style scoped>
.quality-review-container {
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

.stat-card {
  cursor: pointer;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.pending {
  background: linear-gradient(135deg, #E6A23C, #F0C78E);
}

.stat-icon.approved {
  background: linear-gradient(135deg, #67C23A, #95D475);
}

.stat-icon.corrected {
  background: linear-gradient(135deg, #409EFF, #79BBFF);
}

.stat-icon.ignored {
  background: linear-gradient(135deg, #909399, #C0C4CC);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
</style>
