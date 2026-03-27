@echo off
chcp 65001 >nul
echo ========================================
echo  拼豆计时系统 - 数据库初始化脚本
echo ========================================
echo.

set DB_USER=root
set DB_PASS=123456
set DB_NAME=pindou_timer
set SQL_DIR=%~dp0

echo [1/4] 检查MySQL服务...
mysql -u %DB_USER% -p%DB_PASS% -e "SELECT VERSION();" >nul 2>&1
if errorlevel 1 (
    echo [错误] 无法连接到MySQL，请检查：
    echo   1. MySQL服务是否已启动
    echo   2. 用户名和密码是否正确
    echo   3. MySQL是否已安装
    pause
    exit /b 1
)
echo       MySQL连接成功
echo.

echo [2/4] 创建数据库...
mysql -u %DB_USER% -p%DB_PASS% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo       数据库 %DB_NAME% 创建成功
echo.

echo [3/4] 导入建表脚本...
mysql -u %DB_USER% -p%DB_PASS% %DB_NAME% < "%SQL_DIR%\schema.sql"
if errorlevel 1 (
    echo [错误] 建表脚本导入失败
    pause
    exit /b 1
)
echo       建表脚本导入成功
echo.

echo [4/4] 导入初始数据...
mysql -u %DB_USER% -p%DB_PASS% %DB_NAME% < "%SQL_DIR%\data.sql"
if errorlevel 1 (
    echo [错误] 初始数据导入失败
    pause
    exit /b 1
)
echo       初始数据导入成功
echo.

echo ========================================
echo  数据库初始化完成！
echo ========================================
echo.
echo 数据库名称: %DB_NAME%
echo.
echo 默认登录账号：
echo   超级管理员: admin / 123456
echo   店长: manager / 123456
echo   店员: staff / 123456
echo.
echo 接口文档: http://localhost:8080/api/doc.html
echo Druid监控: http://localhost:8080/api/druid/
echo ========================================
echo.
pause
