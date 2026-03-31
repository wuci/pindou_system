import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import { setupDirectives } from './directives'

const app = createApp(App)
const pinia = createPinia()

// 必须先注册Pinia，再注册路由（因为路由守卫中使用了Pinia store）
app.use(pinia)

// 注册Element Plus
app.use(ElementPlus)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册全局指令
setupDirectives(app)

// 在Pinia之后注册路由
import router from './router'
app.use(router)

// 挂载应用
app.mount('#app')
