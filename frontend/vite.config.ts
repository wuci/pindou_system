import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      // 自动导入 Vue 相关函数
      imports: ['vue', 'vue-router', 'pinia'],
      // 自动导入 Element Plus 相关函数
      resolvers: [ElementPlusResolver()],
      dts: 'src/auto-imports.d.ts'
    }),
    Components({
      // 自动导入 Element Plus 组件
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts'
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:9026',
        changeOrigin: true
      }
    }
  },
  build: {
    // 开发环境输出到 dist，生产环境输出到后端 resources/static
    outDir: process.env.NODE_ENV === 'production'
      ? '../backend/pdsystem-server/src/main/resources/static'
      : 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    emptyOutDir: true,
    // 代码分割
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'echarts': ['echarts']
        }
      }
    },
    // 压缩
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true
      }
    }
  }
})
