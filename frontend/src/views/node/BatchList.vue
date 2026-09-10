<template>
  <div class="card" v-if="mod">
    <div class="head">
      <div class="page-title">产品批号管理</div>
      <router-link class="btn btn-primary" :to="base + '/batch/create'">
        <i class="fas fa-plus"></i> 新建批号
      </router-link>
    </div>

    <div class="toolbar">
      <div class="status-tabs">
        <button
          v-for="opt in mod.statusOptions"
          :key="opt.value"
          class="btn btn-sm"
          :class="{ 'btn-primary': activeStatus === opt.value }"
          @click="changeStatus(opt.value)"
        >{{ opt.label }}</button>
      </div>
      <div class="search">
        <input
          v-model="keyword"
          placeholder="按批号 / 产品名称模糊查询"
          @keyup.enter="load(1)"
        />
        <button class="btn btn-primary btn-sm" @click="load(1)">
          <i class="fas fa-search"></i> 查找
        </button>
        <button class="btn btn-sm" @click="keyword = ''; load(1)">清空</button>
      </div>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th v-for="c in mod.listCols" :key="c.key">{{ c.label }}</th>
          <th>状态</th>
          <th style="width: 180px">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="row in rows"
          :key="row.id"
          class="row-link"
          @click="$router.push(base + '/batch/detail/' + row.id)"
        >
          <td v-for="c in mod.listCols" :key="c.key">{{ fmt(row[c.key]) }}</td>
          <td>
            <span class="tag" :class="statusTag(row.status)">{{ statusText(row.status) }}</span>
          </td>
          <td @click.stop>
            <template v-if="currentOption.acts.includes('update')">
              <button class="btn-link" @click="$router.push(base + '/batch/edit/' + row.id)">
                <i class="fas fa-edit"></i> 更新
              </button>
              <button class="btn-link" style="color: #fa5252" @click="doDelete(row)">
                <i class="fas fa-trash-alt"></i> 删除
              </button>
            </template>
            <template v-if="currentOption.acts.includes('off')">
              <button class="btn-link" style="color: #e8590c" @click="doOff(row)">
                <i class="fas fa-arrow-circle-down"></i> 下架
              </button>
            </template>
          </td>
        </tr>
        <tr v-if="!loading && rows.length === 0">
          <td :colspan="mod.listCols.length + 2" class="empty">暂无"{{ currentOption.label }}"批号</td>
        </tr>
      </tbody>
    </table>

    <Pagination
      :total="total"
      :page="page"
      :size="size"
      @change="onPageChange"
    />
  </div>
</template>

<script>
import MODULES, { statusText, statusTagClass } from '../../modules'
import request, { toast } from '../../util'
import Pagination from '../../components/Pagination.vue'

export default {
  name: 'BatchList',
  components: { Pagination },
  data() {
    const m = this.$route.matched.find((r) => r.meta.module)
    const mod = MODULES[m ? m.meta.module : ''] || null
    return {
      mod,
      base: mod ? '/' + mod.key : '',
      activeStatus: mod ? mod.statusOptions[0].value : null,
      keyword: '',
      rows: [],
      total: 0,
      page: 1,
      size: 10,
      loading: false
    }
  },
  computed: {
    currentOption() {
      const opts = this.mod ? this.mod.statusOptions : []
      return opts.find((o) => o.value === this.activeStatus) || opts[0] || { acts: [] }
    }
  },
  created() {
    this.load(1)
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
    changeStatus(v) {
      this.activeStatus = v
      this.load(1)
    },
    async load(p) {
      this.loading = true
      try {
        const res = await request.get(`/${this.mod.key}/batch`, {
          params: { page: p, size: this.size, status: this.activeStatus, keyword: this.keyword || undefined }
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
    async doDelete(row) {
      if (!window.confirm(`确定删除产品批号 ${row.batchCode} 吗？`)) return
      try {
        await request.delete(`/${this.mod.key}/batch/${row.id}`)
        toast('删除成功', 'success')
        this.load(this.rows.length === 1 && this.page > 1 ? this.page - 1 : this.page)
      } catch (e) {
        /* toast */
      }
    },
    async doOff(row) {
      if (!window.confirm(`确定下架产品批号 ${row.batchCode} 吗？下架后不再可被选用与浏览。`)) return
      try {
        await request.put(`/${this.mod.key}/batch/${row.id}/off`)
        toast('已下架', 'success')
        this.load(this.page)
      } catch (e) {
        /* toast */
      }
    }
  }
}
</script>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}
.status-tabs {
  display: flex;
  gap: 8px;
}
.search {
  display: flex;
  gap: 6px;
  align-items: center;
}
.search input {
  height: 28px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  padding: 0 10px;
  outline: none;
  width: 220px;
}
</style>
