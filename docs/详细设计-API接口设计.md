# 外卖聚合模块 — 详细设计 / API 接口设计（一期）

> 说明：本详细设计基于《外卖聚合平台需求说明书》《技术栈确认》《数据库设计》《概要设计》编写，仅覆盖一期范围（不含“用户申请添加商家”等二期功能）。

---

## 1. 接口通用约定

### 1.1 域名与前缀

- 后端服务基础地址示例：`https://api.xxx.com`
- 小程序端业务接口前缀：`/api/mp`
- 后台管理端业务接口前缀：`/api/admin`
- 公共/字典类接口前缀（如有）：`/api/common`

### 1.2 统一返回结构

所有接口统一使用 JSON 返回：

```json
{
  "code": 0,
  "message": "OK",
  "data": { ... }
}
```

- `code`：
  - `0`：成功
  - 非 0：失败（业务错误码，由后端约定，比如 4001=参数错误、4010=未登录、4040=数据不存在等）
- `message`：用户可读或用于前端展示的错误信息
- `data`：具体业务数据（列表、详情、布尔结果等）

### 1.3 鉴权与头信息

- 小程序端：
  - 登录成功后下发 `token`（JWT），前端存储在本地。
  - 所有需登录接口在 Header 中携带：`Authorization: Bearer <token>`
- 后台管理端：
  - 登录成功后下发 `token`，规则同上。

### 1.4 时间、分页等约定

- 时间格式：统一使用 `yyyy-MM-dd HH:mm:ss`（字符串）或毫秒时间戳（由前后端约定，一期建议使用字符串）。
- 分页请求参数：
  - `page`：页码，从 1 开始
  - `pageSize`：每页条数，默认 10
- 分页响应字段：
  - `total`：总条数
  - `page`：当前页码
  - `pageSize`：每页条数
  - `list`：当前页数据数组

---

## 2. 鉴权与用户模块

### 2.1 小程序登录

**接口**：`POST /api/mp/auth/login`  
**说明**：小程序端调用微信登录接口拿到 `code` 后，后端通过微信接口换取 `openid`，创建/更新用户并签发 JWT。

#### 请求

```json
{
  "code": "wx-login-code",
  "nickname": "可选昵称",
  "avatar": "可选头像URL"
}
```

#### 响应 `data`

```json
{
  "token": "jwt-string",
  "expireAt": "2025-02-10 12:00:00"
}
```

---

### 2.2 管理员登录

**接口**：`POST /api/admin/auth/login`

#### 请求

```json
{
  "username": "admin",
  "password": "plaintext-or-hash-by-protocol"
}
```

#### 响应 `data`

```json
{
  "token": "jwt-string",
  "expireAt": "2025-02-10 12:00:00"
}
```

---

## 3. 小程序端业务接口（前缀统一为 `/api/mp`）

### 3.1 商家列表（小程序）

**接口**：`GET /api/mp/shops`

**说明**：分页查询上架商家列表，支持平台/分类筛选、名称搜索、排序。

#### 请求参数（Query）

- `page`：页码，默认 1
- `pageSize`：每页条数，默认 10
- `platform`：所属平台枚举，可选，取值：`MEITUAN`、`ELEME`、`JD`、`MINIPROGRAM_SELF`、`OTHER`
- `categoryId`：分类 ID，可选
- `sort`：排序字段，可选，取值：
  - `updatedAt`（默认，按更新时间倒序）
  - `weight`（按排序权重倒序）
- `keyword`：名称关键词，可选（按商家名称模糊匹配）

#### 响应 `data`

```json
{
  "total": 100,
  "page": 1,
  "pageSize": 10,
  "list": [
    {
      "id": 1,
      "name": "校内麻辣烫",
      "introduction": "正宗川味麻辣烫，食材新鲜，口味地道",
      "platforms": ["MEITUAN", "ELEME"],
      "categoryId": 10,
      "categoryName": "快餐",
      "coverUrl": "https://oss/xxx.jpg",
      "startingPrice": 15.0,
      "deliveryFeeDesc": "配送费约3元",
      "avgScore": 4.6,
      "ratingCount": 123
    }
  ]
}
```

---

### 3.2 商家详情（小程序）

**接口**：`GET /api/mp/shops/{id}`

**说明**：返回商家详情信息 + 平台跳转链接 + 评分统计 + 是否已收藏。

#### 请求参数

- 路径参数：`id` 商家 ID

#### 响应 `data`

