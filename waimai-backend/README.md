# 校园外卖聚合平台 - 后端服务

## 项目说明

基于 Spring Boot 3.2.x + JDK 21 的后端服务，提供外卖聚合平台的 API 接口。

## 技术栈

- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis 7.x
- RabbitMQ 3.x
- JWT

## 快速开始

### 1. 环境准备

- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.x（可选）
- RabbitMQ 3.x（可选）

### 2. 数据库配置

1. 创建数据库：

```sql
CREATE DATABASE waimai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. 执行数据库脚本（参考 `docs/数据库设计.md`）

3. 修改 `src/main/resources/application.yml` 中的数据库连接信息

### 3. 配置修改

修改 `src/main/resources/application.yml`：

- 数据库连接信息
- Redis 连接信息（如使用）
- RabbitMQ 连接信息（如使用）
- JWT secret（生产环境必须修改）
- 微信小程序 appid 和 secret

### 4. 启动服务

```bash
mvn clean install
mvn spring-boot:run
```

服务默认启动在：http://localhost:8080

### 5. API 文档

启动后访问：http://localhost:8080/swagger-ui.html

## 项目结构

```
src/main/java/com/school/waimai/
├── common/              # 通用模块
│   ├── api/            # 统一响应、分页、错误码
│   ├── exception/      # 异常处理
│   ├── config/         # 配置类
│   └── util/           # 工具类
├── auth/               # 鉴权模块
│   ├── controller/     # 控制器
│   ├── service/        # 服务层
│   ├── domain/         # 领域模型
│   └── repository/     # 数据访问层
├── shop/               # 商家模块
├── category/           # 分类模块
├── recommend/          # 推荐模块
├── rating/             # 评分模块
├── favorite/           # 收藏模块
└── log/                # 日志模块
```

## 开发说明

### 统一响应格式

所有接口统一返回 `ApiResponse<T>`：

```java
ApiResponse.success(data)  // 成功
ApiResponse.error(code, message)  // 失败
```

### 分页查询

使用 `PageResult<T>` 封装分页结果：

```java
PageResult.of(total, page, pageSize, list)
```

### 异常处理

使用 `BizException` 抛出业务异常：

```java
throw new BizException(ErrorCode.Shop.ERR_SHOP_NOT_FOUND);
```

全局异常处理器会自动捕获并转换为统一响应格式。

## 接口前缀

- 小程序端：`/api/mp`
- 后台管理端：`/api/admin`

## 注意事项

1. 生产环境必须修改 JWT secret
2. 数据库连接信息不要提交到代码仓库
3. 敏感配置建议使用环境变量或配置中心
