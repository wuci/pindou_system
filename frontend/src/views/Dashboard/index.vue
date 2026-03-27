<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409eff">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">今日营收</div>
              <div class="stat-value">¥0.00</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">使用中桌台</div>
              <div class="stat-value">0 / 20</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">今日订单</div>
              <div class="stat-value">0</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-title">翻台率</div>
              <div class="stat-value">0%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span>欢迎来到拼豆店计时管理系统</span>
          </template>
          <div class="welcome-content">
            <h3>👋 欢迎使用拼豆店计时管理系统</h3>
            <p>系统正在初始化中，请稍候...</p>
            <p>Day 1 项目初始化已完成 ✅</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
// 工作台首页
</script>

<style scoped lang="scss">
.dashboard {
  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 24px;
    }

    .stat-content {
      flex: 1;

      .stat-title {
        font-size: 14px;
        color: #999;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #333;
      }
    }
  }

  .welcome-content {
    text-align: center;
    padding: 40px 0;

    h3 {
      margin-bottom: 16px;
      font-size: 20px;
      color: #333;
    }

    p {
      margin: 8px 0;
      font-size: 14px;
      color: #666;
    }
  }
}
</style>
