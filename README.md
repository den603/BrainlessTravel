<div align="center">

# 🧠 BrainlessTravel — AI 智能旅游规划小程序

<p>
  <img src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white" alt="Java">
  <img src="https://img.shields.io/badge/SpringBoot-3.2.5-6DB33F?logo=springboot&logoColor=white" alt="SpringBoot">
  <img src="https://img.shields.io/badge/SpringCloud-2023.0.3-6DB33F?logo=spring&logoColor=white" alt="SpringCloud">
  <img src="https://img.shields.io/badge/Nacos-2.x-1E90FF?logo=alibabacloud&logoColor=white" alt="Nacos">
  <img src="https://img.shields.io/badge/LangChain4j-0.36.0-FF6F00?logo=langchain&logoColor=white" alt="LangChain4j">
  <img src="https://img.shields.io/badge/RAG-BGE--small--zh--v1.5-A855F7" alt="RAG">
  <img src="https://img.shields.io/badge/UniApp-Vue3-42b883?logo=vue.js&logoColor=white" alt="UniApp">
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-5.0+-DC382D?logo=redis&logoColor=white" alt="Redis">
  <img src="https://img.shields.io/badge/MinIO-ObjectStorage-C72E49?logo=minio&logoColor=white" alt="MinIO">
  <img src="https://img.shields.io/badge/讯飞星火-AI大模型-1E90FF?logo=openai&logoColor=white" alt="Spark">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</p>

<p><b>Spring Cloud 微服务 + LangChain4j RAG 知识库 + 讯飞星火大模型 + UniApp 微信小程序</b></p>
<p>前后端分离 · 单仓库管理 · 网关统一鉴权 · 本地中文向量模型 · 开箱即用</p>
<p>适合课程设计 / 毕设 / 微服务与 RAG 技术学习 / 二次开发基础框架</p>

</div>

---

## 📑 目录

