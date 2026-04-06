@echo off
REM ================================================================================
REM 品多计时器服务停止脚本 (Windows)
REM @author wuci
REM @date 2026-04-06
REM ================================================================================

setlocal enabledelayedexpansion

REM ==================== 配置区域 ====================
set LOG_DIR=.\logs
set PID_FILE=%LOG_DIR%\app.pid
set JAR_NAME=pdsystem-server-1.0.0.jar

REM ==================== 函数定义 ====================

REM 停止服务
:stop_service
echo [INFO] 正在停止品多计时器服务...

REM 方式1: 使用PID文件
if exist "%PID_FILE%" (
    set /p PID=<%PID_FILE%
    echo [INFO] 从PID文件读取: !PID!

    REM 检查进程是否存在
    tasklist /FI "PID eq !PID!" 2>nul | find "!PID!" >nul
    if !errorlevel! equ 0 (
        echo [INFO] 正在终止进程 !PID!...
        taskkill /PID !PID! /F >nul 2>&1
        timeout /t 2 /nobreak >nul
        del "%PID_FILE%"
        echo [INFO] 服务已停止
        exit /b 0
    ) else (
        echo [WARN] PID !PID! 对应的进程不存在
        del "%PID_FILE%"
    )
)

REM 方式2: 通过窗口标题查找进程
echo [INFO] 正在查找运行中的 Bpd-Server 窗口...
for /f "tokens=2" %%i in ('tasklist /FI "WINDOWTITLE eq Bpd-Server*" /FO CSV 2^>nul ^| find "java.exe"') do (
    set PID=%%~i
    set PID=!PID:"=!
    taskkill /PID !PID! /F >nul 2>&1
    if !errorlevel! equ 0 (
        echo [INFO] 已终止进程 !PID!
    )
)

REM 方式3: 通过JAR名称查找所有java进程并尝试终止
echo [INFO] 正在查找运行中的 %JAR_NAME% 进程...
wmic process where "commandline like '%%pdsystem-server-1.0.0.jar%%'" delete >nul 2>&1

echo [INFO] 服务停止完成
goto :eof

REM ==================== 主程序 ====================
call :stop_service

endlocal
