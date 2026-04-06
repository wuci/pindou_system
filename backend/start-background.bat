@echo off
REM ================================================================================
REM 品多计时器服务后台启动脚本 (Windows)
REM @author wuci
REM @date 2026-04-06
REM ================================================================================

setlocal enabledelayedexpansion

REM ==================== 配置区域 ====================
set JAR_NAME=pdsystem-server-1.0.0.jar
set LOG_DIR=.\logs
set LOG_FILE=%LOG_DIR%\application.log
set PID_FILE=%LOG_DIR%\app.pid

REM JVM参数
set JVM_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8

REM ==================== 函数定义 ====================

REM 检查服务是否已运行
:check_running
if exist "%PID_FILE%" (
    set /p PID=<%PID_FILE%
    tasklist /FI "PID eq !PID!" 2>nul | find "!PID!" >nul
    if !errorlevel! equ 0 (
        echo [WARN] 服务已在运行中, PID: !PID!
        exit /b 1
    )
    REM PID文件存在但进程不存在，删除旧文件
    del "%PID_FILE%"
)
goto :eof

REM 创建日志目录
:create_log_dir
if not exist "%LOG_DIR%" (
    mkdir "%LOG_DIR%"
    echo [INFO] 创建日志目录: %LOG_DIR%
)
goto :eof

REM 启动服务
:start_service
echo [INFO] 正在启动品多计时器服务...
echo [INFO] JAR包: %JAR_NAME%
echo [INFO] 日志文件: %LOG_FILE%

REM 使用 start 命令后台启动，输出到日志文件
start "Bpd-Server" java %JVM_OPTS% ^
    -Dspring.cloud.nacos.config.enabled=false ^
    -Dspring.config.additional-location=./config/ ^
    -jar %JAR_NAME% ^
    >> "%LOG_FILE%" 2>&1

REM 等待一下让进程启动
timeout /t 3 /nobreak >nul

echo [INFO] 服务启动成功!
echo [INFO] 客户端地址: http://localhost:9026/
echo [INFO] 接口文档: http://localhost:9026/api/doc.html
echo [INFO] Druid监控: http://localhost:9026/api/druid/
echo.
echo [INFO] 查看日志: type %LOG_FILE%
echo [INFO] 停止服务: stop.bat
goto :eof

REM ==================== 主程序 ====================
call :check_running
if !errorlevel! neq 0 exit /b 1

call :create_log_dir
call :start_service

endlocal
