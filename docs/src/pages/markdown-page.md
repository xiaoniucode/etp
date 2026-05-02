---
title: 演示页
description: 独立 Markdown 页面在明/暗主题下的展示说明
---

# 演示页

这是一页使用 **Markdown** 编写的独立页面（不经 React 首页组件），样式完全跟随站点 **Infima / Docusaurus** 的全局主题。

在导航栏中切换 **明色 / 暗色** 时，本页标题、正文与代码块会一并切换；若发现某处对比度异常，优先在 `src/css/custom.css` 中调整 `--ifm-*` 变量，再视需要微调 `src/pages/index.module.css` 中仅作用于首页的 `.page` 选择器。

- [返回文档首页](/)
- [项目概述](/docs/overview)
