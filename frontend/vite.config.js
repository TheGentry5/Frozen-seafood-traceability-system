import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 后端默认 8080；开发期前端 5173，/api 走代理避免跨域
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': '/src'
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
