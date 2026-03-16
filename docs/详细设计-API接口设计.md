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
  "password": "plaintext",
  "captchaId": "可选（当登录失败次数达到阈值后必填）",
  "captchaCode": "可选（当登录失败次数达到阈值后必填）"
}
```

#### 响应 `data`

```json
{
  "token": "jwt-string",
  "expireAt": "2025-02-10 12:00:00",
  "refreshToken": "refresh-token-string",
  "refreshExpireAt": "2025-03-12 12:00:00"
}
```

---

### 2.3 管理员注册（补齐）

> 说明：当前文档 DTO/VO 中已存在 `AdminRegisterRequest`，但接口清单未定义。本节补齐接口与返回结构。
>
> **推荐策略**（默认）：后台不开放公网自助注册，仅允许“已登录管理员”创建新管理员账号（对应已存在的 `POST /api/admin/admin-users`）。
> 若你确实需要“后台登录页自助注册”，请采用下面接口并配合验证码/注册开关/限流。

#### 2.3.1 自助注册（启用）

**接口**：`POST /api/admin/auth/register`

**请求**

```json
{
  "username": "newadmin",
  "password": "password123",
  "confirmPassword": "password123",
  "captchaId": "必填",
  "captchaCode": "必填"
}
```

**响应 `data`**（建议返回 id）

```json
{ "id": 2 }
```

**错误码建议**

- `4091`：用户名已存在
- `4002`：两次密码不一致
- `4291`：操作过于频繁（触发限流/防刷）
- `4012`：验证码错误/过期
- `4032`：注册已关闭（注册开关关闭）

---

### 2.4 管理员登出（补齐）

**接口**：`POST /api/admin/auth/logout`（需登录）

**说明**：

- 本项目要求 **“登出立即失效”**：服务端需让当前 accessToken 与 refreshToken 立刻不可用。
- 本项目采用：**Redis token blacklist（基于 `jti`）**：
  - JWT 载荷中包含 `jti`（随机唯一 ID）
  - 登出时把当前 accessToken 的 `jti` 写入 Redis 黑名单，TTL=token 剩余有效期
  - 鉴权时除验签/过期外，还需检查 `jti` 是否命中黑名单；命中则返回 `4014`
  - refreshToken 建议存 Redis（或 DB），登出时删除/撤销该管理员当前 refreshToken（返回 `4014`）

**请求体**：无或空 JSON  
**响应 `data`**：无（空对象）

---

### 2.5 刷新 Token（补齐，推荐）

**接口**：`POST /api/admin/auth/refresh`（需登录或携带 refreshToken）

**说明**：用于延长后台登录态（需求里提到后台会话超时 24 小时）。本项目采用 **accessToken + refreshToken**。

#### 方案 A（推荐）：accessToken + refreshToken

**请求**

```json
{
  "refreshToken": "refresh-token-string"
}
```

**响应 `data`**

```json
{
  "token": "new-jwt-string",
  "expireAt": "2025-02-10 12:00:00",
  "refreshToken": "new-refresh-token-string",
  "refreshExpireAt": "2025-03-12 12:00:00"
}
```

**错误码建议**

- `4013`：refreshToken 无效/过期
- `4014`：refreshToken 已被撤销（登出/改密/禁用/重置密码后）
- `4033`：管理员已禁用（即使 refreshToken 仍存在也不可刷新）

---

### 2.6 获取当前管理员信息（补齐）

**接口**：`GET /api/admin/auth/me`（需登录）

**说明**：后台前端启动时用来拉取登录信息（用户名、id、状态等），避免仅靠本地 token。

**响应 `data`**

```json
{
  "id": 1,
  "username": "admin",
  "status": 1,
  "lastLoginTime": "2025-02-10 09:00:00"
}
```

---

### 2.7 修改自己的密码（补齐）

**接口**：`POST /api/admin/auth/change-password`（需登录）

**请求**

```json
{
  "oldPassword": "oldpass",
  "newPassword": "newpass123",
  "confirmPassword": "newpass123"
}
```

**响应 `data`**：无（空对象）

**错误码建议**

- `4002`：两次新密码不一致
- `4031`：旧密码错误
- `4014`：已下线（token 已失效，需要重新登录）

---

### 2.8 图形验证码（可选，建议用于登录/注册）

> 本项目策略：
>
> - **注册**：强制验证码（必填）。
> - **登录**：默认不需要；当“同一账号”或“同一 IP”任一在窗口期内连续失败 **≥3 次** 后，登录接口要求填写验证码（两者都算，任一触发即要求）。
> - **限流**：对 `login/register/captcha/refresh` 做 IP+账号维度限流（建议使用 Redis 滑动窗口/令牌桶）。

#### 2.8.1 获取验证码

**接口**：`GET /api/admin/auth/captcha`

**响应 `data`**

```json
{
  "captchaId": "uuid",
  "imageBase64": "data:image/png;base64,xxxx",
  "expireAt": "2025-02-10 12:00:00"
}
```

**错误码建议**

- `4291`：获取过于频繁（触发限流）

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

### 4.6 商家批量操作（后台）

#### 4.6.1 批量上架

**接口**：`POST /api/admin/shops/batch-online`

**请求体**：

```json
{
  "shopIds": [1, 2, 3] // 商家ID数组
}
```

**说明**：批量将商家状态设置为上架。

**响应 `data`**：

```json
{
  "successCount": 2,
  "failCount": 1,
  "failShopIds": [3] // 失败的商家ID（如已删除、数据不完整等）
}
```

#### 4.6.2 批量下架

**接口**：`POST /api/admin/shops/batch-offline`

**请求体**：同批量上架

**响应 `data`**：同批量上架

---

### 4.7 商家导出（后台）

**接口**：`GET /api/admin/shops/export`

**请求参数（Query）**：

- `platform`：平台枚举，可选
- `categoryId`：分类 ID，可选
- `status`：状态，可选（1=上架，0=下架）
- `keyword`：名称关键词，可选
- `sort`：`updatedAt` / `weight`

**说明**：导出当前筛选条件下的商家列表，返回 Excel 文件。

**响应**：

- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Content-Disposition: `attachment; filename="shops_2025-02-10.xlsx"`
- 文件流

---

### 4.8 分类删除（后台）

**接口**：`DELETE /api/admin/categories/{id}`

**说明**：

- 删除前需检查是否有商家引用该分类
- 若有商家引用，返回错误提示，禁止删除
- 若无商家引用，执行逻辑删除

**响应 `data`**：无（返回空对象）

**错误情况**：

- 若分类被商家引用，返回错误码 `ERR_CATEGORY_IN_USE`（3011）

---

### 4.9 管理员账号管理（后台）

#### 4.9.1 管理员列表

**接口**：`GET /api/admin/admin-users`

**请求参数（Query）**：

- `page`、`pageSize`
- `status`：状态，可选（1=正常，0=禁用）
- `keyword`：用户名关键词，可选

**响应 `data`**：

```json
{
  "total": 10,
  "page": 1,
  "pageSize": 10,
  "list": [
    {
      "id": 1,
      "username": "admin",
      "status": 1,
      "lastLoginTime": "2025-02-10 09:00:00",
      "createTime": "2025-01-01 00:00:00"
    }
  ]
}
```

#### 4.9.2 新增管理员

**接口**：`POST /api/admin/admin-users`

**请求体**：

```json
{
  "username": "newadmin",
  "password": "password123",
  "confirmPassword": "password123"
}
```

**响应 `data`**：

```json
{ "id": 2 }
```

#### 4.9.3 禁用/启用管理员

**接口**：`POST /api/admin/admin-users/{id}/status`

**请求体**：

```json
{
  "status": 0 // 1=正常，0=禁用
}
```

**响应 `data`**：无

#### 4.9.4 重置密码

**接口**：`POST /api/admin/admin-users/{id}/reset-password`

**请求体**：

```json
{
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

**响应 `data`**：无

**说明**：重置指定管理员的密码。

---

### 4.10 操作日志查询（后台）

**接口**：`GET /api/admin/operation-logs`

**请求参数（Query）**：

- `page`、`pageSize`
- `adminId`：管理员 ID，可选
- `targetType`：目标类型，可选（SHOP、CATEGORY、RECOMMEND、ADMIN_USER）
- `action`：操作类型，可选（如 SHOP_CREATE、SHOP_UPDATE、SHOP_DELETE、SHOP_STATUS 等）
- `startTime`：开始时间，可选（格式：yyyy-MM-dd HH:mm:ss）
- `endTime`：结束时间，可选（格式：yyyy-MM-dd HH:mm:ss）

**响应 `data`**：

```json
{
  "total": 100,
  "page": 1,
  "pageSize": 10,
  "list": [
    {
      "id": 1,
      "adminId": 1,
      "adminUsername": "admin",
      "action": "SHOP_CREATE",
      "actionDesc": "商家创建",
      "targetType": "SHOP",
      "targetId": 10,
      "targetName": "校内麻辣烫",
      "requestData": "{\"name\":\"校内麻辣烫\",\"categoryId\":1}",
      "ip": "192.168.1.100",
      "createTime": "2025-02-10 09:00:00"
    }
  ]
}
```

**说明**：

- 记录关键操作：商家创建/编辑/删除/上下架、分类创建/编辑/删除/禁用、推荐配置变更、管理员账号操作
- 不记录：管理员登录/登出
- 支持按管理员、时间范围、对象类型、操作类型筛选

---

### 4.11 系统参数（后台，可配置注册开关）（补齐）

> 说明：用于在后台管理界面配置系统开关与参数（如“管理员自助注册开关”）。

#### 4.11.1 查询系统参数列表

**接口**：`GET /api/admin/system-config`

**请求参数（Query）**：

- `keyword`：按 key/名称模糊搜索（可选）

**响应 `data`**：

```json
[
  {
    "key": "ADMIN_REGISTER_ENABLED",
    "name": "管理员自助注册开关",
    "value": "true",
    "valueType": "BOOLEAN",
    "remark": "关闭后 /api/admin/auth/register 返回 4032",
    "updateTime": "2025-02-10 09:00:00"
  }
]
```

#### 4.11.2 更新系统参数

**接口**：`PUT /api/admin/system-config/{key}`

**请求体**：

```json
{
  "value": "false"
}
```

**响应 `data`**：无（空对象）

**错误码建议**

- `4041`：参数 key 不存在
- `4003`：参数值格式不合法（如期望 BOOLEAN 但传了非 true/false）

---

## 5. 操作日志（概要）

> 详细字段见 `数据库设计.md` 中 `operation_log` 表设计。

- 操作日志查询接口已在上方 4.10 节详细定义
- 写日志时机（示例）：
  - 商家创建/编辑/删除/上下架
  - 分类创建/编辑/删除/禁用
  - 每日推荐配置变更
  - 管理员账号操作

一期可同步写库；后续如需要可用 MQ 异步化日志写入。

---

## 6. DTO/VO 详细设计

> 本节详细定义每个接口的请求 DTO 和响应 VO 类，包含字段类型、校验规则、说明等。

### 6.1 鉴权模块 DTO/VO

#### 6.1.1 小程序登录

**请求 DTO**：`MpLoginRequest`

```java
package com.school.waimai.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MpLoginRequest {
    @NotBlank(message = "微信登录code不能为空")
    private String code;

    private String nickname;  // 可选，用户昵称

    private String avatar;     // 可选，用户头像URL
}
```

**响应 VO**：`MpLoginVO`

```java
package com.school.waimai.auth.dto;

import lombok.Data;

@Data
public class MpLoginVO {
    private String token;      // JWT token
    private String expireAt;    // 过期时间，格式：yyyy-MM-dd HH:mm:ss
}
```

#### 6.1.2 管理员登录

**请求 DTO**：`AdminLoginRequest`

```java
package com.school.waimai.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    // 登录失败次数达到阈值后才要求填写
    private String captchaId;
    private String captchaCode;
}
```

**响应 VO**：`AdminLoginVO`

```java
package com.school.waimai.auth.dto;

import lombok.Data;

@Data
public class AdminLoginVO {
    private String token;      // JWT token
    private String expireAt;    // 过期时间，格式：yyyy-MM-dd HH:mm:ss

    // refresh token（本项目采用 accessToken + refreshToken）
    private String refreshToken;
    private String refreshExpireAt;
}
```

#### 6.1.3 管理员注册

**请求 DTO**：`AdminRegisterRequest`

```java
package com.school.waimai.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminRegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度必须在3-32之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @NotBlank(message = "captchaId不能为空")
    private String captchaId;

    @NotBlank(message = "captchaCode不能为空")
    private String captchaCode;
}
```

**响应 VO**：无（返回空对象或 `IdVO`）

---

#### 6.1.4 管理员登出

**请求 DTO**：无  
**响应 VO**：无

---

#### 6.1.5 刷新 Token

> 说明：若采用“accessToken + refreshToken”方案，补齐以下 DTO/VO；若不采用，可忽略本小节。

**请求 DTO**：`AdminRefreshTokenRequest`

```java
package com.school.waimai.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminRefreshTokenRequest {
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
```

**响应 VO**：`AdminRefreshTokenVO`

```java
package com.school.waimai.auth.dto;

import lombok.Data;

@Data
public class AdminRefreshTokenVO {
    private String token;              // 新JWT
    private String expireAt;           // JWT过期时间
    private String refreshToken;       // 新refreshToken
    private String refreshExpireAt;    // refreshToken过期时间
}
```

---

#### 6.1.6 当前管理员信息（Me）

**请求 DTO**：无  
**响应 VO**：`AdminMeVO`

```java
package com.school.waimai.auth.dto;

import lombok.Data;

@Data
public class AdminMeVO {
    private Long id;
    private String username;
    private Integer status;        // 1=正常，0=禁用
    private String lastLoginTime;  // yyyy-MM-dd HH:mm:ss，可为空
}
```

---

#### 6.1.7 修改自己的密码

**请求 DTO**：`AdminChangePasswordRequest`

```java
package com.school.waimai.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminChangePasswordRequest {
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
}
```

**响应 VO**：无

---

#### 6.1.8 图形验证码

**请求 DTO**：无  
**响应 VO**：`AdminCaptchaVO`

```java
package com.school.waimai.auth.dto;

import lombok.Data;

@Data
public class AdminCaptchaVO {
    private String captchaId;
    private String imageBase64;  // data:image/png;base64,xxxx
    private String expireAt;     // yyyy-MM-dd HH:mm:ss
}
```

---

### 6.9 系统参数模块 DTO/VO（补齐）

#### 6.9.1 更新系统参数

**请求 DTO**：`UpdateSystemConfigRequest`

```java
package com.school.waimai.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSystemConfigRequest {
    @NotBlank(message = "value不能为空")
    private String value;
}
```

**响应 VO**：无

### 6.2 商家模块 DTO/VO

#### 6.2.1 商家列表查询（小程序）

**请求 DTO**：`MpShopListQuery`（Query 参数，可用 `@RequestParam` 或封装为 DTO）

```java
package com.school.waimai.shop.dto;

import lombok.Data;

@Data
public class MpShopListQuery {
    private Integer page = 1;           // 页码，默认1
    private Integer pageSize = 10;      // 每页条数，默认10

    private String platform;            // 平台枚举：MEITUAN, ELEME, JD, MINIPROGRAM_SELF, OTHER
    private Long categoryId;            // 分类ID
    private String sort = "updatedAt";  // 排序：updatedAt(默认), weight
    private String keyword;             // 名称关键词（模糊匹配）
}
```

**响应 VO**：`PageResult<MpShopListItemVO>`

**列表项 VO**：`MpShopListItemVO`

```java
package com.school.waimai.shop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MpShopListItemVO {
    private Long id;                    // 商家ID
    private String name;                // 商家名称
    private String introduction;        // 商家介绍（可选）
    private List<String> platforms;      // 所属平台列表，如：["MEITUAN", "ELEME"]
    private Long categoryId;            // 分类ID
    private String categoryName;        // 分类名称
    private String coverUrl;            // 封面图URL（可选）
    private BigDecimal startingPrice;   // 起送价（可选）
    private String deliveryFeeDesc;     // 配送费说明（可选）
    private BigDecimal avgScore;        // 平均评分（可选，有评分时才有）
    private Integer ratingCount;         // 评分人数（可选，有评分时才有）
}
```

#### 6.2.2 商家详情（小程序）

**请求 DTO**：无（路径参数 `id`）

**响应 VO**：`MpShopDetailVO`

```java
package com.school.waimai.shop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MpShopDetailVO {
    private Long id;
    private String name;
    private String introduction;
    private Long categoryId;
    private String categoryName;
    private String coverUrl;
    private String businessHours;       // 营业时间（可选）
    private String deliveryScope;        // 配送范围说明（可选）
    private BigDecimal startingPrice;    // 起送价（可选）
    private String deliveryFeeDesc;      // 配送费说明（可选）
    private Integer status;              // 状态：1=上架，0=下架

    // 平台跳转链接列表
    private List<PlatformLinkVO> platformLinks;

    // 评分信息
    private BigDecimal avgScore;         // 平均评分（可选）
    private Integer ratingCount;         // 评分人数（可选）
    private Integer userScore;           // 当前用户的评分，未评分则为null

    // 收藏状态
    private Boolean favorited;            // 当前用户是否已收藏
    private Boolean canOrder;             // 是否可以下单（下架时为false）
}

// 平台链接 VO
@Data
class PlatformLinkVO {
    private String platform;             // 平台枚举：MEITUAN, ELEME, JD, MINIPROGRAM_SELF, OTHER
    private String type;                  // 链接类型：H5, MINI_PATH
    private String url;                   // H5链接（type=H5时使用）
    private String path;                  // 小程序path（type=MINI_PATH时使用）
}
```

#### 6.2.3 商家列表查询（后台）

**请求 DTO**：`AdminShopListQuery`

```java
package com.school.waimai.shop.dto;

import lombok.Data;

@Data
public class AdminShopListQuery {
    private Integer page = 1;
    private Integer pageSize = 10;

    private String platform;            // 平台枚举
    private Long categoryId;            // 分类ID
    private Integer status;              // 状态：1=上架，0=下架
    private String keyword;             // 名称关键词
    private String sort;                // 排序：updatedAt, weight
}
```

**响应 VO**：`PageResult<AdminShopListItemVO>`

**列表项 VO**：`AdminShopListItemVO`

```java
package com.school.waimai.shop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminShopListItemVO {
    private Long id;
    private String name;
    private String introduction;
    private Long categoryId;
    private String categoryName;
    private String coverUrl;
    private BigDecimal startingPrice;
    private String deliveryFeeDesc;
    private Integer sortWeight;          // 排序权重
    private Integer status;              // 状态：1=上架，0=下架
    private BigDecimal avgScore;         // 平均评分
    private Integer ratingCount;          // 评分人数
    private LocalDateTime createTime;     // 创建时间
    private LocalDateTime updateTime;     // 更新时间
}
```

#### 6.2.4 新增商家（后台）

**请求 DTO**：`CreateShopRequest`

```java
package com.school.waimai.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateShopRequest {
    @NotBlank(message = "商家名称不能为空")
    @Size(min = 2, max = 30, message = "商家名称长度必须在2-30之间")
    private String name;

    @Size(max = 200, message = "商家介绍长度不能超过200")
    private String introduction;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private String coverUrl;             // 封面图URL（可选）

    @Size(max = 128, message = "营业时间长度不能超过128")
    private String businessHours;        // 营业时间（可选）

    @Size(max = 128, message = "配送范围说明长度不能超过128")
    private String deliveryScope;        // 配送范围说明（可选）

    private BigDecimal startingPrice;    // 起送价（可选）

    @Size(max = 64, message = "配送费说明长度不能超过64")
    private String deliveryFeeDesc;      // 配送费说明（可选）

    private Integer sortWeight = 0;       // 排序权重，默认0

    @NotNull(message = "状态不能为空")
    private Integer status;              // 状态：1=上架，0=下架

    @Valid
    @NotNull(message = "平台链接不能为空")
    @Size(min = 1, message = "至少需要一个平台链接")
    private List<PlatformLinkRequest> platformLinks;  // 平台链接列表

    @Size(max = 255, message = "备注长度不能超过255")
    private String remark;               // 后台备注（可选）
}

// 平台链接请求 DTO
@Data
class PlatformLinkRequest {
    @NotBlank(message = "平台类型不能为空")
    private String platform;            // 平台枚举：MEITUAN, ELEME, JD, MINIPROGRAM_SELF, OTHER

    private String url;                 // H5链接（platform不为MINIPROGRAM_SELF时使用）
    private String path;                // 小程序path（platform为MINIPROGRAM_SELF时使用）
}
```

**响应 VO**：`IdVO`

```java
package com.school.waimai.common.api;

import lombok.Data;

@Data
public class IdVO {
    private Long id;
}
```

#### 6.2.5 编辑商家（后台）

**请求 DTO**：`UpdateShopRequest`（字段同 `CreateShopRequest`，所有字段可选）

```java
package com.school.waimai.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateShopRequest {
    @Size(min = 2, max = 30, message = "商家名称长度必须在2-30之间")
    private String name;

    @Size(max = 200, message = "商家介绍长度不能超过200")
    private String introduction;

    private Long categoryId;
    private String coverUrl;

    @Size(max = 128)
    private String businessHours;

    @Size(max = 128)
    private String deliveryScope;

    private BigDecimal startingPrice;

    @Size(max = 64)
    private String deliveryFeeDesc;

    private Integer sortWeight;
    private Integer status;

    @Valid
    private List<PlatformLinkRequest> platformLinks;

    @Size(max = 255)
    private String remark;
}
```

**响应 VO**：无（返回空对象）

#### 6.2.6 商家上下架

**请求 DTO**：`UpdateShopStatusRequest`

```java
package com.school.waimai.shop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateShopStatusRequest {
    @NotNull(message = "状态不能为空")
    private Integer status;  // 1=上架，0=下架
}
```

**响应 VO**：无（返回空对象）

---

### 6.3 分类模块 DTO/VO

#### 6.3.1 分类列表（后台）

**请求 DTO**：无

**响应 VO**：`List<CategoryVO>`

**分类 VO**：`CategoryVO`

```java
package com.school.waimai.category.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private String logoUrl;             // Logo URL（可选）
    private Integer sortOrder;          // 排序值
    private Integer status;             // 状态：1=启用，0=禁用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

#### 6.3.2 新增分类（后台）

**请求 DTO**：`CreateCategoryRequest`

```java
package com.school.waimai.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 32, message = "分类名称长度不能超过32")
    private String name;

    @Size(max = 255, message = "Logo URL长度不能超过255")
    private String logoUrl;            // Logo URL（可选）

    @NotNull(message = "排序值不能为空")
    private Integer sortOrder;         // 排序值

    @NotNull(message = "状态不能为空")
    private Integer status;            // 状态：1=启用，0=禁用
}
```

**响应 VO**：`IdVO`

#### 6.3.3 编辑分类（后台）

**请求 DTO**：`UpdateCategoryRequest`（字段同 `CreateCategoryRequest`，所有字段可选）

```java
package com.school.waimai.category.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryRequest {
    @Size(max = 32)
    private String name;

    @Size(max = 255)
    private String logoUrl;

    private Integer sortOrder;
    private Integer status;
}
```

**响应 VO**：无

#### 6.3.4 分类状态更新

**请求 DTO**：`UpdateCategoryStatusRequest`

```java
package com.school.waimai.category.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCategoryStatusRequest {
    @NotNull(message = "状态不能为空")
    private Integer status;  // 1=启用，0=禁用
}
```

**响应 VO**：无

---

### 6.4 推荐模块 DTO/VO

#### 6.4.1 每日推荐（小程序）

**请求 DTO**：无

**响应 VO**：`DailyRecommendVO`

```java
package com.school.waimai.recommend.dto;