```json
{
  "id": 1,
  "name": "校内麻辣烫",
  "introduction": "正宗川味麻辣烫，食材新鲜，口味地道",
  "categoryId": 10,
  "categoryName": "快餐",
  "coverUrl": "https://oss/xxx.jpg",
  "businessHours": "10:00-22:00",
  "deliveryScope": "校园内配送",
  "startingPrice": 15.0,
  "deliveryFeeDesc": "配送费约3元",
  "status": 1,
  "platformLinks": [
    {
      "platform": "MEITUAN",
      "type": "H5",
      "url": "https://waimai.meituan.com/...shop"
    },
    {
      "platform": "MINIPROGRAM_SELF",
      "type": "MINI_PATH",
      "path": "/pages/self-shop/index?id=1"
    }
  ],
  "avgScore": 4.6,
  "ratingCount": 123,
  "userScore": 5, // 当前用户的评分，未评分则可为 null
  "favorited": true, // 当前用户是否已收藏
  "canOrder": true // 若商家下架或不允许下单，则为 false，用于前端隐藏“去下单”按钮
}
```

> 规则：当商家为“已下架”但在用户收藏列表中仍展示时，`status=0`，`favorited=true`，`canOrder=false`。

---

### 3.3 每日推荐（小程序）

**接口**：`GET /api/mp/recommend/daily`

**说明**：返回当日推荐商家列表，数量按后台配置 N（默认 4），手动优先，其次随机。结果按自然日固定。

#### 请求参数

无（根据当前日期与后台配置生成）。

#### 响应 `data`

```json
{
  "date": "2025-02-10",
  "mode": "MANUAL", // MANUAL/RANDOM
  "list": [
    {
      "id": 1,
      "name": "校内麻辣烫",
      "introduction": "正宗川味麻辣烫，食材新鲜，口味地道",
      "categoryName": "快餐",
      "coverUrl": "https://oss/xxx.jpg",
      "startingPrice": 15.0,
      "deliveryFeeDesc": "配送费约3元",
      "avgScore": 4.6,
      "ratingCount": 123
    }
  ]
}
```

---

### 3.4 商家评分（小程序）

#### 3.4.1 提交/更新评分

**接口**：`POST /api/mp/shops/{id}/rating`（需登录）

#### 请求体

```json
{
  "score": 4
}
```

- `score`：1～5 的整数

#### 响应 `data`

```json
{
  "avgScore": 4.5,
  "ratingCount": 120
}
```

> 规则：若用户此前已评分，则视为“更新评分”，`ratingCount` 不变，仅按规则更新 `avgScore`。

---

### 3.5 收藏商家（小程序）

#### 3.5.1 收藏

**接口**：`POST /api/mp/shops/{id}/favorite`（需登录）

**说明**：为当前用户添加收藏记录（幂等，多次调用不报错）。

请求体：无或空 JSON  
响应 `data`：

```json
{
  "favorited": true
}
```

#### 3.5.2 取消收藏

**接口**：`DELETE /api/mp/shops/{id}/favorite`（需登录）

响应 `data`：

```json
{
  "favorited": false
}
```

#### 3.5.3 我的收藏列表

**接口**：`GET /api/mp/user/favorites`（需登录）

**请求参数（Query）**：

- `page`、`pageSize`（同通用约定）

**响应 `data`**：

```json
{
  "total": 20,
  "page": 1,
  "pageSize": 10,
  "list": [
    {
      "shopId": 1,
      "name": "校内麻辣烫",
      "introduction": "正宗川味麻辣烫，食材新鲜，口味地道",
      "categoryName": "快餐",
      "coverUrl": "https://oss/xxx.jpg",
      "startingPrice": 15.0,
      "deliveryFeeDesc": "配送费约3元",
      "avgScore": 4.6,
      "ratingCount": 123,
      "favoritedAt": "2025-02-10 09:00:00",
      "status": 1, // 1=上架，0=下架
      "canOrder": true, // 下架时为 false
      "statusLabel": null // 下架时可为 "已下架"
    }
  ]
}
```

---

## 4. 后台管理端接口（Admin）

### 4.1 商家管理

#### 4.1.1 商家列表（后台）

**接口**：`GET /api/admin/shops`

**请求参数（Query）**：

- `page`、`pageSize`
- `platform`：平台枚举，可选
- `categoryId`：分类 ID，可选
- `status`：状态，可选（1=上架，0=下架）
- `keyword`：名称关键词，可选
- `sort`：`updatedAt` / `weight`

**响应 `data`**：

类似小程序列表，多出后台信息（状态、排序权重、更新时间等）。

#### 4.1.2 新增商家

**接口**：`POST /api/admin/shops`

**请求体（示例）**：

