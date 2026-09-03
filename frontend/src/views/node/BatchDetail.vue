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
import MODULES, { statusText, statusTagClass } from '../../modules'
import request from '../../util'

export default {
  name: 'BatchDetail',
  data() {
    const m = this.$route.matched.find((r) => r.meta.module)
    const mod = MODULES[m ? m.meta.module : ''] || null
    return {
      mod,
      base: mod ? '/' + mod.key : '',
      row: {},
      loading: true
    }
  },
  computed: {
    detailCols() {
      const cols = (this.mod && this.mod.detailCols) || []
      // 详情中状态单独展示，过滤 traceCode 已在面板处理
      return cols
    },
    canEdit() {
      if (!this.mod) return false
      const allowed =
        this.mod.key === 'farm' ? [1] : [1]
      return allowed.includes(this.row.status)
    }
  },
  created() {
    this.load()
  },
  methods: {
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
</style>
