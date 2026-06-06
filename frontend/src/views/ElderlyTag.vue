<template>
  <div class="tag-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>标签管理</span>
          <div>
            <el-select v-model="statusFilter" placeholder="筛选状态" style="width: 120px; margin-right: 10px" clearable @change="loadData">
              <el-option label="全部" value="" />
              <el-option label="启用" value="启用" />
              <el-option label="停用" value="停用" />
            </el-select>
            <el-button type="primary" @click="handleAdd">新增标签</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="标签名称" width="150">
          <template #default="scope">
            <el-tag :color="scope.row.color" effect="dark" size="small">
              {{ scope.row.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="color" label="标签颜色" width="120">
          <template #default="scope">
            <div class="color-preview">
              <div class="color-box" :style="{ backgroundColor: scope.row.color }"></div>
              <span>{{ scope.row.color }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '启用' ? 'success' : 'info'" size="small">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              :type="scope.row.status === '启用' ? 'warning' : 'success'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === '启用' ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑标签' : '新增标签'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="标签颜色">
          <el-color-picker v-model="form.color" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 200px">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" maxlength="255" />
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
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const statusFilter = ref('')
const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref({
  id: null,
  name: '',
  color: '#409EFF',
  status: '启用',
  sort: 0,
  remark: ''
})

const rules = {
  name: [
    { required: true, message: '标签名称不能为空', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/elderly-tags/list', {
      params: { status: statusFilter.value }
    })
    tableData.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

const handleAdd = () => {
  form.value = {
    id: null,
    name: '',
    color: '#409EFF',
    status: '启用',
    sort: 0,
    remark: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const handleToggleStatus = async (row) => {
  const action = row.status === '启用' ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定${action}标签「${row.name}」吗？`,
      '确认操作',
      { type: 'warning' }
    )
    if (row.status === '启用') {
      await request.put(`/elderly-tags/disable/${row.id}`)
    } else {
      await request.put(`/elderly-tags/enable/${row.id}`)
    }
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error(`${action}失败`)
    }
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除此标签吗？删除后该标签与老人的关联也将解除。', '警告', { type: 'warning' }).then(async () => {
    await request.delete(`/elderly-tags/delete/${id}`)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (form.value.id) {
    await request.put('/elderly-tags/update', form.value)
  } else {
    await request.post('/elderly-tags/add', form.value)
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

.color-preview {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-box {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}
</style>
