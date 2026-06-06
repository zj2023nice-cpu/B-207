<template>
  <div class="shift-handover-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>交接班记录</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleAdd">新建交接</el-button>
          </div>
        </div>
      </template>
      <el-table 
        :data="tableData" 
        border 
        style="width: 100%" 
        v-loading="loading"
      >
        <el-table-column prop="handoverPerson" label="交班人" width="120" />
        <el-table-column prop="takeoverPerson" label="接班人" width="120" />
        <el-table-column prop="handoverTime" label="交接时间" width="180" />
        <el-table-column prop="keyElderly" label="重点关注老人" min-width="200" show-overflow-tooltip />
        <el-table-column prop="pendingWarningSummary" label="待跟进预警摘要" min-width="250" show-overflow-tooltip />
        <el-table-column prop="remarks" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleView(scope.row.id)">查看</el-button>
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
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

    <shift-handover-form
      v-model:visible="formVisible"
      :record="currentRecord"
      @success="handleSuccess"
    />

    <shift-handover-detail
      v-model:visible="detailVisible"
      :record-id="currentDetailId"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import ShiftHandoverForm from './ShiftHandoverForm.vue'
import ShiftHandoverDetail from './ShiftHandoverDetail.vue'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const formVisible = ref(false)
const detailVisible = ref(false)
const currentRecord = ref(null)
const currentDetailId = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/shift-handover/page', {
      params: {
        pageNum: currentPage.value,
        pageSize: pageSize.value
      }
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

const handleSizeChange = (val) => {
  pageSize.value = val
  loadData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadData()
}

const handleAdd = () => {
  currentRecord.value = null
  formVisible.value = true
}

const handleEdit = (row) => {
  currentRecord.value = { ...row }
  formVisible.value = true
}

const handleView = (id) => {
  currentDetailId.value = id
  detailVisible.value = true
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除此交接班记录吗？', '警告', { type: 'warning' }).then(async () => {
    await request.delete(`/shift-handover/delete/${id}`)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleSuccess = () => {
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
