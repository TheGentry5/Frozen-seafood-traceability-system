<template>
  <div class="admin-wrap">
    <header class="admin-top">
      <div class="brand">
        <i class="fas fa-cog"></i> 冷冻海产品溯源系统 · 管理端
      </div>
      <nav class="admin-nav">
        <router-link to="/admin/node"><i class="fas fa-building"></i> 节点企业注册管理</router-link>
        <router-link to="/admin/stats"><i class="fas fa-chart-pie"></i> 注册信息统计大屏</router-link>
      </nav>
      <div class="admin-user">
        <span><i class="fas fa-user-shield"></i> 系统管理员</span>
        <button class="btn btn-sm btn-red" @click="logout">退出</button>
      </div>
    </header>
    <router-view />
  </div>
</template>

<script>
import request, { clearAuth } from '../../util'

export default {
  name: 'AdminLayout',
  methods: {
    async logout() {
      try {
        await request.post('/logout')
      } catch (e) {
        /* ignore */
      }
      clearAuth()
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.admin-wrap {
  min-height: 100vh;
  background: #f2f6fc;
}
.admin-top {
  display: flex;
  align-items: center;
  gap: 26px;
  background: linear-gradient(90deg, #10375c, #1c4e80);
  color: #fff;
  padding: 0 26px;
  height: 58px;
}
.brand {
  font-size: 17px;
  font-weight: 700;
}
.brand i {
  color: #7fd8ff;
}
.admin-nav {
  display: flex;
  gap: 4px;
  flex: 1;
}
.admin-nav a {
  padding: 18px 16px;
  color: #cfe4f7;
  font-size: 14px;
}
.admin-nav a:hover,
.admin-nav a.router-link-active {
  color: #fff;
  background: rgba(255, 255, 255, 0.12);
}
.admin-user {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
