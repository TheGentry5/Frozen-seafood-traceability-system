<template>
  <div class="pager" v-if="total > 0">
    <span class="pager-total">共 {{ total }} 条</span>
    <button class="btn btn-sm" :disabled="page <= 1" @click="change(page - 1)">
      <i class="fas fa-chevron-left"></i> 上一页
    </button>
    <span class="pager-info">{{ page }} / {{ pageCount }}</span>
    <button class="btn btn-sm" :disabled="page >= pageCount" @click="change(page + 1)">
      下一页 <i class="fas fa-chevron-right"></i>
    </button>
    <select class="page-size" :value="size" @change="onSize">
      <option :value="5">5 条/页</option>
      <option :value="10">10 条/页</option>
      <option :value="20">20 条/页</option>
    </select>
  </div>
</template>

<script>
export default {
  name: 'Pagination',
  props: {
    total: { type: Number, default: 0 },
    page: { type: Number, default: 1 },
    size: { type: Number, default: 10 }
  },
  computed: {
    pageCount() {
      return Math.max(1, Math.ceil(this.total / this.size))
    }
  },
  emits: ['change'],
  methods: {
    change(p) {
      if (p < 1 || p > this.pageCount) return
      this.$emit('change', { page: p, size: this.size })
    },
    onSize(e) {
      this.$emit('change', { page: 1, size: Number(e.target.value) })
    }
  }
}
</script>

<style scoped>
.pager {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 14px;
  font-size: 13px;
}
.pager-total {
  color: #868e96;
  margin-right: auto;
}
.pager-info {
  color: #5c677d;
}
.page-size {
  height: 28px;
  border: 1px solid #ced4da;
  border-radius: 4px;
}
</style>
