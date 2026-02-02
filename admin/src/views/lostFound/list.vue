<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-gray-900">失物招领</h1>
        <p class="text-sm text-gray-500 mt-1">管理校园内的失物寻回与招领信息。</p>
      </div>
      <div class="bg-white px-4 py-2 rounded-lg border border-gray-200 shadow-sm flex items-center gap-2">
           <span class="text-xs text-gray-400">TOTAL CASES</span>
           <span class="text-lg font-bold text-teal-600">{{ total }}</span>
       </div>
    </div>

    <!-- Filters -->
    <!-- Filters -->
    <div class="bg-white p-5 rounded-lg shadow-sm border border-gray-200 flex flex-wrap gap-4 items-end">
      
      <div class="w-full sm:w-40">
        <label class="block text-xs font-medium text-gray-500 uppercase tracking-wider mb-1.5 pl-1">类型</label>
        <el-select 
            v-model="listQuery.type" 
            placeholder="全部" 
            class="w-full !h-9"
            size="default"
            @change="handleFilter"
            :teleported="false"
        >
             <el-option label="失物" :value="0" />
             <el-option label="招领" :value="1" />
        </el-select>
      </div>

      <div class="w-full sm:w-64">
        <label class="block text-xs font-medium text-gray-500 uppercase tracking-wider mb-1.5 pl-1">物品搜索</label>
        <div class="relative">
             <input 
              v-model="listQuery.itemName" 
              placeholder="物品名称关键词..." 
              class="block w-full rounded-md border-gray-200 shadow-sm focus:border-teal-500 focus:ring-teal-500 sm:text-sm h-9 pl-3 pr-3 transition-colors bg-gray-50 focus:bg-white"
              @keyup.enter="handleFilter"
            />
        </div>
      </div>

      <div class="w-full sm:w-40">
        <label class="block text-xs font-medium text-gray-500 uppercase tracking-wider mb-1.5 pl-1">案件状态</label>
        <el-select 
            v-model="listQuery.status" 
            placeholder="全部状态" 
            class="w-full !h-9"
            size="default"
            @change="handleFilter"
            :teleported="false"
        >
             <el-option label="寻找中" :value="0" />
             <el-option label="已解决" :value="1" />
             <el-option label="已关闭" :value="2" />
        </el-select>
      </div>

      <div class="flex gap-2">
          <button @click="handleFilter" class="h-9 px-4 bg-teal-600 text-white text-sm font-medium rounded-md hover:bg-teal-700 transition-all shadow-sm flex items-center gap-2 active:scale-95 shadow-teal-100">
            <el-icon><Search /></el-icon>
            搜索
          </button>
          <button @click="resetQuery" class="h-9 px-4 border border-gray-200 bg-white text-gray-600 text-sm font-medium rounded-md hover:bg-gray-50 transition-all active:scale-95">
            重置
          </button>
      </div>
    </div>

    <!-- Grid Table -->
    <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden flex flex-col h-[calc(100vh-280px)]">
      <!-- Header -->
      <div class="grid grid-cols-12 gap-4 px-6 py-3 bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider">
        <div class="col-span-1 text-center">ID</div>
        <div class="col-span-1 text-center">类型</div>
        <div class="col-span-2">物品名称</div>
        <div class="col-span-3">位置详情</div>
        <div class="col-span-2">学校</div>
        <div class="col-span-1 text-center">状态</div>
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
             <div class="col-span-1 text-center font-mono text-xs text-gray-400">{{ row.id }}</div>

             <!-- Type -->
             <div class="col-span-1 text-center">
                 <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium"
                    :class="row.type === 1 ? 'bg-blue-50 text-blue-700' : 'bg-orange-50 text-orange-700'">
                     {{ row.type === 1 ? '招领' : '失物' }}
                 </span>
             </div>

             <!-- Name -->
             <div class="col-span-2">
                 <div class="font-medium text-sm text-gray-900 truncate" :title="row.itemName">{{ row.itemName }}</div>
                 <div class="text-xs text-gray-400 mt-0.5">{{ formatDate(row.createTime) }}</div>
             </div>

             <!-- Location -->
             <div class="col-span-3">
                 <div class="text-xs text-gray-600 truncate" :title="row.location">
                     <el-icon class="mr-1 relative top-0.5"><Location /></el-icon>
                     {{ row.location }}
                 </div>
             </div>

             <!-- School -->
             <div class="col-span-2">
                 <span class="text-xs text-gray-500">{{ row.university }}</span>
             </div>

             <!-- Status -->
             <div class="col-span-1 text-center">
                 <span class="text-xs font-medium" :class="{
                     'text-teal-600': row.status === 1,
                     'text-blue-600': row.status === 0,
                     'text-gray-400': row.status === 2
                 }">
                     {{ getStatusText(row.status) }}
                 </span>
             </div>

             <!-- Actions -->
             <div class="col-span-2 flex justify-end gap-2 pr-2">
                 <button 
                  class="text-xs font-medium text-gray-600 hover:text-teal-600 hover:bg-gray-100 px-2 py-1 rounded transition-colors"
                  @click="handleDetail(row)"
                 >
                   详情
                 </button>
                 <button 
                  v-if="row.status === 0"
                  class="text-xs font-medium text-teal-600 hover:text-teal-700 hover:bg-teal-50 px-2 py-1 rounded transition-colors"
                  @click="handleMarkFound(row)"
                 >
                   标记解决
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
             <span class="text-xs">加载记录...</span>
          </div>
          <div v-if="list.length === 0 && !loading" class="py-20 flex flex-col items-center justify-center text-gray-300">
             <div class="text-4xl opacity-20 mb-2">🔍</div>
             <p class="text-sm">暂无失物招领记录</p>
          </div>
      </div>
    </div>

    <!-- Detail Dialog -->
     <el-dialog
      v-model="detailVisible"
      :title="`案件详情 #${currentItem?.id || ''}`"
      width="500px"
      align-center
      class="mantine-dialog"
    >
        <div class="p-1 space-y-4" v-if="currentItem">
            <div class="grid grid-cols-2 gap-4">
                <div class="space-y-1">
                    <label class="text-xs font-medium text-gray-500 uppercase">物品名称</label>
                    <div class="text-sm font-semibold text-gray-900 border-b border-gray-100 pb-1">{{ currentItem.itemName }}</div>
                </div>
                <div class="space-y-1">
                     <label class="text-xs font-medium text-gray-500 uppercase">分类</label>
                     <div class="text-sm text-gray-900">{{ currentItem.category || '未分类' }}</div>
                </div>
            </div>
            
            <div class="space-y-1">
                 <label class="text-xs font-medium text-gray-500 uppercase">位置坐标</label>
                 <div class="text-sm text-gray-700 bg-gray-50 px-3 py-2 rounded-md border border-gray-200 flex items-center gap-2">
                     <el-icon><Location /></el-icon>
                     {{ currentItem.location }}
                 </div>
            </div>

            <div class="space-y-1">
                 <label class="text-xs font-medium text-gray-500 uppercase">特征描述</label>
                 <div class="text-sm text-gray-600 bg-white border border-gray-200 rounded-md p-3 min-h-[60px]">{{ currentItem.features || '无描述' }}</div>
            </div>

            <div class="grid grid-cols-2 gap-4 pt-2">
                 <div class="space-y-1">
                     <label class="text-xs font-medium text-gray-500 uppercase">联系信息</label>
                     <div class="text-sm font-mono text-teal-600 bg-teal-50 px-2 py-0.5 rounded inline-block">{{ currentItem.contactInfo || '隐藏' }}</div>
                 </div>
                 <div class="space-y-1 text-right">
                      <label class="text-xs font-medium text-gray-500 uppercase">发布时间</label>
                      <div class="text-xs text-gray-400">{{ formatDate(currentItem.createTime) }}</div>
                 </div>
            </div>

            <div class="mt-6 flex justify-end pt-4 border-t border-gray-100">
                <button @click="detailVisible = false" class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors">
                    关闭
                </button>
            </div>
        </div>
     </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Location } from '@element-plus/icons-vue'
