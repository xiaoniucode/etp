---
sidebar_position: 12
title: 常见问题
description: 安装运行与使用中的常见问题
---

# 常见问题

## 1、Linux二进制运行出现类似`GNU libc2.32`错误解决方案


:::tip 提示

`orbien`运行环境要求`Linux`系统中`GNU libc`版本为`2.34`及以上版本，对于CentOS-7 低版本系统并天然不支持，需要升级GNU libc库。

:::

解决方案：

- 升级`GNU libc` 到`2.34`
- 采用`高版本`的Linux服务器

## 客户端无法连接到Orbien-Server

如果服务端`删除`了已经认证的客户端，那么下次客户端再次连接的时候就需要先删除本地身份标识`/path/to/.orbien/agent.id`，然后再重新连接。