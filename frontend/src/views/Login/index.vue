<template>
  <div class="login-container">
    <!-- 动态背景层 -->
    <div class="bg-gradient"></div>
    <div class="bg-particles">
      <span v-for="i in 20" :key="i" class="particle" :style="getParticleStyle(i)"></span>
    </div>
    <div class="bg-waves">
      <div class="wave wave1"></div>
      <div class="wave wave2"></div>
      <div class="wave wave3"></div>
    </div>

    <!-- 登录框 -->
    <div class="login-box">
      <div class="login-title">
        <div class="logo-container">
          <svg class="logo-large" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
            <!-- 海洋水面 -->
            <path d="M10 65 Q25 58 40 65 Q55 72 70 65 Q85 58 100 65" fill="none" stroke="url(#waterGradientLarge)" stroke-width="3" opacity="0.4"/>
            <path d="M15 75 Q30 68 45 75 Q60 82 75 75 Q90 68 100 72" fill="none" stroke="url(#waterGradientLarge)" stroke-width="2" opacity="0.3"/>
            <path d="M20 82 Q35 77 50 82 Q65 87 80 82" fill="none" stroke="url(#waterGradientLarge)" stroke-width="1.5" opacity="0.2"/>

            <!-- 岛屿主体 -->
            <path d="M50 72 C32 72 22 52 22 42 C22 28 38 18 50 18 C62 18 78 28 78 42 C78 52 68 72 50 72Z"
                  fill="url(#islandGradientLarge)"/>

            <!-- 岛屿沙滩高光 -->
            <path d="M50 24 C60 24 68 28 72 34" stroke="rgba(255,255,255,0.5)" stroke-width="3" fill="none" stroke-linecap="round"/>
            <ellipse cx="35" cy="30" rx="8" ry="4" fill="rgba(255,255,255,0.2)" transform="rotate(-20, 35, 30)"/>

            <!-- 岛屿上集结的光点 -->
            <circle cx="42" cy="44" r="4" fill="#fff" opacity="0.92"/>
            <circle cx="58" cy="40" r="5" fill="#fff" opacity="0.95"/>
            <circle cx="50" cy="54" r="3.5" fill="#fff" opacity="0.88"/>
            <circle cx="38" cy="34" r="3" fill="#fff" opacity="0.85"/>
            <circle cx="62" cy="48" r="3.5" fill="#fff" opacity="0.9"/>
            <circle cx="46" cy="58" r="2.5" fill="#fff" opacity="0.82"/>
            <circle cx="54" cy="32" r="2.5" fill="#fff" opacity="0.8"/>

            <!-- 渡船连接线 -->
            <line x1="42" y1="44" x2="58" y2="40" stroke="#fff" stroke-width="1.2" opacity="0.4"/>
            <line x1="42" y1="44" x2="50" y2="54" stroke="#fff" stroke-width="1.2" opacity="0.35"/>
            <line x1="58" y1="40" x2="50" y2="54" stroke="#fff" stroke-width="1.2" opacity="0.35"/>
            <line x1="38" y1="34" x2="58" y2="40" stroke="#fff" stroke-width="1" opacity="0.3"/>
            <line x1="62" y1="48" x2="50" y2="54" stroke="#fff" stroke-width="1" opacity="0.3"/>
            <line x1="46" y1="58" x2="50" y2="54" stroke="#fff" stroke-width="1" opacity="0.25"/>
            <line x1="38" y1="34" x2="42" y2="44" stroke="#fff" stroke-width="0.8" opacity="0.25"/>

            <!-- 回响光晕 -->
            <circle cx="50" cy="45" r="30" fill="none" stroke="#fff" stroke-width="1" opacity="0.15"/>
            <circle cx="50" cy="45" r="36" fill="none" stroke="#fff" stroke-width="0.6" opacity="0.1"/>

            <defs>
              <linearGradient id="waterGradientLarge" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#c5e3ff"/>
                <stop offset="50%" stop-color="#d4a5ff"/>
                <stop offset="100%" stop-color="#ffb6d9"/>
              </linearGradient>
              <linearGradient id="islandGradientLarge" x1="0%" y1="0%" x2="0%" y2="100%">
                <stop offset="0%" stop-color="#f0d0f0"/>
                <stop offset="30%" stop-color="#e0b8e8"/>
                <stop offset="60%" stop-color="#d4a5ff"/>
                <stop offset="100%" stop-color="#b794e0"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1>豆屿温柔集</h1>
        <p class="subtitle">温柔时光 · 静候君至</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.remember">记住密码</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" @click="handleLogin" :loading="loading">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 生成粒子样式
const getParticleStyle = (_index: number) => {
  const size = Math.random() * 60 + 20
  const left = Math.random() * 100
  const delay = Math.random() * 5
  const duration = Math.random() * 10 + 15
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}

