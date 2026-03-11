# Spring 依赖注入笔记

## 一、为什么构造器可以不加 @Autowired

当类里**只有一个构造器**时，Spring 4.3+ 会**自动**用这个构造器做依赖注入，不需要再写 `@Autowired`。

| 情况                             | 是否需要 @Autowired               |
| -------------------------------- | --------------------------------- |
| 只有一个构造器（无论几个参数）   | 不需要                            |
| 有多个构造器，要指定用哪个做注入 | 需要在目标构造器上写 `@Autowired` |

所以「一个构造器 + 多个参数」也依然不用写 `@Autowired`。

---

## 二、两种注入方式：字段注入 vs 构造器注入

### 写法对比

**字段注入（旧写法）**

```java
@Autowired
private AdminAuthService adminAuthService;
```

**构造器注入（推荐写法）**

```java
private final AdminAuthService adminAuthService;

public AdminAuthController(AdminAuthService adminAuthService) {
    this.adminAuthService = adminAuthService;
}
```

### 本质区别

| 方面         | 字段注入                     | 构造器注入                           |
| ------------ | ---------------------------- | ------------------------------------ |
| 依赖是否可变 | 字段可被改（没有 final）     | 可用 `final`，创建后不可变           |
| 单元测试     | 必须用 Spring 容器或反射注入 | 可直接 `new Controller(mockService)` |
| 依赖是否明显 | 依赖“藏”在字段里             | 看构造器参数就知道要哪些依赖         |
| 循环依赖     | Spring 可能用代理兜住        | 会直接报错，逼你改设计               |
| 代码量       | 少，一行搞定                 | 多几行（构造器）                     |

### 执行过程简述

- **字段注入**：Spring 先用无参构造器创建对象，再通过反射给带 `@Autowired` 的字段赋值。
- **构造器注入**：Spring 从容器取出依赖，再调用你的构造器创建对象，依赖通过参数传入。

两种方式都能完成注入，构造器注入更利于维护、测试和表达“必需依赖”。

---

## 三、注入多个对象怎么写（构造器方式）

在**同一个构造器**里增加参数，并在构造器内赋值即可。

```java
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final SomeOtherService someOtherService;

    public AdminAuthController(AdminAuthService adminAuthService,
                               SomeOtherService someOtherService) {
        this.adminAuthService = adminAuthService;
        this.someOtherService = someOtherService;
    }

    // ...
}
```

要点：

1. 每个依赖一个 `private final` 字段。
2. 构造器参数与字段一一对应，在构造器里 `this.xxx = xxx` 赋值。
3. 仍然只有一个构造器，不需要写 `@Autowired`。

---

## 四、小结

| 问题                          | 结论                                                                         |
| ----------------------------- | ---------------------------------------------------------------------------- |
| 单构造器要不要写 @Autowired？ | 不用写，Spring 会自动用该构造器注入。                                        |
| 多依赖怎么办？                | 同一个构造器多加几个参数并赋值。                                             |
| 字段注入和构造器注入选哪个？  | 推荐构造器注入：依赖清晰、可 final、易测；习惯字段注入也可以，功能上都能用。 |
