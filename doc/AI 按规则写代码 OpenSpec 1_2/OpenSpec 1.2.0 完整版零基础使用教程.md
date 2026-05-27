# OpenSpec 1\.2\.0 完整版零基础使用教程

## 一、OpenSpec 简介

### 1\.1 什么是 OpenSpec

OpenSpec 是由 Fission\-AI 推出的**轻量级规范驱动AI开发框架（SDD）**，以CLI命令行工具为载体，适配Cursor、Claude Code、GitHub Copilot等主流AI编程工具。

简单直白定义：OpenSpec是专门解决AI编程失忆、随意开发、需求错位问题的工具，将聊天中碎片化的口头需求，转化为项目内标准化的书面规范，让AI严格按照既定规则开发，从根源减少代码返工、需求歧义问题。

### 1\.2 核心开发理念（SDD规范驱动开发）

官方核心口号：**Align before code，先对齐需求，再编写代码**。摒弃传统AI编程“先写代码，后期调整”的模式，固定标准化开发流程：提案→制定规范→技术设计→拆解任务→代码开发→归档沉淀。

### 1\.3 核心优势

- 无侵入性：不绑定编程语言、开发框架，适配所有类型项目

- 离线可用：本地运行，无需配置第三方API密钥

- 易维护：所有规范文档均为Markdown格式，完美适配Git版本控制

- 解决行业痛点：杜绝AI失忆、需求理解偏差、会话重置上下文丢失等问题；适配单人开发与团队协作场景

### 1\.4 版本说明

本文档适配版本：**OpenSpec 1\.2\.0**，该版本为稳定正式版，所有操作命令向下兼容1\.2\.x系列迭代版本；官方暂无中文官网，仅提供英文原版文档，本教程为整合官方文档\+实战经验的中文完整版手册。

## 二、环境安装

### 2\.1 前置环境要求

设备必须安装 **Node\.js 20\.19\.0及以上版本**，配套包管理器支持：npm、pnpm、yarn、bun。

### 2\.2 全局安装（原版英文）

```bash
# npm全局安装指定1.2.0版本（推荐）
npm install -g @fission-ai/openspec@1.2.0

# 校验是否安装成功
openspec --version
# 正常输出：1.2.0

```

### 2\.3 社区中文增强版（可选）

社区推出汉化版本，所有CLI命令为中文，完全兼容原版1\.2\.0所有功能，适合零基础新手：

```bash
npm install -g @zhspec/openspec-zh

# 使用方式
openspec-chinese proposal "新增用户登录功能"

```

### 2\.4 官方资源地址

- 官方官网：https://openspec\.dev/

- 官方英文文档：https://openspec\.dev/docs

- 官方开源仓库：https://github\.com/Fission\-AI/OpenSpec

- 中文社区仓库：https://github\.com/Teatime\-AI/OpenSpec\-Chinese

## 三、项目初始化

每个新项目使用OpenSpec前，必须执行初始化命令，生成专属目录结构。

```bash
# 切换至你的项目根目录
cd 你的项目文件夹路径

# 执行初始化
openspec init

```

初始化过程为交互式操作，根据提示选择日常使用的AI编程工具（Claude Code、Cursor、Copilot等）。执行完毕后，项目自动生成openspec目录，目录结构如下：

```Plain Text
openspec/
├── specs/          # 项目全局永久规范（所有归档后的功能统一存放）
└── changes/        # 功能变更目录，所有待开发功能存放此处
    └── 自定义功能名/
        ├── proposal.md  # 功能提案：描述开发目的、业务需求、核心功能
        ├── design.md    # 技术设计：接口方案、数据库表结构、技术选型
        ├── specs/       # 当前功能专属细化规范文档
        └── tasks.md     # 开发任务清单：AI根据清单分步完成编码

```

## 四、核心工作流（标准开发流程）

OpenSpec 1\.2\.0固定六大开发步骤，单人/团队开发统一遵循，是该工具的核心使用逻辑。

### 4\.1 创建功能提案（Proposal）

用于新建开发需求，定义功能：为什么做、需要实现什么效果。支持CLI命令、AI斜杠命令两种调用方式。

```bash
# 1. CLI命令创建
openspec proposal "新增用户登录注册功能"
```

```plain
# 2. AI编辑器斜杠命令（Cursor/Claude Code专用）
/opsx:proposal 新增用户登录注册功能
```

### 4\.2 查看所有功能变更

快速查看项目内所有未归档的开发功能，同步展示每个功能的开发进度。

```bash
openspec list

```

### 4\.3 查看单个功能详情

查看指定功能的提案、设计方案、规范、任务清单全部内容。

```bash
# 格式：openspec show 功能文件夹名
openspec show add-user-login

```

### 4\.4 校验规范文件

自动检测md文档格式、参数依赖、书写规范是否合规，提前规避开发报错。

```bash
openspec validate add-user-login

```

### 4\.5 AI执行开发任务

完成提案、设计、规范编写后，直接在AI编程工具内下发指令，AI自动读取对应目录下的specs规范与tasks任务清单，严格按照要求编写、修改代码。

通用指令：**请实施【功能名称】变更，严格遵循openspec目录下的所有规范文档**

### 4\.6 归档完成功能（Archive）

功能开发测试完成后，执行归档命令：将当前功能的所有规范，合并存入全局specs目录，同时将该功能移入archive归档文件夹，成为项目永久文档。

```bash
# 静默归档（无需二次确认，推荐）
openspec archive add-user-login --yes

```

```plain
# AI斜杠命令快速归档
/opsx:archive

```

## 五、全局命令速查表

|CLI命令|功能详细描述|
|---|---|
|openspec \-\-version|查看当前安装的OpenSpec版本|
|openspec init|初始化项目，生成完整openspec目录结构|
|openspec proposal \&\#34;功能名\&\#34;|创建全新的功能开发提案|
|openspec list|列出项目内所有未归档的功能变更|
|openspec show 功能名|查看指定功能全套文档与开发进度|
|openspec validate 功能名|校验功能下所有规范文档格式合法性|
|openspec archive 功能名|归档已完成的功能，合并至全局规范|
|openspec view|打开交互式可视化仪表板|

## 六、新手使用总结

1. OpenSpec核心定位：AI编程的**需求契约**，用静态Markdown规范约束AI行为，替代碎片化聊天需求；

2. 固定最简开发流程：初始化项目 → 创建提案 → 完善设计与规范 → AI编码开发 → 归档沉淀；

3. 适用人群：零基础开发者、AI编程使用者、小型开发团队，是目前解决AI开发乱象最简单、最轻量的工具。

> （注：文档部分内容可能由 AI 生成）
