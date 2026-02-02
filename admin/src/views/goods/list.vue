<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-gray-900">商品管理</h1>
        <p class="text-sm text-gray-500 mt-1">管理用户发布的二手商品及库存状态。</p>
      </div>
       <div class="bg-white px-4 py-2 rounded-lg border border-gray-200 shadow-sm flex items-center gap-2">
           <span class="text-xs text-gray-400">TOTAL ITEMS</span>
           <span class="text-lg font-bold text-teal-600">{{ total }}</span>
       </div>
    </div>

    <!-- Filters -->
    <!-- Filters (Clean Toolbar) -->
    <div class="bg-white p-4 rounded-lg border border-gray-200 shadow-sm flex flex-wrap items-center gap-3">
      <!-- Search Input -->
      <div class="relative w-full sm:w-64">
           <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <el-icon class="text-gray-400"><Search /></el-icon>
           </div>
           <input 
            v-model="query.name" 
            placeholder="搜索商品名称..." 
            class="mantine-input pl-9"
            @keyup.enter="handleSearch"
          />
      </div>

      <!-- Category Select -->
      <div class="w-full sm:w-40">
        <el-select 
            v-model="query.category" 
            placeholder="商品分类" 
            class="w-full mantine-select"
            @change="handleSearch"
            :teleported="false"
        >
            <el-option label="所有分类" value="" />
            <el-option label="电子产品" value="电子产品" />
            <el-option label="图书资料" value="图书资料" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="服饰鞋包" value="服饰鞋包" />
            <el-option label="其他" value="其他" />
        </el-select>
      </div>

      <!-- Status Select -->
      <div class="w-full sm:w-40">
        <el-select 
            v-model="query.status" 
            placeholder="商品状态" 
            class="w-full mantine-select"
            @change="handleSearch"
            :teleported="false"
        >
            <el-option label="所有状态" :value="undefined" />
            <el-option label="上架中" :value="0" />
            <el-option label="已下架" :value="3" />
        </el-select>
      </div>

      <!-- Action -->
      <button @click="handleSearch" class="h-9 px-4 bg-gray-900 text-white text-sm font-medium rounded-md hover:bg-gray-800 transition-colors shadow-sm flex items-center gap-2">
         <span>查询</span>
      </button>
    </div>

    <!-- Goods List Table -->
    <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden flex flex-col h-[calc(100vh-280px)]">
      <!-- Header -->
      <div class="grid grid-cols-12 gap-4 px-6 py-3 bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider">
        <div class="col-span-4 pl-2">商品详情</div>
        <div class="col-span-2">发布者</div>
        <div class="col-span-1 text-center">价格</div>
        <div class="col-span-1 text-center">分类</div>
        <div class="col-span-1 text-center">成色</div>
        <div class="col-span-1 text-center">状态</div>
        <div class="col-span-2 text-right pr-2">操作</div>
      </div>

      <!-- List Content -->
      <div 
        class="flex-1 overflow-y-auto divide-y divide-gray-100"
        @scroll="handleScroll"
      >
          <div 
            v-for="item in tableData" 
            :key="item.id"
            class="grid grid-cols-12 gap-4 items-center px-6 py-4 hover:bg-gray-50 transition-colors group"
          >
             <!-- Item Info -->
             <div class="col-span-4 flex items-start gap-4">
                 <div class="w-12 h-12 rounded-lg bg-gray-100 shrink-0 relative overflow-hidden ring-1 ring-gray-200">
                     <img 
                        v-if="item.imageUrl"
                        :src="getProcessedImageUrl(item.imageUrl)"
                        class="w-full h-full object-cover transform hover:scale-110 transition-transform duration-500"
                        @error="handleImageError"
                     />
                 </div>
                 <div class="min-w-0">
                     <div class="font-medium text-gray-900 text-sm truncate" :title="item.name">{{ item.name }}</div>
                     <div class="text-xs text-gray-500 mt-0.5 font-mono">ID:{{ item.id }}</div>
                 </div>
             </div>

             <!-- Publisher -->
             <div class="col-span-2 flex items-center gap-2">
                 <div class="w-6 h-6 rounded-full bg-gray-200 shrink-0 overflow-hidden">
                    <img 
                      v-if="item.sellerAvatar" 
                      :src="getProcessedImageUrl(item.sellerAvatar)" 
                      class="w-full h-full object-cover"
                      @error="(e) => e.target.style.display='none'"
                    />
                    <div v-else class="w-full h-full flex items-center justify-center text-[10px] text-gray-500 bg-gray-100">
                        {{ (item.sellerName && item.sellerName[0]) || 'U' }}
                    </div>
                 </div>
                 <span class="text-xs text-gray-700 truncate" :title="item.sellerName">{{ item.sellerName || '未命名用户' }}</span>
             </div>

             <!-- Price -->
             <div class="col-span-1 text-center flex flex-col items-center justify-center">
                 <span class="font-bold text-gray-900">¥{{ item.price }}</span>
             </div>

             <!-- Category -->
             <div class="col-span-1 text-center">
                 <span class="inline-flex items-center px-2 py-0.5 rounded text-[10px] font-medium bg-gray-50 text-gray-600 border border-gray-100 truncate max-w-full">
                    {{ item.category || '-' }}
                 </span>
             </div>

             <!-- Condition -->
             <div class="col-span-1 text-center">
                 <span class="text-xs text-gray-500 scale-90 inline-block">{{ item.goodsCondition || '-' }}</span>
             </div>

             <!-- Status -->
             <div class="col-span-1 text-center">
                 <span 
                   class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-medium"
                   :class="item.status === 0 ? 'bg-teal-50 text-teal-700' : 'bg-gray-100 text-gray-500'"
                 >
                     <i class="w-1 h-1 rounded-full" :class="item.status === 0 ? 'bg-teal-500' : 'bg-gray-400'"></i>
                     {{ item.status === 0 ? '上架' : '下架' }}
                 </span>
             </div>

             <!-- Actions -->
             <div class="col-span-2 flex justify-end gap-2 pr-2">
                 <button 
                  class="text-xs font-medium text-teal-600 hover:text-teal-700 hover:bg-teal-50 px-2 py-1 rounded transition-colors"
                  @click="showDetail(item)"
                 >
                   查看
                 </button>
                 <button 
                  v-if="item.status === 0"
                  class="text-xs font-medium text-red-600 hover:text-red-700 hover:bg-red-50 px-2 py-1 rounded transition-colors"
                  @click="handleOffShelf(item)"
                 >
                   下架
                 </button>
                 <button 
                  v-else
                  class="text-xs font-medium text-gray-500 hover:text-gray-700 hover:bg-gray-100 px-2 py-1 rounded transition-colors"
                  @click="handleOnShelf(item)"
                 >
                   上架
                 </button>
             </div>
          </div>

          <!-- Empty/Loading -->
          <div v-if="loading" class="py-12 flex flex-col items-center justify-center gap-3 text-gray-400">
             <div class="animate-spin rounded-full h-5 w-5 border-2 border-gray-300 border-t-teal-500"></div>
             <span class="text-xs">加载数据...</span>
          </div>
          <div v-if="tableData.length === 0 && !loading" class="py-20 flex flex-col items-center justify-center text-gray-300">
             <div class="text-4xl opacity-20 mb-2">📦</div>
             <p class="text-sm">暂无商品</p>
          </div>
      </div>
    </div>

    <!-- Detail Dialog -->
    <el-dialog
      v-model="detailVisible"
      title="商品详情"
      width="600px"
      align-center
      class="mantine-dialog"
    >
        <div class="p-1" v-if="currentGoods">
            <div class="flex gap-6">
                <!-- Image -->
                <div class="w-1/3 shrink-0">
                    <div class="w-full aspect-square rounded-lg bg-gray-100 overflow-hidden border border-gray-200">
                        <img :src="getProcessedImageUrl(currentGoods.imageUrl)" class="w-full h-full object-cover" @error="handleImageError"/>
                    </div>
                </div>

                <!-- Info -->
                <div class="flex-1 space-y-4">
                    <div>
                        <div class="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">商品名称</div>
                        <div class="font-bold text-lg text-gray-900 border-b border-gray-100 pb-2">{{ currentGoods.name }}</div>
                    </div>
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <div class="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">价格</div>
                            <div class="text-xl font-bold text-teal-600">¥{{ currentGoods.price }}</div>
                        </div>
                        <div>
                             <div class="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">分类</div>
                             <div class="text-gray-900 font-medium">{{ currentGoods.category || '未分类' }}</div>
                        </div>
                    </div>
                    <div>
                        <div class="text-xs font-medium text-gray-500 uppercase tracking-wide mb-1">描述</div>
                        <div class="text-sm text-gray-600 bg-gray-50 p-3 rounded-lg border border-gray-100 min-h-[80px]">{{ currentGoods.description || '暂无描述' }}</div>
                    </div>
                </div>
            </div>

            <!-- Footer -->
            <div class="mt-8 flex justify-end gap-3 pt-4 border-t border-gray-100">
                 <button 
                  v-if="currentGoods.status === 0"
                  class="px-4 py-2 bg-red-50 text-red-600 rounded-md text-sm font-medium hover:bg-red-100 transition-colors"
                  @click="handleOffShelf(currentGoods); detailVisible = false">
                   执行下架
                 </button>
                 <button 
                  v-else
                  class="px-4 py-2 bg-teal-600 text-white rounded-md text-sm font-medium hover:bg-teal-700 shadow-sm transition-all"
                  @click="handleOnShelf(currentGoods); detailVisible = false">
                   重新上架
                 </button>
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
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getGoodsList, updateGoodsStatus } from '../../api'

