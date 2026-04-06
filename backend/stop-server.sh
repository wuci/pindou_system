#!/bin/bash
# ================================================================================
# 品多计时器服务停止脚本 (Linux/Mac)
# @author wuci
# @date 2026-04-06
# ================================================================================

# ==================== 配置区域 ====================
PID_FILE="./app.pid"
JAR_NAME="pdsystem-server-1.0.0.jar"

# ==================== 函数定义 ====================

# 停止服务
stop_service() {
    echo "[INFO] 正在停止品多计时器服务..."

    # 方式1: 使用PID文件
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        echo "[INFO] 从PID文件读取: $PID"

        if ps -p "$PID" > /dev/null 2>&1; then
            echo "[INFO] 正在终止进程 $PID..."
            kill "$PID" 2>/dev/null
            sleep 2

            # 如果进程还在，强制终止
            if ps -p "$PID" > /dev/null 2>&1; then
                echo "[INFO] 强制终止进程 $PID..."
                kill -9 "$PID" 2>/dev/null
            fi

            rm -f "$PID_FILE"
            echo "[INFO] 服务已停止"
            return 0
        else
            echo "[WARN] PID $PID 对应的进程不存在"
            rm -f "$PID_FILE"
        fi
    fi

    # 方式2: 通过JAR名称查找进程
    echo "[INFO] 正在查找运行中的 $JAR_NAME 进程..."
    PIDS=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')

    if [ -n "$PIDS" ]; then
        for PID in $PIDS; do
            echo "[INFO] 正在终止进程 $PID..."
            kill "$PID" 2>/dev/null
        done
        sleep 1

        # 清理残留进程
        PIDS=$(ps aux | grep "$JAR_NAME" | grep -v grep | awk '{print $2}')
        if [ -n "$PIDS" ]; then
            for PID in $PIDS; do
                echo "[INFO] 强制终止进程 $PID..."
                kill -9 "$PID" 2>/dev/null
            done
        fi

        echo "[INFO] 服务已停止"
    else
        echo "[INFO] 未找到运行中的服务"
    fi
}

# ==================== 主程序 ====================
main() {
    # 切换到脚本所在目录
    cd "$(dirname "$0")" || exit 1
    stop_service
}

main "$@"
