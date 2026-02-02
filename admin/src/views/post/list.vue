<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-gray-900">动态管理</h1>
        <p class="text-sm text-gray-500 mt-1">管理用户发布的广场动态及评论。</p>
      </div>
      <div class="bg-white px-4 py-2 rounded-lg border border-gray-200 shadow-sm flex items-center gap-2">
           <span class="text-xs text-gray-400">TOTAL FEEDS</span>
           <span class="text-lg font-bold text-teal-600">{{ total }}</span>
       </div>
    </div>

    <!-- Filters -->
    <div class="bg-white p-5 rounded-lg shadow-sm border border-gray-200 flex flex-wrap gap-4 items-end">
      <div class="w-full sm:w-64">
        <label class="block text-xs font-medium text-gray-500 uppercase tracking-wider mb-1.5">内容搜索</label>
        <div class="relative">
             <input 
              v-model="listQuery.content" 
              placeholder="搜索动态内容..." 
              class="mantine-input"
              @keyup.enter="handleFilter"
            />
        </div>
      </div>

      <div class="w-full sm:w-48">
        <label class="block text-xs font-medium text-gray-500 uppercase tracking-wider mb-1.5">发布者</label>
        <div class="relative">
             <input 
              v-model="listQuery.nickname" 
              placeholder="用户昵称..." 
              class="mantine-input"
              @keyup.enter="handleFilter"
            />
        </div>
      </div>

      <div class="w-full sm:w-40">
        <label class="block text-xs font-medium text-gray-500 uppercase tracking-wider mb-1.5">区域</label>
        <el-select 
            v-model="listQuery.area" 
            placeholder="全部区域" 
            class="w-full mantine-select"
            @change="handleFilter"
            :teleported="false"
        >
             <el-option label="北京大学" value="北京大学" />
             <el-option label="天通苑社区" value="天通苑社区" />
        </el-select>
      </div>

      <div class="flex gap-2">
          <button @click="handleFilter" class="h-9 px-4 bg-gray-900 text-white text-sm font-medium rounded-md hover:bg-gray-700 transition-colors shadow-sm flex items-center gap-2">
            <el-icon><Search /></el-icon>
            查询
          </button>
          <button @click="resetQuery" class="h-9 px-4 border border-gray-300 bg-white text-gray-700 text-sm font-medium rounded-md hover:bg-gray-50 transition-colors">
            重置
          </button>
      </div>
    </div>

    <!-- Post List Table -->
    <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden flex flex-col h-[calc(100vh-280px)]">
      <!-- Header -->
      <div class="grid grid-cols-12 gap-4 px-6 py-3 bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider">
        <div class="col-span-1 text-center">ID</div>
        <div class="col-span-4">内容摘要</div>
        <div class="col-span-1 text-center">图片</div>
        <div class="col-span-2">发布者</div>
        <div class="col-span-2">发布位置</div>
        <div class="col-span-2 text-right pr-2">操作</div>
      </div>

      <!-- Content -->
      <div class="flex-1 overflow-y-auto divide-y divide-gray-100">
          <div 
            v-for="row in list" 
            :key="row.id"
            class="grid grid-cols-12 gap-4 items-center px-6 py-4 hover:bg-gray-50 transition-colors group"
          >
             <!-- ID -->
             <div class="col-span-1 text-center font-mono text-xs text-gray-400">
                 #{{ row.id }}
             </div>

             <!-- Content -->
             <div class="col-span-4">
                 <div class="text-sm font-medium text-gray-900 line-clamp-2" :title="row.content">{{ row.content || '（暂无文本内容）' }}</div>
                 <div class="text-xs text-gray-400 mt-1">{{ formatDate(row.createTime) }}</div>
             </div>

             <!-- Media -->
             <div class="col-span-1 text-center flex justify-center">
                 <div v-if="row.imageUrls" class="w-10 h-10 rounded bg-gray-100 flex items-center justify-center overflow-hidden border border-gray-200">
                     <img :src="getProcessedImageUrl(row.imageUrls.split(',')[0])" class="w-full h-full object-cover" @error="(e) => e.target.style.display='none'"/>
                 </div>
                 <div v-else-if="row.hasImage" class="w-8 h-8 rounded bg-teal-50 text-teal-600 flex items-center justify-center border border-teal-100">
                     <el-icon><Picture /></el-icon>
                 </div>
                 <span v-else class="text-xs text-gray-300">-</span>
             </div>

             <!-- Publisher -->
             <div class="col-span-2">
                 <div class="text-sm font-medium text-gray-800 truncate">{{ row.nickname }}</div>
             </div>

             <!-- Zone -->
             <div class="col-span-2">
                 <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-gray-50 text-gray-600 border border-gray-200 max-w-full truncate">
                     {{ row.area || '未知' }}
                 </span>
             </div>

             <!-- Actions -->
             <div class="col-span-2 flex justify-end gap-2 pr-2">
                 <button 
                  class="text-xs font-medium text-gray-600 hover:text-teal-600 hover:bg-gray-100 px-2 py-1 rounded transition-colors"
                  @click="handleDetail(row)"
                 >
                   评论管理
                 </button>
                 <button 
                  class="text-xs font-medium text-red-600 hover:text-red-700 hover:bg-red-50 px-2 py-1 rounded transition-colors"
                  @click="handleDelete(row)"
                 >
                   删除
                 </button>
             </div>
          </div>
          
           <!-- Empty/Loading -->
          <div v-if="loading" class="py-12 flex flex-col items-center justify-center gap-3 text-gray-400">
             <div class="animate-spin rounded-full h-5 w-5 border-2 border-gray-300 border-t-teal-500"></div>
             <span class="text-xs">加载动态列表...</span>
          </div>
          <div v-if="list.length === 0 && !loading" class="py-20 flex flex-col items-center justify-center text-gray-300">
             <div class="text-4xl opacity-20 mb-2">💬</div>
             <p class="text-sm">暂无动态记录</p>
          </div>
      </div>
    </div>

    <!-- Pagination -->
    <div class="flex justify-end gap-2">
       <button 
        :disabled="listQuery.page <= 1"
        class="px-3 py-1.5 border border-gray-300 rounded-md bg-white text-xs font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        @click="listQuery.page--; getList()"
       >
         上一页
       </button>
       <span class="px-3 py-1.5 text-xs font-medium text-gray-600 self-center">第 {{ listQuery.page }} 页</span>
        <button 
        :disabled="list.length < listQuery.size"
        class="px-3 py-1.5 border border-gray-300 rounded-md bg-white text-xs font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        @click="listQuery.page++; getList()"
       >
         下一页
       </button>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Search, Refresh } from '@element-plus/icons-vue'
