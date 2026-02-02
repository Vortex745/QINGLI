<template>
  <div class="space-y-6">
    <!-- Top Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-gray-900">控制台</h1>
        <p class="text-sm text-gray-500 mt-1">系统运行状态概览。</p>
      </div>
      
      <div class="flex items-center gap-4">
        <div class="hidden sm:flex items-center gap-2 text-xs font-medium text-gray-500 bg-white px-3 py-1.5 rounded-md border border-gray-200 shadow-sm">
          <span class="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></span>
          <span>系统正常</span>
          <span class="w-px h-3 bg-gray-200 mx-1"></span>
          <span>{{ currentTime }}</span>
        </div>
        <button 
          @click="loadStats" 
          :disabled="loading" 
          class="flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 text-sm font-medium text-gray-700 rounded-md shadow-sm hover:bg-gray-50 hover:text-teal-600 focus:outline-none transition-all disabled:opacity-70"
        >
          <el-icon :class="{'is-loading': loading}"><Refresh /></el-icon>
          <span>同步数据</span>
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="!stats && loading" class="py-20 flex flex-col items-center justify-center gap-4 text-gray-400">
       <div class="w-8 h-8 border-2 border-gray-200 border-t-teal-500 rounded-full animate-spin"></div>
       <span class="text-xs font-medium">数据加载中...</span>
    </div>

    <!-- Main Content -->
    <div v-if="stats" class="space-y-6 animate-fade-in">
      
      <!-- Row 1: Key Metrics (3 cards) -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <!-- Total Users -->
          <div class="bg-white p-6 rounded-lg border border-gray-200 shadow-sm flex items-start justify-between group hover:border-teal-200 transition-colors">
              <div>
                  <p class="text-sm font-medium text-gray-500">用户总量</p>
                  <div class="mt-2 flex items-baseline gap-2">
                       <span class="text-3xl font-bold text-gray-900 tracking-tight">{{ formatNumber(stats.totalUsers) }}</span>
                       <span class="text-xs font-medium" :class="stats.userGrowth > 0 ? 'text-green-600' : 'text-gray-400'">
                           {{ stats.userGrowth > 0 ? '+' : ''}}{{ stats.userGrowth }}%
                       </span>
                  </div>
              </div>
              <div class="p-3 rounded-md bg-teal-50 text-teal-600 group-hover:bg-teal-100 transition-colors">
                   <el-icon class="text-xl"><User /></el-icon>
              </div>
          </div>

          <!-- Goods -->
          <div class="bg-white p-6 rounded-lg border border-gray-200 shadow-sm flex items-start justify-between group hover:border-blue-200 transition-colors">
              <div>
                  <p class="text-sm font-medium text-gray-500">商品库存</p>
                  <div class="mt-2 flex items-baseline gap-2">
                       <span class="text-3xl font-bold text-gray-900 tracking-tight">{{ formatNumber(stats.totalGoods) }}</span>
                       <span class="text-xs font-medium" :class="stats.goodsGrowth > 0 ? 'text-green-600' : 'text-gray-400'">
                           {{ stats.goodsGrowth > 0 ? '+' : ''}}{{ stats.goodsGrowth }}%
                       </span>
                  </div>
              </div>
              <div class="p-3 rounded-md bg-blue-50 text-blue-600 group-hover:bg-blue-100 transition-colors">
                   <el-icon class="text-xl"><Goods /></el-icon>
              </div>
          </div>

          <!-- Posts -->
          <div class="bg-white p-6 rounded-lg border border-gray-200 shadow-sm flex items-start justify-between group hover:border-purple-200 transition-colors">
              <div>
                  <p class="text-sm font-medium text-gray-500">帖子总量</p>
                  <div class="mt-2 flex items-baseline gap-2">
                       <span class="text-3xl font-bold text-gray-900 tracking-tight">{{ formatNumber(stats.totalPosts) }}</span>
                       <span class="text-xs font-medium" :class="stats.postGrowth > 0 ? 'text-green-600' : 'text-gray-400'">
                           {{ stats.postGrowth > 0 ? '+' : ''}}{{ stats.postGrowth }}%
                       </span>
                  </div>
              </div>
              <div class="p-3 rounded-md bg-purple-50 text-purple-600 group-hover:bg-purple-100 transition-colors">
                   <el-icon class="text-xl"><ChatDotSquare /></el-icon>
              </div>
          </div>
      </div>

      <!-- Row 2: Charts -->
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <!-- Line Chart -->
          <div class="lg:col-span-2 bg-white p-6 rounded-lg border border-gray-200 shadow-sm flex flex-col h-[400px]">
              <div class="flex items-center justify-between mb-6">
                  <div>
                      <h3 class="text-base font-bold text-gray-900">帖子流量趋势</h3>
                      <p class="text-xs text-gray-500 mt-1">最近7天发布量统计</p>
                  </div>
              </div>
              <div class="flex-1 w-full relative">
                  <div ref="lineChartRef" class="absolute inset-0 w-full h-full"></div>
              </div>
          </div>

          <!-- Pie Chart -->
          <div class="bg-white p-6 rounded-lg border border-gray-200 shadow-sm flex flex-col h-[400px]">
              <div class="mb-6">
                  <h3 class="text-base font-bold text-gray-900">商品分类占比</h3>
                  <p class="text-xs text-gray-500 mt-1">库存商品分布</p>
              </div>
              <div class="flex-1 w-full relative">
                  <div ref="pieChartRef" class="absolute inset-0 w-full h-full"></div>
              </div>
               <!-- Simple Legend -->
              <div class="mt-4 grid grid-cols-2 gap-2">
                 <div v-for="(item, index) in stats.goodsCategoryStat?.slice(0, 4)" :key="index" class="flex items-center gap-2 text-xs text-gray-600">
                    <span class="w-2 h-2 rounded-full" :style="{backgroundColor: getChartColor(index)}"></span>
                    <span class="truncate">{{ item.name }}</span>
                    <span class="text-gray-900 font-medium ml-auto">{{ getPercentage(item.value) }}</span>
                 </div>
              </div>
          </div>
      </div>

       <!-- Row 3: Today's Snapshot -->
       <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-gray-50 p-5 rounded-lg border border-gray-200">
               <div class="text-sm font-medium text-gray-500 mb-1">今日新增用户</div>
               <div class="text-2xl font-bold text-gray-900">{{ formatNumber(stats.todayNewUsers) }}</div>
          </div>
           <div class="bg-gray-50 p-5 rounded-lg border border-gray-200">
               <div class="text-sm font-medium text-gray-500 mb-1">今日新增商品</div>
               <div class="text-2xl font-bold text-gray-900">{{ formatNumber(stats.todayNewGoods) }}</div>
          </div>
           <div class="bg-gray-50 p-5 rounded-lg border border-gray-200">
               <div class="text-sm font-medium text-gray-500 mb-1">今日新增帖子</div>
               <div class="text-2xl font-bold text-gray-900">{{ formatNumber(stats.todayNewPosts) }}</div>
          </div>
       </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getDashboardStats } from '../../api'
