import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const proxy = {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}

// 后端默认 8080；开发期前端 5173，/api 走代理避免跨域
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy
  },
  preview: {
    proxy
  }
})
