<template>
  <div class="card" v-if="mod">
    <div class="page-title">{{ mod.confirmTitle || '下游企业进场确认' }}</div>
    <p class="desc">
      以下为<strong>进场批号来自本企业</strong>且状态为"待确认"的下游批号，请核对后确认。
      确认后下游批号将正式进入溯源链。
    </p>

    <div class="search">
      <input v-model="keyword" placeholder="按下游企业名称模糊查询" @keyup.enter="load(1)" />
      <button class="btn btn-primary btn-sm" @click="load(1)">
        <i class="fas fa-search"></i> 查找
      </button>
      <button class="btn btn-sm" @click="keyword = ''; load(1)">清空</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>下游产品批号</th>
          <th>产品名称</th>
          <th v-if="mod.showProductType">产品类型</th>
          <th>待确认企业</th>
          <th>创建时间</th>
          <th style="width: 120px">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row.id">
          <td>{{ row.batchCode || '—' }}</td>
          <td>{{ row.productName || '—' }}</td>
          <td v-if="mod.showProductType">{{ row.productType || '—' }}</td>
          <td>{{ row.nodeName || '—' }}</td>
          <td>{{ row.createTime || '—' }}</td>
          <td>
            <button class="btn btn-green btn-sm" @click="doConfirm(row)" :disabled="confirming">
              <i class="fas fa-check"></i> 确认
            </button>
          </td>
        </tr>
        <tr v-if="!loading && rows.length === 0">
          <td :colspan="5" class="empty">暂无待确认的下游进场请求</td>
        </tr>
      </tbody>
    </table>

    <Pagination :total="total" :page="page" :size="size" @change="onPageChange" />
  </div>
</template>

<script>
import MODULES from '../../modules'
import request, { toast } from '../../util'
import Pagination from '../../components/Pagination.vue'

export default {
  name: 'ConfirmBatch',
  components: { Pagination },
  data() {
    const m = this.$route.matched.find((r) => r.meta.module)
    const mod = MODULES[m ? m.meta.module : ''] || null
    return {
      mod,
      base: mod ? '/' + mod.key : '',
      keyword: '',
      rows: [],
      total: 0,
      page: 1,
      size: 10,
      loading: false,
      confirming: false
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    async load(p) {
      this.loading = true
      try {
        const res = await request.get(`/${this.mod.key}/confirm`, {
          params: { page: p, size: this.size, keyword: this.keyword || undefined }
        })
        this.page = p
        this.rows = res.list || []
        this.total = res.total || 0
      } catch (e) {
        /* toast */
      } finally {
        this.loading = false
      }
    },
    onPageChange({ page, size }) {
      this.size = size
      this.load(page)
    },
    async doConfirm(row) {
      if (!window.confirm(`确认"${row.nodeName}"的产品批号 ${row.batchCode} 进场无误？`)) return
      this.confirming = true
      try {
        await request.put(`/${this.mod.key}/confirm/${row.id}`)
        const msg =
          this.mod.key === 'whol'
            ? '已确认，零售商批号溯源码已自动生成'
            : '已确认该批号进场'
        toast(msg, 'success')
        this.load(this.rows.length === 1 && this.page > 1 ? this.page - 1 : this.page)
      } catch (e) {
        /* toast */
      } finally {
        this.confirming = false
      }
    }
  }
}
</script>

<style scoped>
.desc {
  color: #5c677d;
  margin-bottom: 14px;
  line-height: 1.7;
}
.search {
  display: flex;
  gap: 6px;
  margin-bottom: 14px;
}
.search input {
  width: 260px;
  height: 28px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  padding: 0 10px;
  outline: none;
}
</style>
