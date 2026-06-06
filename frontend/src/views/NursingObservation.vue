<template>
  <div class="nursing-observation-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>护理观察记录</span>
          <div class="header-actions">
            <el-select v-model="selectedElderlyId" placeholder="按老人筛选" style="width: 180px; margin-right: 10px" clearable @change="loadData">
              <el-option label="全部老人" :value="null" />
              <el-option 
                v-for="item in elderlyList" 
                :key="item.id" 
                :label="item.name" 
                :value="item.id"
              />
            </el-select>
            <el-button type="primary" @click="handleAdd">新增记录</el-button>
          </div>
        </div>
      </template>
      <el-table 
        :data="tableData" 
        border 
        style="width: 100%" 
        v-loading="loading"
      >
        <el-table-column prop="elderlyName" label="老人姓名" width="120" />
        <el-table-column prop="observationType" label="观察类型" width="120" />
        <el-table-column prop="observer" label="观察人" width="120" />
        <el-table-column prop="observationTime" label="观察时间" width="180" />
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="需要跟进" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.needFollowUp" type="danger" size="small">是</el-tag>
            <el-tag v-else type="success" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑观察记录' : '新增观察记录'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="老人" prop="elderlyId">
              <el-select v-model="form.elderlyId" placeholder="请选择老人" style="width: 100%">
                <el-option 
                  v-for="item in elderlyList" 
                  :key="item.id" 
                  :label="item.name" 
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="观察时间" prop="observationTime">
              <el-date-picker
                v-model="form.observationTime"
                type="datetime"
                placeholder="选择观察时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="观察人" prop="observer">
              <el-input v-model="form.observer" placeholder="请输入观察人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="观察类型" prop="observationType">
              <el-select v-model="form.observationType" placeholder="请选择观察类型" style="width: 100%">
                <el-option label="日常观察" value="日常观察" />
                <el-option label="精神状态" value="精神状态" />
                <el-option label="饮食情况" value="饮食情况" />
                <el-option label="排便情况" value="排便情况" />
                <el-option label="睡眠情况" value="睡眠情况" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="4" placeholder="请输入观察备注" />
        </el-form-item>
        <el-form-item label="需要跟进">
          <el-switch v-model="form.needFollowUp" active-text="是" inactive-text="否" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'

const route = useRoute()
const loading = ref(false)
const tableData = ref([])
const elderlyList = ref([])
const selectedElderlyId = ref(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref(null)

const form = ref({
  id: null,
  elderlyId: null,
  observationTime: null,
  observer: '',
  observationType: '',
  remark: '',
  needFollowUp: false
})

const rules = {
  elderlyId: [
    { required: true, message: '请选择老人', trigger: 'change' }
  ],
  observationTime: [
    { required: true, message: '请选择观察时间', trigger: 'change' }
  ],
  observer: [
    { required: true, message: '请输入观察人', trigger: 'blur' }
  ],
  observationType: [
    { required: true, message: '请选择观察类型', trigger: 'change' }
  ],
  remark: [
    { required: true, message: '请输入备注内容', trigger: 'blur' }
  ]
}

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
    if (selectedElderlyId.value) {
      const res = await request.get(`/nursing-observation/list/${selectedElderlyId.value}`)
      tableData.value = res.data
      total.value = res.data.length
    } else {
      const res = await request.get('/nursing-observation/page', {
        params: {
          pageNum: currentPage.value,
          pageSize: pageSize.value
        }
      })
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadElderly()
  if (route.query.elderlyId) {
    selectedElderlyId.value = parseInt(route.query.elderlyId)
  }
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
  const now = new Date()
  const formatted = now.getFullYear() + '-' +
    String(now.getMonth() + 1).padStart(2, '0') + '-' +
    String(now.getDate()).padStart(2, '0') + ' ' +
    String(now.getHours()).padStart(2, '0') + ':' +
    String(now.getMinutes()).padStart(2, '0') + ':' +
    String(now.getSeconds()).padStart(2, '0')
  
  form.value = {
    id: null,
    elderlyId: selectedElderlyId.value,
    observationTime: formatted,
    observer: '',
    observationType: '',
    remark: '',
    needFollowUp: false
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除此观察记录吗？', '警告', { type: 'warning' }).then(async () => {
    await request.delete(`/nursing-observation/delete/${id}`)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (form.value.id) {
    await request.put('/nursing-observation/update', form.value)
  } else {
    await request.post('/nursing-observation/add', form.value)
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
