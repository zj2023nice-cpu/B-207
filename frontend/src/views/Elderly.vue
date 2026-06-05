<template>
  <div class="elderly-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>老人信息列表</span>
          <el-button type="primary" @click="handleAdd">新增老人</el-button>
        </div>
      </template>
      <el-table :data="tableData" border style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="age" label="年龄" />
        <el-table-column prop="gender" label="性别" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="address" label="居住地址" min-width="150" />
        <el-table-column label="紧急联系人" min-width="120">
          <template #default="scope">
            <div v-if="scope.row.emergencyContactName">
              <div>{{ scope.row.emergencyContactName }}</div>
              <div class="small-text">{{ scope.row.emergencyContactRelation }}</div>
              <div class="small-text">{{ scope.row.emergencyContactPhone }}</div>
            </div>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ scope.row.status || '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-dropdown @command="(command) => handleStatusChange(scope.row, command)" trigger="click">
              <el-button size="small" type="warning">修改状态</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="正常">正常</el-dropdown-item>
                  <el-dropdown-item command="住院">住院</el-dropdown-item>
                  <el-dropdown-item command="外出">外出</el-dropdown-item>
                  <el-dropdown-item command="失联" divided>失联</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑老人' : '新增老人'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄">
              <el-input-number v-model="form.age" :min="0" :max="120" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="居住地址">
          <el-input v-model="form.address" type="textarea" :rows="2" placeholder="请输入居住地址" />
        </el-form-item>
        
        <el-divider content-position="left">紧急联系人</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人姓名">
              <el-input v-model="form.emergencyContactName" placeholder="请输入紧急联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关系">
              <el-select v-model="form.emergencyContactRelation" placeholder="请选择关系" style="width: 100%">
                <el-option label="配偶" value="配偶" />
                <el-option label="儿子" value="儿子" />
                <el-option label="女儿" value="女儿" />
                <el-option label="孙子" value="孙子" />
                <el-option label="孙女" value="孙女" />
                <el-option label="侄子" value="侄子" />
                <el-option label="侄女" value="侄女" />
                <el-option label="其他亲属" value="其他亲属" />
                <el-option label="朋友" value="朋友" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="联系人电话">
          <el-input v-model="form.emergencyContactPhone" placeholder="请输入紧急联系人电话" />
        </el-form-item>
        
        <el-divider content-position="left">状态管理</el-divider>
        <el-form-item label="状态">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 200px">
            <el-option label="正常" value="正常" />
            <el-option label="住院" value="住院" />
            <el-option label="外出" value="外出" />
            <el-option label="失联" value="失联" />
          </el-select>
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
const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref({
  id: null,
  name: '',
  age: 70,
  gender: '男',
  phone: '',
  address: '',
  emergencyContactName: '',
  emergencyContactPhone: '',
  emergencyContactRelation: '',
  status: '正常'
})

const rules = {
  name: [
    { required: true, message: '姓名不能为空', trigger: 'blur' }
  ]
}

const getStatusType = (status) => {
  const statusMap = {
    '正常': 'success',
    '住院': 'warning',
    '外出': 'info',
    '失联': 'danger'
  }
  return statusMap[status] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/elderly/list')
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
    age: 70, 
    gender: '男', 
    phone: '', 
    address: '',
    emergencyContactName: '',
    emergencyContactPhone: '',
    emergencyContactRelation: '',
    status: '正常'
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  form.value = { ...row }
  dialogVisible.value = true
}

const handleStatusChange = async (row, newStatus) => {
  try {
    await ElMessageBox.confirm(
      `确定将 ${row.name} 的状态修改为 "${newStatus}" 吗？`,
      '确认修改',
      { type: 'warning' }
    )
    
    const updateData = { id: row.id, status: newStatus }
    await request.put('/elderly/update', updateData)
    ElMessage.success('状态修改成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('状态修改失败')
    }
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除此老人信息吗？', '警告', { type: 'warning' }).then(async () => {
    await request.delete(`/elderly/delete/${id}`)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (form.value.id) {
    await request.put('/elderly/update', form.value)
  } else {
    await request.post('/elderly/add', form.value)
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

.small-text {
  font-size: 12px;
  color: #909399;
}

.text-gray {
  color: #c0c4cc;
}
</style>