import lombok.Data;
import java.util.List;

@Data
public class DailyRecommendVO {
    private String date;                // 日期，格式：yyyy-MM-dd
    private String mode;                 // 模式：MANUAL（手动）, RANDOM（随机）
    private List<RecommendShopItemVO> list;  // 推荐商家列表
}

// 推荐商家项 VO
@Data
class RecommendShopItemVO {
    private Long id;
    private String name;
    private String introduction;
    private String categoryName;
    private String coverUrl;
    private java.math.BigDecimal startingPrice;
    private String deliveryFeeDesc;
    private java.math.BigDecimal avgScore;
    private Integer ratingCount;
}
```

#### 6.4.2 查询每日推荐配置（后台）

**请求 DTO**：无

**响应 VO**：`DailyRecommendConfigVO`

```java
package com.school.waimai.recommend.dto;

import lombok.Data;
import java.util.List;

@Data
public class DailyRecommendConfigVO {
    private String bizDate;             // 业务日期，格式：yyyy-MM-dd
    private String mode;                 // 模式：MANUAL, RANDOM
    private List<Long> manualShopIds;   // 手动模式下的商家ID列表（mode=MANUAL时使用）
    private Integer randomCount;         // 随机模式每日展示数量（mode=RANDOM时使用，默认4）
}
```

#### 6.4.3 保存每日推荐配置（后台）

**请求 DTO**：`SaveDailyRecommendConfigRequest`

```java
package com.school.waimai.recommend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.util.List;

