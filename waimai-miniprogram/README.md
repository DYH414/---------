# 校园外卖聚合平台 - 小程序端

## 项目说明

基于微信原生小程序开发的用户端应用。

## 快速开始

### 1. 环境准备

- 微信开发者工具
- 微信小程序账号（appid）

### 2. 配置项目

1. 使用微信开发者工具打开 `waimai-miniprogram` 目录
2. 修改 `project.config.json` 中的 `appid`
3. 修改 `config/env.js` 中的后端接口地址

### 3. 编译运行

在微信开发者工具中点击"编译"按钮即可运行。

## 项目结构

```
waimai-miniprogram/
├── pages/                    # 页面
│   ├── waimai-index/         # 外卖首页（每日推荐+列表入口）
│   ├── waimai-list/          # 商家列表
│   ├── waimai-detail/        # 商家详情（评分/收藏/跳转）
│   └── waimai-favorite/      # 我的收藏
├── utils/                    # 工具函数
│   ├── request.js            # 网络请求封装
│   └── auth.js               # 登录相关
├── config/                   # 配置文件
│   └── env.js                # 环境配置
├── app.js                    # 小程序入口
├── app.json                  # 小程序配置
└── app.wxss                  # 全局样式
```

## 功能说明

### 页面说明

- **waimai-index**: 外卖首页，展示每日推荐和商家列表入口
- **waimai-list**: 商家列表页，支持筛选、搜索、排序
- **waimai-detail**: 商家详情页，支持评分、收藏、跳转下单
- **waimai-favorite**: 我的收藏列表

### 网络请求

使用封装的 `request` 方法：

```javascript
const { request } = require("../../utils/request.js");

const data = await request({
  url: "/mp/shops",
  data: { page: 1, pageSize: 10 },
});
```

### 登录流程

1. 调用 `wx.login()` 获取 code
2. 将 code 发送到后端换取 token
3. 将 token 存储到本地存储
4. 后续请求在 header 中携带 token

## 注意事项

1. **服务器域名配置**：需要在微信公众平台配置服务器域名（request 合法域名）
2. **HTTPS 要求**：服务器域名必须使用 HTTPS
3. **小程序审核**：需要在审核说明中明确"信息聚合与跳转、不参与交易"
4. **用户协议**：首次进入需要展示用户协议和免责声明

## 开发规范

- 使用微信原生 API
- 遵循小程序开发规范
- 统一错误处理
- 统一样式规范
