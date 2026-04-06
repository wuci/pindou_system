#!/bin/bash
# ================================================================================
# 品多计时器服务后台启动脚本 (Linux/Mac)
# @author wuci
# @date 2026-04-06
# ================================================================================

# ==================== 配置区域 ====================
JAR_NAME="pdsystem-server-1.0.0.jar"
CONFIG_DIR="./config"
PID_FILE="./app.pid"

# JVM参数
JVM_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8"

# ==================== 函数定义 ====================

# 检查服务是否已运行
check_running() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            echo "[WARN] 服务已在运行中, PID: $PID"
            return 1
        else
            # PID文件存在但进程不存在，删除旧文件
            rm -f "$PID_FILE"
        fi
    fi
    return 0
}

# 启动服务
start_service() {
    echo "[INFO] 正在启动品多计时器服务..."
    echo "[INFO] JAR包: $JAR_NAME"

    # 后台启动，不输出日志
    nohup java $JVM_OPTS \
        -Dspring.cloud.nacos.config.enabled=false \
        -Dspring.config.additional-location=./config/ \
        -jar $JAR_NAME \
        > /dev/null 2>&1 &

    # 保存PID
    echo $! > "$PID_FILE"

    echo "[INFO] 服务启动成功! PID: $!"
    echo "[INFO] 客户端地址: http://localhost:9026/"
    echo "[INFO] 接口文档: http://localhost:9026/api/doc.html"
    echo "[INFO] Druid监控: http://localhost:9026/api/druid/"
    echo ""
    echo "[INFO] 停止服务: ./stop-server.sh"
}

# ==================== 主程序 ====================
main() {
    # 切换到脚本所在目录
    cd "$(dirname "$0")" || exit 1

    check_running || exit 1
    create_log_dir
    start_service
}

main "$@"