@Data
public class SaveDailyRecommendConfigRequest {
    @NotBlank(message = "推荐模式不能为空")
    private String mode;                 // MANUAL 或 RANDOM

    private List<Long> manualShopIds;   // 手动模式下的商家ID列表（mode=MANUAL时必填）

    @Min(value = 1, message = "随机数量至少为1")
    private Integer randomCount;         // 随机模式每日展示数量（mode=RANDOM时必填，建议默认4）
}
```

**响应 VO**：无

---

### 6.5 评分模块 DTO/VO

#### 6.5.1 提交/更新评分（小程序）

**请求 DTO**：`MpShopRatingRequest`

```java
package com.school.waimai.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MpShopRatingRequest {
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小值为1")
    @Max(value = 5, message = "评分最大值为5")
    private Integer score;  // 1～5的整数
}
```

**响应 VO**：`ShopRatingVO`

```java
package com.school.waimai.rating.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShopRatingVO {
    private BigDecimal avgScore;    // 更新后的平均评分
    private Integer ratingCount;     // 评分人数（更新评分时不变）
}
```

#### 6.5.2 评分统计查看（后台）

**请求 DTO**：`AdminRatingStatsQuery`

```java
package com.school.waimai.rating.dto;

import lombok.Data;

@Data
public class AdminRatingStatsQuery {
    private Integer page = 1;
    private Integer pageSize = 10;

