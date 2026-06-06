<template>
  <div class="visitor-visit-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>访客来访登记</span>
          <div class="header-actions">
            <el-select v-model="filterElderlyId" placeholder="按老人筛选" style="width: 160px; margin-right: 10px" clearable @change="handleFilterChange">
              <el-option label="全部老人" :value="null" />
              <el-option 
                v-for="item in elderlyList" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id"
              />
            </el-select>
            <el-select v-model="filterStatus" placeholder="按状态筛选" style="width: 140px; margin-right: 10px" clearable @change="handleFilterChange">
              <el-option label="来访中" value="VISITING" />
              <el-option label="已离开" value="LEFT" />
            </el-select>
            <el-date-picker
              v-model="dateRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              style="width: 320px; margin-right: 10px"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              @change="handleFilterChange"
            />
            <el-button type="primary" @click="handleAdd">来访登记</el-button>
          </div>
        </div>
      </template>
      <el-table 
        :data="tableData" 
        border 
        style="width: 100%" 
        v-loading="loading"
      >
        <el-table-column prop="visitorName" label="来访人姓名" width="120" />
        <el-table-column prop="visitorPhone" label="联系电话" width="140" />
        <el-table-column prop="relationWithElderly" label="与老人关系" width="120" />
        <el-table-column prop="elderlyName" label="探访对象" width="120" />
        <el-table-column prop="visitTime" label="到访时间" width="180" />
        <el-table-column prop="leaveTime" label="离开时间" width="180">
          <template #default="scope">
            <span v-if="scope.row.leaveTime">{{ scope.row.leaveTime }}</span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
        <el-table-column label="来访状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'VISITING'" type="warning" size="small">来访中</el-tag>
            <el-tag v-else type="success" size="small">已离开</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="260">
          <template #default="scope">
            <el-button size="small" @click="handleDetail(scope.row)">详情</el-button>
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              v-if="scope.row.status === 'VISITING'" 
              size="small" 
              type="success" 
              @click="handleLeave(scope.row)"
            >
              离开登记
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end; display: flex;"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="来访人姓名" prop="visitorName">
              <el-input v-model="form.visitorName" placeholder="请输入来访人姓名" :disabled="isDetailMode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="visitorPhone">
              <el-input v-model="form.visitorPhone" placeholder="请输入联系电话" :disabled="isDetailMode" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="与老人关系" prop="relationWithElderly">
              <el-select v-model="form.relationWithElderly" placeholder="请选择关系" style="width: 100%" :disabled="isDetailMode">
                <el-option label="配偶" value="配偶" />
                <el-option label="儿子" value="儿子" />
                <el-option label="女儿" value="女儿" />
                <el-option label="孙子/孙女" value="孙子/孙女" />
                <el-option label="父母" value="父母" />
                <el-option label="兄弟" value="兄弟" />
                <el-option label="姐妹" value="姐妹" />
                <el-option label="亲戚" value="亲戚" />
                <el-option label="朋友" value="朋友" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="探访对象" prop="elderlyId">
              <el-select v-model="form.elderlyId" placeholder="请选择老人" style="width: 100%" :disabled="isDetailMode">
                <el-option 
                  v-for="item in elderlyList" 
                  :key="item.id" 
                  :label="item.name" 
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="到访时间" prop="visitTime">
              <el-date-picker
                v-model="form.visitTime"
                type="datetime"
                placeholder="选择到访时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                :disabled="isDetailMode"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="离开时间" prop="leaveTime">
              <el-date-picker
                v-model="form.leaveTime"
                type="datetime"
                placeholder="选择离开时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                :disabled="isDetailMode"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="来访状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%" :disabled="isDetailMode">
                <el-option label="来访中" value="VISITING" />
                <el-option label="已离开" value="LEFT" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" :disabled="isDetailMode" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!isDetailMode" type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const elderlyList = ref([])
const filterElderlyId = ref(null)
const filterStatus = ref(null)
const dateRange = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref(null)
const isDetailMode = ref(false)

const form = reactive({
  id: null,
  visitorName: '',
  visitorPhone: '',
  relationWithElderly: '',
  elderlyId: null,
  visitTime: '',
  leaveTime: '',
  status: '',
  remark: ''
})

const rules = {
  visitorName: [
    { required: true, message: '请输入来访人姓名', trigger: 'blur' }
  ],
  visitorPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ],
  relationWithElderly: [
    { required: true, message: '请选择与老人关系', trigger: 'change' }
  ],
  elderlyId: [
    { required: true, message: '请选择探访对象', trigger: 'change' }
  ],
  visitTime: [
    { required: true, message: '请选择到访时间', trigger: 'change' }
  ]
}

const dialogTitle = computed(() => {
  if (isDetailMode.value) return '访客详情'
  if (form.id) return '编辑来访记录'
  return '来访登记'
})

const loadElderly = async () => {
  try {
    const res = await request.get('/elderly/list')
    elderlyList.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (filterElderlyId.value) {
      params.elderlyId = filterElderlyId.value
    }
    if (filterStatus.value) {
      params.status = filterStatus.value
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await request.get('/visitor-visit/page', { params })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadElderly()
  loadData()
})

const handleFilterChange = () => {
  currentPage.value = 1
  loadData()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

const formatDateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const handleAdd = () => {
  isDetailMode.value = false
  Object.assign(form, {
    id: null,
    visitorName: '',
    visitorPhone: '',
    relationWithElderly: '',
    elderlyId: null,
    visitTime: formatDateTime(),
    leaveTime: '',
    status: 'VISITING',
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isDetailMode.value = false
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDetail = (row) => {
  isDetailMode.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

const handleDialogClose = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleLeave = (row) => {
  ElMessageBox.confirm('确定登记该访客离开吗？', '提示', { type: 'warning' }).then(async () => {
    await request.put(`/visitor-visit/leave/${row.id}`)
    ElMessage.success('离开登记成功')
    loadData()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除此来访记录吗？', '警告', { type: 'warning' }).then(async () => {
    await request.delete(`/visitor-visit/delete/${id}`)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (form.id) {
    await request.put('/visitor-visit/update', form)
  } else {
    await request.post('/visitor-visit/add', form)
  }
  ElMessage.success('操作成功')
  dialogVisible.value = false
  loadData()
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}
</style>