const query = reactive({
  name: '',
  category: '',
  status: undefined,
  page: 1,
  size: 20
})

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const noMore = ref(false)
const detailVisible = ref(false)
const currentGoods = ref(null)
// BASE_URL for images - use the VITE_API_BASE_URL (which includes /admin-api)
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'

const getProcessedImageUrl = (url) => {
  if (!url) return ''
  // Filter out WeChat temp paths which are accessible only on the specific device
  if (url.includes('http://tmp/') || url.includes('wxfile://')) {
      return ''
  }
  if (url.startsWith('http')) return url
  // Ensure we don't double slash if base ends with / and url starts with /
  const base = BASE_URL.endsWith('/') ? BASE_URL.slice(0, -1) : BASE_URL
  const path = url.startsWith('/') ? url : '/' + url
  return `${base}${path}`
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const handleImageError = (e) => {
  e.target.src = 'https://images.unsplash.com/photo-1594322436404-5a0526db4d13?w=300&q=80'
}

const loadMore = async (refresh = false) => {
  if (loading.value || (noMore.value && !refresh)) return
  loading.value = true
  
  if (refresh) {
    query.page = 1
    noMore.value = false
    tableData.value = []
  }

  try {
    const data = await getGoodsList(query)
    if (data) {
      const list = data.records || []
      total.value = data.total || 0
      if (list.length < query.size) noMore.value = true
      tableData.value = [...tableData.value, ...list]
      query.page++
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleScroll = (e) => {
  const { scrollTop, scrollHeight, clientHeight } = e.target
  if (scrollTop + clientHeight >= scrollHeight - 50) {
    loadMore()
  }
}

const handleSearch = () => loadMore(true)

const showDetail = (goods) => {
  currentGoods.value = goods
  detailVisible.value = true
}

const handleOffShelf = async (goods) => {
  try {
    await ElMessageBox.confirm(`确认是否下架商品 "${goods.name}" ?`, '下架确认', {
      confirmButtonText: '下架',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await updateGoodsStatus(goods.id, 3)
    goods.status = 3
    ElMessage.success('商品已下架')
  } catch (e) {
    //
  }
}

const handleOnShelf = async (goods) => {
  try {
    await ElMessageBox.confirm(`确认重新上架商品 "${goods.name}" ?`, '上架确认', {
      confirmButtonText: '上架',
      cancelButtonText: '取消',
      type: 'success',
    })
    await updateGoodsStatus(goods.id, 0)
    goods.status = 0
    ElMessage.success('商品已上架')
  } catch (e) {
    //
  }
}

onMounted(() => loadMore(true))
</script>

<style scoped>
/* Global styles in src/style.css */

/* Dialog Customization */
:deep(.mantine-dialog) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}
:deep(.mantine-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 16px 24px;
  border-bottom: 1px solid #f3f4f6;
}
:deep(.mantine-dialog .el-dialog__title) {
  font-weight: 600;
  color: #111827;
}
:deep(.mantine-dialog .el-dialog__body) {
  padding: 24px;
}
</style>
