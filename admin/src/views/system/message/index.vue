<template>
  <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 h-[calc(100vh-140px)]">
    <!-- Send Message Form -->
    <div class="lg:col-span-1 bg-white rounded-lg p-6 shadow-sm border border-gray-200 flex flex-col">
       <div class="mb-6">
          <h2 class="text-lg font-bold text-gray-900 tracking-tight">发送系统通知</h2>
          <p class="text-sm text-gray-500 mt-1">向用户推送重要消息。</p>
       </div>

       <el-form :model="form" label-position="top" class="flex-1 flex flex-col space-y-2">
          <div class="space-y-1">
             <label class="block text-sm font-medium text-gray-700">消息标题</label>
             <input v-model="form.title" class="mantine-input" placeholder="请输入标题"/>
          </div>

          <div class="space-y-1 pt-2">
             <label class="block text-sm font-medium text-gray-700">发送对象</label>
             <div class="flex gap-2">
                 <button 
                  class="flex-1 py-1.5 text-sm font-medium rounded-md border transition-all"
                  :class="form.type === 0 ? 'bg-teal-50 border-teal-200 text-teal-700' : 'bg-white border-gray-300 text-gray-600 hover:bg-gray-50'"
                  @click="form.type = 0"
                 >
                   全员通知
                 </button>
                 <button 
                  class="flex-1 py-1.5 text-sm font-medium rounded-md border transition-all"
                  :class="form.type === 1 ? 'bg-teal-50 border-teal-200 text-teal-700' : 'bg-white border-gray-300 text-gray-600 hover:bg-gray-50'"
                  @click="form.type = 1"
                 >
                   指定用户
                 </button>
             </div>
          </div>
          
          <div class="space-y-1 pt-2" v-if="form.type === 1">
             <label class="block text-sm font-medium text-gray-700">目标用户ID</label>
             <input v-model="form.targetId" class="mantine-input" placeholder="例如: 10001"/>
          </div>

          <div class="space-y-1 pt-2 flex-1 flex flex-col">
             <label class="block text-sm font-medium text-gray-700">内容详情</label>
             <textarea 
               v-model="form.content" 
               class="mantine-input h-full min-h-[150px] py-2 resize-none" 
               placeholder="请输入通知内容 details..."
             ></textarea>
          </div>

          <div class="mt-6">
             <button 
               class="w-full py-2.5 bg-gray-900 text-white font-medium rounded-md shadow-sm hover:bg-gray-800 transition-all flex items-center justify-center gap-2 disabled:opacity-70"
               @click="handleSend" 
               :disabled="sending"
             >
                <div v-if="sending" class="animate-spin rounded-full h-4 w-4 border-2 border-white/30 border-t-white"></div>
                <span>{{ sending ? '发送中...' : '立即发送' }}</span>
             </button>
          </div>
       </el-form>
    </div>

    <!-- History List -->
    <div class="lg:col-span-2 bg-white rounded-lg shadow-sm border border-gray-200 flex flex-col overflow-hidden">
       <div class="p-4 border-b border-gray-100 flex items-center justify-between bg-gray-50">
          <h2 class="text-sm font-bold text-gray-700 uppercase tracking-wide">历史记录</h2>
          <button @click="refreshList" class="text-gray-400 hover:text-teal-600 transition-colors"><el-icon><Refresh /></el-icon></button>
       </div>

       <div class="flex-1 overflow-y-auto p-4 space-y-3">
          <div v-if="loading" class="py-10 text-center text-gray-400 text-sm">加载中...</div>
          <template v-else>
             <div v-for="item in list" :key="item.id" class="group relative p-4 rounded-lg border border-gray-100 bg-white hover:border-gray-200 hover:shadow-sm transition-all">
                <div class="flex items-start justify-between mb-2">
                   <div class="flex items-center gap-2">
                      <span 
                        class="px-1.5 py-0.5 rounded text-[10px] font-bold uppercase tracking-wide border"
                        :class="item.type === 0 ? 'bg-red-50 text-red-600 border-red-100' : 'bg-blue-50 text-blue-600 border-blue-100'"
                      >
                        {{ item.type === 0 ? '全员' : '私信' }}
                      </span>
                      <h3 class="font-bold text-gray-900 text-sm">{{ item.title }}</h3>
                   </div>
                   <span class="text-xs text-gray-400 font-mono">{{ item.createTime }}</span>
                </div>
                
                <p class="text-sm text-gray-600 leading-relaxed pl-1">{{ item.content }}</p>
                
                <div class="mt-3 flex items-center gap-4 text-xs text-gray-400">
                   <span v-if="item.type===1" class="bg-gray-50 px-1.5 py-0.5 rounded">To: {{ item.targetId }}</span>
                   <span class="font-mono">ID:{{ item.id }}</span>
                </div>

                <!-- Delete Action -->
                <button 
                  class="absolute top-3 right-3 p-1.5 rounded text-gray-300 hover:text-red-600 hover:bg-red-50 opacity-0 group-hover:opacity-100 transition-all"
                  @click="handleDelete(item)"
                  title="删除记录"
                >
                   <el-icon><Delete /></el-icon>
                </button>
             </div>
             
             <div v-if="list.length === 0" class="py-20 text-center text-gray-300 flex flex-col items-center">
                <div class="text-4xl opacity-20 mb-2">📭</div>
                <span class="text-sm">暂无发送记录</span>
             </div>
          </template>
       </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getNoticeList, sendNotice, deleteNotice } from '../../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Delete } from '@element-plus/icons-vue'

const sending = ref(false)
const loading = ref(false)
const list = ref([])
const form = reactive({
  title: '',
  content: '',
  type: 0,
  targetId: ''
})

const refreshList = async () => {
  loading.value = true
  try {
    const res = await getNoticeList({ page: 1, size: 50 })
    list.value = res.records || []
  } finally {
    loading.value = false
  }
}

const handleSend = async () => {
  if(!form.title || !form.content) {
     return ElMessage.warning('请填写完整的标题和内容')
  }
  
  sending.value = true
  try {
     await sendNotice(form)
     ElMessage.success('发送成功')
     form.title = ''
     form.content = ''
     refreshList()
  } catch(e) {
     //
  } finally {
     sending.value = false
  }
}

const handleDelete = (item) => {
   ElMessageBox.confirm('确定删除这条历史记录吗？', '删除确认', { 
       confirmButtonText: '删除',
       cancelButtonText: '取消',
       type: 'warning' 
    })
     .then(async () => {
        await deleteNotice(item.id)
        ElMessage.success('删除成功')
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
