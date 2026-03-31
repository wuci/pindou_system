# 修复布局保存created_at不能为null问题

## 问题描述
保存布局配置时，后台报错"Column 'created_at' cannot be null"

## 问题原因
TableLayoutConfig实体类中的created_at和updated_at字段使用了FieldFill.INSERT和FieldFill.INSERT_UPDATE注解，但项目中缺少MyBatis-Plus的MetaObjectHandler配置来处理自动填充。

## 解决方案
在pdsystem-server模块的config目录下创建MyBatisPlusConfig类，实现MetaObjectHandler接口，处理插入和更新时的自动填充逻辑。

## 修改文件
- 新增文件：backend/pdsystem-server/src/main/java/com/pindou/timer/config/MyBatisPlusConfig.java

## 作者
@wuci
@date 2026-03-30
