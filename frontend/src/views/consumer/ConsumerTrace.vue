<template>
  <div class="trace-page">
    <header class="c-top">
      <div class="c-brand"><i class="fas fa-water"></i> 冷冻海产品溯源系统</div>
      <div>
        <router-link class="link" to="/consumer">再次查询</router-link>
      </div>
    </header>

    <div class="page">
      <div class="card result-head">
        <div>
          溯源标识码：
          <strong class="trace-code">{{ traceCode }}</strong>
        </div>
        <span v-if="result" class="tag tag-green">查询成功 · 该批次全程可追溯</span>
      </div>

      <div v-if="loading" class="empty">正在查询溯源信息...</div>
      <div v-else-if="errorMsg" class="card">
        <div class="empty"><i class="fas fa-search"></i> {{ errorMsg }}</div>
      </div>

      <template v-else-if="chain.length">
        <div
          v-for="(node, idx) in chain"
          :key="idx"
          class="card step-card"
        >
          <div class="step-left">
            <div class="dot" :style="{ background: colors[idx] }">
              <i class="fas" :class="icons[idx]"></i>
            </div>
            <div v-if="idx < chain.length - 1" class="line"></div>
          </div>
          <div class="step-body">
            <div class="step-head">
              <span class="stage-tag" :style="{ background: colors[idx] }">{{ node.stage }}</span>
              <span class="enterprise">{{ node.nodeName }}</span>
            </div>
            <div class="detail-list">
              <div class="item">
                <div class="label">产品批号</div>
                <div class="value">{{ node.batchCode || '—' }}</div>
              </div>
              <div class="item">
                <div class="label">产品名称</div>
                <div class="value">{{ node.productName || '—' }}</div>
              </div>
              <div class="item">
                <div class="label">检验检疫证明</div>
                <div class="value">{{ node.inspectionCert || '—' }}</div>
              </div>
              <div class="item">
                <div class="label">检验员</div>
                <div class="value">{{ node.inspector || '—' }}</div>
              </div>
              <div class="item">
                <div class="label">入库/出场时间</div>
                <div class="value">{{ node.createTime || '—' }}</div>
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script>
import request from '../../util'

export default {
  name: 'ConsumerTrace',
  data() {
    return {
      traceCode: this.$route.query.code || '',
      result: null,
      chain: [],
      loading: true,
      errorMsg: '',
      colors: ['#2f9e44', '#1971c2', '#e8590c', '#862e9c'],
      icons: ['fa-fish', 'fa-industry', 'fa-warehouse', 'fa-store']
    }
  },
  async created() {
    if (!this.traceCode) {
      this.errorMsg = '请从溯源查询页输入溯源码'
      this.loading = false
      return
    }
    try {
      const res = await request.get(`/trace/info/${encodeURIComponent(this.traceCode)}`)
      this.result = res
      this.chain = (res && res.chain) || []
      if (!this.chain.length) this.errorMsg = '未查询到该溯源码对应的溯源链'
    } catch (e) {
      this.errorMsg = '未查询到该溯源码信息'
    } finally {
      this.loading = false
    }
  }
}
</script>

<style scoped>
.trace-page {
  min-height: 100vh;
  background: #f2f6fc;
}
.c-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 40px;
  background: #fff;
  box-shadow: 0 1px 6px rgba(30, 100, 180, 0.08);
}
.c-brand {
  font-size: 20px;
  font-weight: 700;
  color: #10375c;
}
.c-brand i {
  color: #1c7ed6;
  margin-right: 6px;
}
.link {
  color: #1971c2;
}
.result-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.trace-code {
  color: #1971c2;
  font-size: 18px;
  letter-spacing: 1px;
}
.step-card {
  display: flex;
  gap: 18px;
}
.step-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 40px;
}
.dot {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}
.line {
  flex: 1;
  width: 2px;
  background: #dee2e6;
  margin: 6px 0;
  min-height: 40px;
}
.step-body {
  flex: 1;
}
.step-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.stage-tag {
  color: #fff;
  padding: 3px 12px;
  border-radius: 4px;
  font-size: 13px;
}
.enterprise {
  font-size: 16px;
  font-weight: 600;
  color: #1c4e80;
}
.detail-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 30px;
}
.detail-list .item {
  display: flex;
}
.detail-list .label {
  color: #868e96;
  width: 110px;
  flex-shrink: 0;
}
.detail-list .value {
  word-break: break-all;
}
</style>