```json
{
  "name": "校内麻辣烫",
  "introduction": "正宗川味麻辣烫，食材新鲜，口味地道",
  "categoryId": 10,
  "coverUrl": "https://oss/xxx.jpg",
  "businessHours": "10:00-22:00",
  "deliveryScope": "校园内配送",
  "startingPrice": 15.0,
  "deliveryFeeDesc": "配送费约3元",
  "sortWeight": 0,
  "status": 0,
  "platformLinks": [
    {
      "platform": "MEITUAN",
      "url": "https://waimai.meituan.com/..."
    },
    {
      "platform": "MINIPROGRAM_SELF",
      "path": "/pages/self-shop/index?id=1"
    }
  ],
  "remark": "门口小摊，口味不错"
}
```

**响应 `data`**：

```json
{ "id": 1 }
```

#### 4.1.3 编辑商家

**接口**：`PUT /api/admin/shops/{id}`

请求体同新增，允许部分字段修改。  
响应可返回空对象或更新后的简要信息。

#### 4.1.4 删除商家（逻辑删除）

**接口**：`DELETE /api/admin/shops/{id}`

**说明**：标记 `deleted=1`，数据不物理删除。

---

### 4.2 商家上下架

**接口**：`POST /api/admin/shops/{id}/status`

**请求体**：

```json
{
  "status": 1 // 1=上架，0=下架
}
```

**说明**：

- 上架：仅允许状态合法的商家（数据字段完整、至少一个有效平台链接）上架。
- 下架：不会删除收藏记录；小程序端在收藏中标注“已下架”且隐藏下单按钮。

---

### 4.3 分类管理

#### 4.3.1 分类列表

**接口**：`GET /api/admin/categories`

返回所有分类及状态、排序字段。

#### 4.3.2 新增分类

**接口**：`POST /api/admin/categories`

**请求体**：

```json
{
  "name": "快餐",
  "logoUrl": "https://oss/xxx/category-fastfood.png",
  "sortOrder": 10,
  "status": 1
}
```

> 说明：`logoUrl` 为可选字段，用于存储分类的 Logo/图标图片 URL。

#### 4.3.3 编辑分类

**接口**：`PUT /api/admin/categories/{id}`

#### 4.3.4 禁用/启用分类

**接口**：`POST /api/admin/categories/{id}/status`

**请求体**：

```json
{
  "status": 0 // 1=启用，0=禁用
}
```

**规则**：

- 有商家引用时禁止删除分类，只允许禁用；
- 若需彻底删除，需先迁移商家到其他分类，然后允许 `DELETE /api/admin/categories/{id}`。

---

### 4.4 每日推荐配置（后台）

#### 4.4.1 查询当日配置

**接口**：`GET /api/admin/recommend/daily-config`

**响应 `data`**：

```json
{
  "bizDate": "2025-02-10",
  "mode": "MANUAL", // MANUAL/RANDOM
  "manualShopIds": [1, 2], // 手动模式下的商家ID
  "randomCount": 4 // 随机模式每日展示数量
}
```

#### 4.4.2 保存配置

**接口**：`PUT /api/admin/recommend/daily-config`

**请求体**：

```json
{
  "mode": "MANUAL",
  "manualShopIds": [1, 2, 3],
  "randomCount": 4
}
```

> 规则：保存后对当日生效；次日若未重新配置手动模式，自动按随机模式生成推荐。

---

### 4.5 评分统计查看（后台）

**接口**：`GET /api/admin/shops/ratings`

**说明**：按商家维度列出评分统计（平均分、评分人数），可按商家名称、分类、状态筛选。

**请求参数**：

- `page`、`pageSize`
- `categoryId`、`status`、`keyword`

**响应 `data`**：

```json
{
  "total": 50,
  "page": 1,
  "pageSize": 10,
  "list": [
    {
      "shopId": 1,
      "name": "校内麻辣烫",
      "categoryName": "快餐",
      "avgScore": 4.6,
      "ratingCount": 123,
      "lastRatingTime": "2025-02-10 09:00:00"
    }
  ]
}
```

---

## 5. 操作日志（概要）

> 详细字段见 `数据库设计.md` 中 `operation_log` 表设计。

- 建议提供后台接口：`GET /api/admin/operation-logs`
  - 支持按管理员、时间范围、对象类型（SHOP/CATEGORY/RECOMMEND）筛选；
  - 分页返回最近操作，用于排查问题。
- 写日志时机（示例）：
  - 商家创建/编辑/删除/上下架
  - 分类创建/编辑/删除/禁用
  - 每日推荐配置变更

一期可同步写库；后续如需要可用 MQ 异步化日志写入。

---

## 6. 后续详细设计可扩展部分

本文件为 API 级详细设计，后续如有需要可扩展：

- DTO/VO/Entity 类设计（字段、校验规则、映射关系）
- 典型业务流程的时序图（已在《概要设计》中给出示例）
- 错误码表（code 与业务含义的完整清单）
- 安全细节（防刷、防重放、限流规则等）
