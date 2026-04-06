#!/bin/bash

################################################################################
# 品多计时器服务启动脚本
# @author wuci
# @date 2026-04-06
################################################################################

# 设置脚本在出错时退出
set -e

# ==================== 配置区域 ====================

# 应用名称
APP_NAME="pd-server"

# JAR包名称
JAR_NAME="pdsystem-server-1.0.0.jar"

# 配置文件路径（外部配置文件）
CONFIG_FILE="config/bootstrap.yml"

# 静态资源目录（外部静态资源，如前端打包后的文件）
STATIC_RESOURCE_DIR="config/static"

# Nacos配置（需要检查的Nacos地址）
NACOS_SERVER_ADDR="8.130.49.28:8848"

# 是否跳过Nacos检查（设置为true可跳过检查，用于本地开发）
SKIP_NACOS_CHECK="${SKIP_NACOS_CHECK:-false}"

# JVM参数配置
JVM_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8"

# Spring Boot参数
SPRING_OPTS="--spring.profiles.active=prod"

# 日志目录
LOG_DIR="./logs"

# PID文件
PID_FILE="$APP_NAME.pid"

# ==================== 函数定义 ====================

# 打印信息
print_info() {
    echo -e "\033[32m[INFO]\033[0m $1"
}

# 打印警告
print_warn() {
    echo -e "\033[33m[WARN]\033[0m $1"
}

# 打印错误
print_error() {
    echo -e "\033[31m[ERROR]\033[0m $1"
}

# 检查JDK
check_jdk() {
    if [ -z "$JAVA_HOME" ]; then
        JAVA_CMD="java"
    else
        JAVA_CMD="$JAVA_HOME/bin/java"
    fi

    if ! command -v $JAVA_CMD &> /dev/null; then
        print_error "未找到Java运行环境，请先安装JDK"
        exit 1
    fi

    # 获取Java版本信息
    JAVA_VERSION_FULL=$($JAVA_CMD -version 2>&1 | head -n 1)

    # 解析版本号（兼容 JDK 8 的 1.8.x 格式和 JDK 9+ 的 x.x.x 格式）
    JAVA_VERSION=$($JAVA_CMD -version 2>&1 | awk -F '"' '/version/ {print $2}')

    # JDK 8 版本格式为 1.8.x_，提取主版本号
    if [[ $JAVA_VERSION == 1.* ]]; then
        MAJOR_VERSION=$(echo $JAVA_VERSION | awk -F '.' '{print $2}')
    else
        MAJOR_VERSION=$(echo $JAVA_VERSION | awk -F '.' '{print $1}')
    fi

    # 检查最低版本要求为 JDK 8
    if [ "$MAJOR_VERSION" -lt 8 ]; then
        print_error "需要JDK 8或更高版本"
        exit 1
    fi

    print_info "Java版本: $JAVA_VERSION_FULL"
}

# 检查JAR包
check_jar() {
    if [ ! -f "$JAR_NAME" ]; then
        print_error "JAR包不存在: $JAR_NAME"
        exit 1
    fi
    print_info "JAR包: $JAR_NAME"
}

# 检查Nacos服务
check_nacos() {
    # 如果设置了跳过检查，则直接返回
    if [ "$SKIP_NACOS_CHECK" = "true" ]; then
        print_warn "已跳过Nacos服务检查 (SKIP_NACOS_CHECK=true)"
        return 0
    fi

    print_info "检查Nacos服务: $NACOS_SERVER_ADDR"

    # 分离主机和端口
    NACOS_HOST=$(echo $NACOS_SERVER_ADDR | cut -d':' -f1)
    NACOS_PORT=$(echo $NACOS_SERVER_ADDR | cut -d':' -f2)

    # 使用nc检查端口
    if command -v nc &> /dev/null; then
        if nc -z -w 5 $NACOS_HOST $NACOS_PORT 2>/dev/null; then
            print_info "Nacos服务可用"
            return 0
        fi
    # 使用telnet检查端口
    elif command -v telnet &> /dev/null; then
        if timeout 5 telnet $NACOS_HOST $NACOS_PORT 2>/dev/null | grep -q "Connected"; then
            print_info "Nacos服务可用"
            return 0
        fi
    # 使用curl检查Nacos HTTP API
    else
        if curl -s --connect-timeout 5 "http://$NACOS_SERVER_ADDR/nacos/v1/console/health/readiness" > /dev/null 2>&1; then
            print_info "Nacos服务可用"
            return 0
        fi
    fi

    print_error "Nacos服务不可用: $NACOS_SERVER_ADDR"
    print_error "请先启动Nacos服务后再启动应用"
    print_error "如需跳过Nacos检查，可设置环境变量: export SKIP_NACOS_CHECK=true"
    exit 1
}

# 检查配置文件
check_config() {
    if [ ! -f "$CONFIG_FILE" ]; then
        print_warn "外部配置文件不存在: $CONFIG_FILE"
        print_warn "将使用JAR包内置配置和Nacos配置中心"
        SPRING_OPTS="$SPRING_OPTS"
    else
        print_info "使用外部配置文件: $CONFIG_FILE"
        SPRING_OPTS="$SPRING_OPTS --spring.config.location=$CONFIG_FILE"
    fi
}

