<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-gray-900">兼职管理</h1>
        <p class="text-sm text-gray-500 mt-1">管理校园兼职招聘信息发布。</p>
      </div>
      <div class="bg-white px-4 py-2 rounded-lg border border-gray-200 shadow-sm flex items-center gap-2">
           <span class="text-xs text-gray-400">ACTIVE JOBS</span>
           <span class="text-lg font-bold text-teal-600">{{ total }}</span>
       </div>
    </div>

    <!-- Filters -->
    <!-- Filters -->
    <div class="bg-white p-5 rounded-lg shadow-sm border border-gray-200 flex flex-wrap gap-4 items-end">
      <div class="w-full sm:w-80">
        <label class="block text-xs font-medium text-gray-500 uppercase tracking-wider mb-1.5 pl-1">关键词搜索</label>
        <div class="relative">
             <input 
              v-model="query.keyword" 
              placeholder="搜索职位..." 
              class="block w-full rounded-md border-gray-200 shadow-sm focus:border-teal-500 focus:ring-teal-500 sm:text-sm h-9 pl-3 pr-10 transition-colors bg-gray-50 focus:bg-white"
              @keyup.enter="refreshList"
            />
            <div class="absolute inset-y-0 right-0 flex items-center pr-3 cursor-pointer text-gray-400 hover:text-teal-600" @click="refreshList">
                <el-icon><Search /></el-icon>
            </div>
        </div>
      </div>

      <div class="flex gap-2">
          <button @click="refreshList" class="h-9 px-4 bg-white border border-gray-200 text-gray-700 text-sm font-medium rounded-md hover:bg-gray-50 transition-all shadow-sm flex items-center gap-2 active:scale-95">
            <el-icon class="text-gray-500"><Refresh /></el-icon>
            刷新
          </button>
      </div>
    </div>

    <!-- Job List Table -->
    <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden flex flex-col h-[calc(100vh-280px)]">
      <!-- Header -->
      <div class="grid grid-cols-12 gap-4 px-6 py-3 bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider">
        <div class="col-span-1 text-center">ID</div>
        <div class="col-span-3">职位标题 / 详情</div>
        <div class="col-span-2">薪资待遇</div>
        <div class="col-span-2">联系方式</div>
        <div class="col-span-2 text-center">状态</div>
        <div class="col-span-2 text-right pr-2">操作</div>
      </div>

      <!-- Content -->
      <div class="flex-1 overflow-y-auto divide-y divide-gray-100">
          <div 
            v-for="row in tableData" 
            :key="row.id"
            class="grid grid-cols-12 gap-4 items-center px-6 py-4 hover:bg-gray-50 transition-colors group"
          >
             <!-- ID -->
             <div class="col-span-1 text-center font-mono text-xs text-gray-400">{{ row.id }}</div>

             <!-- Title -->
             <div class="col-span-3">
                 <div class="font-medium text-sm text-gray-900 truncate">{{ row.title }}</div>
                 <div class="text-xs text-gray-500 mt-1 line-clamp-1 pr-4">{{ row.content }}</div>
                 <div class="text-xs text-gray-400 mt-1">{{ row.createTime }}</div>
             </div>

             <!-- Wage -->
             <div class="col-span-2">
                 <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-gray-100 text-teal-700">
                    {{ row.salary }}
                 </span>
             </div>

             <!-- Contact -->
             <div class="col-span-2 text-sm text-gray-600 truncate">
                 {{ row.contactInfo }}
             </div>

             <!-- Status -->
             <div class="col-span-2 text-center">
                 <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium"
                 :class="{
                     'bg-teal-50 text-teal-700': row.status === 0,
                     'bg-gray-100 text-gray-500': row.status !== 0
                 }">
                     <i class="w-1.5 h-1.5 rounded-full" :class="row.status === 0 ? 'bg-teal-500' : 'bg-gray-400'"></i>
                     {{ row.status === 0 ? '招聘中' : (row.status === 1 ? '已满员' : '已下架') }}
                 </span>
             </div>

             <!-- Actions -->
             <div class="col-span-2 flex justify-end gap-2 pr-2">
                 <button 
                  v-if="row.status === 0"
                  class="text-xs font-medium text-gray-600 hover:text-teal-600 hover:bg-gray-100 px-2 py-1 rounded transition-colors"
                  @click="handleStatus(row, 2)"
                 >
                   下架
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
             <span class="text-xs">加载职位信息...</span>
          </div>
          <div v-if="tableData.length === 0 && !loading" class="py-20 flex flex-col items-center justify-center text-gray-300">
             <div class="text-4xl opacity-20 mb-2">💼</div>
             <p class="text-sm">暂无招聘信息</p>
          </div>
      </div>
    </div>
    
     <!-- Pagination -->
    <div class="flex justify-end gap-2">
       <button 
        :disabled="query.page <= 1"
        class="px-3 py-1.5 border border-gray-300 rounded-md bg-white text-xs font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        @click="query.page--; refreshList()"
       >
         上一页
       </button>
       <span class="px-3 py-1.5 text-xs font-medium text-gray-600 self-center">第 {{ query.page }} 页</span>
        <button 
        :disabled="tableData.length < query.size"
        class="px-3 py-1.5 border border-gray-300 rounded-md bg-white text-xs font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        @click="query.page++; refreshList()"
       >
         下一页
       </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPartTimeList, updatePartTimeStatus, deletePartTime } from '../../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const refreshList = async () => {
  loading.value = true
  try {
    const res = await getPartTimeList(query)
    tableData.value = res.records
    total.value = res.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleStatus = (row, status) => {
   ElMessageBox.confirm(
    `确认将职位 "${row.title}" 设为 ${status === 2 ? '下架' : '更新'} 状态?`,
    '职位状态更新',
    { 
        confirmButtonText: '确认', 
        cancelButtonText: '取消', 
        type: 'warning',
    }
  ).then(async () => {
    await updatePartTimeStatus(row.id, status)
    ElMessage.success('状态已更新')
    refreshList()
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认永久删除该职位招聘信息?',
    '删除确认',
    { 
        confirmButtonText: '删除', 
        cancelButtonText: '取消', 
        type: 'warning',
    }
  ).then(async () => {
    await deletePartTime(row.id)
    ElMessage.success('职位已删除')
    refreshList()
  })
}

onMounted(() => {
  refreshList()
})
</script>

<style scoped>
/* Global styles in src/style.css */
</style>