    private Long categoryId;         // 分类ID（可选）
    private Integer status;          // 商家状态：1=上架，0=下架（可选）
    private String keyword;          // 商家名称关键词（可选）
}
```

**响应 VO**：`PageResult<RatingStatsItemVO>`

**统计项 VO**：`RatingStatsItemVO`

```java
package com.school.waimai.rating.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RatingStatsItemVO {
    private Long shopId;             // 商家ID
    private String name;              // 商家名称
    private String categoryName;      // 分类名称
    private BigDecimal avgScore;      // 平均评分
    private Integer ratingCount;      // 评分人数
    private LocalDateTime lastRatingTime;  // 最近评分时间（可选）
}
```

---

### 6.6 收藏模块 DTO/VO

#### 6.6.1 收藏/取消收藏（小程序）

**请求 DTO**：无（路径参数 `shopId`）

**响应 VO**：`FavoriteStatusVO`

```java
package com.school.waimai.favorite.dto;

import lombok.Data;

@Data
public class FavoriteStatusVO {
    private Boolean favorited;  // true=已收藏，false=未收藏
}
```

#### 6.6.2 我的收藏列表（小程序）

**请求 DTO**：`FavoriteListQuery`

```java
package com.school.waimai.favorite.dto;

import lombok.Data;

@Data
public class FavoriteListQuery {
    private Integer page = 1;
    private Integer pageSize = 10;
}
```

**响应 VO**：`PageResult<FavoriteShopItemVO>`

**收藏项 VO**：`FavoriteShopItemVO`

```java
package com.school.waimai.favorite.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteShopItemVO {
    private Long shopId;              // 商家ID
    private String name;               // 商家名称
    private String introduction;       // 商家介绍
    private String categoryName;       // 分类名称
    private String coverUrl;           // 封面图URL
    private BigDecimal startingPrice;  // 起送价（可选）
    private String deliveryFeeDesc;    // 配送费说明（可选）
    private BigDecimal avgScore;       // 平均评分（可选）
    private Integer ratingCount;       // 评分人数（可选）
    private LocalDateTime favoritedAt;  // 收藏时间
    private Integer status;             // 商家状态：1=上架，0=下架
    private Boolean canOrder;          // 是否可以下单（下架时为false）
    private String statusLabel;        // 状态标签（下架时为"已下架"，上架时为null）
}
```

---

### 6.7 通用 VO

#### 6.7.1 IdVO（返回ID）

```java
package com.school.waimai.common.api;

