<template>
  <div class="card" v-if="mod">
    <div class="page-title">
      产品批号详情
      <span class="tag tag-cyan" style="margin-left: 8px">{{ mod.title }}</span>
      <span v-if="row.id" class="tag" :class="statusTag(row.status)" style="margin-left: 6px">
        {{ statusText(row.status) }}
      </span>
    </div>

    <div v-if="!loading && row.id">
      <!-- 零售商溯源码突出展示 -->
      <div v-if="mod.key === 'reta'" class="trace-panel">
        <div class="tp-label"><i class="fas fa-qrcode"></i> 溯源标识码</div>
        <div v-if="row.traceCode" class="tp-code">{{ row.traceCode }}</div>
        <div v-else class="tp-empty">
          该批号尚未被上游确认，确认后将自动生成溯源标识码，供消费者查询
        </div>
      </div>

      <h4 class="section-title"><i class="fas fa-box-open"></i> 批号信息</h4>
      <div class="detail-list">
        <div v-for="c in detailCols" :key="c.key" class="item">
          <div class="label">{{ c.label }}</div>
          <div class="value">{{ fmt(row[c.key]) }}</div>
        </div>
      </div>

      <!-- 进场信息 -->
      <template v-if="mod.upstream">
        <h4 class="section-title"><i class="fas fa-truck-loading"></i> 进场信息 - {{ mod.upstream.title }}</h4>
        <div class="detail-list">
          <div v-for="c in mod.upstreamDetailCols" :key="c.key" class="item">
            <div class="label">{{ c.label }}</div>
            <div class="value">{{ fmt(row[c.key]) }}</div>
          </div>
        </div>
      </template>

      <h4 class="section-title"><i class="fas fa-temperature-low"></i> 冷链温度记录</h4>
      <div v-if="tempRecords.length" ref="tempChart" class="cold-chart"></div>
      <div v-else class="cold-empty">暂无温度填报记录</div>

      <div class="cold-form form-grid">
        <div class="form-item">
          <label>温度(℃)<span class="req">*</span></label>
          <input v-model="tempForm.temperature" type="number" step="0.1" placeholder="如 -18.5" />
        </div>
        <div class="form-item">
          <label>湿度(%)</label>
          <input v-model="tempForm.humidity" type="number" step="0.1" placeholder="可选" />
        </div>
        <div class="form-item">
          <label>采集时间</label>
          <input v-model="tempForm.recordTime" type="datetime-local" />
        </div>
        <div class="form-item">
          <label>备注</label>
          <input v-model="tempForm.remark" placeholder="可选" />
        </div>
      </div>
      <div class="cold-actions">
        <span v-if="!canReport" class="cold-tip">该批号已下架，不可继续填报</span>
        <button class="btn btn-primary" :disabled="!canReport || reporting" @click="reportTemp">
          <i class="fas fa-plus"></i> 填报温度
        </button>
      </div>

      <div class="op-row">
        <router-link class="btn btn-gray" :to="base + '/batch/list'">返回列表</router-link>
        <router-link v-if="canEdit" class="btn btn-primary" :to="base + '/batch/edit/' + row.id">
          <i class="fas fa-edit"></i> 更新
        </router-link>
      </div>
    </div>
    <div v-else class="empty">{{ loading ? '加载中...' : '未找到该批号或无权查看' }}</div>
  </div>
</template>

<script>
import echarts from '../../charts'
import MODULES, { statusText, statusTagClass } from '../../modules'
import request, { toast } from '../../util'

