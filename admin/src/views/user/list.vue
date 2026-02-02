<template>
  <div class="space-y-6">
    <!-- Page Header -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-gray-900">用户管理</h1>
        <p class="text-sm text-gray-500 mt-1">管理系统注册用户及权限状态。</p>
      </div>
      <button 
        class="inline-flex items-center justify-center gap-2 px-4 py-2 bg-teal-600 text-white text-sm font-medium rounded-md shadow-sm hover:bg-teal-700 focus:outline-none focus:ring-2 focus:ring-teal-500 focus:ring-offset-2 transition-all"
        @click="openAddUser"
      >
        <el-icon><Plus /></el-icon>
        <span>新增用户</span>
      </button>
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
          v-model="query.nickname" 
          placeholder="搜索昵称或ID" 
          class="mantine-input pl-9"
          @keyup.enter="handleSearch"
        />
      </div>
      
      <!-- Status Select -->
      <div class="w-full sm:w-40">
        <el-select 
            v-model="query.status" 
            placeholder="用户状态" 
            class="w-full mantine-select"
            @change="handleSearch"
            :teleported="false"
        >
            <el-option label="所有状态" :value="undefined" />
            <el-option label="正常" :value="1" />
            <el-option label="封禁" :value="0" />
        </el-select>
      </div>

      <!-- Action -->
      <button @click="handleSearch" class="h-9 px-4 bg-gray-900 text-white text-sm font-medium rounded-md hover:bg-gray-800 transition-colors shadow-sm flex items-center gap-2">
        <span>查询</span>
      </button>
      
      <div class="ml-auto text-xs text-gray-500">
        共 <span class="font-bold text-gray-900">{{ tableData.length }}</span> 用户
      </div>
    </div>

    <!-- User List Table -->
    <div class="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden flex flex-col h-[calc(100vh-280px)]">
      <!-- Header -->
      <div class="grid grid-cols-12 gap-4 px-6 py-3 bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider">
        <div class="col-span-4 pl-2">用户身份</div>
        <div class="col-span-2">OPENID</div>
        <div class="col-span-2 text-center">注册时间</div>
        <div class="col-span-2 text-center">状态</div>
        <div class="col-span-2 text-right pr-2">操作</div>
      </div>

      <!-- List Content -->
      <div 
        ref="scrollContainer"
        class="flex-1 overflow-y-auto divide-y divide-gray-100"
        @scroll="handleScroll"
      >
        <div 
          v-for="user in tableData" 
          :key="user.id"
          class="grid grid-cols-12 gap-4 items-center px-6 py-4 hover:bg-gray-50 transition-colors group"
        >
          <!-- User Info -->
          <div class="col-span-4 flex items-center gap-4">
            <div class="relative shrink-0">
              <div class="w-10 h-10 rounded-full bg-gray-200 flex items-center justify-center overflow-hidden ring-2 ring-white shadow-sm">
                  <img 
                    v-if="user.avatarUrl && !imgErrors[user.id]"
                    :src="getProcessedImageUrl(user.avatarUrl)" 
                    class="w-full h-full object-cover"
                    @error="handleImageError(user.id)"
                  />
                  <span v-else class="text-sm font-semibold text-gray-500">{{ user.nickname?.charAt(0).toUpperCase() || '?' }}</span>
              </div>
              <!-- Status Dot -->
              <span class="absolute bottom-0 right-0 block w-2.5 h-2.5 rounded-full ring-2 ring-white" :class="user.status === 1 ? 'bg-teal-500' : 'bg-red-500'"></span>
            </div>
            <div class="min-w-0">
              <div class="font-medium text-gray-900 text-sm truncate">{{ user.nickname || '未知用户' }}</div>
              <div class="flex items-center gap-2 mt-0.5">
                 <span class="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-medium bg-gray-100 text-gray-600">
                   {{ user.role === 'admin' ? '管理员' : '用户' }}
                 </span>
                 <span class="text-[10px] text-gray-400 font-mono">ID:{{ user.id }}</span>
              </div>
            </div>
          </div>

          <!-- OpenID -->
          <div class="col-span-2">
            <span class="text-xs font-mono text-gray-500 bg-gray-50 px-2 py-1 rounded border border-gray-100" :title="user.openid">
              {{ user.openid ? user.openid.substring(0, 8) + '...' : '---' }}
            </span>
          </div>

          <!-- Create Time -->
          <div class="col-span-2 text-center">
            <div class="text-sm text-gray-700">{{ formatDate(user.createTime) }}</div>
            <div class="text-xs text-gray-400">{{ formatTime(user.createTime) }}</div>
          </div>

          <!-- Status -->
          <div class="col-span-2 flex justify-center">
             <span 
               class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium cursor-pointer transition-colors"
               :class="user.status === 1 ? 'bg-teal-50 text-teal-700 hover:bg-teal-100' : 'bg-red-50 text-red-700 hover:bg-red-100'"
               @click="toggleUserStatus(user)"
             >
               {{ user.status === 1 ? '正常' : '封禁中' }}
             </span>
          </div>

          <!-- Actions -->
          <div class="col-span-2 flex justify-end gap-2 pr-2">
            <button 
              v-if="user.status === 1"
              class="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-md transition-colors"
              title="封禁"
              @click="banUser(user)"
            >
              <el-icon><Lock /></el-icon>
            </button>
            <button 
              v-else
              class="p-1.5 text-gray-400 hover:text-teal-600 hover:bg-teal-50 rounded-md transition-colors"
              title="解封"
              @click="unbanUser(user)"
            >
              <el-icon><Unlock /></el-icon>
            </button>
          </div>
        </div>
        
        <!-- States -->
        <div v-if="loading" class="py-12 flex flex-col items-center justify-center gap-3 text-gray-400">
          <el-icon class="is-loading text-2xl text-teal-500"><Loading /></el-icon>
          <span class="text-xs font-medium">加载数据中...</span>
        </div>
        <div v-if="noMore && tableData.length > 0" class="py-6 text-center text-xs text-gray-400 italic">-- END --</div>
        <div v-if="tableData.length === 0 && !loading" class="py-20 flex flex-col items-center justify-center text-gray-300">
             <div class="text-4xl opacity-20 mb-2"><User /></div>
             <p class="text-sm">暂无用户数据</p>
        </div>
      </div>
    </div>

    <!-- Add User Dialog (Mantine Style) -->
    <el-dialog
      v-model="dialogVisible"
      title="新增用户"
      width="440px"
      align-center
      class="mantine-dialog"
    >
      <div class="p-1">
          <p class="text-sm text-gray-500 mb-6">创建一个新的用户账号。默认密码为 <code class="bg-gray-100 px-1 py-0.5 rounded text-gray-800 font-mono text-xs">123456</code></p>
          
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="space-y-4">
            <div class="space-y-1">
                <label class="block text-sm font-medium text-gray-700">用户名</label>
                <input v-model="form.username" class="mantine-input" placeholder="设置登录用户名"/>
            </div>
            <div class="space-y-1">
                <label class="block text-sm font-medium text-gray-700">昵称</label>
                <input v-model="form.nickname" class="mantine-input" placeholder="显示昵称"/>
            </div>
            <div class="space-y-1">
                <label class="block text-sm font-medium text-gray-700">联系电话 <span class="text-gray-400 font-normal text-xs">(选填)</span></label>
                <input v-model="form.phone" class="mantine-input" placeholder="手机号码"/>
            </div>
          </el-form>

          <div class="mt-8 flex justify-end gap-3">
              <button @click="dialogVisible = false" class="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors">取消</button>
              <button @click="submitForm" class="px-4 py-2 bg-teal-600 rounded-md text-sm font-medium text-white hover:bg-teal-700 shadow-sm transition-all focus:ring-2 focus:ring-teal-500 focus:ring-offset-2">确认创建</button>
          </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Loading, Lock, Unlock, User } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUserStatus } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const query = reactive({
  nickname: '',
  status: undefined,
  current: 1,
  size: 20
})

