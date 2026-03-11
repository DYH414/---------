# JWT 笔记（精炼版）

## 一、JWT 是什么

**JWT（JSON Web Token）**：一个带数字签名的字符串，用来表示「用户已登录」。

| 目的       | 说明                                                |
| ---------- | --------------------------------------------------- |
| 前后端分离 | 用 token 代替 Session，前端保存 token，每次请求带上 |
| 无状态     | 后端不存会话，只要验签即可，适合分布式              |
| 防篡改     | 签名可校验 token 是否被改过                         |

---

## 二、结构：三段用 `.` 分隔

```
Header.Payload.Signature
```

| 段            | 内容（Base64 编码）                                           |
| ------------- | ------------------------------------------------------------- |
| **Header**    | `{"alg":"HS256","typ":"JWT"}` 算法和类型                      |
| **Payload**   | `{"sub":"用户ID","username":"admin","exp":过期时间}` 业务数据 |
| **Signature** | `HMACSHA256(Header.Payload, secret)` 签名，防篡改             |

---

## 三、核心流程（一图看懂）

```
登录请求 → 后端校验密码 → 生成 JWT → 返回给前端
                                          ↓
后续请求 ← 放行 ← 过滤器验签 ← 请求头带 Authorization: Bearer <token>
```

---

## 四、最简例子

### 1. 登录：后端生成 token

```java
String token = Jwts.builder()
    .setSubject("123")              // 用户 ID
    .claim("username", "admin")
    .setExpiration(过期时间)
    .signWith(secretKey, HS256)
    .compact();
// 返回 {"token":"xxx","username":"admin"}
```

### 2. 前端：保存并每次请求带上

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

### 3. 后端：过滤器校验

```java
String token = request.getHeader("Authorization").replace("Bearer ", "");
Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
// 通过 → 放行；签名错误/过期 → 抛异常 → 返回 401
```

---

## 五、密钥配置

在 `application.yml` 中：

```yaml
app:
  jwt:
    secret: 你的32位以上随机字符串
    expiration: 86400000 # 24 小时，毫秒
```

**安全建议**：生产环境务必用长且随机的 `secret`，不要用默认值。

---

## 六、常见错误码

| 场景            | 返回              |
| --------------- | ----------------- |
| 未带 token      | 401，未登录       |
| token 过期      | 401，token 已过期 |
| token 非法/篡改 | 401，token 非法   |

---

## 七、一句话总结

> JWT = 签名过的「用户身份小纸条」，前端每次带上，后端验签放行或拒绝。

---

# Sa-Token 笔记（精炼版）

## 一、Sa-Token 是什么

**Sa-Token**：国产轻量级 Java 权限认证框架，封装登录、鉴权、权限、踢人、多端会话等常见能力。

| 特点       | 说明                                                               |
| ---------- | ------------------------------------------------------------------ |
| 开箱即用   | 登录用 `StpUtil.login(id)`，校验用 `StpUtil.checkLogin()`          |
| 链式配置   | 用 DSL 配置拦截路径、白名单、异常处理                              |
| 支持多环境 | Servlet 用 `SaServletFilter`，WebFlux/Gateway 用 `SaReactorFilter` |

---

## 二、适用场景

| 场景                      | 是否适合                                     |
| ------------------------- | -------------------------------------------- |
| 企业后台、多角色多权限    | ✅ 有角色/权限注解、会话管理                 |
| 多端登录（PC/App/小程序） | ✅ 支持会话隔离、单端互踢                    |
| 网关统一鉴权              | ✅ `SaReactorFilter` 在 Gateway 里做登录校验 |
| 毕设、简单后台            | ⚪ 自研 JWT 够用，Sa-Token 可学可不学        |

---

## 三、典型写法：链式 Filter + Lambda

### WebFlux / Gateway 版（SaReactorFilter）

```java
@Bean
public SaReactorFilter getSaReactorFilter(IgnoreWhiteProperties ignoreWhite) {
    return new SaReactorFilter()
        .addInclude("/**")
        .addExclude("/favicon.ico", "/actuator/**")
        .setAuth(obj -> {
            SaRouter.match("/**")
                .notMatch(ignoreWhite.getWhites())
                .check(r -> StpUtil.checkLogin());
        })
        .setError(e -> SaResult.error(e.getMessage()).setCode(401));
}
```

| 方法         | 作用                                                               |
| ------------ | ------------------------------------------------------------------ |
| `addInclude` | 拦截哪些路径                                                       |
| `addExclude` | 排除哪些路径（白名单）                                             |
| `setAuth`    | 鉴权逻辑：`SaRouter` 匹配路径，`check` 里调 `StpUtil.checkLogin()` |
| `setError`   | 异常时返回的 JSON                                                  |

### 登录时

```java
StpUtil.login(adminId);
```

### 业务里获取当前用户

```java
Long userId = StpUtil.getLoginIdAsLong();
```

---

## 四、与自研 JWT 的对比

| 维度 | 自研 JWT + Filter                              | Sa-Token                                                   |
| ---- | ---------------------------------------------- | ---------------------------------------------------------- |
| 实现 | 自己生成/解析 JWT，手写 `OncePerRequestFilter` | 框架提供 token、过滤器、工具类                             |
| 扩展 | 要自己加角色、权限、踢人                       | 内置角色、权限注解、踢人、在线列表                         |
| 学习 | 能更好理解原理                                 | 上手快，但多一层抽象                                       |
| 环境 | Servlet 用 `OncePerRequestFilter`              | Servlet 用 `SaServletFilter`，WebFlux 用 `SaReactorFilter` |

---

## 五、一句话总结

> Sa-Token = 把「登录 + token + 权限 + 会话」打成一体的框架，适合权限需求复杂或想少写鉴权代码的项目。
