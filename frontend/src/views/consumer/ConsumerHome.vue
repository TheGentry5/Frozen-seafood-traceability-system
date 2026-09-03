<template>
  <div class="consumer">
    <header class="c-top">
      <div class="c-brand"><i class="fas fa-water"></i> 冷冻海产品溯源系统</div>
      <div>
        <router-link class="link" to="/consumer">溯源查询</router-link>
        <router-link class="link" to="/login">登录</router-link>
      </div>
    </header>

    <div class="hero">
      <h1>买得安心 · 吃得放心</h1>
      <p>输入产品包装上的溯源标识码，查询从养殖场到餐桌的全程信息</p>
      <div class="search-box">
        <input
          v-model="traceCode"
          placeholder="请输入溯源标识码，如 TSF-20260902-XXXXXX"
          @keyup.enter="doTrace"
        />
        <button class="btn btn-primary" @click="doTrace">
          <i class="fas fa-search"></i> 溯源
        </button>
      </div>
      <div class="steps-tip">
        <span>养殖企业</span><i class="fas fa-arrow-right"></i>
        <span>加工企业</span><i class="fas fa-arrow-right"></i>
        <span>批发商</span><i class="fas fa-arrow-right"></i>
        <span>零售商</span><i class="fas fa-arrow-right"></i>
        <span>消费者</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ConsumerHome',
  data() {
    return { traceCode: '' }
  },
  methods: {
    doTrace() {
      const code = (this.traceCode || '').trim()
      if (!code) {
        alert('请输入溯源标识码')
        return
      }
      this.$router.push('/consumer/trace?code=' + encodeURIComponent(code))
    }
  }
}
</script>

<style scoped>
.consumer {
  min-height: 100vh;
  background: linear-gradient(180deg, #e7f5ff 0%, #f8fbff 40%, #ffffff 100%);
}
.c-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 40px;
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
  margin-left: 20px;
  color: #1971c2;
}
.hero {
  text-align: center;
  padding: 90px 20px 70px;
}
.hero h1 {
  font-size: 40px;
  color: #10375c;
}
.hero p {
  color: #5c677d;
  margin: 14px 0 34px;
  font-size: 15px;
}
.search-box {
  display: flex;
  justify-content: center;
  gap: 10px;
}
.search-box input {
  width: 480px;
  max-width: 82vw;
  height: 46px;
  border: 2px solid #1c7ed6;
  border-radius: 6px;
  padding: 0 16px;
  font-size: 15px;
  outline: none;
}
.search-box .btn {
  height: 46px;
  font-size: 15px;
  padding: 0 26px;
}
.steps-tip {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  color: #1971c2;
  font-size: 13px;
  flex-wrap: wrap;
}
.steps-tip span {
  background: #fff;
  padding: 6px 14px;
  border-radius: 20px;
  box-shadow: 0 1px 6px rgba(30, 100, 180, 0.12);
}
.steps-tip i {
  color: #74c0fc;
}
</style>