const tableData = ref([])
const loading = ref(false)
const noMore = ref(false)
const imgErrors = reactive({})
const statusLoading = reactive({})
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const scrollContainer = ref(null)

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  role: 'user'
})

const rules = {
  username: [{ required: true, message: '必填项', trigger: 'blur' }],
  nickname: [{ required: true, message: '必填项', trigger: 'blur' }]
}

const handleImageError = (id) => {
  imgErrors[id] = true
}

// BASE_URL for images - use the VITE_API_BASE_URL (which includes /admin-api)
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'

const getProcessedImageUrl = (url) => {
  if (!url) return ''
  // Filter out WeChat temp paths
  if (url.includes('http://tmp/') || url.includes('wxfile://')) {
    return ''
  }
  if (url.startsWith('http')) return url
  // Ensure we don't double slash
  const base = BASE_URL.endsWith('/') ? BASE_URL.slice(0, -1) : BASE_URL
  const path = url.startsWith('/') ? url : '/' + url
  return `${base}${path}`
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const loadMore = async () => {
  if (loading.value || noMore.value) return
  loading.value = true
  try {
    const data = await getUserList(query)
    if (data && data.records) {
      const list = data.records || []
      if (list.length < query.size) {
        noMore.value = true
      }
      if (query.current === 1) {
        tableData.value = list
      } else {
        tableData.value.push(...list)
      }
      query.current++
    }
  } catch (e) {
    console.error('Load Error:', e)
  } finally {
    loading.value = false
  }
}

const handleScroll = () => {
  if (!scrollContainer.value) return
  const { scrollTop, scrollHeight, clientHeight } = scrollContainer.value
  if (scrollTop + clientHeight >= scrollHeight - 50) {
    loadMore()
  }
}

const handleSearch = () => {
  query.current = 1
  noMore.value = false
  tableData.value = []
  loadMore()
}

const openAddUser = () => {
  form.username = ''
  form.nickname = ''
  form.phone = ''
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await addUser(form)
        ElMessage.success('用户已创建')
        dialogVisible.value = false
        handleSearch()
      } catch (e) {
        // Error handled by interceptor
      } finally {
        submitting.value = false
      }
    }
  })
}

const toggleUserStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '封禁' : '解封'
  
  try {
    await ElMessageBox.confirm(
      `确认要${action}用户 "${user.nickname}" 吗?`,
      '状态变更',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    statusLoading[user.id] = true
    await updateUserStatus(user.id, newStatus)
    user.status = newStatus
    ElMessage.success(`用户已${action}`)
  } catch (e) {
    // cancelled
  } finally {
    statusLoading[user.id] = false
  }
}

const banUser = (user) => toggleUserStatus(user)
const unbanUser = (user) => toggleUserStatus(user)

onMounted(() => {
  loadMore()
})
</script>

<style scoped>
/* Dialog Customization */
:deep(.mantine-dialog) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}
:deep(.mantine-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #f3f4f6;
}
:deep(.mantine-dialog .el-dialog__title) {
  font-weight: 600;
  font-size: 1.125rem;
  color: #111827;
}
:deep(.mantine-dialog .el-dialog__body) {
  padding: 24px;
}
</style>