export default {
  name: 'BatchDetail',
  data() {
    const m = this.$route.matched.find((r) => r.meta.module)
    const mod = MODULES[m ? m.meta.module : ''] || null
    return {
      mod,
      base: mod ? '/' + mod.key : '',
      row: {},
      loading: true,
      tempRecords: [],
      tempForm: { temperature: '', humidity: '', recordTime: '', remark: '' },
      tempChart: null,
      reporting: false
    }
  },
  computed: {
    detailCols() {
      const cols = (this.mod && this.mod.detailCols) || []
      // 状态单独展示；零售溯源码由上方面板突出展示，避免重复
      return cols.filter((c) => c.key !== 'traceCode')
    },
    canEdit() {
      return !!(this.mod && this.row.status === 1)
    },
    canReport() {
      if (!this.row.id) return false
      const offStatus = this.mod.key === 'farm' ? 3 : 4
      return this.row.status !== offStatus
    }
  },
  created() {
    this.load()
    this.loadTemp()
    window.addEventListener('resize', this.onResize)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.onResize)
    if (this.tempChart) this.tempChart.dispose()
  },
  methods: {
    onResize() {
      if (this.tempChart) this.tempChart.resize()
    },
    statusText(s) {
      return this.mod ? statusText(this.mod.key, s) : '—'
    },
    statusTag(s) {
      return this.mod ? statusTagClass(this.mod.key, s) : 'tag-gray'
    },
    fmt(v) {
      return v === null || v === undefined || v === '' ? '—' : v
    },
    async load() {
      try {
        this.row = (await request.get(`/${this.mod.key}/batch/${this.$route.params.id}`)) || {}
      } catch (e) {
        /* toast */
      } finally {
        this.loading = false
      }
    },
    async loadTemp() {
      try {
        this.tempRecords = (await request.get('/cold-chain', {
          params: { batchType: this.mod.nodeType, batchId: this.$route.params.id }
        })) || []
      } catch (e) {
        /* toast */
      }
      this.$nextTick(() => this.renderTempChart())
    },
    fmtTime(v) {
      if (!v) return ''
      const d = new Date(v)
      if (isNaN(d.getTime())) return v
      const p = (n) => String(n).padStart(2, '0')
      return `${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
    },
    renderTempChart() {
      const el = this.$refs.tempChart
      if (!el || !this.tempRecords.length) return
      if (this.tempChart) this.tempChart.dispose()
      this.tempChart = echarts.init(el)
      this.tempChart.setOption({
        tooltip: {
          trigger: 'axis',
          formatter: (ps) => {
            const r = this.tempRecords[ps[0].dataIndex] || {}
            return `${this.fmtTime(r.recordTime)}<br/>温度：${ps[0].value} ℃`
          }
        },
        grid: { left: 55, right: 30, top: 30, bottom: 30 },
        xAxis: {
          type: 'category',
          data: this.tempRecords.map((r) => this.fmtTime(r.recordTime))
        },
        yAxis: { type: 'value', name: '℃' },
        series: [
          {
            name: '温度',
            type: 'line',
            smooth: true,
            data: this.tempRecords.map((r) => {
              const t = Number(r.temperature)
              return { value: t, itemStyle: t > 0 || t < -25 ? { color: '#fa5252' } : undefined }
            }),
            lineStyle: { color: '#1c7ed6' },
            itemStyle: { color: '#1c7ed6' },
            areaStyle: { color: 'rgba(28,126,214,0.12)' },
            markLine: {
              silent: true,
              symbol: 'none',
              lineStyle: { type: 'dashed' },
              data: [
                { yAxis: 0, name: '上限 0℃', lineStyle: { color: '#fa5252' } },
                { yAxis: -25, name: '下限 -25℃', lineStyle: { color: '#1c7ed6' } }
              ]
            }
          }
        ]
      })
    },
    async reportTemp() {
      if (this.reporting) return
      if (this.tempForm.temperature === '' || this.tempForm.temperature === null) {
        toast('请输入温度', 'warn')
        return
      }
      this.reporting = true
      try {
        await request.post('/cold-chain/report', {
          batchType: this.mod.nodeType,
          batchId: Number(this.$route.params.id),
          temperature: Number(this.tempForm.temperature),
          humidity: this.tempForm.humidity === '' ? null : Number(this.tempForm.humidity),
          recordTime: this.tempForm.recordTime || null,
          remark: this.tempForm.remark || null
        })
        toast('填报成功', 'success')
        this.tempForm = { temperature: '', humidity: '', recordTime: '', remark: '' }
        this.loadTemp()
      } catch (e) {
        /* toast */
      } finally {
        this.reporting = false
      }
    }
  }
}
</script>

<style scoped>
.section-title {
  margin: 18px 0 12px;
  color: #1c4e80;
  font-size: 15px;
}
.section-title i {
  color: #1c7ed6;
  margin-right: 6px;
}
.trace-panel {
  background: linear-gradient(90deg, #e7f5ff, #f0f8ff);
  border: 1px dashed #1c7ed6;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 18px;
}
.tp-label {
  color: #1c4e80;
  font-weight: 600;
}
.tp-code {
  font-size: 22px;
  font-weight: 700;
  color: #1971c2;
  letter-spacing: 1px;
  margin-top: 8px;
}
.tp-empty {
  color: #868e96;
  margin-top: 8px;
}
.op-row {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
.cold-chart {
  height: 300px;
  margin-bottom: 16px;
}
.cold-empty {
  color: #868e96;
  text-align: center;
  padding: 24px 0;
  margin-bottom: 16px;
}
.cold-form {
  margin-bottom: 12px;
}
.cold-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}
.cold-tip {
  color: #e8590c;
  font-size: 13px;
}
</style>
