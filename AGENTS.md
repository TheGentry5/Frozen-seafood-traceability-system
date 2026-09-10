# AGENTS.md

冷冻海产品溯源系统。单仓库包含两个应用：根目录为 Spring Boot 后端，`frontend/` 为 Vue 3 前端。

## 目录结构
- 后端：Spring Boot 3.5.4 + MyBatis-Plus 3.5.9，Java 17。代码在 `src/main/java`，SQL 在 `db/`，Mapper XML 在 `src/main/resources/mapper/*.xml`。端口 8080。
- 前端：Vue 3 + Vite（纯 JS，无 TypeScript），位于 `frontend/`。开发端口 5173，`/api` 代理到 `http://localhost:8080`（`frontend/vite.config.js`）。
- Java 包名使用下划线：`com.example.frozen_seafood_traceability_system`。这是脚手架生成的，禁止“规范化”成短横线。
- `frontend/node_modules/` 与 `frontend/dist/` 已通过 `frontend/.gitignore` 忽略并从索引移除。`npm install` / `npm run build` 仅在必要时执行。

## 命令
- 一键启动（同时起后端+前端）：`.\run.ps1`（参数 `-Mode both|backend|frontend`、`-InitDb`、`-Install`、`-CheckOnly`；UTF-8 with BOM，须用 PowerShell 运行）。文件编码含非 ASCII 时 PowerShell 5.1 需 BOM，改动该脚本后请保留 BOM。
- 后端编译：`.\mvnw.cmd -DskipTests compile`（PowerShell）/ `./mvnw -DskipTests compile`
- 后端运行：`.\mvnw.cmd spring-boot:run`
- 后端测试：`.\mvnw.cmd test` —— 依赖 MySQL 与 `dev` profile。含 `contextLoads` 及集成测试套件（`AuthIntegrationTest`/`BatchFlowIntegrationTest`/`AdminNodeIntegrationTest`/`HttpContractIntegrationTest`，基于 MockMvc，走真实 SQL 且每用例事务回滚、测试前重建种子数据，可重复运行不污染数据库）。
- 前端（在 `frontend/` 下）：`npm run dev`、`npm run build`、`npm run preview`。没有 lint / typecheck / test 脚本。

## 数据源配置
`src/main/resources/application.yml` 使用 Hikari（`spring.datasource.*`），参数取自 `${traceability.datasource.*}`，默认激活 `dev` profile。本地数据库参数放在 gitignore 的 `src/main/resources/application-dev.yml`（由 `application-dev-template.yml` 复制而来）。干净的 checkout 需先复制该模板，否则启动报占位符未解析。

## 数据库
- MySQL 数据库 `seafood_trace`（utf8mb4）。按顺序执行：`db/schema.sql`、`db/area.sql`、`db/data.sql`。
- 初始登录：节点编码 `FARM001` / `PROC001` / `WHOL001` / `RETA001`，管理员 `admin`；密码均为 `123456`。
- 密码为无盐小写 MD5 32 位十六进制（`DigestUtils.md5DigestAsHex`）。
- 实体瞬态字段（`nodeName`、`inNodeName`、`inBatchCode`、`nodeTypeName`、`provinceName`、`cityName`）只由 XML 的 `JOIN ... AS` 别名填充。`map-underscore-to-camel-case: true` 已开启，别名列需对应蛇形字段名。

## 与默认约定不同之处
- 鉴权使用自定义 `X-Token` 请求头，由内存中的 `common/TokenStore` 支撑（单实例；重启后所有 token 失效；带 2 小时滑动过期，按主体可批量吊销）。拦截器白名单在 `config/WebMvcConfig.java`：登录、管理员登录、`/api/trace/info/**`。`LoginInterceptor` 每次请求重查 `node_info.status`，停用/改类型后已发 token 立即失效。
- 所有接口 HTTP 状态码恒为 200，响应体为 `{code, msg, data}`。成功为 `code=200`；失败抛出带 `common/BizCode` 的 `BizException`（400/401/403/404/409/500）。前端 axios 拦截器（`frontend/src/util.js`）会解包 `data` 并根据 `code` 处理，401 时清除登录态。
- 状态/类型常量以 `common/StatusConst.java` 为准（节点类型 1–4；养殖状态 1–3；链条状态 1–4）。前端展示文案与颜色在 `frontend/src/modules.js`。

## 前端结构
- 四类节点角色复用 `src/views/node/` 下的同一批页面组件。所有按角色的差异（列、上游、状态选项、标签）都是 `frontend/src/modules.js` 中的数据；应修改该文件而不是复制组件。各角色路由由 `frontend/src/router/index.js` 依据 `MODULES` 生成。
- 冷链温度填报/曲线：`views/node/BatchDetail.vue` 与 `views/consumer/ConsumerTrace.vue`，后端为 `/api/cold-chain`（`ColdChainController`）。

## 文档
- `docs/开发实施文档.md` 是接口/DDL/SQL 的权威契约；代码注释引用其章节号。
- `docs/开发实施文档-业务待办清单.md` 已标注为历史任务书（全部任务含冷链均已实现）；以源码为准。
- `HELP.md` 是已被 gitignore 的脚手架产物。
