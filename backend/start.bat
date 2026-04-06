@echo off
REM ================================================================================
REM 品多计时器服务启动脚本 (Windows)
REM @author wuci
REM @date 2026-04-06
REM ================================================================================

setlocal enabledelayedexpansion

REM ==================== 配置区域 ====================

set APP_NAME=pd-server
set JAR_NAME=pdsystem-server-1.0.0.jar
set CONFIG_FILE=.\config\application.yml
set LOG_DIR=.\logs

REM JVM参数
set JVM_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8

REM Spring Boot参数
set SPRING_OPTS=--spring.profiles.active=prod

REM ==================== 函数定义 ====================

REM 打印信息
:print_info
echo [INFO] %~1
goto :eof

REM 打印警告
:print_warn
echo [WARN] %~1
goto :eof

REM 打印错误
:print_error
echo [ERROR] %~1
goto :eof

REM 检查Java
:check_java
where java >nul 2>&1
if errorlevel 1 (
    call :print_error "未找到Java运行环境，请先安装JDK"
    exit /b 1
)

REM 检查Java版本
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%i
    set JAVA_VERSION=!JAVA_VERSION:"=!
    for /f "tokens=1,2 delims=." %%a in ("!JAVA_VERSION!") do (
        if %%a lss 17 (
            call :print_error "需要JDK 17或更高版本"
            exit /b 1
        )
    )
    goto :break_version
)
:break_version
call :print_info "Java版本检查通过"
goto :eof

REM 检查JAR包
:check_jar
if not exist "%JAR_NAME%" (
    call :print_error "JAR包不存在: %JAR_NAME%"
    exit /b 1
)
call :print_info "JAR包: %JAR_NAME%"
goto :eof

REM 检查配置文件
:check_config
if exist "%CONFIG_FILE%" (
    call :print_info "使用外部配置文件: %CONFIG_FILE%"
    set SPRING_CONFIG=--spring.config.location=%CONFIG_FILE% --spring.cloud.nacos.config.enabled=false
) else (
    call :print_warn "外部配置文件不存在: %CONFIG_FILE%"
    call :print_warn "将使用JAR包内置配置"
    set SPRING_CONFIG=--spring.cloud.nacos.config.enabled=false
)
goto :eof

REM 创建日志目录
:create_log_dir
if not exist "%LOG_DIR%" (
    mkdir "%LOG_DIR%"
    call :print_info "创建日志目录: %LOG_DIR%"
)
goto :eof

REM 启动应用
:start
call :print_info "开始启动 %APP_NAME% ..."

start "pd-server" java %JVM_OPTS% ^
    -Dspring.config.location=%CONFIG_FILE% ^
    -Dspring.cloud.nacos.config.enabled=false ^
    -jar %JAR_NAME% ^
    %SPRING_OPTS% ^
    %SPRING_CONFIG% ^
    >> "%LOG_DIR%\console.log" 2>&1

call :print_info "启动成功!"
call :print_info "日志文件: %LOG_DIR%\console.log"
goto :eof

REM 打印帮助信息
:usage
echo 用法: %~nx0 {start}
echo.
echo 命令说明:
echo   start - 启动应用
echo.
echo 配置说明:
echo   外部配置文件路径: %CONFIG_FILE%
echo   如需使用外部配置，请创建配置文件并启动应用
goto :eof

REM ==================== 主程序 ====================
if "%1"=="" goto usage
if "%1"=="start" (
    call :check_java
    call :check_jar
    call :check_config
    call :create_log_dir
    call :start
) else (
    goto usage
)

endlocal
