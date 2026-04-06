# 修复 java -jar 启动报错

## 日期
2026-04-06

## 问题描述
使用 `java -jar` 启动时报错，而本地 IDE 启动正常。

### 错误信息
```
org.yaml.snakeyaml.error.YAMLException: java.nio.charset.MalformedInputException: Input length = 1
Could not resolve placeholder 'jwt.secret'
```

## 根本原因
Nacos 配置中心中的 `pd_common.yaml` 包含中文注释，当使用 `java -jar` 启动时，JVM 默认使用系统编码（Windows 下为 GBK），导致 YAML 解析器无法正确解析 UTF-8 编码的中文配置。

## 解决方案
在启动脚本中添加 JVM 编码参数 `-Dfile.encoding=UTF-8`，确保使用 UTF-8 编码解析配置文件。

## 修改文件

### 1. start.sh
```bash
# 修改前
JVM_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 修改后
JVM_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8"
```

### 2. start.bat
```bat
REM 修改前
set JVM_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200

REM 修改后
set JVM_OPTS=-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dfile.encoding=UTF-8
```

## 验证
使用以下命令启动已验证成功：
```bash
java -Dfile.encoding=UTF-8 -jar pdsystem-server-1.0.0.jar
```

应用成功启动，Nacos 配置正确加载，无 YAML 解析错误。

## 相关说明
- 本地 IDE 启动正常是因为 IDE 默认使用 UTF-8 编码
- Windows 命令行默认使用 GBK 编码，导致直接 `java -jar` 启动失败
- 添加 `-Dfile.encoding=UTF-8` 参数后，JVM 统一使用 UTF-8 编码处理文件和配置

## 作者
wuci
