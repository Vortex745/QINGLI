<template>
  <div class="flex h-screen w-full bg-gray-50 font-sans text-gray-900 bg-noise">
    <!-- Sidebar (Application Shell - Navbar) -->
    <aside class="fixed inset-y-0 left-0 z-50 w-64 bg-white border-r border-gray-200 flex flex-col transition-all duration-300">
      
      <!-- Logo Section -->
      <div class="h-16 flex items-center px-6 border-b border-gray-100">
        <div class="flex items-center gap-3 cursor-pointer group" @click="router.push('/')">
          <!-- Logo Image -->
          <img src="@/assets/logo.png" class="w-8 h-8 rounded-md object-cover shadow-sm" alt="Logo" />
          <h1 class="text-lg font-bold tracking-tight text-gray-800">
            青里<span class="text-teal-600">管理后台</span>
          </h1>
        </div>
      </div>

      <!-- Navigation Menu -->
      <nav class="flex-1 overflow-y-auto px-3 py-4 space-y-1">
        <template v-for="item in menuItems" :key="item.index">
          <!-- Single Menu Item -->
          <div 
            v-if="!item.children"
            @click="navigateTo(item.path)"
            class="group flex items-center px-3 py-2 text-sm font-medium rounded-md cursor-pointer transition-colors duration-200"
            :class="isActive(item.path) 
              ? 'bg-teal-50 text-teal-700' 
              : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'"
          >
            <el-icon 
              class="mr-3 text-lg transition-colors"
              :class="isActive(item.path) ? 'text-teal-500' : 'text-gray-400 group-hover:text-gray-500'"
            >
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
          </div>

          <!-- Sub Menu Group -->
          <div v-else class="space-y-1">
            <div 
              @click="toggleSubMenu(item.index)"
              class="group flex items-center justify-between px-3 py-2 text-sm font-medium rounded-md cursor-pointer text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition-colors"
            >
              <div class="flex items-center">
                <el-icon class="mr-3 text-lg text-gray-400 group-hover:text-gray-500"><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
              </div>
              <el-icon 
                class="text-xs text-gray-400 transition-transform duration-200" 
                :class="{ 'rotate-180': isSubMenuOpen(item.index) }"
              >
                <ArrowDown />
              </el-icon>
            </div>

            <!-- Children -->
            <div 
              v-show="isSubMenuOpen(item.index)"
              class="space-y-1 pl-10 pr-2 overflow-hidden transition-all duration-200 ease-in-out"
            >
               <div 
                v-for="child in item.children"
                :key="child.path"
                @click="navigateTo(child.path)"
                class="block px-3 py-2 text-sm font-medium rounded-md cursor-pointer transition-colors"
                :class="isActive(child.path) 
                  ? 'bg-teal-50 text-teal-700' 
                  : 'text-gray-500 hover:text-gray-900 hover:bg-gray-50'"
              >
                {{ child.title }}
              </div>
            </div>
          </div>
        </template>
      </nav>
      
      <!-- User Profile (Footer) -->
      <div class="p-4 border-t border-gray-100">
        <div class="flex items-center gap-3 w-full p-2 rounded-md hover:bg-gray-50 transition-colors cursor-pointer group">
          <div class="w-9 h-9 rounded-full bg-gray-200 flex items-center justify-center text-gray-500 text-sm font-medium group-hover:bg-white group-hover:shadow-sm">
            <el-icon><User /></el-icon>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-semibold text-gray-700 truncate">管理员</p>
            <p class="text-xs text-gray-500 truncate">超级权限</p>
          </div>
          <el-icon class="text-gray-400 hover:text-gray-600" @click.stop="handleLogout"><SwitchButton /></el-icon>
        </div>
      </div>
    </aside>

    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col pl-64 min-w-0 bg-gray-50 min-h-screen">
      <!-- Top Header -->
      <header class="sticky top-0 z-40 h-16 bg-white/80 backdrop-blur-md border-b border-gray-200 px-8 flex items-center justify-between">
        
        <!-- Breadcrumbs -->
        <div class="flex items-center text-sm text-gray-500">
            <span class="hover:text-gray-800 cursor-pointer transition-colors">系统</span>
            <span class="mx-2 text-gray-300">/</span>
            <el-breadcrumb separator="/" class="text-sm">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="$route.meta.title"><span class="font-medium text-gray-900">{{ $route.meta.title }}</span></el-breadcrumb-item>
            </el-breadcrumb>
        </div>

        <!-- Right: Actions -->
        <div class="flex items-center gap-4">
           <!-- Notification -->
           <!-- Notification -->
           <el-popover
             placement="bottom-end"
             :width="320"
             trigger="click"
             popper-class="!p-0"
           >
             <template #reference>
               <button 
                 class="relative p-2 rounded-full text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
                 @click="hasViewedNotifications = true"
               >
                  <el-icon class="text-lg"><Bell /></el-icon>
                  <span v-if="pendingCount > 0 && !hasViewedNotifications" class="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-red-500 ring-2 ring-white animate-pulse"></span>
               </button>
             </template>
             
             <!-- Popover Content -->
             <div class="flex flex-col">
               <div class="px-4 py-3 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
                 <h3 class="font-semibold text-sm text-gray-800">举报提醒</h3>
                 <span v-if="pendingCount > 0" class="bg-red-100 text-red-600 text-xs px-2 py-0.5 rounded-full font-medium">{{ pendingCount }} 待处理</span>
               </div>
               
               <div class="max-h-[300px] overflow-y-auto">
                 <template v-if="notifications.length > 0">
                   <div 
                      v-for="item in notifications" 
                      :key="item.id"
                      class="px-4 py-3 hover:bg-gray-50 cursor-pointer transition-colors border-b border-gray-50 last:border-0"
                      @click="handleViewReport"
                   >
                      <div class="flex justify-between items-start mb-1">
                        <span class="text-xs font-medium text-teal-600 bg-teal-50 px-1.5 py-0.5 rounded">{{ item.reportTypeName }}</span>
                        <span class="text-xs text-gray-400">{{ formatTime(item.createTime) }}</span>
                      </div>
                      <p class="text-sm text-gray-700 line-clamp-2 mb-1">
                        <span class="font-medium text-gray-900">{{ item.reporterName }}</span> 举报了: {{ item.targetTitle }}
                      </p>
                      <p class="text-xs text-gray-500 bg-gray-50 p-1.5 rounded line-clamp-1">
                        理由: {{ item.reason }}
                      </p>
                   </div>
                 </template>
                 <div v-else class="py-8 text-center text-gray-400">
                    <el-icon class="text-3xl mb-2 opacity-20"><Bell /></el-icon>
                    <p class="text-xs">暂无新的举报提醒</p>
                 </div>
               </div>
               
               <div class="p-2 border-t border-gray-100 bg-gray-50/30">
                 <button 
                  @click="handleViewReport"
                  class="w-full py-1.5 text-xs font-medium text-teal-600 hover:text-teal-700 hover:bg-teal-50 rounded transition-colors"
                 >
                   查看全部举报
                 </button>
               </div>
             </div>
           </el-popover>
        </div>
      </header>

      <!-- Main Scrollable Area -->
      <main class="flex-1 p-8 overflow-y-auto">
        <div class="max-w-7xl mx-auto space-y-6">
            <router-view v-slot="{ Component }">
              <transition 
                enter-active-class="transition ease-out duration-200"
                enter-from-class="opacity-0 translate-y-2"
                enter-to-class="opacity-100 translate-y-0"
                leave-active-class="transition ease-in duration-150"
                leave-from-class="opacity-100 translate-y-0"
                leave-to-class="opacity-0 translate-y-2"
                mode="out-in"
              >
                <component :is="Component" />
              </transition>
            </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowDown, Bell, SwitchButton, User, Setting, Document, DataLine } from '@element-plus/icons-vue'
