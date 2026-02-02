<template>
  <div class="min-h-screen w-full flex bg-white font-sans selection:bg-teal-500 selection:text-white">
    
    <!-- Left: Brand Side (55% Width) - Dark & Sophisticated -->
    <div class="hidden lg:flex lg:w-[55%] relative flex-col justify-between p-16 overflow-hidden bg-[#0F1115]">
      
      <!-- Background Effects -->
      <div class="absolute inset-0 pointer-events-none">
        <!-- Subtle Grid -->
        <div class="absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.03)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.03)_1px,transparent_1px)] bg-size-[64px_64px]"></div>
        <!-- Gradient Orbs -->
        <div class="absolute top-[-10%] right-[-10%] w-[600px] h-[600px] bg-teal-900/30 rounded-full blur-[120px]"></div>
        <div class="absolute bottom-[-10%] left-[-10%] w-[500px] h-[500px] bg-blue-900/20 rounded-full blur-[100px]"></div>
      </div>

      <!-- Top: Brand -->
      <div class="relative z-10 flex items-center gap-3">
        <div class="w-10 h-10 bg-white rounded-xl flex items-center justify-center shadow-lg overflow-hidden">
           <img src="@/assets/logo.png" class="w-full h-full object-cover" />
        </div>
        <span class="text-2xl font-bold tracking-tight text-white">青里<span class="text-teal-400">后台</span></span>
      </div>

      <!-- Center: Main Copy -->
      <div class="relative z-10 max-w-2xl my-auto">
        <h1 class="text-6xl font-bold text-white leading-[1.1] mb-8 tracking-tight">
          构建未来的<br>
          <span class="text-transparent bg-clip-text bg-linear-to-r from-teal-400 to-blue-400">数字管理中枢</span>
        </h1>
        <div class="h-1 w-20 bg-teal-500 rounded-full mb-8"></div>
        <p class="text-lg text-gray-400 leading-relaxed max-w-lg">
          我们为现代组织打造极致的后台管理体验。安全、高效、美观，让复杂的每一个数据流转都尽在掌握。
        </p>
      </div>
    </div>

    <!-- Right: Login Form Side (45% Width) -->
    <div class="w-full lg:w-[45%] flex flex-col justify-center px-8 sm:px-16 xl:px-32 relative bg-white">
       
      <!-- Mobile Brand -->
      <div class="lg:hidden absolute top-8 left-8 flex items-center gap-2">
         <div class="w-8 h-8 rounded-lg overflow-hidden">
            <img src="@/assets/logo.png" class="w-full h-full object-cover" />
         </div>
         <span class="text-lg font-bold text-gray-900">青里后台</span>
      </div>

      <div class="w-full max-w-md mx-auto">
        <div class="mb-10 text-center lg:text-left">
           <h2 class="text-3xl font-bold text-gray-900 tracking-tight">欢迎回来</h2>
           <p class="text-base text-gray-500 mt-3">请输入您的管理员账号进行登录</p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-6">
           
           <div class="space-y-5">
              <div class="space-y-2">
                 <label class="block text-sm font-semibold text-gray-700">账号/邮箱</label>
                 <div class="relative">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" /></svg>
                    </div>
                    <input 
                        v-model="loginForm.username"
                        type="text" 
                        class="w-full h-12 pl-10 pr-4 bg-gray-50 border border-gray-200 rounded-xl text-gray-900 placeholder-gray-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 transition-all font-medium"
                        placeholder="请输入您的账号"
                    />
                 </div>
              </div>

              <div class="space-y-2">
                 <div class="flex items-center justify-between">
                    <label class="block text-sm font-semibold text-gray-700">密码</label>
                    <a href="#" class="text-sm font-medium text-teal-600 hover:text-teal-700 transition-colors">忘记密码？</a>
                 </div>
                 <div class="relative">
                     <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg>
                     </div>
                     <input 
                        v-model="loginForm.password"
                        :type="showPassword ? 'text' : 'password'"
                        class="w-full h-12 pl-10 pr-12 bg-gray-50 border border-gray-200 rounded-xl text-gray-900 placeholder-gray-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-teal-500/20 focus:border-teal-500 transition-all font-medium"
                        placeholder="请输入您的密码"
                     />
                     <button type="button" @click="showPassword = !showPassword" class="absolute right-3 top-0 h-full flex items-center px-1 text-gray-400 hover:text-gray-600 transition-colors">
                        <svg v-if="!showPassword" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" /></svg>
                        <svg v-else class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" /></svg>
                     </button>
                 </div>
              </div>
           </div>

           <button 
             type="submit" 
             :disabled="loading"
             class="w-full h-12 bg-gray-900 hover:bg-black text-white text-base font-bold rounded-xl shadow-lg shadow-gray-900/10 hover:shadow-gray-900/20 active:scale-[0.98] transition-all flex items-center justify-center gap-2"
           >
             <span v-if="loading" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
             <span>{{ loading ? '验证中...' : '立即登录' }}</span>
           </button>

           <div class="flex items-center justify-center gap-2 mt-6">
              <span class="h-px w-10 bg-gray-200"></span>
              <span class="text-xs text-gray-400">或者</span>
              <span class="h-px w-10 bg-gray-200"></span>
           </div>

           <div class="text-center">
              <p class="text-sm text-gray-500">
                  还没有账号？ <a href="#" class="font-semibold text-teal-600 hover:text-teal-700">联系管理员</a>
              </p>
           </div>

        </form>
        
        <div class="mt-12 flex justify-center lg:justify-start gap-4 text-xs text-gray-400">
             <span>© 2026 Qingli Inc.</span>
             <span>•</span>
             <span>隐私政策</span>
             <span>•</span>
             <span>帮助中心</span>
        </div>
      </div>
    </div>

    <!-- Toast Notification -->
    <Transition
      enter-active-class="transition ease-out duration-300 transform"
      enter-from-class="translate-y-2 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition ease-in duration-200 transform"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-2 opacity-0"
    >
      <div v-if="toast.show" 
           :class="['fixed top-6 left-1/2 -translate-x-1/2 lg:left-auto lg:right-6 lg:translate-x-0 px-6 py-4 rounded-xl shadow-2xl border text-sm font-semibold z-50 flex items-center gap-3',
             toast.type === 'error' ? 'bg-white border-red-50 text-red-600' : 'bg-gray-900 border-gray-800 text-teal-400']">
        <div :class="toast.type === 'error' ? 'bg-red-50 p-1 rounded-full' : 'bg-white/10 p-1 rounded-full'">
            <svg v-if="toast.type === 'error'" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            <svg v-else class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" /></svg>
        </div>
        <span>{{ toast.message }}</span>
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { adminLogin } from '../../api'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)

const loginForm = reactive({ username: '', password: '', rememberMe: true })
const toast = reactive({ show: false, message: '', type: 'success' })

const showToast = (msg, type = 'success') => {
  toast.message = msg
  toast.type = type
  toast.show = true
  setTimeout(() => toast.show = false, 3000)
}

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    showToast('请输入账号和密码', 'error')
    return
  }

  loading.value = true
  try {
    const res = await adminLogin(loginForm)
    localStorage.setItem('admin_token', res.token)
    localStorage.setItem('admin_user', JSON.stringify(res))
    
    showToast('登录成功，正在跳转...')
    setTimeout(() => router.push('/dashboard'), 800)
  } catch (error) {
    showToast(error.message || '登录失败，请检查账号密码', 'error')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* Scoped styles mainly handled by global Tailwind utility classes */
</style>