import { Refresh, User, Goods, ChatDotSquare } from '@element-plus/icons-vue'

const stats = ref(null)
const loading = ref(true)
const currentTime = ref('')
const pieChartRef = ref(null)
const lineChartRef = ref(null)

let pieChart = null
let lineChart = null
let timer = null

// Teal Theme Palette
const chartColors = ['#0d9488', '#2dd4bf', '#99f6e4', '#ccfbf1', '#f0fdfa']
const getChartColor = (index) => chartColors[index % chartColors.length]

const totalGoodsCount = computed(() => {
  if (!stats.value || !stats.value.goodsCategoryStat) return 0
  return stats.value.goodsCategoryStat.reduce((sum, item) => sum + item.value, 0)
})

const getPercentage = (value) => {
  if (totalGoodsCount.value === 0) return '0%'
  return Math.round((value / totalGoodsCount.value) * 100) + '%'
}

const formatNumber = (num) => {
  if (num === null || num === undefined) return '0'
  if (typeof num === 'string') return num
  if (num >= 10000) return (num / 10000).toFixed(1) + 'W'
  return num.toLocaleString()
}

const updateTime = () => {
  currentTime.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
}

const loadStats = async () => {
  loading.value = true
  try {
    stats.value = await getDashboardStats()
    await nextTick()
    setTimeout(initCharts, 100)
  } catch (e) {
    console.error('Stats Sync Error:', e)
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
    if (!stats.value) return

    // Pie Chart
    if (pieChartRef.value && stats.value.goodsCategoryStat) {
        if (pieChart) pieChart.dispose()
        pieChart = echarts.init(pieChartRef.value)
        pieChart.setOption({
            animation: true,
            tooltip: { 
                trigger: 'item',
            },
            series: [{
                type: 'pie',
                radius: ['60%', '80%'],
                center: ['50%', '50%'],
                itemStyle: { 
                    borderRadius: 4, 
                    borderColor: '#fff', 
                    borderWidth: 2 
                },
                label: { show: false },
                data: stats.value.goodsCategoryStat.map((cat, i) => ({
                    value: cat.value,
                    name: cat.name,
                    itemStyle: { color: getChartColor(i) }
                }))
            }]
        })
    }

    // Line Chart
    if (lineChartRef.value && stats.value.postTrend) {
        if (lineChart) lineChart.dispose()
        lineChart = echarts.init(lineChartRef.value)
        const dates = stats.value.postTrend.map(t => t.date)
        const values = stats.value.postTrend.map(t => t.postCount)
        
        lineChart.setOption({
            grid: { top: 20, right: 20, bottom: 20, left: 30, containLabel: true },
            tooltip: { 
                trigger: 'axis',
                backgroundColor: 'rgba(255, 255, 255, 0.95)',
                borderColor: '#e5e7eb',
                textStyle: { color: '#374151' },
                extraCssText: 'box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); border-radius: 6px;'
            },
            xAxis: { 
                type: 'category', 
                data: dates,
                axisLine: { lineStyle: { color: '#e5e7eb' } },
                axisTick: { show: false },
                axisLabel: { color: '#6b7280', fontSize: 11 }
            },
            yAxis: { 
                type: 'value',
                splitLine: { 
                    lineStyle: { type: 'dashed', color: '#f3f4f6' } 
                },
                axisLine: { show: false },
                axisLabel: { color: '#6b7280', fontSize: 11 }
            },
            series: [{
                data: values,
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 6,
                itemStyle: { color: '#0d9488', borderColor: '#fff', borderWidth: 2 },
                lineStyle: { color: '#0d9488', width: 2 },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(13, 148, 136, 0.1)' },
                        { offset: 1, color: 'rgba(13, 148, 136, 0)' }
                    ])
                }
            }]
        })
    }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  loadStats()
  window.addEventListener('resize', () => {
      pieChart?.resize()
      lineChart?.resize()
  })
})

onUnmounted(() => {
  clearInterval(timer)
  pieChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fadeIn 0.5s ease-out forwards;
}
</style>