import { getLostFoundList, updateLostFoundStatus, deleteLostFound } from '../../api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentItem = ref(null)

const listQuery = reactive({ 
  page: 1, 
  size: 10, 
  type: null, 
  itemName: '',
  status: null
})

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const getStatusText = (status) => {
  if (status === 1) return '已解决'
  if (status === 2) return '已关闭'
  return '寻找中'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await getLostFoundList(listQuery)
    if (data) {
      list.value = data.records || []
      total.value = data.total || 0
    }
  } catch (e) {
    console.error('Failed to fetch lost found list:', e)
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  listQuery.page = 1
  getList()
}

const resetQuery = () => {
  listQuery.type = null
  listQuery.itemName = ''
  listQuery.status = null
  handleFilter()
}

const handleDetail = (row) => {
  currentItem.value = row
  detailVisible.value = true
}

const handleMarkFound = async (row) => {
  try {
    await ElMessageBox.confirm('确认将此案件标记为已解决?', '状态更新', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'success',
    })
    await updateLostFoundStatus(row.id, 1)
    ElMessage.success('案件已解决')
    getList()
  } catch (e) {
    //
  }
}

const handleDelete = async (row) => {
  try {
     await ElMessageBox.confirm('确认永久删除此记录?', '删除确认', {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
    })
    await deleteLostFound(row.id)
    ElMessage.success('记录已清除')
    getList()
  } catch (e) {
    //
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
/* Global styles in src/style.css */
</style>