import lombok.Data;

@Data
public class IdVO {
    private Long id;
}
```

#### 6.7.2 PageResult（分页结果）

已在 `common.api` 包中定义，见项目结构说明。

---

### 6.8 枚举类型

#### 6.8.1 平台枚举

```java
package com.school.waimai.common.enums;

public enum PlatformType {
    MEITUAN("美团"),
    ELEME("饿了么"),
    JD("京东"),
    MINIPROGRAM_SELF("小程序自营"),
    OTHER("其他");

    private final String desc;

    PlatformType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
```

#### 6.8.2 链接类型枚举

```java
package com.school.waimai.common.enums;

public enum LinkType {
    H5("H5链接"),
    MINI_PATH("小程序路径");

    private final String desc;

    LinkType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
```

#### 6.8.3 推荐模式枚举

```java
package com.school.waimai.common.enums;

public enum RecommendMode {
    MANUAL("手动"),
    RANDOM("随机");

    private final String desc;

    RecommendMode(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
```

---

## 7. 后续详细设计可扩展部分

本文件为 API 级详细设计，后续如有需要可扩展：

- Entity 类设计（字段、映射关系）
- 典型业务流程的时序图（已在《概要设计》中给出示例）
- 错误码表（code 与业务含义的完整清单）
- 安全细节（防刷、防重放、限流规则等）
