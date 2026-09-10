<template>
  <div class="page stats">
    <div class="card">
      <div class="page-title">节点企业注册信息统计大屏</div>
      <div class="summary" v-if="loaded">
        <div class="s-item">
          <div class="num">{{ totalCount }}</div>
          <div class="txt">累计注册企业</div>
        </div>
        <div class="s-item" v-for="(name, t) in NODE_TYPES" :key="t">
          <div class="num small">{{ typeCount[t] || 0 }}</div>
          <div class="txt">{{ name }}</div>
        </div>
      </div>
    </div>

    <div class="chart-grid">
      <div class="card chart-card">
        <div class="chart-title">十二个月注册数量趋势</div>
        <div ref="monthChart" class="chart"></div>
      </div>
      <div class="card chart-card">
        <div class="chart-title">按省分组的注册数量分布</div>
        <div ref="provinceChart" class="chart"></div>
      </div>
      <div class="card chart-card">
        <div class="chart-title">按企业类型分组的注册数量分布</div>
        <div ref="typeChart" class="chart"></div>
      </div>
    </div>
    <div v-if="!loaded" class="empty">数据加载中...</div>
  </div>
</template>

<script>
import echarts from '../../charts'
import { NODE_TYPES } from '../../modules'
import request from '../../util'

// 企业类型配色按 nodeType 固定，避免随查询顺序漂移
const TYPE_COLOR = { 1: '#2f9e44', 2: '#1c7ed6', 3: '#e8590c', 4: '#862e9c' }

export default {
  name: 'AdminStats',
  data() {
    return {
      NODE_TYPES,
      loaded: false,
      totalCount: 0,
      typeCount: {},
      charts: [],
      data: { monthTrend: [], provinceDist: [], typeDist: [] }
    }
  },
  async created() {
    window.addEventListener('resize', this.onResize)
    try {
      const res = (await request.get('/admin/stats')) || {}
      this.data = res
      this.computeSummary()
    } catch (e) {
      /* toast */
    } finally {
      this.loaded = true
      this.$nextTick(() => this.renderCharts())
    }
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.onResize)
    this.charts.forEach((c) => c && c.dispose())
  },
  methods: {
    onResize() {
      this.charts.forEach((c) => c && c.resize())
    },
    computeSummary() {
      const d = this.data
      const byType = {}
      ;(d.typeDist || []).forEach((x) => {
        byType[x.nodeType] = x.count
      })
      this.typeCount = byType
      this.totalCount = Object.values(byType).reduce((a, b) => a + b, 0)
    },
    renderCharts() {
      this.renderMonth()
      this.renderProvince()
      this.renderType()
    },
    newChart(el) {
      const chart = echarts.init(el)
      this.charts.push(chart)
      return chart
    },
    renderMonth() {
      const months = new Array(12).fill(0)
      ;(this.data.monthTrend || []).forEach((x) => {
        if (x.month >= 1 && x.month <= 12) months[x.month - 1] = x.count
      })
      const chart = this.newChart(this.$refs.monthChart)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 40, right: 20, top: 30, bottom: 30 },
        xAxis: {
          type: 'category',
          data: Array.from({ length: 12 }, (_, i) => `${i + 1}月`)
        },
        yAxis: { type: 'value', minInterval: 1 },
        series: [
          {
            name: '注册数量',
            type: 'line',
            smooth: true,
            data: months,
            areaStyle: { color: 'rgba(28,126,214,0.15)' },
            lineStyle: { color: '#1c7ed6' },
            itemStyle: { color: '#1c7ed6' }
          }
        ]
      })
    },
    renderProvince() {
      const dist = this.data.provinceDist || []
      const chart = this.newChart(this.$refs.provinceChart)
      chart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: 30, right: 20, top: 30, bottom: 60 },
        xAxis: { type: 'category', data: dist.map((x) => x.provinceName || '未知'), axisLabel: { rotate: 30 } },
        yAxis: { type: 'value', minInterval: 1 },
        series: [
          {
            name: '注册数量',
            type: 'bar',
            barMaxWidth: 44,
            data: dist.map((x) => x.count),
            itemStyle: { color: '#1c7ed6' }
          }
        ]
      })
    },
    renderType() {
      const dist = [...(this.data.typeDist || [])].sort((a, b) => a.nodeType - b.nodeType)
      const chart = this.newChart(this.$refs.typeChart)
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} 家 ({d}%)' },
        legend: { bottom: 0 },
        series: [
          {
            name: '企业类型',
            type: 'pie',
            radius: ['38%', '66%'],
            itemStyle: { borderColor: '#fff', borderWidth: 2 },
            label: { formatter: '{b}\n{c} 家' },
            data: dist.map((x) => ({
              name: NODE_TYPES[x.nodeType] || `类型${x.nodeType}`,
              value: x.count,
              itemStyle: { color: TYPE_COLOR[x.nodeType] || '#868e96' }
            }))
          }
        ]
      })
    }
  }
}
</script>

<style scoped>
.summary {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
.s-item {
  flex: 1;
  min-width: 140px;
  background: #f0f8ff;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}
.s-item .num {
  font-size: 30px;
  font-weight: 700;
  color: #1971c2;
}
.s-item .num.small {
  font-size: 22px;
}
.s-item .txt {
  color: #5c677d;
  margin-top: 4px;
  font-size: 13px;
}
.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}
.chart-card:nth-child(3) {
  grid-column: 1 / -1;
}
.chart-title {
  font-weight: 600;
  color: #1c4e80;
  margin-bottom: 10px;
}
.chart {
  height: 320px;
}
</style>
