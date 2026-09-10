<template>
  <div v-if="mod">
    <div class="card">
      <div class="page-title">功能菜单</div>
      <p style="color: #5c677d; margin-bottom: 18px">
        请选择要进行的业务操作，批号流转状态请随时关注"待确认"结果。
      </p>
      <div class="grid">
        <router-link :to="base + '/batch/create'" class="tile">
          <i class="fas fa-plus-circle"></i>
          <div class="t">新建产品批号</div>
          <div class="d">录入本企业一批产品的基本信息</div>
        </router-link>
        <router-link :to="base + '/batch/list'" class="tile">
          <i class="fas fa-list-alt"></i>
          <div class="t">产品批号管理</div>
          <div class="d">浏览 / 更新 / 删除 / 下架产品批号</div>
        </router-link>
        <router-link v-if="mod.hasConfirm" :to="base + '/confirm'" class="tile">
          <i class="fas fa-check-double"></i>
          <div class="t">下游企业进场确认</div>
          <div class="d">确认下游企业以本企业批号作为进场批号的请求</div>
        </router-link>
      </div>
    </div>

    <div class="card">
      <div class="page-title">溯源链说明</div>
      <div class="chain">
        <template v-for="(s, i) in chainSteps" :key="i">
          <span class="step" :class="{ active: s.type === mod.nodeType }">{{ s.name }}</span>
          <i v-if="i < chainSteps.length - 1" class="fas fa-arrow-right"></i>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import MODULES, { NODE_TYPES } from '../../modules'

export default {
  name: 'NodeHome',
  data() {
    const m = this.$route.matched.find((r) => r.meta.module)
    const mod = MODULES[m ? m.meta.module : ''] || null
    return {
      mod,
      base: mod ? '/' + mod.key : ''
    }
  },
  computed: {
    chainSteps() {
      return Object.entries(NODE_TYPES).map(([type, name]) => ({ type: Number(type), name }))
    }
  }
}
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}
.tile {
  display: block;
  background: #f7fafc;
  border: 1px solid #e3edf7;
  border-radius: 8px;
  padding: 22px;
  transition: all 0.2s;
}
.tile:hover {
  border-color: #1c7ed6;
  box-shadow: 0 4px 14px rgba(28, 126, 214, 0.15);
  transform: translateY(-2px);
}
.tile i {
  font-size: 30px;
  color: #1c7ed6;
}
.tile .t {
  margin-top: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #1c4e80;
}
.tile .d {
  margin-top: 6px;
  color: #868e96;
  font-size: 12px;
}
.chain {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  background: #f7fafc;
  border-radius: 8px;
  padding: 18px;
}
.step {
  padding: 6px 16px;
  background: #fff;
  border: 1px solid #ced4da;
  border-radius: 20px;
  color: #5c677d;
}
.step.active {
  background: #1c7ed6;
  color: #fff;
  border-color: #1c7ed6;
}
.chain i {
  color: #adb5bd;
}
</style>
