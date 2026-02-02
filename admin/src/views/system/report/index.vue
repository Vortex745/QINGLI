<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-gray-900">举报管理</h1>
        <p class="text-sm text-gray-500 mt-1">处理用户的举报信息，维护社区环境。</p>
      </div>
      
      <div class="bg-white p-1 rounded-lg border border-gray-200 shadow-sm flex items-center">
         <button 
            v-for="opt in statusOptions"
            :key="opt.value"
            class="px-4 py-1.5 text-xs font-medium rounded-md transition-all"
            :class="query.status === opt.value ? 'bg-gray-100 text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
            @click="query.status = opt.value; refreshList()"
         >
           {{ opt.label }}
         </button>
      </div>
    </div>

    <!-- Report List Table -->
    <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden flex flex-col h-[calc(100vh-220px)]">
      <!-- Header -->
      <div class="grid grid-cols-12 gap-4 px-6 py-3 bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider">
        <div class="col-span-1 text-center">ID</div>
        <div class="col-span-1 text-center">对象</div>
        <div class="col-span-1 text-center">类型</div>
        <div class="col-span-3">理由详情</div>
        <div class="col-span-1 text-center">举报人</div>
        <div class="col-span-2 text-center">被举报ID / 名称</div>
        <div class="col-span-1 text-center">状态</div>
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
             <div class="col-span-1 text-center font-mono text-xs text-gray-400">#{{ row.id }}</div>

             <!-- Target Type -->
             <div class="col-span-1 text-center">
                 <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium border"
                    :class="getTargetTypeStyle(row.targetType)">
                     {{ getTargetTypeName(row.targetType) }}
                 </span>
             </div>

             <!-- Report Type -->
             <div class="col-span-1 text-center">
                 <span class="text-xs text-gray-600">{{ row.reportTypeName }}</span>
             </div>

             <!-- Reason -->
             <div class="col-span-3">
                 <div class="text-sm text-gray-800 line-clamp-2" :title="row.reason">{{ row.reason }}</div>
                 <div class="text-xs text-gray-400 mt-1">{{ row.createTime }}</div>
             </div>

             <!-- Reporter -->
             <div class="col-span-1 text-center text-xs text-gray-600">
                 {{ row.reporterName || '匿名' }}
             </div>

             <!-- Target -->
             <div class="col-span-2 text-center">
                 <div class="text-xs font-mono bg-gray-50 px-2 py-1 rounded inline-block text-gray-500 truncate max-w-full" :title="row.targetTitle">
                    {{ row.targetTitle || `ID:${row.targetId}` }}
                 </div>
             </div>

             <!-- Status -->
             <div class="col-span-1 text-center">
                 <span class="flex items-center justify-center gap-1.5 text-xs font-medium"
                 :class="{
                     'text-orange-600': row.status === 0,
                     'text-teal-600': row.status === 1,
                     'text-gray-400': row.status === 2
                 }">
                     <i class="w-1.5 h-1.5 rounded-full" :class="{
                         'bg-orange-500': row.status === 0,
                         'bg-teal-500': row.status === 1,
                         'bg-gray-400': row.status === 2
                     }"></i>
                     {{ row.status === 0 ? '待处理' : (row.status === 1 ? '已处理' : '已驳回') }}
                 </span>
             </div>

             <!-- Actions -->
             <div class="col-span-2 flex justify-end gap-2 pr-2">
                 <template v-if="row.status === 0">
                     <button 
                      class="text-xs font-medium text-teal-600 hover:text-teal-700 hover:bg-teal-50 px-2 py-1 rounded transition-colors"
                      @click="handleAction(row, 1)"
                     >
                       处理
                     </button>
                     <button 
                      class="text-xs font-medium text-gray-500 hover:text-gray-700 hover:bg-gray-100 px-2 py-1 rounded transition-colors"
                      @click="handleAction(row, 2)"
                     >
                       驳回
                     </button>
                 </template>
                 <span v-else class="text-xs text-gray-300 italic">已归档</span>
             </div>
          </div>

          <!-- Empty/Loading -->
          <div v-if="loading" class="py-12 flex flex-col items-center justify-center gap-3 text-gray-400">
             <div class="animate-spin rounded-full h-5 w-5 border-2 border-gray-300 border-t-teal-500"></div>
             <span>加载举报数据...</span>
          </div>
          <div v-if="tableData.length === 0 && !loading" class="py-20 flex flex-col items-center justify-center text-gray-300">
             <div class="text-4xl opacity-20 mb-2">⚖️</div>
             <p class="text-sm">暂无举报记录</p>
          </div>
      </div>
      
       <!-- Pagination -->
       <div class="border-t border-gray-100 p-3 bg-gray-50 flex justify-end">
            <el-pagination
                v-model:current-page="query.page"
                v-model:page-size="query.size"
                :total="total"
                :pager-count="5"
                layout="prev, pager, next"
                class="mantine-pagination"
                @current-change="refreshList"
            />
       </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getReportList, handleReport } from '../../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 10,
  status: 0 // Default pending
})

const statusOptions = [
    { label: '全部', value: null },
    { label: '待处理', value: 0 },
    { label: '已解决', value: 1 },
    { label: '已驳回', value: 2 },
]

const getTargetTypeName = (type) => {
    const map = { 1: '商品', 2: '动态', 3: '兼职', 4: '失物' }
    return map[type] || '用户'
}

const getTargetTypeStyle = (type) => {
    const map = {
        1: 'bg-green-50 text-green-700 border-green-100',
        2: 'bg-blue-50 text-blue-700 border-blue-100',
        3: 'bg-purple-50 text-purple-700 border-purple-100',
        4: 'bg-orange-50 text-orange-700 border-orange-100',
    }
    return map[type] || 'bg-gray-50 text-gray-600 border-gray-200'
}

const refreshList = async () => {
  loading.value = true
  try {
    const res = await getReportList(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleAction = (row, status) => {
  const actionText = status === 1 ? '标记已处理' : '驳回该举报'
  ElMessageBox.prompt(`确认${actionText}？请输入理由（可选）`, '处理举报', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(async ({ value }) => {
    await handleReport(row.id, status, value || '')
    ElMessage.success('操作成功')
    refreshList()
  })
}

onMounted(() => {
  refreshList()
})
</script>

<style scoped>
/* Minimal Element Pagination override to match */
:deep(.mantine-pagination .el-pager li) {
    background: transparent;
    font-weight: 500;
    color: #4b5563;
}
:deep(.mantine-pagination .el-pager li.is-active) {
    color: #0d9488;
    font-weight: 700;
}
</style>