import { getReportList } from '../../api'

const router = useRouter()
const route = useRoute()

// Notification Logic
const notifications = ref([])
const pendingCount = ref(0)
const hasViewedNotifications = ref(false)
let pollTimer = null

// Reset viewed state if new notifications arrive (count increases)
watch(pendingCount, (newVal, oldVal) => {
    if (newVal > oldVal) {
        hasViewedNotifications.value = false
    }
})

const fetchNotifications = async () => {
    try {
        // Fetch only pending reports (status = 0)
        const res = await getReportList({
            page: 1,
            size: 5,
            status: 0
        })
        if (res && res.records) {
            notifications.value = res.records
            pendingCount.value = res.total || 0
        }
    } catch (e) {
        console.error('Failed to fetch notifications:', e)
    }
}

const formatTime = (timeStr) => {
    if (!timeStr) return ''
    const date = new Date(timeStr)
    const now = new Date()
    const diff = now - date
    
    // Less than 1 minute
    if (diff < 60000) return '刚刚'
    // Less than 1 hour
    if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
    // Less than 24 hours
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
    
    return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

const handleViewReport = () => {
    router.push('/system/report')
}

// Menu Configuration
const menuItems = ref([
  { index: 'dashboard', path: '/dashboard', icon: 'DataLine', title: '控制台' },
  { 
    index: 'user-manage',
    title: '用户管理', 
    icon: 'User', 
    children: [
      { path: '/user/list', title: '用户列表' }
    ]
  },
  { 
    index: 'content-manage', 
    title: '内容管理', 
    icon: 'Document', 
    children: [
      { path: '/goods/list', title: '商品管理' },
      { path: '/post/list', title: '广场管理' },
      { path: '/lost-found/list', title: '失物招领' },
      { path: '/part-time/list', title: '兼职管理' }
    ]
  },
  {
    index: 'system-manage',
    title: '系统管理',
    icon: 'Setting',
    children: [
       { path: '/system/report', title: '举报管理' },
       { path: '/system/message', title: '系统消息' }
    ]
  }
])

const expandedMenus = ref([])

const isSubMenuOpen = (index) => expandedMenus.value.includes(index)

const toggleSubMenu = (index) => {
  if (expandedMenus.value.includes(index)) {
    expandedMenus.value = expandedMenus.value.filter(i => i !== index)
  } else {
    expandedMenus.value.push(index)
  }
}

const navigateTo = (path) => {
  router.push(path)
}

const isActive = (path) => {
  if (!path) return false
  if (path === '/dashboard') return route.path === '/dashboard'
  return route.path.startsWith(path)
}

const syncMenuWithRoute = () => {
  menuItems.value.forEach(item => {
    if (item.children) {
      const hasActiveChild = item.children.some(child => child.path === route.path)
      if (hasActiveChild && !expandedMenus.value.includes(item.index)) {
        expandedMenus.value.push(item.index)
      }
    }
  })
}

watch(() => route.path, () => {
  syncMenuWithRoute()
})

onMounted(() => {
  syncMenuWithRoute()
  fetchNotifications()
  // Poll every 30 seconds
  pollTimer = setInterval(fetchNotifications, 30000)
})

onUnmounted(() => {
    if (pollTimer) clearInterval(pollTimer)
})

const handleLogout = () => {
    localStorage.removeItem('admin_token')
    router.push('/login')
}
</script>

<style scoped>
/* Scrollbar Refinement */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #d1d5db;
}
</style>
