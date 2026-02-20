# 校园外卖信息聚合平台

校园外卖信息聚合平台是一个面向单一高校、单一校区的校园外卖商家信息聚合与跳转服务。本平台仅对校内外卖商家信息进行统一展示，用户通过本平台跳转至美团、饿了么、京东及小程序自营等第三方平台完成下单，**本平台不参与任何交易环节**。

## 项目结构

```
校园外卖与二手平台/
├── docs/                    # 项目文档
│   ├── 外卖聚合平台需求说明书.md
│   ├── 项目结构说明.md
│   ├── 详细设计-API接口设计.md
│   ├── 技术栈确认.md
│   ├── 错误码设计.md
│   └── 数据库设计.md
├── waimai-backend/          # 后端服务（Spring Boot）
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/school/waimai/
│   │       │       ├── common/        # 通用模块
│   │       │       ├── auth/          # 鉴权模块
│   │       │       ├── shop/          # 商家模块
│   │       │       ├── category/     # 分类模块
│   │       │       ├── recommend/     # 推荐模块
│   │       │       ├── rating/        # 评分模块
│   │       │       ├── favorite/     # 收藏模块
│   │       │       └── log/           # 日志模块
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
├── waimai-admin/            # 后台管理前端（Vue3 + Vite）
│   ├── src/
│   │   ├── api/             # API 接口
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 公共组件
│   │   ├── router/          # 路由配置
│   │   ├── store/           # 状态管理
│   │   └── utils/           # 工具函数
│   └── package.json
└── waimai-miniprogram/      # 小程序端（微信原生）
    ├── pages/               # 页面
    ├── utils/               # 工具函数
    ├── config/              # 配置文件
    └── app.json
```

## 技术栈

### 后端

- **框架**: Spring Boot 3.2.x
- **JDK**: 21
- **ORM**: MyBatis + MyBatis-Plus 3.5.x
- **数据库**: MySQL 8.0.x
- **缓存**: Redis 7.x
- **消息队列**: RabbitMQ 3.x
- **鉴权**: JWT
- **构建工具**: Maven

### 后台管理前端

- **框架**: Vue 3.4+
- **构建工具**: Vite 5.x
- **语言**: TypeScript
- **UI 组件库**: Ant Design Vue 4.x
- **状态管理**: Pinia
- **路由**: Vue Router 4.x
- **HTTP 客户端**: Axios

### 小程序端

- **开发方式**: 微信原生小程序
- **语言**: JavaScript

## 快速开始

### 环境要求

- JDK 21+
- Node.js 18.x / 20.x LTS
- MySQL 8.0+
- Redis 7.x
- RabbitMQ 3.x（可选，一期可暂不启用）
- Maven 3.6+

### 后端启动

1. 配置数据库连接信息（`waimai-backend/src/main/resources/application.yml`）
2. 执行数据库脚本（参考 `docs/数据库设计.md`）
3. 启动 Redis（如需要）
4. 启动 RabbitMQ（如需要）
5. 运行主类 `WaimaiApplication`

```bash
cd waimai-backend
mvn clean install
mvn spring-boot:run
```

### 后台管理前端启动

```bash
cd waimai-admin
npm install
npm run dev
```

访问：http://localhost:3000

### 小程序端启动

1. 使用微信开发者工具打开 `waimai-miniprogram` 目录
2. 配置 `project.config.json` 中的 `appid`
3. 配置 `config/env.js` 中的后端接口地址
4. 编译运行

## 功能模块

### 小程序端

- ✅ 每日推荐（手动设置/随机）
- ✅ 商家列表（筛选/搜索/排序）
- ✅ 商家详情与跳转
- ✅ 商家评分（1-5星）
- ✅ 收藏商家
- ✅ 我的收藏列表

### 后台管理端

- ✅ 商家管理（CRUD、上下架）
- ✅ 每日推荐设置
- ✅ 分类管理
- ✅ 评分统计查看
- ✅ 管理员账号管理

## 开发规范

### 代码规范

- 后端遵循 Java 编码规范，使用 Lombok 简化代码
- 前端遵循 Vue 3 Composition API 规范
- 统一使用中文注释

### 接口规范

- RESTful 风格
- 统一返回格式：`{code, message, data}`
- 错误码定义见 `docs/错误码设计.md`

### 数据库规范

- 表名小写下划线
- 统一字段：`create_time`, `update_time`, `deleted`
- 字符集：utf8mb4

## 文档

详细文档请参考 `docs/` 目录：

- [需求说明书](docs/外卖聚合平台需求说明书.md)
- [项目结构说明](docs/项目结构说明.md)
- [API 接口设计](docs/详细设计-API接口设计.md)
- [技术栈确认](docs/技术栈确认.md)
- [错误码设计](docs/错误码设计.md)
- [数据库设计](docs/数据库设计.md)

## 注意事项

1. **生产环境配置**：请修改 `application.yml` 中的敏感信息（JWT secret、数据库密码等）
2. **微信小程序配置**：需要在微信公众平台配置服务器域名
3. **HTTPS 要求**：微信小程序要求服务器域名必须使用 HTTPS
4. **免责声明**：平台仅做信息聚合与跳转，不参与交易

## 许可证

本项目仅供学习使用。
