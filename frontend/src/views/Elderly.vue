<template>
  <div class="elderly-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>老人信息列表</span>
          <div class="header-actions">
            <el-select v-model="selectedTagId" placeholder="按标签筛选" style="width: 180px; margin-right: 10px" clearable @change="loadData">
              <el-option label="全部老人" :value="null" />
              <el-option 
                v-for="tag in tagList" 
                :key="tag.id" 
                :label="tag.name" 
                :value="tag.id"
              >
                <el-tag :color="tag.color" effect="dark" size="small">{{ tag.name }}</el-tag>
              </el-option>
            </el-select>
            <el-input 
              v-model="keyword" 
              placeholder="搜索姓名" 
              style="width: 200px; margin-right: 10px" 
              clearable 
              @keyup.enter="loadData"
            />
            <el-button type="primary" @click="handleAdd">新增老人</el-button>
            <el-dropdown @command="handleBatchAction" trigger="click" :disabled="selectedRows.length === 0">
              <el-button type="success" :disabled="selectedRows.length === 0">
                批量操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="bindTag">批量绑定标签</el-dropdown-item>
                  <el-dropdown-item command="unbindTag">批量解绑标签</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>
      <el-table 
        :data="tableData" 
        border 
        style="width: 100%" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="elderly.name" label="姓名" width="100" />
        <el-table-column prop="elderly.age" label="年龄" width="80" />
        <el-table-column prop="elderly.gender" label="性别" width="80" />
        <el-table-column prop="elderly.phone" label="联系电话" width="130" />
        <el-table-column prop="elderly.address" label="居住地址" min-width="150" />
        <el-table-column label="标签" min-width="200">
          <template #default="scope">
            <div class="tag-list">
              <el-tag 
                v-for="tag in scope.row.tags" 
                :key="tag.id" 
                :color="tag.color" 
                effect="dark" 
                size="small"
                style="margin-right: 4px; margin-bottom: 4px;"
              >
                {{ tag.name }}
              </el-tag>
              <span v-if="!scope.row.tags || scope.row.tags.length === 0" class="text-gray">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="elderly.status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.elderly.status)" size="small">
              {{ scope.row.elderly.status || '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button size="small" @click="handleEdit(scope.row.elderly)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleManageTags(scope.row.elderly)">管理标签</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.elderly.id)">删除</el-button>
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

    <el-dialog v-model="detailVisible" title="老人详情" width="600px">
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ currentDetail.elderly.name }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ currentDetail.elderly.age }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ currentDetail.elderly.gender }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentDetail.elderly.phone }}</el-descriptions-item>
          <el-descriptions-item label="居住地址" :span="2">{{ currentDetail.elderly.address }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系人">{{ currentDetail.elderly.emergencyContactName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关系">{{ currentDetail.elderly.emergencyContactRelation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentDetail.elderly.emergencyContactPhone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentDetail.elderly.status)" size="small">
              {{ currentDetail.elderly.status || '正常' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标签" :span="2">
            <div class="tag-list">
              <el-tag 
                v-for="tag in currentDetail.tags" 
                :key="tag.id" 
                :color="tag.color" 
                effect="dark" 
                size="small"
                style="margin-right: 4px; margin-bottom: 4px;"
              >
                {{ tag.name }}
              </el-tag>
              <span v-if="!currentDetail.tags || currentDetail.tags.length === 0" class="text-gray">-</span>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tagDialogVisible" :title="isBatch ? '批量管理标签' : '管理标签 - ' + currentElderlyName" width="500px">
      <div class="tag-manage">
        <p style="margin-bottom: 16px; color: #606266;">请选择要{{ isBatch ? actionType === 'bind' ? '绑定' : '解绑' : '绑定/解绑' }}的标签：</p>
        <el-alert 
          v-if="!isBatch && disabledTagCount > 0" 
          title="提示：以下标签中包含已停用的标签，您仍可取消绑定以清理数据" 
          type="info" 
          :closable="false" 
          style="margin-bottom: 16px;"
          size="small"
        />
        <el-checkbox-group v-model="selectedTagIds">
          <el-checkbox 
            v-for="tag in isBatch ? enabledTagList : tagList" 
            :key="tag.id" 
            :label="tag.id"
            style="margin-bottom: 12px; display: block;"
          >
            <el-tag :color="tag.color" effect="dark" size="small" :disabled="tag.status === '停用'">
              {{ tag.name }}
            </el-tag>
            <span v-if="tag.status === '停用'" style="margin-left: 4px; color: #F56C6C; font-size: 12px;">(已停用)</span>
            <span style="margin-left: 8px; color: #909399; font-size: 12px;">{{ tag.remark || '' }}</span>
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTagSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const tagList = ref([])
const selectedTagId = ref(null)
const keyword = ref('')
const selectedRows = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const tagDialogVisible = ref(false)
const formRef = ref(null)
const currentDetail = ref(null)
const currentElderlyId = ref(null)
const currentElderlyName = ref('')
const selectedTagIds = ref([])
const originalTagIds = ref([])
const isBatch = ref(false)
const actionType = ref('bind')

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

const enabledTagList = computed(() => {
  return tagList.value.filter(tag => tag.status === '启用')
})

const disabledTagCount = computed(() => {
  const disabledTags = tagList.value.filter(tag => tag.status === '停用')
  return disabledTags.filter(tag => originalTagIds.value.includes(tag.id)).length
})

const getStatusType = (status) => {
  const statusMap = {
    '正常': 'success',
    '住院': 'warning',
    '外出': 'info',
    '失联': 'danger'
  }
  return statusMap[status] || 'info'
}

const loadTags = async () => {
  try {
    const res = await request.get('/elderly-tags/list')
    tagList.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/elderly-tags/elderly/list-by-tag', {
      params: { 
        tagId: selectedTagId.value,
        keyword: keyword.value
      }
    })
    tableData.value = res.data
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTags()
  loadData()
})

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

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

const handleView = (row) => {
  currentDetail.value = row
  detailVisible.value = true
}

const handleManageTags = async (elderly) => {
  isBatch.value = false
  currentElderlyId.value = elderly.id
  currentElderlyName.value = elderly.name
  try {
    const res = await request.get(`/elderly-tags/elderly/${elderly.id}`)
    const tagIds = res.data.map(tag => tag.id)
    originalTagIds.value = [...tagIds]
    selectedTagIds.value = [...tagIds]
    tagDialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleBatchAction = async (command) => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择老人')
    return
  }
  isBatch.value = true
  actionType.value = command === 'bindTag' ? 'bind' : 'unbind'
  selectedTagIds.value = []
  tagDialogVisible.value = true
}

const handleTagSave = async () => {
  const elderlyIds = isBatch.value 
    ? selectedRows.value.map(item => item.elderly.id)
    : [currentElderlyId.value]
  
  try {
    if (isBatch.value) {
      if (selectedTagIds.value.length === 0) {
        ElMessage.warning('请选择标签')
        return
      }
      if (actionType.value === 'unbind') {
        await request.post('/elderly-tags/unbind', {
          elderlyIds,
          tagIds: selectedTagIds.value
        })
      } else {
        await request.post('/elderly-tags/bind', {
          elderlyIds,
          tagIds: selectedTagIds.value
        })
      }
    } else {
      const toBind = selectedTagIds.value.filter(id => !originalTagIds.value.includes(id))
      const toUnbind = originalTagIds.value.filter(id => !selectedTagIds.value.includes(id))
      
      if (toBind.length === 0 && toUnbind.length === 0) {
        ElMessage.info('未修改标签')
        tagDialogVisible.value = false
        return
      }
      
      const requests = []
      if (toBind.length > 0) {
        requests.push(request.post('/elderly-tags/bind', {
          elderlyIds,
          tagIds: toBind
        }))
      }
      if (toUnbind.length > 0) {
        requests.push(request.post('/elderly-tags/unbind', {
          elderlyIds,
          tagIds: toUnbind
        }))
      }
      
      await Promise.all(requests)
    }
    
    ElMessage.success('操作成功')
    tagDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
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

.header-actions {
  display: flex;
  align-items: center;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
}

.small-text {
  font-size: 12px;
  color: #909399;
}

.text-gray {
  color: #c0c4cc;
}

.detail-content {
  padding: 10px 0;
}

.tag-manage {
  max-height: 400px;
  overflow-y: auto;
}
</style>