# 检查静态资源目录
check_static_resources() {
    if [ ! -d "$STATIC_RESOURCE_DIR" ]; then
        print_warn "外部静态资源目录不存在: $STATIC_RESOURCE_DIR"
        print_warn "将使用JAR包内置静态资源"
        return 0
    fi

    # 检查目录是否为空
    if [ -z "$(ls -A $STATIC_RESOURCE_DIR 2>/dev/null)" ]; then
        print_warn "外部静态资源目录为空: $STATIC_RESOURCE_DIR"
        print_warn "将使用JAR包内置静态资源"
        return 0
    fi

    print_info "使用外部静态资源目录: $STATIC_RESOURCE_DIR"
    # 使用file://协议指定外部静态资源位置
    SPRING_OPTS="$SPRING_OPTS --spring.web.resources.static-locations=file:$STATIC_RESOURCE_DIR"
    return 0
}

# 创建日志目录
create_log_dir() {
    if [ ! -d "$LOG_DIR" ]; then
        mkdir -p "$LOG_DIR"
        print_info "创建日志目录: $LOG_DIR"
    fi
}

# 启动应用
start() {
    # 检查是否已运行
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null 2>&1; then
            print_warn "应用已在运行中 (PID: $PID)"
            return 0
        else
            print_warn "PID文件存在但进程不存在，清理PID文件"
            rm -f "$PID_FILE"
        fi
    fi

    # 检查Nacos服务（如果未禁用检查）
    check_nacos

    print_info "开始启动 $APP_NAME ..."

    # 启动应用
    nohup $JAVA_CMD $JVM_OPTS \
        -jar $JAR_NAME \
        $SPRING_OPTS \
        > "$LOG_DIR/console.log" 2>&1 &

    PID=$!
    echo $PID > "$PID_FILE"

    # 等待启动
    sleep 3

    # 检查进程是否仍在运行
    if ps -p $PID > /dev/null 2>&1; then
        print_info "启动成功! PID: $PID"
        print_info "日志文件: $LOG_DIR/console.log"
        print_info "查看日志: tail -f $LOG_DIR/console.log"
    else
        print_error "启动失败，请查看日志: $LOG_DIR/console.log"
        rm -f "$PID_FILE"
        exit 1
    fi
}

# 停止应用
stop() {
    if [ ! -f "$PID_FILE" ]; then
        print_warn "PID文件不存在，应用可能未运行"
        return 0
    fi

    PID=$(cat "$PID_FILE")

    if ! ps -p $PID > /dev/null 2>&1; then
        print_warn "进程不存在 (PID: $PID)"
        rm -f "$PID_FILE"
        return 0
    fi

    print_info "停止 $APP_NAME (PID: $PID) ..."

    kill $PID

    # 等待进程结束
    for i in {1..30}; do
        if ! ps -p $PID > /dev/null 2>&1; then
            print_info "应用已停止"
            rm -f "$PID_FILE"
            return 0
        fi
        sleep 1
    done

    # 强制杀死进程
    print_warn "强制停止应用..."
    kill -9 $PID
    rm -f "$PID_FILE"
    print_info "应用已强制停止"
}

# 重启应用
restart() {
    stop
    sleep 2
    start
}

# 查看状态
status() {
    if [ ! -f "$PID_FILE" ]; then
        print_info "应用未运行"
        return 1
    fi

    PID=$(cat "$PID_FILE")

    if ps -p $PID > /dev/null 2>&1; then
        print_info "应用正在运行 (PID: $PID)"
        return 0
    else
        print_info "应用未运行 (PID文件存在但进程不存在)"
        return 1
    fi
}

# 查看日志
logs() {
    if [ -f "$LOG_DIR/console.log" ]; then
        tail -f "$LOG_DIR/console.log"
    else
        print_warn "日志文件不存在: $LOG_DIR/console.log"
    fi
}

# 打印帮助信息
usage() {
    echo "用法: $0 {start|stop|restart|status|logs}"
    echo ""
    echo "命令说明:"
    echo "  start   - 启动应用（会检查Nacos服务是否可用）"
    echo "  stop    - 停止应用"
    echo "  restart - 重启应用"
    echo "  status  - 查看应用状态"
    echo "  logs    - 查看应用日志（实时）"
    echo ""
    echo "Nacos检查:"
    echo "  启动前会自动检查Nacos服务: $NACOS_SERVER_ADDR"
    echo "  如Nacos不可用，启动将失败"
    echo "  跳过检查: export SKIP_NACOS_CHECK=true && ./start.sh start"
    echo ""
    echo "配置说明:"
    echo "  外部配置文件路径: $CONFIG_FILE"
    echo "  外部静态资源目录: $STATIC_RESOURCE_DIR"
    echo "  如需使用外部配置/静态资源，请创建相应的文件/目录并启动应用"
}

# ==================== 主程序 ====================

# 解析命令行参数
case "$1" in
    start)
        check_jdk
        check_jar
        check_config
        check_static_resources
        create_log_dir
        start
        ;;
    stop)
        stop
        ;;
    restart)
        check_jdk
        check_jar
        check_config
        check_static_resources
        create_log_dir
        restart
        ;;
    status)
        status
        ;;
    logs)
        logs
        ;;
    *)
        usage
        exit 1
        ;;
esac

exit 0
