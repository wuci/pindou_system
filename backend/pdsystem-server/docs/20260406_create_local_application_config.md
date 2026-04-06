# 修改记录

## 日期
2026-04-06

## 修改内容
新增本地配置文件 application.yml

## 问题原因
项目原完全依赖 Nacos 配置中心，本地没有 application.yml。当禁用 Nacos 后（使用 --spring.cloud.nacos.config.enabled=false），应用无法获取必要的配置项（jwt.secret、数据源、Redis等），导致启动失败。

## 解决方案
创建本地 application.yml 配置文件，包含：
- 数据源配置（MySQL）
- Redis集群配置
- JWT配置
- MyBatis-Plus配置
- 日志配置

## 配置来源
从 Nacos 配置中心的 pd_common.yaml 迁移而来

## 注意事项
- 修改后需要重新编译打包
- 密码等敏感信息需在生产环境中修改

@author wuci