import { getPostList, deletePost } from '../../api'

const loading = ref(false)
const list = ref([])
const total = ref(0) 

const listQuery = reactive({
  page: 1,
  size: 10,
  content: '',
  nickname: '',
  area: '',
  type: 3
})

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// BASE_URL for images - use the VITE_API_BASE_URL (which includes /admin-api)
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'

const getProcessedImageUrl = (url) => {
  if (!url) return ''
  // 1. Filter invalid WeChat temp paths
  if (url.includes('http://tmp/') || url.includes('wxfile://')) return ''
  
  // 2. If it is already a full http URL, return it
  if (url.startsWith('http')) return url
  
  // 3. Handle relative paths (add BASE_URL)
  const base = BASE_URL.endsWith('/') ? BASE_URL.slice(0, -1) : BASE_URL
  const path = url.startsWith('/') ? url : '/' + url
  return `${base}${path}`
}

const getList = async () => {
  loading.value = true
  try {
    const data = await getPostList(listQuery)
    if (data) {
      list.value = data.records || []
      total.value = data.total || 0
    }
  } catch (e) {
    console.error('Failed to fetch post list:', e)
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  listQuery.page = 1
  getList()
}

const resetQuery = () => {
  listQuery.content = ''
  listQuery.nickname = ''
  listQuery.area = ''
  handleFilter()
}

const handleDetail = (row) => {
  ElMessage.info(`管理动态 ${row.id} 的评论`)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认永久删除该条动态? 此操作不可恢复。',
      '删除警告',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'error',
      }
    )
    await deletePost(row.id)
    ElMessage.success('动态已删除')
    getList()
  } catch (e) {
     if(e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
/* Global styles in src/style.css */
</style>
