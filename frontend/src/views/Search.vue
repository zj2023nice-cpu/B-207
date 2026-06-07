<template>
  <div class="search-page">
    <div class="search-header">
      <el-input
        v-model="keyword"
        placeholder="输入关键词搜索..."
        prefix-icon="Search"
        size="large"
        class="search-input-large"
        clearable
        @keyup.enter="doSearch"
      >
        <template #append>
          <el-button type="primary" size="large" @click="doSearch" :loading="loading">
            搜索
          </el-button>
        </template>
      </el-input>
    </div>

    <div v-if="result" class="search-content">
      <div class="search-stats">
        <span>找到 <strong>{{ result.total }}</strong> 条关于 "<strong>{{ result.keyword }}</strong>" 的结果</span>
      </div>

      <div class="search-tabs">
        <el-radio-group v-model="activeModule" size="default" @change="filterResults">
          <el-radio-button value="all">
            全部
            <el-badge v-if="result.total > 0" :value="result.total" class="tab-badge" />
          </el-radio-button>
          <el-radio-button v-for="config in moduleList" :key="config.key" :value="config.key">
            {{ config.name }}
            <el-badge v-if="getModuleCount(config.key) > 0" :value="getModuleCount(config.key)" class="tab-badge" />
          </el-radio-button>
        </el-radio-group>
      </div>

      <div v-if="filteredItems.length === 0" class="empty-result">
        <el-empty description="没有找到匹配的结果">
          <template #image>
            <el-icon :size="80" color="#dcdfe6"><Search /></el-icon>
          </template>
          <p>请尝试使用其他关键词搜索</p>
          <p class="empty-tips">
            提示：可以搜索老人姓名、电话、异常原因、预警消息等
          </p>
        </el-empty>
      </div>

      <div v-else class="search-results">
        <div
          v-for="item in filteredItems"
          :key="item.module + '-' + item.id"
          class="result-item"
          @click="goToItem(item)"
        >
          <div class="result-header">
            <el-tag :type="getModuleTagType(item.module)" size="small" class="module-tag">
              <el-icon class="tag-icon"><component :is="getModuleIcon(item.module)" /></el-icon>
              {{ item.moduleName }}
            </el-tag>
            <span class="result-time" v-if="item.time">
              {{ formatTime(item.time) }}
            </span>
          </div>
          <h3 class="result-title" v-html="highlight(item.title, result.keyword)"></h3>
          <p class="result-desc" v-if="item.description" v-html="highlight(item.description, result.keyword)"></p>
          <div class="result-footer">
            <span v-if="item.extraInfo" class="extra-info">{{ item.extraInfo }}</span>
            <span class="go-link">
              查看详情
              <el-icon><ArrowRight /></el-icon>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../utils/request'
import { highlightText, MODULE_CONFIG } from '../utils/search'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const result = ref(null)
const loading = ref(false)
const activeModule = ref('all')
const filteredItems = ref([])

const moduleList = computed(() => {
  return Object.keys(MODULE_CONFIG).map(key => ({
    key,
    ...MODULE_CONFIG[key]
  }))
})

const getModuleCount = (moduleKey) => {
  if (!result.value?.moduleCounts) return 0
  return result.value.moduleCounts[moduleKey] || 0
}

const getModuleTagType = (module) => {
  const typeMap = {
    elderly: 'primary',
    health_record: 'warning',
    warning: 'danger',
    notification: 'success'
  }
  return typeMap[module] || 'info'
}

const getModuleIcon = (module) => {
  return MODULE_CONFIG[module]?.icon || 'Document'
}

const highlight = (text, keyword) => {
  return highlightText(text, keyword)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const doSearch = async () => {
  if (!keyword.value.trim()) return
  
  loading.value = true
  try {
    const res = await request.get('/search', {
      params: {
        keyword: keyword.value.trim(),
        limit: 50
      }
    })
    result.value = res.data
    activeModule.value = 'all'
    filterResults()
    
    router.replace({
      path: '/search',
      query: { q: keyword.value.trim() }
    })
  } catch (e) {
    console.error('搜索失败', e)
  } finally {
    loading.value = false
  }
}

const filterResults = () => {
  if (!result.value?.items) {
    filteredItems.value = []
    return
  }
  
  if (activeModule.value === 'all') {
    filteredItems.value = [...result.value.items]
  } else {
    filteredItems.value = result.value.items.filter(item => item.module === activeModule.value)
  }
}

const buildRouteLocation = (item) => {
  switch (item.targetType || item.module) {
    case 'elderly':
      if (item.targetElderlyId) {
        return {
          path: '/elderly',
          query: { elderlyId: item.targetElderlyId }
        }
      }
      break
    case 'health_record':
      if (item.targetElderlyId && item.targetId) {
        return {
          path: '/health',
          query: {
            elderlyId: item.targetElderlyId,
            recordId: item.targetId
          }
        }
      }
      break
    case 'warning':
      if (item.targetId) {
        return {
          path: '/warning',
          query: { id: item.targetId }
        }
      }
      break
    case 'notification':
      if (item.targetId) {
        return {
          path: '/notification',
          query: { id: item.targetId }
        }
      }
      break
  }

  return item.routePath || null
}

const goToItem = (item) => {
  const target = buildRouteLocation(item)
  if (target) {
    router.push(target)
  }
}

watch(() => route.query.q, (newQ) => {
  if (newQ && newQ !== keyword.value) {
    keyword.value = newQ
    doSearch()
  }
})

onMounted(() => {
  if (route.query.q) {
    keyword.value = route.query.q
    doSearch()
  }
})
</script>

<style scoped>
.search-page {
  padding: 20px;
}

.search-header {
  max-width: 800px;
  margin: 0 auto 30px;
}

.search-input-large {
  width: 100%;
}

.search-content {
  max-width: 900px;
  margin: 0 auto;
}

.search-stats {
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
}

.search-stats strong {
  color: #409EFF;
}

.search-tabs {
  margin-bottom: 24px;
}

.tab-badge {
  margin-left: 6px;
}

.empty-result {
  text-align: center;
  padding: 60px 0;
}

.empty-tips {
  color: #909399;
  font-size: 13px;
  margin-top: 10px;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-item {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.result-item:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-1px);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.module-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.tag-icon {
  font-size: 12px;
}

.result-time {
  color: #909399;
  font-size: 13px;
}

.result-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 8px 0;
  line-height: 1.5;
}

.result-desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 8px 0;
}

.result-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.extra-info {
  color: #909399;
  font-size: 13px;
}

.go-link {
  color: #409EFF;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.result-item:hover .go-link {
  font-weight: 500;
}

:deep(.highlight) {
  color: #F56C6C;
  background-color: #fef0f0;
  padding: 0 2px;
  border-radius: 2px;
  font-weight: 500;
}
</style>
