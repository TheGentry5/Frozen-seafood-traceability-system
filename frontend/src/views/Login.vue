<template>
  <div class="login-wrap">
    <div class="login-box">
      <div class="brand">
        <i class="fas fa-water"></i>
        <h1>冷冻海产品溯源系统</h1>
        <p>从养殖场到餐桌 · 全程可信可溯</p>
      </div>

      <div class="tabs">
        <button
          class="btn tab-btn"
          :class="{ active: mode === 'node' }"
          @click="mode = 'node'; resetMsg()"
        >流通节点登录</button>
        <button
          class="btn tab-btn"
          :class="{ active: mode === 'admin' }"
          @click="mode = 'admin'; resetMsg()"
        >系统管理登录</button>
      </div>

      <form class="login-form" @submit.prevent="submit">
        <div class="form-item">
          <label>{{ mode === 'node' ? '企业登录编码' : '管理员账号' }}</label>
          <input
            v-model="username"
            :placeholder="mode === 'node' ? '请输入企业登录编码' : '请输入管理员账号'"
          />
        </div>
        <div class="form-item">
          <label>登录密码</label>
          <input v-model="password" type="password" placeholder="请输入登录密码" />
        </div>
        <button class="btn btn-primary login-btn" type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
        <div class="tips">
          节点端可直接输入演示账号：FARM001 / PROC001 / WHOL001 / RETA001（密码 123456）
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import request, { setAuth } from '../util'
import { homePathOf } from '../modules'

export default {
  name: 'Login',
  data() {
    return { mode: 'node', username: '', password: '', loading: false }
  },
  methods: {
    resetMsg() {
      this.password = ''
    },
    async submit() {
      if (!this.username || !this.password) {
        alert('请输入账号与密码')
        return
      }
      this.loading = true
      try {
        const isAdmin = this.mode === 'admin'
        const url = isAdmin ? '/admin/login' : '/login'
        const payload = isAdmin
          ? { username: this.username, password: this.password }
          : { nodeCode: this.username, password: this.password }
        const res = await request.post(url, payload)
        const user = isAdmin
          ? { type: 'ADMIN', name: '系统管理员', username: this.username }
          : { ...(res.user || {}), type: 'NODE' }
        setAuth(res.token, user)
        this.$router.push(homePathOf(user))
      } catch (e) {
        /* 错误提示已由拦截器 toast 统一处理 */
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0b3d66 0%, #1971a8 60%, #4dabf7 100%);
}
.login-box {
  width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 34px 38px;
  box-shadow: 0 12px 40px rgba(0, 30, 60, 0.3);
}
.brand {
  text-align: center;
  margin-bottom: 20px;
}
.brand i {
  font-size: 40px;
  color: #1c7ed6;
}
.brand h1 {
  font-size: 22px;
  color: #10375c;
  margin: 8px 0 4px;
}
.brand p {
  color: #868e96;
  font-size: 13px;
}
.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
.tab-btn {
  flex: 1;
  background: #eef3fa;
}
.tab-btn.active {
  background: #1c7ed6;
  color: #fff;
}
.login-form .form-item {
  margin-bottom: 14px;
}
.login-btn {
  width: 100%;
  margin-top: 6px;
}
.tips {
  margin-top: 12px;
  color: #868e96;
  font-size: 12px;
  line-height: 1.6;
}
</style>
