# 校园外卖聚合平台 - 后台管理端

## 项目说明

基于 Vue 3 + Vite + TypeScript + Ant Design Vue 的后台管理系统。

## 技术栈

- Vue 3.4+
- Vite 5.x
- TypeScript
- Ant Design Vue 4.x
- Pinia
- Vue Router 4.x
- Axios

## 快速开始

### 1. 环境准备

- Node.js 18.x / 20.x LTS
- npm / pnpm / yarn

### 2. 安装依赖

```bash
npm install
# 或
pnpm install
```

### 3. 配置环境变量

修改 `.env.development` 中的后端接口地址：

```
VITE_API_BASE_URL=/api
```

### 4. 启动开发服务器

```bash
npm run dev
```

访问：http://localhost:3000

### 5. 构建生产版本

```bash
npm run build
```

构建产物在 `dist/` 目录。

## 项目结构

```
src/
├── api/              # API 接口定义
│   ├── request.ts   # Axios 封装
│   ├── types.ts      # 类型定义
│   ├── auth.ts       # 鉴权接口
│   └── shop.ts       # 商家接口
├── views/            # 页面组件
│   ├── Login.vue     # 登录页
│   ├── shop/         # 商家管理
│   ├── category/     # 分类管理
│   ├── recommend/    # 推荐配置
│   └── rating/       # 评分统计
├── layouts/          # 布局组件
│   └── BasicLayout.vue
├── components/       # 公共组件
├── router/           # 路由配置
├── store/            # 状态管理
└── utils/            # 工具函数
```

## 开发说明

### API 调用

使用封装的 `request` 方法：

```typescript
import request from "@/api/request";
import { getShopList } from "@/api/shop";

// 调用接口
const data = await getShopList({ page: 1, pageSize: 10 });
```

### 状态管理

使用 Pinia：

```typescript
import { useUserStore } from "@/store/user";

const userStore = useUserStore();
userStore.setToken("xxx");
```

### 路由守卫

在 `router/index.ts` 中配置路由守卫，检查登录状态。

## 注意事项

1. 开发环境使用 Vite proxy 解决跨域
2. Token 存储在 localStorage
3. 生产环境需要配置正确的后端接口地址
