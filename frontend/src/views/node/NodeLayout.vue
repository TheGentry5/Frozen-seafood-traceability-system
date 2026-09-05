<template>
  <div class="node-layout">
    <aside class="sidebar">
      <div class="logo"><i class="fas fa-water"></i> 海产品溯源</div>
      <div class="menu">
        <router-link :to="base + '/home'"><i class="fas fa-home"></i> 首页</router-link>
        <router-link :to="base + '/batch/create'"><i class="fas fa-plus-circle"></i> 新建产品批号</router-link>
        <router-link :to="base + '/batch/list'"><i class="fas fa-list-alt"></i> 产品批号管理</router-link>
        <router-link v-if="mod.hasConfirm" :to="base + '/confirm'">
          <i class="fas fa-check-double"></i> 下游企业进场确认
        </router-link>
        <a href="javascript:void(0)" @click="showPwd = true">
          <i class="fas fa-key"></i> 更新密码
        </a>
        <a href="javascript:void(0)" @click="logout">
          <i class="fas fa-sign-out-alt"></i> 退出登录
        </a>
      </div>
    </aside>

    <section class="content">
      <div class="topbar">
        <span class="who">
          当前企业：
          <strong>{{ user ? user.nodeName : '' }}</strong>
          <span v-if="mod" style="margin-left: 8px" class="tag tag-cyan">{{ mod.title }}</span>
        </span>
        <span>
          <router-link class="btn btn-sm btn-link" :to="base + '/batch/create'">新建批号</router-link>
          <router-link class="btn btn-sm" :to="'/consumer'">
            <i class="fas fa-search"></i> 消费者溯源
          </router-link>
        </span>
      </div>
      <router-view />
    </section>

    <!-- 更新密码弹窗 -->
    <div class="mask" v-if="showPwd" @click.self="showPwd = false">
      <div class="dialog">
        <div class="dialog-title">更新密码</div>
        <div class="form-item" style="margin-bottom: 12px">
          <label>原密码</label>
          <input v-model="pwdForm.oldPwd" type="password" placeholder="请输入原密码" />
        </div>
        <div class="form-item" style="margin-bottom: 12px">
          <label>新密码</label>
          <input v-model="pwdForm.newPwd" type="password" placeholder="请输入新密码" />
        </div>
        <div class="form-item">
          <label>确认新密码</label>
          <input v-model="pwdForm.confirmPwd" type="password" placeholder="再次输入新密码" />
        </div>
        <div class="dialog-footer">
          <button class="btn btn-gray" @click="showPwd = false">取消</button>
          <button class="btn btn-primary" @click="doUpdatePwd" :disabled="pwdLoading">
            {{ pwdLoading ? '提交中...' : '确认修改' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import MODULES from '../../modules'
import request, { clearAuth, getUser, toast } from '../../util'

export default {
  name: 'NodeLayout',
  data() {
    return {
      base: '',
      mod: null,
      user: getUser(),
      showPwd: false,
      pwdLoading: false,
      pwdForm: { oldPwd: '', newPwd: '', confirmPwd: '' }
    }
  },
  created() {
    const m = this.$route.matched.find((r) => r.meta.module)
    this.mod = MODULES[m ? m.meta.module : '']
    if (this.mod) this.base = '/' + this.mod.key
  },
  methods: {
    async logout() {
      try {
        await request.post('/logout')
      } catch (e) {
        /* 忽略 */
      }
      clearAuth()
      this.$router.push('/login')
    },
    async doUpdatePwd() {
      const { oldPwd, newPwd, confirmPwd } = this.pwdForm
      if (!oldPwd || !newPwd) {
        toast('请输入原密码与新密码', 'warn')
        return
      }
      if (newPwd.length < 6 || newPwd.length > 20) {
        toast('新密码长度须为 6~20 位', 'warn')
        return
      }
      if (newPwd !== confirmPwd) {
        toast('两次输入的新密码不一致', 'warn')
        return
      }
      this.pwdLoading = true
      try {
        await request.post('/user/updatePwd', { oldPwd, newPwd })
        toast('密码修改成功，请重新登录', 'success')
        clearAuth()
        this.$router.push('/login')
      } catch (e) {
        /* toast 已提示 */
      } finally {
        this.pwdLoading = false
      }
    }
  }
}
</script>
