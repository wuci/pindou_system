# 修改记录

## 日期
2026-04-06

## 修改文件
- `src/main/resources/bootstrap.yml`

## 修改内容
修复 Nacos 共享配置的编码问题

### 修改前
```yaml
shared-configs:
  - data-id: pd_common.yaml
    group: DEFAULT_GROUP
    refresh: true
    file-extension: yaml
  - data-id: pd_server.yaml
    group: DEFAULT_GROUP
    refresh: true
    file-extension: yaml
```

### 修改后
```yaml
shared-configs:
  - data-id: pd_common.yaml
    group: DEFAULT_GROUP
    refresh: true
    file-extension: yaml
    encode: UTF-8  # 新增：配置编码格式
  - data-id: pd_server.yaml
    group: DEFAULT_GROUP
    refresh: true
    file-extension: yaml
    encode: UTF-8  # 新增：配置编码格式
```

## 问题原因
Nacos 配置中心返回的 YAML 配置内容存在编码问题，导致 YAML 解析失败。

## 解决方案
为共享配置添加 `encode: UTF-8` 参数，明确指定编码格式。

## 注意事项
- 修改后需要重新编译打包
- 临时解决方案：启动时添加 `--spring.cloud.nacos.config.enabled=false` 参数禁用 Nacos

@author wuci