- [项目简介](#-项目简介)
- [功能特性](#-功能特性)
- [界面预览](#-界面预览)
- [技术栈](#-技术栈)
- [系统架构](#-系统架构)
  - [微服务架构图](#微服务架构图)
  - [服务拆分与职责](#服务拆分与职责)
  - [网关路由表](#网关路由表)
  - [JWT 统一鉴权流程](#jwt-统一鉴权流程)
- [项目结构](#-项目结构)
- [环境依赖](#-环境依赖)
- [磁盘空间规划](#-磁盘空间规划)
- [快速开始](#-快速开始)
  - [1. 克隆仓库](#1-克隆仓库)
  - [2. 数据库初始化](#2-数据库初始化)
  - [3. MinIO 对象存储安装配置](#3-minio-对象存储安装配置)
  - [4. Nacos 注册中心安装启动](#4-nacos-注册中心安装启动)
  - [5. 后端配置与启动](#5-后端配置与启动)
  - [6. 初始化 RAG 知识库](#6-初始化-rag-知识库)
  - [7. 前端启动](#7-前端启动)
- [RAG 知识库](#-rag-知识库)
  - [检索增强问答流程](#检索增强问答流程)
  - [RAG 接口一览](#rag-接口一览)
  - [调用示例](#调用示例)
  - [RAG 配置项](#rag-配置项)
- [配置说明](#-配置说明)
- [数据库表结构](#-数据库表结构)
- [接口文档](#-接口文档)
- [安全规范](#-安全规范)
- [已知限制](#-已知限制)
- [常见问题](#-常见问题)
- [许可证](#-许可证)

---

## 🎯 项目简介

**BrainlessTravel** 是一款面向微信小程序的 AI 智能旅游规划应用。用户通过微信授权快速登录后，即可享受 AI 智能行程规划、景点知识问答、寻伴同游组队、景点浏览收藏等一站式旅游服务。

后端已由**单体 Spring Boot** 改造为 **Spring Cloud 微服务**架构：网关统一鉴权与路由，Nacos 负责注册发现与配置中心，服务间通过 OpenFeign 调用；并集成 **LangChain4j + RAG 检索增强生成**，让 AI 回答优先依据景点知识库，并给出可追溯的引用来源。

前端基于 UniApp + Vue3 跨平台开发，**网关保留 `/api` 前缀**，因此微服务改造后前端所有请求路径与改造前完全一致，真正做到开箱即用。

> 💡 适合用作：课程设计、毕业设计、Spring Cloud 微服务与 RAG 技术学习、二次开发基础框架

---

## ✨ 功能特性

### 🏠 首页探索
- 景点搜索与智能推荐
- 轮播图展示热门景点
- 景点卡片列表（含开放时间、分类标签）
- 瀑布流式布局，浏览体验流畅

### 🤖 AI 智能助手
- **自由聊天模式**：与讯飞星火大模型实时对话，咨询旅游攻略、预算规划、路线推荐
- **表单模式**：输入目的地、天数、预算，AI 自动生成结构化行程单（含每日时间安排、景点推荐、餐饮住宿、费用明细）

### 📚 RAG 知识库增强（LangChain4j）
- **知识库增强开关**：AI 助手页顶部可一键开启/关闭，开启后回答优先依据景点库与知识库文档
- **引用来源展示**：AI 回答气泡下方展示「📚 参考来源」，含文档标题、相似度、片段序号，可展开查看原文片段
- **文档上传与解析**：支持 PDF / DOCX / TXT / XLSX（单个 ≤ 10MB），上传后异步解析、切分、向量化，不阻塞接口
- **景点一键同步**：把 `scenic` 表景点数据一键索引进知识库，重复同步幂等（不会产生重复片段）
- **知识库管理页**：文档列表（类型/来源/状态/片段数）、上传、删除、重建索引、下拉刷新
- **本地中文向量模型**：BGE-small-zh-v1.5（512 维 ONNX）本地推理，**零 API 依赖、零外网调用**
- **双向量存储模式**：开发用内存向量库（开箱即用），生产可切 Redis Stack

### 🤝 寻伴同游
- 发布旅游组队信息（目的地、日期、人数、描述）
- 大厅浏览所有组队卡片，支持关键词搜索与多维筛选
- 一键加入队伍，实时查看组队进度

### ❓ 旅游景点知识问答
- 基于 AI 自动生成个性化测试题目（景点名称、题目数量、选项数、难度自定义）
- 沉浸式答题体验，进度条实时反馈
- AI 智能评分与个性化评价报告，生成专属称号（如"故宫探索者"）

### 👤 个人中心
- 微信授权登录，JWT 安全认证
- 用户数据统计（点赞、喜欢、浏览、收藏）
- 每日签到、我的购物车、用户反馈、邮件通知、分享有礼
- 知识库管理入口、个人信息管理与设置

### 🛡️ 系统能力
- **微服务架构**：Spring Cloud Gateway 统一网关 + Nacos 注册发现/配置中心 + OpenFeign 服务间调用
- **网关统一 JWT 鉴权**：token 校验收敛到网关，解析出的用户 ID 通过 `X-User-Id` 请求头下发，下游服务零查库获取登录态
- **前后端零改动迁移**：网关保留 `/api` 前缀，前端所有请求路径与改造前完全一致
- **图片地址相对化**：数据库只存相对 object key，完整地址由前端 `resolveImage()` 拼接 —— 换电脑/换网络只改一行 `BASE_URL`，历史图片永不失效
- Redis 缓存热门景点与用户信息，接口响应极速
- MyBatis-Plus 逻辑删除，数据安全不丢失
- MinIO 对象存储，支持 10MB 单文件 / 50MB 批量图片上传

---

## 📸 界面预览

<table>
  <tr>
    <td align="center"><b>🏠 首页</b></td>
    <td align="center"><b>🤝 寻伴同游</b></td>
  </tr>
  <tr>
    <td><img src="docs/screenshots/home.png" width="280" alt="首页"/></td>
    <td><img src="docs/screenshots/travel-buddy.png" width="280" alt="寻伴同游"/></td>
  </tr>
  <tr>
    <td align="center"><b>🤖 AI 自由对话</b></td>
    <td align="center"><b>📋 AI 行程规划</b></td>
  </tr>
  <tr>
    <td><img src="docs/screenshots/ai-chat.png" width="280" alt="AI自由对话"/></td>
    <td><img src="docs/screenshots/ai-plan.png" width="280" alt="AI行程规划"/></td>
  </tr>
  <tr>
    <td align="center"><b>⚙️ 问答设置</b></td>
    <td align="center"><b>📝 AI 智能出题</b></td>
  </tr>
  <tr>
    <td><img src="docs/screenshots/quiz-setup.png" width="280" alt="问答设置"/></td>
    <td><img src="docs/screenshots/quiz-question.png" width="280" alt="智能出题"/></td>
  </tr>
  <tr>
    <td align="center"><b>📊 AI 评价报告</b></td>
    <td align="center"><b>👤 个人中心</b></td>
  </tr>
  <tr>
    <td><img src="docs/screenshots/quiz-result.png" width="280" alt="评价报告"/></td>
    <td><img src="docs/screenshots/profile.png" width="280" alt="个人中心"/></td>
  </tr>
</table>

### 底部导航栏

| 图标 | 页面 | 功能描述 |
|:----:|:----:|:---------|
| 🏠 | 首页 | 景点浏览、搜索、推荐 |
| 🌴 | 新模式 | 寻伴同游组队功能 |
| 🤖 | AI助手 | 自由聊天 / 表单模式行程规划 / 知识库增强 |
| 💬 | 问答 | AI 景点知识问答挑战 |
| 👤 | 我的 | 个人中心、知识库管理、系统设置 |

---

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 语言与编译版本 |
| Spring Boot | 3.2.5 | 核心框架 |
| Spring Cloud | 2023.0.3 | 微服务框架（BOM 统一管理） |
| Spring Cloud Alibaba | 2023.0.1.3 | Nacos 服务注册发现 + 配置中心 |
| Spring Cloud Gateway | 4.1.5 | API 网关（WebFlux，统一 JWT 鉴权 + 路由 + CORS） |
| Spring Cloud OpenFeign | 4.1.5 | 服务间声明式调用 |
| Spring Cloud LoadBalancer | 4.1.5 | 客户端负载均衡（`lb://` 协议） |
| Nacos | 2.x | 注册中心 + 配置中心 |
| MyBatis-Plus | 3.5.6 | ORM 框架（逻辑删除、自动填充、分页插件） |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 5.0+ | 缓存中间件（Lettuce 连接池） |
| MinIO | 8.5.7 | 对象存储服务（景点图片上传存储） |
| LangChain4j | 0.36.0 | RAG 检索增强框架（文档解析、切分、向量检索） |
| BGE-small-zh-v1.5 | — | 本地中文嵌入模型（512 维 ONNX，零 API 依赖） |
| 讯飞星火大模型 | V2/V3 | AI 智能旅游问答、行程生成、知识评测 |
| JWT (jjwt) | 0.11.5 | 登录令牌认证 |
| Maven | 3.6+ | 项目构建工具（多模块父工程） |

> **明确不引入**：Spring Cloud Config（用 Nacos Config 替代）、Sentinel / Seata、Sleuth / Zipkin、
> Milvus / Chroma / Pinecone、Spring AI、OpenAI / 讯飞嵌入 API —— 保持依赖精简。

### 前端技术

| 技术 | 说明 |
|------|------|
| UniApp | 跨平台小程序开发框架 |
| Vue 3 | 前端响应式框架 |
| uni_modules | 官方组件生态 |
| 自定义封装 | 全局 API 请求拦截、统一错误处理、JWT 自动携带、`resolveImage()` 图片地址解析 |

---

## 🏗️ 系统架构

### 微服务架构图

```text
                          ┌─────────────────┐
                          │   微信小程序前端  │
                          │  (UniApp/Vue3)  │
                          └────────┬────────┘
                                   │ HTTP :8080
                                   ▼
                          ┌─────────────────┐
                          │  travel-gateway  │  8080
                          │  Spring Cloud    │  · JWT 校验
                          │  Gateway (Flux)  │  · 路由转发
                          │                  │  · CORS 统一处理
                          └────┬───────┬────┘
                               │       │
                    lb://      │       │    lb://
              travel-user-svc  │       │  travel-ai-svc
                               ▼       ▼
                    ┌─────────────┐ ┌─────────────┐
                    │user-service │ │ ai-service  │
                    │    8081     │ │    8082     │
                    │             │ │             │
                    │ · 用户登录   │ │ · AI 自由聊天│
                    │ · 景点浏览   │ │ · 行程规划   │
                    │ · 寻伴组队   │ │ · 智能出题   │
                    │ · 知识问答   │ │ · RAG 知识库 │
                    │ · 文件上传   │ │ · 文档管理   │
                    │ · 导游预约   │ │ · BGE 向量化 │
                    └──────┬──────┘ └──────┬──────┘
                           │               │
              ┌────────────┼───────────────┼────────────┐
              ▼            ▼               ▼            ▼
        ┌─────────┐  ┌─────────┐    ┌──────────┐  ┌─────────┐
        │  MySQL  │  │  Redis  │    │  MinIO   │  │  Nacos  │
        │  3306   │  │  6379   │    │   9000   │  │  8848   │
        └─────────┘  └─────────┘    └──────────┘  └─────────┘
```

> 服务间调用：`user-service` 的问答模块需要调用星火大模型（封装在 `ai-service`），通过
> `AiSparkFeignClient` 走 Feign；`ai-service` 需要景点数据时**直接查 MySQL**，不走 Feign，减少耦合。

### 服务拆分与职责

| 服务 | 端口 | context-path | 职责 | 包含的 Controller |
|------|:----:|:------------:|------|-------------------|
| `travel-gateway` | 8080 | 无 | 统一入口、JWT 校验、路由、CORS | 无（纯网关，WebFlux） |
| `travel-user-service` | 8081 | `/api` | 用户、景点、组队、问答、文件、导游等业务 | Login、Scenic、ScenicPlay、Team、Qa、Upload、Banner、BaseData、GroupBooking、GuideBooking、Traveler（共 11 个） |
| `travel-ai-service` | 8082 | `/api` | AI 对话、行程规划、RAG 知识库 | Chat、TravelPlan、RagDocument、SparkInternal（Feign 内部接口） |
| `travel-common` | — | — | 公共模块（普通 jar，不可执行） | `Result`、`JwtUtil`、`UserContext` |

> **拆分依据**：AI 服务涉及大模型调用、向量检索、模型加载，资源消耗大且独立演进，单独拆出；
> 其余业务耦合度高，合并为一个用户业务服务。

### 网关路由表

| 路径前缀 | 路由到 | 服务名 |
|---------|--------|--------|
| `/api/login/**` | user-service | `travel-user-service` |
| `/api/scenic/**`（含 `/api/scenic/play/**`） | user-service | `travel-user-service` |
| `/api/scenicPlay/**`、`/api/groupBooking/**`、`/api/guideBooking/**`、`/api/file/**` | user-service | `travel-user-service` |
| `/api/group/booking/**`、`/api/guide/booking/**`、`/api/upload/**` | user-service | `travel-user-service` |
| `/api/team/**`、`/api/qa/**`、`/api/banner/**`、`/api/base/**`、`/api/traveler/**` | user-service | `travel-user-service` |
| `/api/chat/**` | ai-service | `travel-ai-service` |
| `/api/plan/**` | ai-service | `travel-ai-service` |
| `/api/rag/**` | ai-service | `travel-ai-service` |

> ⚠️ **网关不剥离 `/api` 前缀**：下游服务各自配置了 `context-path: /api`，因此前端请求路径
> 与单体时代完全一致，无需任何改动。

### JWT 统一鉴权流程

```text
① 前端登录 → POST /api/login/wxLogin → 网关放行（白名单）→ user-service
② user-service 签发 token（载荷含 openid + userId），返回前端
③ 前端后续请求携带 Authorization: Bearer <token>
④ 网关 JwtAuthGlobalFilter 校验签名与有效期 → 解析出 userId
   → 写入请求头 X-User-Id → 转发给下游服务
⑤ 下游服务 UserIdInterceptor 读 X-User-Id 存入 UserContext（ThreadLocal）
   → 业务代码用 UserContext.getUserId() 取当前用户，零查库
```

**白名单**（`jwt.white-list`，无需 token 即可访问）：

```yaml
jwt:
  white-list: /api/login/**,/api/banner/list,/api/scenic/list,/api/scenic/detail/**,/api/scenic/play/list,/api/base/**,/api/team/list,/api/team/detail/**
```

> **设计说明**：小程序首页的轮播图、景点列表、景点详情、游玩项目、银发模式列表、寻伴大厅
> 都是**游客态请求**，必须放行，否则未登录用户首页会直接空白。
>
> ✅ **安全性实际是增强的**：单体时代 `/scenic/add`、`/scenic/update`、`/scenic/delete`、
> `/upload/scenic` **完全无鉴权**；改造后这些写接口一律不在白名单内，必须携带有效 token。

---

## 📁 项目结构

```text
BrainlessTravel/
├── backend/                              # Spring Cloud 多模块父工程
│   ├── pom.xml                           # 父 POM（BOM 版本统一管理）
│   ├── travel-common/                    # 公共模块（普通 jar，不可执行）
│   │   └── net/togogo/travel/common/
│   │       ├── Result/Result.java        # 统一返回封装
│   │       └── util/
│   │           ├── JwtUtil.java          # JWT 生成/校验（支持 openid + userId 载荷）
│   │           └── UserContext.java      # 用户上下文（ThreadLocal）
│   ├── travel-gateway/                   # 网关服务（8080，WebFlux）
│   │   └── net/togogo/travel/gateway/
│   │       ├── GatewayApplication.java
│   │       ├── config/
│   │       │   ├── CorsConfig.java       # CorsWebFilter 统一跨域
│   │       │   └── GatewayConfig.java    # 网关访问日志过滤器
│   │       └── filter/
│   │           └── JwtAuthGlobalFilter.java  # 全局 JWT 鉴权，下发 X-User-Id
│   ├── travel-user-service/              # 用户业务服务（8081）
│   │   └── net/togogo/travel/user/
│   │       ├── UserServiceApplication.java
│   │       ├── config/                   # RedisConfig、MinioConfig、WebMvcConfig
│   │       ├── controller/               # 11 个业务 Controller
│   │       ├── service/                  # 业务层 + SparkService(Feign 适配)
│   │       ├── mapper/ entity/ dto/ vo/
│   │       ├── feign/                    # AiSparkFeignClient（调 ai-service）
│   │       └── util/                     # UserIdInterceptor、RedisUtil、MinioUtil
│   │   └── resources/
│   │       ├── application.yml           # ⚠️ 含密钥，不上传 GitHub
│   │       ├── application-template.yml  # ✅ 配置模板，上传仓库
│   │       ├── bootstrap.yml             # Nacos 引导配置
│   │       └── sql/
│   │           ├── travel_init.sql                        # 建表脚本（含 rag_document）
│   │           └── migration_v2_relative_image_path.sql   # 图片路径相对化迁移脚本
│   ├── travel-ai-service/                # AI + RAG 服务（8082）
│   │   └── net/togogo/travel/ai/
│   │       ├── AiServiceApplication.java # @EnableAsync
│   │       ├── config/
│   │       │   ├── RagProperties.java    # RAG 配置属性（D 盘路径）
│   │       │   ├── RagConfig.java        # 嵌入模型/切分器/向量库 Bean
│   │       │   ├── RedisConfig.java
│   │       │   └── WebMvcConfig.java
│   │       ├── controller/               # ChatController(+RAG 接口)、
│   │       │                             # TravelPlanController、RagDocumentController、
│   │       │                             # SparkInternalController(Feign 内部接口)
│   │       ├── service/
│   │       │   ├── SparkService(Impl)            # 星火大模型封装
│   │       │   ├── TravelPlanService(Impl)       # 行程规划
│   │       │   ├── RagService(Impl)              # RAG 检索增强
│   │       │   └── RagDocumentService(Impl)      # 文档上传/解析/向量化
│   │       ├── store/InMemoryEmbeddingStoreHolder.java
│   │       ├── entity/ mapper/ dto/
│   │       └── util/UserIdInterceptor.java
│   │   └── resources/
│   │       ├── application.yml           # ⚠️ 含密钥，不上传 GitHub
│   │       ├── application-template.yml  # ✅ 配置模板，上传仓库
│   │       └── bootstrap.yml             # Nacos 引导配置
├── uniapp-front/                         # UniApp 微信小程序前端源码
│   ├── api/
│   │   ├── config.js                     # ⭐ 全局唯一配置源（BASE_URL / resolveImage）
│   │   ├── api.js chat.js plan.js question.js team.js digitalHuman.js
│   │   └── rag.js                        # 📚 RAG 知识库接口封装
│   ├── pages/
│   │   ├── index/                        # 首页
│   │   ├── AI-asistant/                  # AI 助手（知识库增强开关 + 引用来源）
│   │   ├── rag-knowledge/                # 📚 知识库管理页
│   │   ├── SilverHairedModel/            # 银发模式（组队、私人导游、我的预约）
│   │   ├── answer/ TravelPlanHistory/ mine/ ...
│   ├── static/                           # 静态资源
│   ├── pages.json                        # 页面路由注册
│   └── manifest.json                     # 小程序配置
├── docs/
│   ├── screenshots/                      # 项目截图（README 引用）
│   ├── LANGCHAIN_RAG_DEV_REQUIREMENTS.md # 微服务 + RAG 改造需求文档（开发指令）
│   └── MIGRATION_DELIVERY.md             # 改造交付说明（环境/启动/验收/差异）
├── .gitignore                            # Git 忽略配置
├── LICENSE                               # MIT 开源协议
└── README.md                             # 项目说明文档（本文件）
```

---

## 📋 环境依赖

在启动项目前，请确保本地已安装以下环境：

| 依赖 | 版本要求 | 下载/安装 | 必需 |
|------|---------|----------|:----:|
| JDK | 21+ | [Oracle](https://www.oracle.com/java/technologies/downloads/) / [Adoptium](https://adoptium.net/) | ✅ |
| Maven | 3.6+ | [官方下载](https://maven.apache.org/download.cgi) | ✅ |
| MySQL | 8.0+ | [官方下载](https://dev.mysql.com/downloads/) | ✅ |
| Redis | 5.0+ | [官方下载](https://redis.io/download) | ✅ |
| Nacos | 2.x | [GitHub Releases](https://github.com/alibaba/nacos/releases)（注册中心 + 配置中心） | ✅ |
| MinIO | 任意 | [官方下载](https://min.io/download) 或 Docker | ✅ |
| Node.js | 16+ | [官方下载](https://nodejs.org/) | ✅ |
| HBuilderX | 最新 | [官方下载](https://www.dcloud.io/hbuilderx.html) | ✅ |
| 微信开发者工具 | 最新 | [官方下载](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html) | ✅ |
| Docker | 任意 | [官方下载](https://www.docker.com/)（可选，用于运行 MinIO） | ⭕ |

**外部服务账号**（需自行申请）：

| 服务 | 用途 | 申请地址 |
|------|------|---------|
| 微信小程序 | 登录授权、AppID / AppSecret | [微信公众平台](https://mp.weixin.qq.com/) |
| 讯飞星火大模型 | AI 对话、行程生成、出题评测 | [讯飞开放平台](https://xinghuo.xfyun.cn/) |

---

## 💽 磁盘空间规划

本项目在改造中遵循「**已有资源延用原位置，新增资源放 D 盘，同性质资源不跨盘拆分**」原则。
如果你的 C 盘空间紧张（本项目开发机 C 盘仅剩约 2 GB），可参考以下规划：

### 新增资源（建议放 D 盘）

| 目录 | 用途 | 体积量级 |
|------|------|---------|
| `D:\dev-resources\maven-repo\` | Maven 本地仓库（Spring Cloud + LangChain4j 新增依赖） | 约 1.0 GB |
| `D:\dev-resources\rag-models\` | BGE 中文嵌入模型 ONNX 文件（首次启动 ai-service 时自动提取） | 约 95 MB |
| `D:\dev-resources\rag-docs\` | RAG 知识库上传文档存储（PDF/DOCX/TXT/XLSX） | 随上传增长 |
| `D:\dev-resources\temp\` | JVM 临时文件 | 运行时产生 |
| `D:\dev-resources\nacos\` 或已有 Nacos 目录 | Nacos 安装目录（内嵌 Derby 数据随之） | 约 200 MB |

**Maven 仓库重定向**（新增依赖约 1.5–2 GB，必须执行）：在 `C:\Users\<你>\.m2\settings.xml` 中设置

```xml
<localRepository>D:\dev-resources\maven-repo</localRepository>
```

验证是否生效：

```bash
mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout
# 期望输出：D:\dev-resources\maven-repo
```

### 已在原位置、不做迁移

MySQL、Redis、MinIO、JDK、IDEA、Node.js 等**程序与数据目录都留在原位置**，不要出现
「程序在 C 盘、数据迁到 D 盘」这类跨盘拆分。

> 📌 **代码级 D 盘保护**：`AiServiceApplication.main()` 在 Spring 启动前设置
> `java.io.tmpdir`，`RagConfig.@PostConstruct` 再次设置并创建目录；BGE 模型、上传文档
> 的路径全部来自 `rag.model-path` / `rag.doc-storage-path` 配置，**代码中无任何硬编码 C 盘路径**。

---

## 🚀 快速开始

### 1. 克隆仓库

```bash
git clone https://github.com/den603/BrainlessTravel.git
cd BrainlessTravel
```

---

### 2. 数据库初始化

#### 2.1 创建数据库

使用 MySQL 客户端（命令行、Navicat、DataGrip 均可）创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS travel
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

> 💡 `utf8mb4` 支持完整的 Unicode 字符（包括 emoji），`utf8mb4_unicode_ci` 排序规则对中文支持更好。

#### 2.2 导入建表脚本

```bash
mysql -u root -p travel < backend/travel-user-service/src/main/resources/sql/travel_init.sql
```

**图形工具导入方式（Navicat / DataGrip）：**
1. 连接本地 MySQL，选中 `travel` 数据库
2. 右键 → 运行 SQL 文件 → 选择 `travel_init.sql`
3. 执行完成后刷新表列表，确认表已创建

> ⚠️ **重要：脚本只含建表语句（DDL），不含任何示例数据。**
> 导入后 `scenic`、`banner`、`question` 等表都是空的，需要自行录入数据
> （或通过小程序「新增景点」页面上传）。首页在数据录入前会是空白，这是正常现象。

> ⚠️ **重复执行会清空业务数据**：脚本中 19 张业务表均为 `DROP TABLE IF EXISTS` + `CREATE TABLE`，
> 再次执行会**删除并重建**这些表，已有数据全部丢失。
> 只有末尾新增的 `rag_document` 表用的是 `CREATE TABLE IF NOT EXISTS`，可安全重复执行。

#### 2.3 验证导入结果

导入成功后应包含 **20 张表**：

| 模块 | 表名 | 说明 |
|------|------|------|
| 用户 | `user` | 微信用户信息 |
| 首页 | `banner` | 首页轮播图 |
| 首页 | `scenic` | 景点信息 |
| 首页 | `scenic_play` | 景点游玩推荐 |
| AI 助手 | `chat_history` | AI 自由聊天历史 |
| AI 助手 | `t_travel_plan` | AI 生成的旅行计划 |
| AI 助手 | `rag_document` | 📚 RAG 知识库文档（用户上传 / 景点同步） |
| 问答 | `question` | 景点题库 |
| 问答 | `question_fallback` | 题库兜底表 |
| 问答 | `user_answer_record` | 用户答题记录 |
| 问答 | `user_answer_detail` | 用户答题详情 |
| 组队 | `team` | 寻伴组队信息 |
| 组队 | `team_member` | 组队成员关系 |
| 组队 | `tb_travel_group` | 旅行团信息 |
| 导游 | `tb_private_guide` | 私人导游信息 |
| 导游 | `tb_guide_booking` | 导游预约表 |
| 导游 | `tb_guide_booking_traveler` | 预约出行人关联 |
| 导游 | `tb_traveler` | 出行人信息 |
| 导游 | `tb_group_booking` | 团体预约表 |
| 导游 | `tb_group_booking_traveler` | 团体预约出行人关联 |

验证命令：

```sql
USE travel;
SHOW TABLES;
```

#### 2.4 图片路径迁移（仅从旧版本升级时执行）

如果你是从**旧版本**升级（数据库里的图片字段是 `http://旧IP:9000/travel/scenic/xxx.jpg`
这类绝对 URL），换电脑/换网络后历史图片会全部失效。执行迁移脚本改为相对路径：

```bash
mysql -u root -p travel < backend/travel-user-service/src/main/resources/sql/migration_v2_relative_image_path.sql
```

脚本内含**执行前自查、迁移、校验、回滚**四段，幂等可重复执行。
**全新部署无需执行** —— 图片上传接口直接返回相对路径。

---

### 3. MinIO 对象存储安装配置

本项目使用 MinIO 存储景点图片、用户头像等文件。

#### 3.1 启动 MinIO

**方式一：Docker（推荐）**

```bash
docker run -d -p 9000:9000 -p 9001:9001 --name minio --restart=always \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  -v ~/minio/data:/data \
  -v ~/minio/config:/root/.minio \
  quay.io/minio/minio server /data --console-address ":9001"
```

**方式二：本地二进制**

下载 `minio.exe` 放入任意盘符（如 `D:\MinIO\bin`），切换到该目录后运行：

```bash
minio.exe server D:\MinIO\data
```

**参数说明：**

| 参数 | 说明 |
|------|------|
| `-p 9000:9000` | **API 端口**：后端程序通过此端口上传/下载文件 |
| `-p 9001:9001` | **控制台端口**：浏览器访问 Web 管理界面 |
| `MINIO_ROOT_USER` | 管理员账号（对应 `application.yml` 中的 `access-key`） |
| `MINIO_ROOT_PASSWORD` | 管理员密码（对应 `application.yml` 中的 `secret-key`） |
| `-v ~/minio/data:/data` | 数据持久化到本地目录，容器删除后数据不丢失 |

#### 3.2 验证启动状态

```bash
docker ps | grep minio

# 应输出类似：
# CONTAINER ID   IMAGE           STATUS         PORTS
# xxxxxxxx       minio/minio     Up 2 minutes   0.0.0.0:9000-9001->9000-9001/tcp
```

#### 3.3 创建存储桶

1. 浏览器访问：`http://localhost:9001`
2. 使用账号 `minioadmin` / 密码 `minioadmin` 登录
3. 左侧菜单 **Buckets** → **Create Bucket**
4. 输入桶名：`travel` → **Create Bucket**

> 💡 桶名必须与 `application.yml` 中 `minio.bucket-name` 的值一致。

#### 3.4 配置桶的访问权限（重要！）

创建桶后需设置访问策略，否则前端无法直接访问图片：

1. 进入 `travel` 桶 → **Access Rules** → **Add Access Rule**
2. **Prefix**：`*`（通配符，匹配所有对象）
3. **Access**：`Read Only`（只读，允许公开访问图片）
4. **Save**

或直接设置匿名访问：进入桶 → **Access Policy** → 选择 `Public`。

> ⚠️ **安全提示**：本地开发可设为 Public；生产环境建议使用 Presigned URL 或更精细的访问策略。

#### 3.5 配置对应

```yaml
minio:
  endpoint: http://localhost:9000    # API 端口（不是 9001）
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: travel
```

> 📌 `endpoint` 只用于**后端连接 MinIO 客户端**和兼容解析历史绝对 URL，
> **不影响已入库数据** —— 数据库只存相对路径（如 `scenic/xxx.jpg`），
> 完整访问地址由前端 `resolveImage()` 拼接。换网络时改前端配置即可，无需批量改库。

---

### 4. Nacos 注册中心安装启动

Nacos 是**必需**的前置依赖，负责服务注册发现（网关靠它做 `lb://` 负载均衡）与配置中心。

```bash
# 单机模式启动（Windows）
D:\dev-resources\nacos\bin\startup.cmd -m standalone

# 若你的 Nacos 装在别处（例如 D:\nacos\nacos），用你自己的路径启动即可
D:\nacos\nacos\bin\startup.cmd -m standalone
```

| 项 | 值 |
|---|---|
| 控制台 | `http://localhost:8848/nacos` |
| 默认账号/密码 | `nacos` / `nacos` |
| 端口 | 8848（控制台） / 9848（gRPC） |
| 数据存储 | 内嵌 Derby，数据目录在 Nacos 安装目录下的 `data\` |

> 💡 **本地开发无需在 Nacos 中创建任何配置**：三个服务的 `bootstrap.yml` 都设置了
> `spring.cloud.nacos.config.import-check.enabled=false`，Nacos 中找不到对应 dataId 时
> **只告警、不阻断启动**，真实配置直接读本地 `application.yml`。
> 后续想把数据库/Redis 连接信息搬到配置中心，在 Nacos 中新建 `travel-gateway.yaml`、
> `travel-user-service.yaml`、`travel-ai-service.yaml`（DEFAULT_GROUP / public）即可自动生效。

---

### 5. 后端配置与启动

#### 5.1 配置核心文件 ⚠️

三个服务各有独立配置文件。进入各服务的 `src/main/resources/`，
将 `application-template.yml` 复制一份并重命名为 **`application.yml`**：

```bash
cd backend
cp travel-gateway/src/main/resources/application-template.yml       travel-gateway/src/main/resources/application.yml
cp travel-user-service/src/main/resources/application-template.yml  travel-user-service/src/main/resources/application.yml
cp travel-ai-service/src/main/resources/application-template.yml    travel-ai-service/src/main/resources/application.yml
```

需要填写的私密配置：

- [ ] **MySQL 账号密码**（`travel-user-service` 与 `travel-ai-service` **都要改**，两个服务连同一个库）
- [ ] **Redis 密码**（本地无密码可留空）
- [ ] **MinIO 端点、AccessKey、SecretKey、桶名**（`travel-user-service`）
- [ ] **微信小程序 AppID、AppSecret**（`travel-user-service`）
- [ ] **讯飞星火 app-id / api-key / api-secret**（`travel-ai-service`）
- [ ] **JWT 密钥** —— ⚠️ **三个服务的 `jwt.secret` 必须完全一致**，否则网关无法解析用户服务签发的 token，所有鉴权接口会 401
- [ ] **D 盘路径**（`travel-ai-service` 的 `rag.model-path`、`rag.doc-storage-path`，默认已指向 `D:\dev-resources\`）

> `application.yml` 已加入 `.gitignore`，不会被提交到 GitHub；`application-template.yml` 模板会正常提交。

#### 5.2 编译打包

```bash
cd backend
mvn clean package -DskipTests
```

父工程会依次构建 `travel-common` → `travel-gateway` → `travel-user-service` → `travel-ai-service`。
四个模块全部 `BUILD SUCCESS` 即表示编译通过。典型产物大小：

```
travel-common ............   8 KB  （普通 jar，不可执行）
travel-gateway ...........  46 MB  （可执行 jar）
travel-user-service ......  74 MB  （可执行 jar）
travel-ai-service ........ 263 MB  （可执行 jar，含 BGE ONNX 模型）
```

#### 5.3 按顺序启动各服务

**启动顺序有讲究，必须严格遵守：**

```text
1. MySQL      (3306)   —— 已有服务
2. Redis      (6379)
3. MinIO      (9000)
4. Nacos      (8848)   ← D:\dev-resources\nacos\bin\startup.cmd -m standalone
5. travel-user-service (8081)
6. travel-ai-service   (8082)   ← 首次启动会提取 BGE 模型到 D 盘（约 95MB）
7. travel-gateway      (8080)   ← 必须最后启动
8. 微信开发者工具打开 uniapp-front
```

```bash
# 5) 再启动两个业务服务（顺序不分先后）
java -jar travel-user-service/target/travel-user-service-1.0.0.jar    # 8081
java -jar travel-ai-service/target/travel-ai-service-1.0.0.jar       # 8082

# 6) 最后启动网关
java -jar travel-gateway/target/travel-gateway-1.0.0.jar             # 8080
```

或在 IDEA 中依次运行三个启动类：`UserServiceApplication` → `AiServiceApplication` → `GatewayApplication`。

> ❗ **为什么网关必须最后启动**：网关按服务名 `lb://travel-user-service` / `lb://travel-ai-service`
> 转发，若下游服务还没注册到 Nacos，请求会返回 **503**。先起业务服务再起网关可避免。

**启动成功的标志：**

- Nacos 控制台「服务列表」中出现 `travel-user-service`、`travel-ai-service`、`travel-gateway` 三个实例
- `travel-ai-service` 首次启动会输出「模型资源已提取到 D 盘」，为 BGE 模型落盘（约 95 MB，仅首次）

```
网关统一入口：http://localhost:8080
前端请求路径与改造前完全一致（如 http://localhost:8080/api/scenic/list）
```

**JVM 内存建议**：`travel-ai-service` 因常驻加载 ONNX 嵌入模型，建议 `-Xmx` 不低于 1 GB。

---

### 6. 初始化 RAG 知识库

服务全部启动后，知识库是空的，需执行一次初始化：

```bash
# ① 景点数据一键同步进知识库（返回同步数量）
curl -X POST http://localhost:8080/api/rag/sync/scenic -H "Authorization: Bearer <你的token>"

# ② 验证检索是否命中（纯检索，不调用大模型）
curl "http://localhost:8080/api/rag/retrieve?query=故宫门票" -H "Authorization: Bearer <你的token>"
```

> ⚠️ **开发模式默认使用内存向量库**（`rag.store-type: memory`），**服务重启后向量会丢失**，
> 需重新调用 `POST /api/rag/reindex` 重建，或再次同步景点。
>
> 生产环境请把 `rag.store-type` 切换为 `redis` —— ⚠️ **需要 Redis Stack**，
> 普通 Redis 5.x/6.x 缺少 RediSearch 模块，切换后会**启动失败并给出明确提示**
> （故意不做静默降级，避免误以为向量已持久化）。

---

### 7. 前端启动

#### 7.1 导入项目并安装依赖

1. 打开 **HBuilderX**
2. 选择 `文件 → 导入 → 从本地目录导入`，选择 `uniapp-front/` 目录

```bash
cd uniapp-front
npm install
```

#### 7.2 配置后端接口地址 ⭐

**整个前端只有一个文件需要按环境修改**：`uniapp-front/api/config.js`

```javascript
/** 后端网关地址（不含 /api） */
export const BASE_URL = 'http://localhost:8080';
```

改这一行即可，其余全部自动跟随：

| 场景 | `BASE_URL` 取值 |
|------|----------------|
| 只在微信开发者工具里调试（后端跑在同一台电脑） | `http://localhost:8080` |
| 真机预览（手机和电脑连同一个 WiFi） | `http://192.168.x.x:8080`（换成电脑局域网 IP，用 `ipconfig` 查） |

> **为什么图片地址不用单独改**：`MINIO_BASE` 由 `BASE_URL` 自动推导（协议 + 主机名不变，端口换成 9000）。
> 若 MinIO 部署在别的机器，直接把 `config.js` 中的 `MINIO_BASE` 改为完整地址即可
> （如 `'http://192.168.1.50:9000/travel'`）。
>
> **为什么数据库只存相对路径**：`scenic.img` 存的是 `scenic/xxx.jpg`，
> 完整地址在渲染时由 `resolveImage()` 拼接，因此**换电脑换网络后历史数据无需任何修改**。

#### 7.3 编译运行

1. HBuilderX 菜单栏 `运行 → 运行到小程序模拟器 → 微信开发者工具`
2. 首次运行需配置微信开发者工具路径（`设置 → 运行配置`）
3. 微信开发者工具自动打开，即可预览小程序

> 📌 **注意**：微信登录功能需要在小程序后台配置合法域名。本地开发可在微信开发者工具中
> 勾选「详情 → 本地设置 → 不校验合法域名」进行测试。

---

## 📚 RAG 知识库

这是本项目的核心亮点：**完全本地化的中文检索增强生成**，不依赖任何外部嵌入 API。

| 组成 | 选型 | 说明 |
|------|------|------|
| 嵌入模型 | BGE-small-zh-v1.5 | 512 维 ONNX，本地推理，中文语义理解 |
| 文档解析 | Apache PDFBox / Apache POI | PDF、DOCX、XLSX；TXT 直接读取 |
| 文本切分 | `DocumentSplitters.recursive` | 默认 chunk-size 500 / overlap 50 |
| 向量存储 | 内存 / Redis Stack | 开发用内存，生产可切 Redis |
| 生成模型 | 讯飞星火大模型 | 拿到参考资料后生成最终回答 |

### 检索增强问答流程

```text
用户提问
   │
   ├─ useRag = false 或 rag.enabled = false
   │     └─→ 直接调用星火大模型 → 返回 answer（ragEnabled=false, sources=[]）
   │
   └─ useRag = true
         │
         ▼
   ① 取最后一条用户消息作为 query
         ▼
   ② BGE 模型 embed(query) → 512 维向量
         ▼
   ③ embeddingStore.findRelevant(向量, topK=4, minScore=0.3)
         ▼
   ④ 命中片段 → 拼装增强 System Prompt（参考资料标注 [1][2]）
         │        插入 messages 头部
         ▼
   ⑤ 调用星火大模型生成回答
         ▼
   ⑥ 从片段 metadata 提取 documentId / title / fileType / chunkIndex
         → 构造 sources[] 返回前端
         ▼
   前端渲染「📚 参考来源」（标题 + 相似度 + 片段序号，可展开原文）
```

**景点同步（`sync/scenic`）流程**：查 `scenic` 表 → 每条景点拼成一份文本文档
（`title`→名称、`introduce`→简介、`times`→开放时间、`tag`→标签、`address`→坐标）
→ 切分 → 向量化 → 以 `sourceType=SCENIC`、`userId=0` 入库。**重复同步幂等，不产生重复片段。**

**文档上传流程**：`PENDING → PROCESSING → COMPLETED / FAILED`，解析与向量化由 `@Async` 异步执行，
不阻塞上传接口。文件类型白名单 `pdf / docx / txt / xlsx`，单个不超过 10 MB。

### RAG 接口一览

| 方法 | 路径 | 说明 | 需 token |
|:----:|------|------|:--------:|
| POST | `/api/chat/ask/rag` | **带知识库增强的 AI 问答**（RAG 主接口） | ✅ |
| POST | `/api/rag/document/upload` | 上传知识库文档（multipart：`file` + `title`） | ✅ |
| GET | `/api/rag/document/list` | 知识库文档列表（自己的 + 系统/景点共享的） | ✅ |
| DELETE | `/api/rag/document/{id}` | 删除文档（只能删自己上传的，系统文档不可删） | ✅ |
| POST | `/api/rag/sync/scenic` | 景点数据一键同步进知识库（幂等） | ✅ |
| POST | `/api/rag/reindex` | 重建全部索引（memory 模式重启后必调） | ✅ |
| GET | `/api/rag/retrieve?query=xxx` | **纯检索测试**（不调用大模型，用于验证命中） | ✅ |

> 内部接口 `POST /api/internal/spark/**` 供 `user-service` 通过 Feign 调用星火大模型，
> **未注册到网关**，属于服务间内部接口。详见[已知限制](#-已知限制)。

### 调用示例

**带 RAG 的问答**

```bash
curl -X POST http://localhost:8080/api/chat/ask/rag \
  -H "Authorization: Bearer <你的token>" \
  -H "Content-Type: application/json" \
  -d '{
        "messages": [{"role": "user", "content": "故宫门票多少钱？"}],
        "useRag": true
      }'
```

响应：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "answer": "根据知识库，故宫门票……[1]",
    "ragEnabled": true,
    "sources": [
      {
        "documentId": 12,
        "title": "故宫",
        "fileType": "SCENIC",
        "chunkIndex": 0,
        "content": "故宫，位于北京市……",
        "score": 0.71
      }
    ]
  }
}
```

**关闭 RAG 对照**

```json
// 请求体改为 "useRag": false
// 响应：{ "answer": "...", "ragEnabled": false, "sources": [] }
```

**纯检索验证（不调用大模型）**

```bash
curl "http://localhost:8080/api/rag/retrieve?query=故宫门票" \
  -H "Authorization: Bearer <你的token>"
# 返回片段列表，score > 0.3 表示命中良好
```

**上传文档**

```bash
curl -X POST http://localhost:8080/api/rag/document/upload \
  -H "Authorization: Bearer <你的token>" \
  -F "file=@北京旅游攻略.pdf" \
  -F "title=北京旅游攻略"
```

### RAG 配置项

```yaml
rag:
  enabled: true                    # RAG 总开关
  store-type: memory               # memory（默认，内存向量库）| redis（需 Redis Stack）
  model-path: D:\dev-resources\rag-models        # BGE 模型落盘目录
  doc-storage-path: D:\dev-resources\rag-docs    # 上传文档存储目录
  splitter:
    chunk-size: 500                # 文本切分块大小（字符）
    chunk-overlap: 50              # 相邻块重叠字符数
  retrieval:
    top-k: 4                       # 检索返回的片段数
    min-score: 0.3                 # 相似度阈值，低于此值不采用
  redis:
    index-name: rag_embeddings     # Redis 向量索引名
    vector-dimension: 512          # BGE-small-zh-v1.5 固定 512 维，不可修改
```

---

## ⚙️ 配置说明

微服务化后**三个服务各有独立的 `application.yml`**（均由 `application-template.yml` 复制而来）。
下表是各服务需要关注的核心配置项。

### 三个服务的差异一览

| 配置项 | travel-gateway | travel-user-service | travel-ai-service |
|--------|:--------------:|:-------------------:|:-----------------:|
| `server.port` | **8080** | 8081 | 8082 |
| `server.servlet.context-path` | 无 | `/api` | `/api` |
| 数据源（MySQL） | — | ✅ | ✅（连同一个库 `travel`） |
| Redis | — | ✅ | ✅ |
| MinIO | — | ✅ | — |
| 微信小程序 | — | ✅ | — |
| 讯飞星火 | — | — | ✅ |
| RAG 配置 | — | — | ✅ |
| `jwt.secret` | ✅ | ✅ | ✅ |

> ⚠️ 三处的 `jwt.secret` 必须**完全一致**。

### 服务端配置

```yaml
# travel-gateway/src/main/resources/application.yml
server:
  port: 8080
# 网关没有 context-path，路径原样透传给下游（下游自带 /api）
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: false        # 关闭自动路由，使用手动路由
      routes:
        - id: user-service
          uri: lb://travel-user-service
          predicates:
            - Path=/api/login/**,/api/scenic/**,/api/scenicPlay/**,/api/team/**,...
        - id: ai-service
          uri: lb://travel-ai-service
          predicates:
            - Path=/api/chat/**,/api/plan/**,/api/rag/**
```

```yaml
# travel-user-service / travel-ai-service 各自
server:
  port: 8081                    # user-service；ai-service 为 8082
  servlet:
    context-path: /api          # 保留 /api 前缀，网关不剥离，前端请求路径不变
```

### 数据库配置

`travel-user-service` 与 `travel-ai-service` **连接同一个库**（`travel`）：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/travel?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root                # 修改为你的 MySQL 用户名
    password: 【填写你的密码】      # 修改为你的 MySQL 密码
```

### Redis 配置（两个业务服务）

```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password:                   # 本地无密码留空，生产环境务必设置
      database: 0
      timeout: 3000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
```

### 文件上传配置

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB         # 单个文件最大 10MB
      max-request-size: 50MB      # 一次请求总大小 50MB
      enabled: true
```

### MyBatis-Plus 配置

注意 `type-aliases-package` **两个服务取值不同**（各自扫描自己的实体包）：

```yaml
# travel-user-service
mybatis-plus:
  type-aliases-package: net.togogo.travel.user.entity

# travel-ai-service
mybatis-plus:
  type-aliases-package: net.togogo.travel.ai.entity

# 两者公共部分
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: is_delete    # 逻辑删除字段
      logic-not-delete-value: 0        # 未删除标记
      logic-delete-value: 1            # 已删除标记
  configuration:
    map-underscore-to-camel-case: true # 数据库下划线转驼峰命名
```

### MinIO 对象存储配置（`travel-user-service`）

```yaml
minio:
  endpoint: http://localhost:9000      # MinIO API 地址（9000 端口，不是 9001）
  access-key: 【填写你的MinIO账号】
  secret-key: 【填写你的MinIO访问密钥】
  bucket-name: travel                  # 存储桶名称（需提前创建）
```

> 📌 详细安装步骤见 [MinIO 对象存储安装配置](#3-minio-对象存储安装配置)。

### 微信小程序配置（`travel-user-service`）

```yaml
wechat:
  mini:
    appid: 【前往微信公众平台申请小程序AppID】
    appsecret: 【对应小程序的AppSecret密钥】
    login-url: https://api.weixin.qq.com/sns/jscode2session  # 微信官方固定接口，无需修改
```

> 📌 获取方式：登录 [微信公众平台](https://mp.weixin.qq.com/) → 开发 → 开发管理 → 开发设置

### JWT 令牌配置（三个服务）

```yaml
jwt:
  secret: 【自定义JWT加密密钥，建议64位以上复杂随机字符串】  # ⚠️ 三处必须完全一致
  expire: 86400000                     # Token 有效期：1 天（单位：毫秒）
  # 仅 travel-gateway 需要白名单
  white-list: /api/login/**,/api/banner/list,/api/scenic/list,/api/scenic/detail/**,/api/scenic/play/list,/api/base/**,/api/team/list,/api/team/detail/**
```

### 讯飞星火大模型配置（`travel-ai-service`）

```yaml
spark:
  app-id: 【讯飞开放平台控制台获取应用ID】
  api-key: 【讯飞开放平台获取API Key】
  api-secret: 【讯飞开放平台获取API Secret】
  url: https://spark-api-open.xf-yun.com/x2/chat/completions
```

> 📌 获取方式：登录 [讯飞开放平台](https://xinghuo.xfyun.cn/) → 控制台 → 创建应用 → 获取服务接口认证信息

### Nacos 引导配置（三个服务）

三个服务的 `bootstrap.yml` 内容基本一致（仅 `spring.application.name` 不同）：

```yaml
spring:
  application:
    name: travel-user-service        # 各服务改为自己的名字
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        file-extension: yaml
        namespace: public
        import-check:
          enabled: false               # Nacos 中无同名配置时只告警，不阻断启动
```

> 📌 本地开发无需在 Nacos 中创建任何配置，真实配置读本地 `application.yml` 即可。

---

## 🗄️ 数据库表结构

### 核心表关系图

```text
┌─────────────┐     ┌─────────────┐     ┌──────────────┐
│    user     │────→│chat_history │     │t_travel_plan │
│  (用户表)   │     │(AI聊天历史) │     │ (旅行计划)   │
└─────────────┘     └─────────────┘     └──────────────┘
       │
       │ 1:N
       ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│user_answer_ │────→│  question   │     │   banner    │
│  record     │     │  (题库表)   │     │ (轮播图表)  │
│(答题记录)   │     └─────────────┘     └─────────────┘
└─────────────┘
       │
       │ 1:N
       ▼
┌─────────────┐
│user_answer_ │
│   detail    │
│(答题详情)   │
└─────────────┘

┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│    team     │────→│ team_member │     │   scenic    │
│  (组队表)   │     │(组队成员表) │     │  (景点表)   │
└─────────────┘     └─────────────┘     └──────┬──────┘
                                               │ 1:N
                                               ▼
                                        ┌─────────────┐
                                        │ scenic_play │
                                        │(游玩推荐表) │
                                        └─────────────┘

┌──────────────────┐     ┌──────────────────┐
│ tb_private_guide │────→│ tb_guide_booking │
│   (私人导游)     │     │   (导游预约)     │
└──────────────────┘     └────────┬─────────┘
                                  │ 1:N
                                  ▼
                         ┌────────────────────────┐
                         │tb_guide_booking_traveler│
                         └───────────┬────────────┘
                                     ▼
                              ┌─────────────┐
                              │ tb_traveler │
                              │  (出行人)   │
                              └─────────────┘

┌─────────────┐
│rag_document │  📚 RAG 知识库文档（user_id=0 表示系统预置，不可删除）
│(知识库文档) │
└─────────────┘
```

### 关键字段设计说明

| 表名 | 关键字段 | 设计意图 |
|------|---------|---------|
| `user` | `openid` | 微信唯一标识，建立 UK 索引防止重复注册 |
| `scenic` | `img` | **只存相对路径**（如 `scenic/tt.jpg`），完整地址由前端拼接，换 IP 不失效 |
| `scenic` | `tag` (JSON) | 灵活存储景点标签数组，如 `["著名", "名胜古迹"]` |
| `scenic` | `address` (JSON) | 存储经纬度数组 `["经度", "纬度"]` |
| `question` | `options` (JSON) | 存储选项数组，支持动态选项数量 |
| `t_travel_plan` | `plan_content` (JSON) | 存储 AI 生成的完整结构化行程 |
| `t_travel_plan` | `preferences` (JSON) | 用户兴趣标签，如 `["美食", "自然风光"]` |
| `rag_document` | `user_id` | `0` 表示系统预置文档（景点同步产生），任何用户都不可删除 |
| `rag_document` | `source_type` | `USER`（用户上传）/ `SYSTEM` / `SCENIC`（景点同步） |
| `rag_document` | `status` | `PENDING` / `PROCESSING` / `COMPLETED` / `FAILED` |
| `rag_document` | `file_path` | 本地绝对路径，位于 `D:\dev-resources\rag-docs\`（**不走 MinIO**） |
| 所有业务表 | `is_delete` | MyBatis-Plus 逻辑删除字段，0=未删除，1=已删除 |

---

## 📡 接口文档

所有请求统一打到网关 `http://localhost:8080`，由网关按路径前缀转发到对应微服务。

| 接口类别 | 路径前缀 | 服务 | 说明 |
|---------|---------|------|------|
| 登录用户 | `/api/login/**` | user-service | 微信登录、用户信息、签到统计 |
| 景点模块 | `/api/scenic/**` | user-service | 景点列表、详情、收藏、搜索 |
| 游玩项目 | `/api/scenic/play/**` | user-service | 景点游玩推荐列表 |
| 轮播图 | `/api/banner/**` | user-service | 首页轮播图 |
| 基础数据 | `/api/base/**` | user-service | 银发模式等基础字典 |
| 组队模块 | `/api/team/**` | user-service | 发布组队、加入队伍、我的组队 |
| 问答模块 | `/api/qa/**` | user-service | AI 出题、答题提交、评价报告 |
| 文件上传 | `/api/upload/**` | user-service | 图片上传、MinIO 预签名 URL |
| 导游预约 | `/api/guide/booking/**` | user-service | 私人导游预约 |
| 团体预约 | `/api/group/booking/**` | user-service | 团体预约 |
| 出行人 | `/api/traveler/**` | user-service | 出行人信息管理 |
| AI 助手 | `/api/chat/**` | ai-service | 自由对话、**RAG 问答**、聊天历史 |
| 行程规划 | `/api/plan/**` | ai-service | AI 行程规划生成 |
| RAG 知识库 | `/api/rag/**` | ai-service | 文档管理、检索、景点同步、重建索引 |

> **鉴权规则**：除[白名单](#jwt-统一鉴权流程)（登录接口 + 游客态只读查询）外，
> 所有接口都需在请求头携带 `Authorization: Bearer <token>`。
> 未携带或 token 失效返回 **401**。

### 统一返回格式

```json
{
  "code": 1,        // 1 = 成功，0 = 失败
  "msg": "success", // 失败时的错误信息
  "data": { }       // 业务数据，失败时为 null
}
```

---

## 🔒 安全规范

本项目已配置完整的 `.gitignore`，**真实密钥配置文件不会上传至 GitHub**，确保仓库安全干净。

### 已屏蔽内容

- ✅ 三个服务的真实 `application.yml`（数据库密码、Redis 密钥、微信 AppID/AppSecret、讯飞星火密钥、MinIO 凭证、JWT 密钥）
- ✅ 编译产物（`target/`、`dist/`、`unpackage/`）
- ✅ 前端依赖包（`node_modules/`、`uni_modules/`）
- ✅ IDE 缓存文件（`.idea/`、`.vscode/`）
- ✅ 本地交付文档（`docs/` 下的需求与交付说明）

> 仓库中只保留 `application-template.yml` 模板，其中所有敏感项均为 `【填写xxx】` 占位符。

### 鉴权与业务安全

| 措施 | 说明 |
|------|------|
| 网关统一鉴权 | JWT 校验收敛到网关，强制校验签名与有效期 |
| 白名单最小化 | 仅放行登录接口与游客态只读接口，所有写接口必须携带 token |
| 知识库权限 | 只能删除自己上传的文档；`userId=0` 的系统文档不可删除 |
| 上传类型白名单 | 仅允许 `pdf / docx / txt / xlsx`，同时校验扩展名与 MIME |
| 上传大小限制 | 单文件 ≤ 10 MB，单次请求 ≤ 50 MB |
| SQL 注入防护 | 全部使用 MyBatis-Plus 参数化查询 |
| 路径穿越防护 | 删除 RAG 文档时校验路径前缀，防止误删 `rag-docs` 目录外的文件 |

### GitHub 开源规范

1. **永远不要**将 `application.yml` 提交到仓库，仓库中仅保留 `application-template.yml` 模板
2. 定期更换生产环境的 JWT 密钥和 API 密钥
3. 生产环境建议使用环境变量或配置中心（Nacos）注入敏感配置
4. 生产环境应通过防火墙/安全组限制 8081、8082 端口**仅内网可达**（见[已知限制](#-已知限制)）

---

## ⚠️ 已知限制

| # | 限制 | 说明与应对 |
|:-:|------|-----------|
| 1 | **内存向量库不持久** | 默认 `store-type=memory`，服务重启后向量丢失，需调 `POST /api/rag/reindex` 重建。生产环境切 `store-type=redis` |
| 2 | **Redis 向量存储需 Redis Stack** | 普通 Redis 5.x/6.x 缺 RediSearch 模块，切 `redis` 后会**启动失败并明确报错**（故意不静默降级） |
| 3 | **BGE 模型首次启动较慢** | 首次需从 jar 提取约 90 MB ONNX 到 D 盘并初始化 ONNX 会话；后续直接读 D 盘文件，明显更快 |
| 4 | **模型常驻内存** | 模型加载后常驻堆内存（约 95 MB 模型 + ORT 会话），`travel-ai-service` 建议 `-Xmx` ≥ 1 GB |
| 5 | **无单点登录** | 新架构下网关只验签，同一账号可在多设备同时在线（原「新登录挤掉旧设备」能力默认关闭） |
| 6 | **Nacos 单机模式** | 开发用 `-m standalone`，生产需集群模式 |
| 7 | **内部接口暴露风险** | `/api/internal/spark/**` 未注册到网关，但若 `travel-ai-service` 的 8082 端口对外网开放，理论上可被直接访问。生产环境须限制 8081、8082 仅内网可达 |
| 8 | **建表脚本无示例数据** | `travel_init.sql` 只含 DDL，导入后各业务表为空，需自行录入数据 |
| 9 | **C 盘空间** | 建议为系统临时文件保留至少 500 MB 可用空间 |

---

## ❓ 常见问题

### Q1: 导入 SQL 报错 "Unknown character set" 或 "utf8mb4_0900_ai_ci"？

**原因**：MySQL 版本低于 8.0，不支持 `utf8mb4_0900_ai_ci` 排序规则（脚本中 12 张表使用了该规则）。

**解决**：升级 MySQL 到 8.0+，或手动将 SQL 文件中所有 `utf8mb4_0900_ai_ci` 替换为 `utf8mb4_unicode_ci`。

---

### Q2: 导入 SQL 成功，但首页没有景点、AI 也检索不到内容？

**原因**：`travel_init.sql` **只含建表语句，不含任何示例数据**。

**解决**：
1. 通过小程序「新增景点」页面上传景点（图片走 MinIO，入库为相对路径），或直接用 SQL 向 `scenic`、`banner`、`question` 表插入数据
2. 录入景点后，调用一次 `POST /api/rag/sync/scenic` 把景点同步进知识库，这样 AI 回答才有参考来源

---

### Q3: 景点图片显示不出来？

**排查步骤：**
1. 确认 MinIO 已启动且 `travel` 桶已创建：`docker ps | grep minio`
2. 确认桶的访问策略已设为 `Read Only` / `Public`（见 [3.4 节](#34-配置桶的访问权限重要)）
3. 检查数据库中 `scenic.img` 的值：应为**相对路径**（`scenic/xxx.jpg`），而不是绝对 URL
   - 若库里是旧 IP 的绝对 URL，执行 `migration_v2_relative_image_path.sql` 迁移
4. 真机预览时，`uniapp-front/api/config.js` 里的 `BASE_URL` 必须改成电脑局域网 IP，不能用 `localhost`

> 💡 图片完整地址 = `MINIO_BASE` + 相对路径，其中 `MINIO_BASE` 由 `BASE_URL` 自动推导
> （协议 + 主机名不变，端口换成 9000）。所以**换电脑/换网络只需改 `BASE_URL` 一行**。

---

### Q4: MinIO 连接失败，上传图片报错？

**排查步骤：**
1. 检查本地 MinIO 服务是否已启动：`docker ps | grep minio`
2. 确认端口 9000（API）和 9001（控制台）是否开放
3. 核对 `application.yml` 中的 `endpoint`、`access-key`、`secret-key` 是否正确
4. 确认 `travel` 存储桶已创建，且权限为 `public` 或已配置正确的访问策略
5. **重点检查 `minio.endpoint` 是否写的是 9000 端口**（不是 9001 控制台端口）
6. 检查微信小程序是否勾选「不校验合法域名」

---

### Q5: 微信登录报错 "appid missing" 或 "code 无效"？

**排查步骤：**
1. 检查 `travel-user-service` 的 `application.yml` 中 `wechat.mini.appid` 和 `appsecret` 是否配置正确
2. 确认小程序 AppID 与当前运行的微信开发者工具绑定一致
3. 检查前端 `wx.login()` 获取的 `code` 是否及时传给后端（code 有效期约 5 分钟）
4. 本地开发时，在微信开发者工具中勾选「详情 → 本地设置 → 不校验合法域名」

---

### Q6: AI 问答无响应或返回错误？

**排查步骤：**
1. 核对讯飞星火开放平台中的 `app-id`、`api-key`、`api-secret` 是否过期
2. 确认讯飞账户已开通「星火大模型 API」权限
3. 检查网络是否能访问 `https://spark-api-open.xf-yun.com`
4. 确认请求打到的是 **ai-service**（`/api/chat/**`、`/api/plan/**`、`/api/rag/**` 路由到 8082）
5. 查看后端日志，确认请求参数和返回错误码

---

### Q7: 接口报 503 Service Unavailable？

**原因**：网关启动了但下游服务未注册到 Nacos。

**解决**：
1. 打开 Nacos 控制台 `http://localhost:8848/nacos` → 服务列表，确认三个服务实例都在
2. 按正确顺序重启：**先起 `user-service` / `ai-service`，最后起 `gateway`**
3. 检查各服务的 `Nacos` 连接配置（`server-addr: localhost:8848`）

---

### Q8: 接口报 401 未授权？

**排查步骤：**
1. 确认请求头带了 `Authorization: Bearer <token>`
2. **检查三个服务的 `jwt.secret` 是否完全一致** —— 不一致会导致网关无法解析 user-service 签发的 token
3. 该接口是否在白名单内？白名单接口无需 token（见 [JWT 统一鉴权流程](#jwt-统一鉴权流程)）
4. 如果你是**从旧版本升级**：改造前签发的旧 token 载荷里没有 `userId`，网关会返回 401，**重新登录即可**（token 有效期 1 天）

---

### Q9: 后端启动成功，但前端请求接口报 404？

**排查步骤：**
1. 确认网关端口为 8080，且请求路径带 `/api` 前缀（如 `http://localhost:8080/api/scenic/list`）
2. 检查 `application.yml` 缩进是否正确
3. 确认该路径前缀已在网关路由表中配置（见 [网关路由表](#网关路由表)）
4. 清除微信开发者工具缓存，重新编译前端

---

### Q10: RAG 问答没有参考来源（sources 为空）？

**排查步骤：**
1. 确认知识库非空：`GET /api/rag/document/list` 应能看到文档，`status=COMPLETED`
2. 若刚重启过 `travel-ai-service`，内存向量库已清空 → 调用 `POST /api/rag/reindex` 重建
3. 用 `GET /api/rag/retrieve?query=xxx` 测试纯检索，若返回空说明相似度低于 `min-score`（默认 0.3）
4. 前端确认「知识库增强」开关处于开启状态，且请求体 `useRag` 为 `true`

---

### Q11: 切换到 Redis 向量存储后服务启动失败？

**原因**：`rag.store-type=redis` 需要 **Redis Stack**（含 RediSearch 模块），普通 Redis 5.x/6.x 不支持向量检索。

**解决**：安装 [Redis Stack](https://redis.io/docs/latest/operate/oss_and_stack/install/install-stack/)，
或把 `rag.store-type` 改回 `memory`。程序**故意不做静默降级**，报错是为了避免误以为向量已持久化。

---

### Q12: Redis 连接报错 "Connection refused"？

**排查步骤：**
1. 确认本地 Redis 服务已启动：`redis-cli ping` 应返回 `PONG`
2. 检查 `application.yml` 中 Redis 的 `host` 和 `port` 配置
3. 如果 Redis 设置了密码，确保 `password` 字段已填写

> 💡 清理 Redis 缓存时**不要用 `flushdb`** —— 那会连同 `user:token:*` 登录态一起清掉，
> 导致所有用户被强制退出登录。应使用 `redis-cli keys "scenic*"` 先列出确认，再 `redis-cli del` 逐个删除。

---

## 📜 许可证

本项目基于 [MIT License](LICENSE) 开源协议发布，您可以自由使用、修改和分发。

> ⚠️ **声明**：本项目仅供学习和技术交流使用，不得用于商业用途。项目中涉及的第三方 API（微信、讯飞星火等）请遵守各自平台的使用协议。

---

## 🙏 致谢

- [Spring Boot](https://spring.io/projects/spring-boot) / [Spring Cloud](https://spring.io/projects/spring-cloud)
- [Nacos](https://nacos.io/)
- [LangChain4j](https://github.com/langchain4j/langchain4j)
- [MyBatis-Plus](https://baomidou.com/)
- [UniApp](https://uniapp.dcloud.net.cn/)
- [讯飞星火大模型](https://xinghuo.xfyun.cn/)
- [MinIO](https://min.io/)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请点个 Star 支持一下！**

**欢迎提交 Issue 和 PR，一起完善这个项目！**

</div>
