<template>
  <div class="log-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>操作审计日志</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operation" placeholder="请选择操作类型" clearable>
            <el-option label="用户登录" value="用户登录" />
            <el-option label="用户注册" value="用户注册" />
            <el-option label="添加老人信息" value="添加老人信息" />
            <el-option label="修改老人信息" value="修改老人信息" />
            <el-option label="删除老人信息" value="删除老人信息" />
            <el-option label="添加健康记录" value="添加健康记录" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="searchForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            clearable
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="searchForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作用户" width="120">
          <template #default="scope">
            <span>{{ scope.row.username || '未知' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作类型" width="130" />
        <el-table-column prop="description" label="操作描述" min-width="150">
          <template #default="scope">
            <span>{{ scope.row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="操作IP" width="130" />
        <el-table-column prop="success" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.success ? 'success' : 'danger'" size="small">
              {{ scope.row.success ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationTime" label="操作时间" width="180" />
        <el-table-column label="详情" width="100" fixed="right">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleView(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="日志详情" width="700px">
      <el-descriptions :column="1" border v-if="currentLog">
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="操作用户">{{ currentLog.username || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.operation }}</el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ currentLog.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog.operationTime }}</el-descriptions-item>
        <el-descriptions-item label="执行方法">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentLog.success ? 'success' : 'danger'" size="small">
            {{ currentLog.success ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="!currentLog.success">
          <span class="error-text">{{ currentLog.errorMsg || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <el-input
            type="textarea"
            :rows="5"
            :model-value="currentLog.params"
            readonly
            style="width: 100%"
          />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const currentLog = ref(null)

const searchForm = reactive({
  username: '',
  operation: '',
  startTime: '',
  endTime: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (searchForm.username) {
      params.username = searchForm.username
    }
    if (searchForm.operation) {
      params.operation = searchForm.operation
    }
    if (searchForm.startTime) {
      params.startTime = searchForm.startTime
    }
    if (searchForm.endTime) {
      params.endTime = searchForm.endTime
    }

    const res = await request.get('/operation-logs/page', { params })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.operation = ''
  searchForm.startTime = ''
  searchForm.endTime = ''
  pagination.pageNum = 1
  loadData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  loadData()
}

const handleView = (row) => {
  currentLog.value = { ...row }
  dialogVisible.value = true
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.log-container {
  height: 100%;
}

.error-text {
  color: #f56c6c;
  word-break: break-all;
}
</style>
