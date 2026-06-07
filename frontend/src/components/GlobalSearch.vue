<template>
  <div class="global-search">
    <el-input
      v-model="searchKeyword"
      placeholder="搜索老人、健康记录、预警、通知..."
      prefix-icon="Search"
      size="default"
      class="search-input"
      clearable
      @keyup.enter="goToSearchPage"
      @focus="showSuggestions = true"
      @blur="handleBlur"
    >
      <template #append>
        <el-button @click="goToSearchPage">
          <el-icon><Search /></el-icon>
        </el-button>
      </template>
    </el-input>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchKeyword = ref('')
const showSuggestions = ref(false)

const goToSearchPage = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/search',
      query: { q: searchKeyword.value.trim() }
    })
  }
}

const handleBlur = () => {
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}
</script>

<style scoped>
.global-search {
  position: relative;
  width: 360px;
  margin-right: 20px;
}

.search-input {
  width: 100%;
}
</style>