// 初始化：从localStorage读取记住的密码
onMounted(() => {
  const savedUsername = localStorage.getItem('remembered_username')
  const savedPassword = localStorage.getItem('remembered_password')
  if (savedUsername && savedPassword) {
    form.username = savedUsername
    form.password = savedPassword
    form.remember = true
  }
})

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 调用登录接口
    await userStore.login(form.username, form.password)

    // 记住密码
    if (form.remember) {
      localStorage.setItem('remembered_username', form.username)
      localStorage.setItem('remembered_password', form.password)
    } else {
      localStorage.removeItem('remembered_username')
      localStorage.removeItem('remembered_password')
    }

    ElMessage.success('登录成功')
    router.push('/')
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

/* 渐变背景 */
.bg-gradient {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg,
    #ffeef8 0%,
    #f3e7ff 25%,
    #e8f4ff 50%,
    #fef3f0 75%,
    #f0f7ff 100%
  );
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

/* 漂浮粒子 */
.bg-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.particle {
  position: absolute;
  bottom: -100px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 182, 193, 0.6) 0%, rgba(255, 182, 193, 0) 70%);
  animation: floatUp linear infinite;
}

@keyframes floatUp {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 0.8;
  }
  90% {
    opacity: 0.8;
  }
  100% {
    transform: translateY(-110vh) rotate(720deg);
    opacity: 0;
  }
}

/* 波浪效果 */
.bg-waves {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 200%;
  height: 100%;
  background-repeat: repeat-x;
  opacity: 0.3;
}

.wave1 {
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%23d4a5ff' fill-opacity='0.4' d='M0,160L48,176C96,192,192,224,288,213.3C384,203,480,149,576,138.7C672,128,768,160,864,186.7C960,213,1056,235,1152,218.7C1248,203,1344,149,1392,122.7L1440,96L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") repeat-x;
  background-size: 50% 100%;
  animation: waveMove 20s linear infinite;
}

.wave2 {
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%23ffb6d9' fill-opacity='0.3' d='M0,96L48,128C96,160,192,224,288,240C384,256,480,224,576,197.3C672,171,768,149,864,165.3C960,181,1056,235,1152,234.7C1248,235,1344,181,1392,154.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") repeat-x;
  background-size: 50% 100%;
  animation: waveMove 15s linear infinite reverse;
}

.wave3 {
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1440 320'%3E%3Cpath fill='%23c5e3ff' fill-opacity='0.2' d='M0,192L48,197.3C96,203,192,213,288,229.3C384,245,480,267,576,250.7C672,235,768,181,864,181.3C960,181,1056,235,1152,234.7C1248,235,1344,181,1392,154.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z'%3E%3C/path%3E%3C/svg%3E") repeat-x;
  background-size: 50% 100%;
  animation: waveMove 25s linear infinite;
}

@keyframes waveMove {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

/* 登录框 */
.login-box {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: 50px 45px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow:
    0 8px 32px rgba(180, 150, 255, 0.15),
    0 2px 8px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.6);
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 标题 */
.login-title {
  text-align: center;
  margin-bottom: 40px;
}

.logo-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.logo-large {
  width: 80px;
  height: 80px;
  animation: logoGentleFloat 4s ease-in-out infinite;
  filter: drop-shadow(0 4px 12px rgba(212, 165, 255, 0.3));
}

@keyframes logoGentleFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  25% {
    transform: translateY(-8px) rotate(2deg);
  }
  50% {
    transform: translateY(-4px) rotate(0deg);
  }
  75% {
    transform: translateY(-8px) rotate(-2deg);
  }
}

.login-title h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 600;
  background: linear-gradient(135deg, #d4a5ff 0%, #ffb6d9 50%, #c5e3ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
  animation: titleGlow 3s ease-in-out infinite;
}

@keyframes titleGlow {
  0%, 100% {
    filter: drop-shadow(0 0 10px rgba(212, 165, 255, 0.3));
  }
  50% {
    filter: drop-shadow(0 0 20px rgba(255, 182, 217, 0.5));
  }
}

.subtitle {
  margin-top: 12px;
  font-size: 14px;
  color: #999;
  letter-spacing: 4px;
  font-weight: 300;
}

/* 表单样式 */
:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(212, 165, 255, 0.3);
  box-shadow: 0 2px 8px rgba(212, 165, 255, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: rgba(212, 165, 255, 0.5);
  box-shadow: 0 4px 12px rgba(212, 165, 255, 0.2);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #d4a5ff;
  box-shadow: 0 4px 16px rgba(212, 165, 255, 0.3);
}

:deep(.el-input__inner) {
  color: #333;
}

:deep(.el-input__inner::placeholder) {
  color: #aaa;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  letter-spacing: 8px;
  font-weight: 500;
  background: linear-gradient(135deg, #d4a5ff 0%, #ffb6d9 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(212, 165, 255, 0.4);
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(212, 165, 255, 0.5);
}

.login-btn:active {
  transform: translateY(0);
}

/* 记住密码 */
:deep(.el-checkbox__label) {
  color: #666;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #d4a5ff;
  border-color: #d4a5ff;
}
</style>
